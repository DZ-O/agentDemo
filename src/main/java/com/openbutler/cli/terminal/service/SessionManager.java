package com.openbutler.cli.terminal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.openbutler.cli.terminal.model.Session;
import com.openbutler.core.memory.Message;
import com.openbutler.core.memory.MemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Session manager service for OpenButler CLI.
 *
 * Manages creation, retrieval, switching, listing, deletion, and renaming of sessions.
 * Sessions are persisted to ~/.openbutler/sessions/{session-id}.json in JSON format.
 * Provides async saving mechanism to avoid blocking operations.
 * Automatically creates a default session on startup.
 *
 * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 6.10
 */
@Slf4j
@Service
public class SessionManager {
    
    private static final String SESSIONS_DIR = ".openbutler/sessions";
    private static final String DEFAULT_SESSION_NAME = "default";
    
    private final ObjectMapper objectMapper;
    private final Path sessionsPath;
    private final MemoryService memoryService;
    private final ExecutorService saveExecutor;
    
    // 内存中的会话缓存
    private final Map<String, Session> sessionCache;
    private volatile String currentSessionId;
    
    public SessionManager(MemoryService memoryService) {
        this.memoryService = memoryService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.sessionsPath = Paths.get(System.getProperty("user.home"), SESSIONS_DIR);
        this.sessionCache = new ConcurrentHashMap<>();
        this.saveExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "session-saver");
            thread.setDaemon(true);
            return thread;
        });
        
        initialize();
    }
    
    /**
     * Initialize the session manager.
     * Creates sessions directory and default session if needed.
     *
     * Validates: Requirement 6.8
     */
    private void initialize() {
        try {
            // 创建会话目录
            Files.createDirectories(sessionsPath);
            log.info("Sessions directory: {}", sessionsPath);
            
            // 加载现有会话
            loadExistingSessions();
            
            // 如果没有会话则创建默认会话
            if (sessionCache.isEmpty()) {
                log.info("No existing sessions found, creating default session");
                Session defaultSession = createSession(DEFAULT_SESSION_NAME);
                currentSessionId = defaultSession.getId();
            } else {
                // 设置第一个会话为当前会话
                currentSessionId = sessionCache.values().iterator().next().getId();
            }
            
        } catch (IOException e) {
            log.error("Failed to initialize session manager", e);
            throw new RuntimeException("Failed to initialize session manager", e);
        }
    }
    
    /**
     * Load existing sessions from file system.
     */
    private void loadExistingSessions() throws IOException {
        if (!Files.exists(sessionsPath)) {
            return;
        }
        
        Files.list(sessionsPath)
            .filter(path -> path.toString().endsWith(".json"))
            .forEach(path -> {
                try {
                    Session session = objectMapper.readValue(path.toFile(), Session.class);
                    sessionCache.put(session.getId(), session);
                    log.debug("Loaded session: {} ({})", session.getName(), session.getId());
                } catch (IOException e) {
                    log.warn("Failed to load session from {}", path, e);
                }
            });
        
        log.info("Loaded {} existing sessions", sessionCache.size());
    }
    
    /**
     * Create a new session with the given name.
     * Generates a unique ID and initializes metadata.
     *
     * Validates: Requirements 6.1, 6.2, 6.3, 6.4
     *
     * @param name session name
     * @return created session
     */
    public Session createSession(String name) {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        Session session = Session.builder()
                .id(sessionId)
                .name(name)
                .createdAt(now)
                .lastActiveAt(now)
                .messageCount(0)
                .active(false)
                .metadata(new HashMap<>())
                .build();
        
        sessionCache.put(sessionId, session);
        saveSessionAsync(session);
        
        log.info("Created new session: {} ({})", name, sessionId);
        return session;
    }
    
    /**
     * Get the current active session.
     *
     * @return current session
     */
    public Session getCurrentSession() {
        Session session = sessionCache.get(currentSessionId);
        if (session == null) {
            log.warn("Current session not found, creating default");
            session = createSession(DEFAULT_SESSION_NAME);
            currentSessionId = session.getId();
        }
        return session;
    }
    
    /**
     * Switch to the specified session.
     * Updates last active time and loads session history.
     *
     * Validates: Requirements 6.6, 6.9
     *
     * @param sessionId session ID to switch to
     * @throws IllegalArgumentException if session doesn't exist
     */
    public void switchSession(String sessionId) {
        Session session = sessionCache.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        
        // 更新上一个会话
        Session previousSession = getCurrentSession();
        if (previousSession != null) {
            previousSession.setActive(false);
            saveSessionAsync(previousSession);
        }
        
        // 更新新会话
        session.setActive(true);
        session.setLastActiveAt(LocalDateTime.now());
        currentSessionId = sessionId;
        saveSessionAsync(session);
        
        log.info("Switched to session: {} ({})", session.getName(), sessionId);
    }
    
    /**
     * List all sessions.
     * Returns sessions sorted by last active time (most recent first).
     *
     * Validates: Requirement 6.5
     *
     * @return list of all sessions
     */
    public List<Session> listSessions() {
        return sessionCache.values().stream()
                .sorted(Comparator.comparing(Session::getLastActiveAt).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Delete the specified session.
     * Removes from cache and deletes file.
     * 
     * Validates: Requirement 6.7
     * 
     * @param sessionId session ID to delete
     * @throws IllegalArgumentException if session doesn't exist or is current session
     */
    public void deleteSession(String sessionId) {
        if (sessionId.equals(currentSessionId)) {
            throw new IllegalArgumentException("Cannot delete current session");
        }
        
        Session session = sessionCache.remove(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        
        // 删除会话文件
        Path sessionFile = sessionsPath.resolve(sessionId + ".json");
        try {
            Files.deleteIfExists(sessionFile);
            log.info("Deleted session: {} ({})", session.getName(), sessionId);
        } catch (IOException e) {
            log.error("Failed to delete session file: {}", sessionFile, e);
        }
    }
    
    /**
     * Rename the specified session.
     * 
     * Validates: Requirement 6.4
     * 
     * @param sessionId session ID to rename
     * @param newName new session name
     * @throws IllegalArgumentException if session doesn't exist
     */
    public void renameSession(String sessionId, String newName) {
        Session session = sessionCache.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        
        String oldName = session.getName();
        session.setName(newName);
        saveSessionAsync(session);
        
        log.info("Renamed session {} from '{}' to '{}'", sessionId, oldName, newName);
    }
    
    /**
     * Get session history (messages).
     * Loads messages from MemoryService.
     * 
     * Validates: Requirement 6.9
     * 
     * @param sessionId session ID
     * @return list of messages
     */
    public List<Message> getSessionHistory(String sessionId) {
        Session session = sessionCache.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        
        // 从 MemoryService 加载消息
        List<String> recentMessages = memoryService.getRecentMessages(sessionId, 1000);
        
        // 转换为 Message 对象（简化版）
        List<Message> messages = new ArrayList<>();
        for (String msg : recentMessages) {
            String[] parts = msg.split(": ", 2);
            if (parts.length == 2) {
                messages.add(Message.builder()
                        .sessionId(sessionId)
                        .role(parts[0])
                        .content(parts[1])
                        .timestamp(LocalDateTime.now())
                        .build());
            }
        }
        
        return messages;
    }
    
    /**
     * Save session synchronously.
     * Persists session to JSON file.
     * 
     * Validates: Requirement 6.10
     * 
     * @param session session to save
     */
    public void saveSession(Session session) {
        Path sessionFile = sessionsPath.resolve(session.getId() + ".json");
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(sessionFile.toFile(), session);
            log.debug("Saved session: {} to {}", session.getId(), sessionFile);
        } catch (IOException e) {
            log.error("Failed to save session: {}", session.getId(), e);
            throw new RuntimeException("Failed to save session", e);
        }
    }
    
    /**
     * Save session asynchronously.
     * Uses background thread to avoid blocking.
     * 
     * Validates: Requirement 6.10
     * 
     * @param session session to save
     */
    public void saveSessionAsync(Session session) {
        saveExecutor.submit(() -> {
            try {
                saveSession(session);
            } catch (Exception e) {
                log.error("Async save failed for session: {}", session.getId(), e);
            }
        });
    }
    
    /**
     * Save all sessions synchronously.
     * Used during shutdown to ensure all data is persisted.
     */
    public void saveAllSessions() {
        log.info("Saving all sessions...");
        sessionCache.values().forEach(this::saveSession);
        log.info("All sessions saved");
    }
    
    /**
     * Get session by ID.
     * 
     * @param sessionId session ID
     * @return session or null if not found
     */
    public Session getSession(String sessionId) {
        return sessionCache.get(sessionId);
    }
    
    /**
     * Update session message count.
     * 
     * @param sessionId session ID
     * @param count new message count
     */
    public void updateMessageCount(String sessionId, int count) {
        Session session = sessionCache.get(sessionId);
        if (session != null) {
            session.setMessageCount(count);
            session.setLastActiveAt(LocalDateTime.now());
            saveSessionAsync(session);
        }
    }
    
    /**
     * Shutdown the session manager.
     * Saves all sessions and shuts down executor.
     */
    public void shutdown() {
        log.info("Shutting down session manager...");
        saveAllSessions();
        saveExecutor.shutdown();
    }
}
