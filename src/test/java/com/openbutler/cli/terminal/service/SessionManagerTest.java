package com.openbutler.cli.terminal.service;

import com.openbutler.cli.terminal.model.Session;
import com.openbutler.core.memory.MemoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SessionManager.
 * 
 * Tests session creation, retrieval, switching, listing, deletion, and renaming.
 */
class SessionManagerTest {
    
    @Mock
    private MemoryService memoryService;
    
    private SessionManager sessionManager;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock MemoryService
        when(memoryService.getRecentMessages(anyString(), anyInt()))
                .thenReturn(Collections.emptyList());
        
        // Override system property to use temp directory
        System.setProperty("user.home", tempDir.toString());
        
        sessionManager = new SessionManager(memoryService);
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() throws InterruptedException {
        // Shutdown the session manager to release file locks
        if (sessionManager != null) {
            sessionManager.shutdown();
            // Give a small delay to ensure all async operations complete
            Thread.sleep(50);
        }
    }
    
    @Test
    void testDefaultSessionCreatedOnStartup() {
        // Validates: Requirement 6.8
        Session currentSession = sessionManager.getCurrentSession();
        
        assertNotNull(currentSession);
        assertEquals("default", currentSession.getName());
        assertNotNull(currentSession.getId());
        assertNotNull(currentSession.getCreatedAt());
        assertNotNull(currentSession.getLastActiveAt());
    }
    
    @Test
    void testCreateSession() {
        // Validates: Requirements 6.1, 6.2, 6.3, 6.4
        Session session = sessionManager.createSession("Test Session");
        
        assertNotNull(session);
        assertEquals("Test Session", session.getName());
        assertNotNull(session.getId());
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getLastActiveAt());
        assertEquals(0, session.getMessageCount());
        assertFalse(session.isActive());
    }
    
    @Test
    void testSessionIdIsUnique() {
        // Validates: Requirement 6.1
        Session session1 = sessionManager.createSession("Session 1");
        Session session2 = sessionManager.createSession("Session 2");
        
        assertNotEquals(session1.getId(), session2.getId());
    }
    
    @Test
    void testGetCurrentSession() {
        Session currentSession = sessionManager.getCurrentSession();
        
        assertNotNull(currentSession);
        assertEquals("default", currentSession.getName());
    }
    
    @Test
    void testSwitchSession() {
        // Validates: Requirement 6.6
        Session newSession = sessionManager.createSession("New Session");
        
        sessionManager.switchSession(newSession.getId());
        
        Session currentSession = sessionManager.getCurrentSession();
        assertEquals(newSession.getId(), currentSession.getId());
        assertTrue(currentSession.isActive());
    }
    
    @Test
    void testSwitchToNonExistentSession() {
        // Validates: Requirement 6.6
        assertThrows(IllegalArgumentException.class, () -> {
            sessionManager.switchSession("non-existent-id");
        });
    }
    
    @Test
    void testListSessions() {
        // Validates: Requirement 6.5
        Session session1 = sessionManager.createSession("Session 1");
        Session session2 = sessionManager.createSession("Session 2");
        
        List<Session> sessions = sessionManager.listSessions();
        
        assertTrue(sessions.size() >= 3); // default + 2 new sessions
        assertTrue(sessions.stream().anyMatch(s -> s.getId().equals(session1.getId())));
        assertTrue(sessions.stream().anyMatch(s -> s.getId().equals(session2.getId())));
    }
    
    @Test
    void testListSessionsSortedByLastActive() {
        // Validates: Requirement 6.5
        Session session1 = sessionManager.createSession("Session 1");
        
        // Wait a bit to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Session session2 = sessionManager.createSession("Session 2");
        
        List<Session> sessions = sessionManager.listSessions();
        
        // Most recent should be first
        assertTrue(sessions.get(0).getLastActiveAt()
                .isAfter(sessions.get(sessions.size() - 1).getLastActiveAt()) ||
                sessions.get(0).getLastActiveAt()
                .isEqual(sessions.get(sessions.size() - 1).getLastActiveAt()));
    }
    
    @Test
    void testDeleteSession() {
        // Validates: Requirement 6.7
        Session newSession = sessionManager.createSession("To Delete");
        String sessionId = newSession.getId();
        
        sessionManager.deleteSession(sessionId);
        
        List<Session> sessions = sessionManager.listSessions();
        assertFalse(sessions.stream().anyMatch(s -> s.getId().equals(sessionId)));
    }
    
    @Test
    void testCannotDeleteCurrentSession() {
        // Validates: Requirement 6.7
        Session currentSession = sessionManager.getCurrentSession();
        
        assertThrows(IllegalArgumentException.class, () -> {
            sessionManager.deleteSession(currentSession.getId());
        });
    }
    
    @Test
    void testDeleteNonExistentSession() {
        // Validates: Requirement 6.7
        assertThrows(IllegalArgumentException.class, () -> {
            sessionManager.deleteSession("non-existent-id");
        });
    }
    
    @Test
    void testRenameSession() {
        // Validates: Requirement 6.4
        Session session = sessionManager.createSession("Old Name");
        String sessionId = session.getId();
        
        sessionManager.renameSession(sessionId, "New Name");
        
        Session renamedSession = sessionManager.getSession(sessionId);
        assertEquals("New Name", renamedSession.getName());
    }
    
    @Test
    void testRenameNonExistentSession() {
        // Validates: Requirement 6.4
        assertThrows(IllegalArgumentException.class, () -> {
            sessionManager.renameSession("non-existent-id", "New Name");
        });
    }
    
    @Test
    void testGetSessionHistory() {
        // Validates: Requirement 6.9
        Session session = sessionManager.createSession("Test Session");
        
        List<?> history = sessionManager.getSessionHistory(session.getId());
        
        assertNotNull(history);
        // Empty initially since we mocked MemoryService to return empty list
        assertTrue(history.isEmpty());
    }
    
    @Test
    void testGetSessionHistoryForNonExistentSession() {
        // Validates: Requirement 6.9
        assertThrows(IllegalArgumentException.class, () -> {
            sessionManager.getSessionHistory("non-existent-id");
        });
    }
    
    @Test
    void testSaveSession() throws IOException {
        // Validates: Requirement 6.10
        Session session = sessionManager.createSession("Test Session");
        
        sessionManager.saveSession(session);
        
        // Verify file exists
        Path sessionFile = tempDir.resolve(".openbutler/sessions/" + session.getId() + ".json");
        assertTrue(Files.exists(sessionFile));
    }
    
    @Test
    void testSessionPersistence() throws IOException, InterruptedException {
        // Validates: Requirement 6.10
        Session session = sessionManager.createSession("Persistent Session");
        String sessionId = session.getId();
        String sessionName = session.getName();
        
        // Save synchronously
        sessionManager.saveSession(session);
        
        // Create new SessionManager instance (simulates restart)
        SessionManager newManager = new SessionManager(memoryService);
        
        // Verify session was loaded
        Session loadedSession = newManager.getSession(sessionId);
        assertNotNull(loadedSession);
        assertEquals(sessionName, loadedSession.getName());
        assertEquals(sessionId, loadedSession.getId());
    }
    
    @Test
    void testUpdateMessageCount() {
        Session session = sessionManager.createSession("Test Session");
        String sessionId = session.getId();
        
        sessionManager.updateMessageCount(sessionId, 5);
        
        Session updatedSession = sessionManager.getSession(sessionId);
        assertEquals(5, updatedSession.getMessageCount());
    }
    
    @Test
    void testGetSession() {
        Session session = sessionManager.createSession("Test Session");
        
        Session retrieved = sessionManager.getSession(session.getId());
        
        assertNotNull(retrieved);
        assertEquals(session.getId(), retrieved.getId());
        assertEquals(session.getName(), retrieved.getName());
    }
    
    @Test
    void testGetNonExistentSession() {
        Session retrieved = sessionManager.getSession("non-existent-id");
        
        assertNull(retrieved);
    }
    
    @Test
    void testSaveAllSessions() throws IOException {
        Session session1 = sessionManager.createSession("Session 1");
        Session session2 = sessionManager.createSession("Session 2");
        
        sessionManager.saveAllSessions();
        
        // Verify files exist
        Path sessionFile1 = tempDir.resolve(".openbutler/sessions/" + session1.getId() + ".json");
        Path sessionFile2 = tempDir.resolve(".openbutler/sessions/" + session2.getId() + ".json");
        
        assertTrue(Files.exists(sessionFile1));
        assertTrue(Files.exists(sessionFile2));
    }
    
    @Test
    void testSessionMetadata() {
        // Validates: Requirements 6.2, 6.3
        Session session = sessionManager.createSession("Test Session");
        
        assertNotNull(session.getId());
        assertNotNull(session.getName());
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getLastActiveAt());
        assertNotNull(session.getMetadata());
    }
}
