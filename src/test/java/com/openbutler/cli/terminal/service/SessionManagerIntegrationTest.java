package com.openbutler.cli.terminal.service;

import com.openbutler.cli.terminal.model.Session;
import com.openbutler.core.memory.MemoryService;
import org.junit.jupiter.api.AfterEach;
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
 * Integration tests for SessionManager.
 * 
 * Tests end-to-end workflows including persistence and session lifecycle.
 */
class SessionManagerIntegrationTest {
    
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
    
    @AfterEach
    void tearDown() throws InterruptedException {
        if (sessionManager != null) {
            sessionManager.shutdown();
            Thread.sleep(50);
        }
    }
    
    @Test
    void testCompleteSessionLifecycle() throws InterruptedException {
        // Create a new session
        Session session = sessionManager.createSession("Project Alpha");
        String sessionId = session.getId();
        
        // Verify session was created
        assertNotNull(session);
        assertEquals("Project Alpha", session.getName());
        
        // Wait for async save
        Thread.sleep(100);
        
        // Verify session file exists
        Path sessionFile = tempDir.resolve(".openbutler/sessions/" + sessionId + ".json");
        assertTrue(Files.exists(sessionFile));
        
        // Switch to the new session
        sessionManager.switchSession(sessionId);
        Session currentSession = sessionManager.getCurrentSession();
        assertEquals(sessionId, currentSession.getId());
        assertTrue(currentSession.isActive());
        
        // Rename the session
        sessionManager.renameSession(sessionId, "Project Beta");
        Session renamedSession = sessionManager.getSession(sessionId);
        assertEquals("Project Beta", renamedSession.getName());
        
        // Update message count
        sessionManager.updateMessageCount(sessionId, 10);
        Session updatedSession = sessionManager.getSession(sessionId);
        assertEquals(10, updatedSession.getMessageCount());
        
        // List all sessions
        List<Session> sessions = sessionManager.listSessions();
        assertTrue(sessions.size() >= 2); // default + Project Beta
        
        // Create another session and switch to it
        Session session2 = sessionManager.createSession("Project Gamma");
        sessionManager.switchSession(session2.getId());
        
        // Now delete the first session
        sessionManager.deleteSession(sessionId);
        
        // Verify session was deleted
        assertNull(sessionManager.getSession(sessionId));
        List<Session> remainingSessions = sessionManager.listSessions();
        assertFalse(remainingSessions.stream().anyMatch(s -> s.getId().equals(sessionId)));
    }
    
    @Test
    void testSessionPersistenceAcrossRestarts() throws IOException, InterruptedException {
        // Create multiple sessions
        Session session1 = sessionManager.createSession("Session One");
        Session session2 = sessionManager.createSession("Session Two");
        Session session3 = sessionManager.createSession("Session Three");
        
        // Update some properties
        sessionManager.updateMessageCount(session1.getId(), 5);
        sessionManager.updateMessageCount(session2.getId(), 10);
        sessionManager.renameSession(session3.getId(), "Session Three Renamed");
        
        // Save all sessions
        sessionManager.saveAllSessions();
        
        // Shutdown the session manager
        sessionManager.shutdown();
        Thread.sleep(50);
        
        // Create a new SessionManager instance (simulates restart)
        SessionManager newManager = new SessionManager(memoryService);
        
        // Verify all sessions were loaded
        List<Session> loadedSessions = newManager.listSessions();
        assertTrue(loadedSessions.size() >= 4); // default + 3 created sessions
        
        // Verify session properties were persisted
        Session loadedSession1 = newManager.getSession(session1.getId());
        assertNotNull(loadedSession1);
        assertEquals("Session One", loadedSession1.getName());
        assertEquals(5, loadedSession1.getMessageCount());
        
        Session loadedSession2 = newManager.getSession(session2.getId());
        assertNotNull(loadedSession2);
        assertEquals("Session Two", loadedSession2.getName());
        assertEquals(10, loadedSession2.getMessageCount());
        
        Session loadedSession3 = newManager.getSession(session3.getId());
        assertNotNull(loadedSession3);
        assertEquals("Session Three Renamed", loadedSession3.getName());
        
        // Cleanup
        newManager.shutdown();
        Thread.sleep(50);
    }
    
    @Test
    void testDefaultSessionCreationOnFirstRun() {
        // Verify default session was created
        Session defaultSession = sessionManager.getCurrentSession();
        
        assertNotNull(defaultSession);
        assertEquals("default", defaultSession.getName());
        assertNotNull(defaultSession.getId());
        assertNotNull(defaultSession.getCreatedAt());
        assertNotNull(defaultSession.getLastActiveAt());
    }
    
    @Test
    void testMultipleSessionsWithSwitching() {
        // Create multiple sessions
        Session session1 = sessionManager.createSession("Work");
        Session session2 = sessionManager.createSession("Personal");
        Session session3 = sessionManager.createSession("Research");
        
        // Switch between sessions
        sessionManager.switchSession(session1.getId());
        assertEquals(session1.getId(), sessionManager.getCurrentSession().getId());
        
        sessionManager.switchSession(session2.getId());
        assertEquals(session2.getId(), sessionManager.getCurrentSession().getId());
        
        sessionManager.switchSession(session3.getId());
        assertEquals(session3.getId(), sessionManager.getCurrentSession().getId());
        
        // Verify only current session is active
        assertFalse(sessionManager.getSession(session1.getId()).isActive());
        assertFalse(sessionManager.getSession(session2.getId()).isActive());
        assertTrue(sessionManager.getSession(session3.getId()).isActive());
    }
    
    @Test
    void testSessionListSortedByActivity() throws InterruptedException {
        // Create sessions with delays to ensure different timestamps
        Session session1 = sessionManager.createSession("Old Session");
        Thread.sleep(50);
        
        Session session2 = sessionManager.createSession("Recent Session");
        Thread.sleep(50);
        
        // Update session1's activity by switching to it
        sessionManager.switchSession(session1.getId());
        
        // List sessions
        List<Session> sessions = sessionManager.listSessions();
        
        // The most recently active session should be first
        // (session1 was just switched to, so it should be first)
        Session firstSession = sessions.get(0);
        assertEquals(session1.getId(), firstSession.getId());
    }
}
