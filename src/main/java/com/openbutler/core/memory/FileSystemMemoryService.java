package com.openbutler.core.memory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Primary
public class FileSystemMemoryService implements MemoryService {

    private final ObjectMapper objectMapper;
    private final Path storagePath;
    private final Map<String, List<Message>> messagesCache = new ConcurrentHashMap<>();
    private final Map<String, Conversation> conversationCache = new ConcurrentHashMap<>();

    public FileSystemMemoryService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 存储在用户主目录下的 .openbutler/conversations 目录中
        this.storagePath = Paths.get(System.getProperty("user.home"), ".openbutler", "conversations");
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory", e);
        }
    }

    @Override
    public void saveConversation(String sessionId, String userInput, String aiResponse) {
        Conversation conversation = getOrCreateConversation(sessionId);
        conversation.setUpdatedAt(LocalDateTime.now());
        saveConversationMeta(conversation);

        List<Message> messages = getMessages(sessionId);
        
        Message userMsg = Message.builder()
                .sessionId(sessionId)
                .role("user")
                .content(userInput)
                .timestamp(LocalDateTime.now())
                .build();
        messages.add(userMsg);

        Message aiMsg = Message.builder()
                .sessionId(sessionId)
                .role("assistant")
                .content(aiResponse)
                .timestamp(LocalDateTime.now())
                .build();
        messages.add(aiMsg);

        saveMessages(sessionId, messages);
    }

    @Override
    public List<Conversation> getConversationHistory(String sessionId) {
        Conversation conversation = getOrCreateConversation(sessionId);
        return Collections.singletonList(conversation);
    }

    @Override
    public String getRelevantContext(String currentInput) {
        // 简单实现：目前返回空字符串
        return "";
    }

    @Override
    public void updateUserPreference(String key, String value) {
        // TODO: 实现偏好存储
    }

    @Override
    public List<String> getRecentMessages(String sessionId, int limit) {
        List<Message> messages = getMessages(sessionId);
        int start = Math.max(0, messages.size() - limit);
        return messages.subList(start, messages.size()).stream()
                .map(msg -> msg.getRole() + ": " + msg.getContent())
                .collect(Collectors.toList());
    }

    private Conversation getOrCreateConversation(String sessionId) {
        if (conversationCache.containsKey(sessionId)) {
            return conversationCache.get(sessionId);
        }

        File file = storagePath.resolve(sessionId + "_meta.json").toFile();
        if (file.exists()) {
            try {
                Conversation conversation = objectMapper.readValue(file, Conversation.class);
                conversationCache.put(sessionId, conversation);
                return conversation;
            } catch (IOException e) {
                // 忽略错误，创建新的
            }
        }

        Conversation conversation = Conversation.builder()
                .sessionId(sessionId)
                .title("New Conversation")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        conversationCache.put(sessionId, conversation);
        saveConversationMeta(conversation);
        return conversation;
    }

    private void saveConversationMeta(Conversation conversation) {
        try {
            File file = storagePath.resolve(conversation.getSessionId() + "_meta.json").toFile();
            objectMapper.writeValue(file, conversation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Message> getMessages(String sessionId) {
        if (messagesCache.containsKey(sessionId)) {
            return messagesCache.get(sessionId);
        }

        File file = storagePath.resolve(sessionId + "_messages.json").toFile();
        List<Message> messages = new ArrayList<>();
        if (file.exists()) {
            try {
                messages = objectMapper.readValue(file, new TypeReference<List<Message>>() {});
            } catch (IOException e) {
                // 忽略错误
            }
        }
        messagesCache.put(sessionId, messages);
        return messages;
    }

    private void saveMessages(String sessionId, List<Message> messages) {
        try {
            File file = storagePath.resolve(sessionId + "_messages.json").toFile();
            objectMapper.writeValue(file, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
