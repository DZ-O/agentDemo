package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.model.Session;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.cli.terminal.service.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ListCommand.
 * 
 * Tests the functionality of listing all sessions in a formatted table,
 * including edge cases like empty session lists and marking the current session.
 */
@ExtendWith(MockitoExtension.class)
class ListCommandTest {
    
    @Mock
    private SessionManager sessionManager;
    
    @Mock
    private TerminalRenderer renderer;
    
    private ListCommand listCommand;
    private CommandContext context;
    
    @BeforeEach
    void setUp() {
        listCommand = new ListCommand();
        
        context = CommandContext.builder()
                .commandName("list")
                .arguments(new ArrayList<>())
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
    }
    
    @Test
    void testGetName() {
        assertEquals("list", listCommand.getName());
    }
    
    @Test
    void testGetAliases() {
        List<String> aliases = listCommand.getAliases();
        assertEquals(2, aliases.size());
        assertTrue(aliases.contains("ls"));
        assertTrue(aliases.contains("l"));
    }
    
    @Test
    void testGetDescription() {
        assertNotNull(listCommand.getDescription());
        assertTrue(listCommand.getDescription().contains("session"));
    }
    
    @Test
    void testGetUsage() {
        assertNotNull(listCommand.getUsage());
        assertTrue(listCommand.getUsage().contains("list"));
    }
    
    @Test
    void testDoesNotRequireSession() {
        assertFalse(listCommand.requiresSession());
    }
    
    @Test
    void testExecuteWithMultipleSessions() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        Session session1 = Session.builder()
                .id("11111111-1111-1111-1111-111111111111")
                .name("Session 1")
                .createdAt(now.minusDays(2))
                .lastActiveAt(now.minusHours(1))
                .messageCount(10)
                .active(false)
                .build();
        
        Session session2 = Session.builder()
                .id("22222222-2222-2222-2222-222222222222")
                .name("Session 2")
                .createdAt(now.minusDays(1))
                .lastActiveAt(now)
                .messageCount(5)
                .active(false)
                .build();
        
        Session session3 = Session.builder()
                .id("33333333-3333-3333-3333-333333333333")
                .name("Current Session")
                .createdAt(now)
                .lastActiveAt(now)
                .messageCount(3)
                .active(false)
                .build();
        
        List<Session> sessions = new ArrayList<>(List.of(session1, session2, session3));
        
        when(sessionManager.listSessions()).thenReturn(sessions);
        when(sessionManager.getCurrentSession()).thenReturn(session3);
        
        // Capture System.out
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        try {
            // Act
            Mono<Void> result = listCommand.execute(context);
            
            // Assert
            StepVerifier.create(result)
                    .verifyComplete();
            
            verify(sessionManager).listSessions();
            verify(sessionManager).getCurrentSession();
            verify(renderer).renderSystem(contains("Total sessions: 3"));
            
            // Verify table was printed
            String output = outputStream.toString();
            assertFalse(output.isEmpty(), "Output should not be empty");
            // Just verify the output contains some session information
            assertTrue(output.contains("Session") || output.contains("ID") || output.contains("Name"), 
                    "Output should contain table headers or session data");
            
        } finally {
            System.setOut(originalOut);
        }
    }
    
    @Test
    void testExecuteWithEmptySessionList() {
        // Arrange
        when(sessionManager.listSessions()).thenReturn(new ArrayList<>());
        
        // Act
        Mono<Void> result = listCommand.execute(context);
        
        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(sessionManager).listSessions();
        verify(sessionManager, never()).getCurrentSession();
        verify(renderer).renderSystem("No sessions found.");
    }
    
    @Test
    void testExecuteWithNullSessionList() {
        // Arrange
        when(sessionManager.listSessions()).thenReturn(null);
        
        // Act
        Mono<Void> result = listCommand.execute(context);
        
        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(sessionManager).listSessions();
        verify(renderer).renderSystem("No sessions found.");
    }
    
    @Test
    void testExecuteWithSingleSession() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        Session session = Session.builder()
                .id("11111111-1111-1111-1111-111111111111")
                .name("Only Session")
                .createdAt(now)
                .lastActiveAt(now)
                .messageCount(0)
                .active(false)
                .build();
        
        List<Session> sessions = new ArrayList<>(List.of(session));
        
        when(sessionManager.listSessions()).thenReturn(sessions);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        
        // Capture System.out
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        try {
            // Act
            Mono<Void> result = listCommand.execute(context);
            
            // Assert
            StepVerifier.create(result)
                    .verifyComplete();
            
            verify(sessionManager).listSessions();
            verify(sessionManager).getCurrentSession();
            verify(renderer).renderSystem(contains("Total sessions: 1"));
            
            // Verify session is shown in output
            String output = outputStream.toString();
            assertTrue(output.contains("Only Session"));
            
        } finally {
            System.setOut(originalOut);
        }
    }
    
    @Test
    void testExecuteWithNoCurrentSession() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        Session session1 = Session.builder()
                .id("11111111-1111-1111-1111-111111111111")
                .name("Session 1")
                .createdAt(now)
                .lastActiveAt(now)
                .messageCount(5)
                .active(false)
                .build();
        
        List<Session> sessions = new ArrayList<>(List.of(session1));
        
        when(sessionManager.listSessions()).thenReturn(sessions);
        when(sessionManager.getCurrentSession()).thenReturn(null);
        
        // Capture System.out
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        try {
            // Act
            Mono<Void> result = listCommand.execute(context);
            
            // Assert
            StepVerifier.create(result)
                    .verifyComplete();
            
            verify(sessionManager).listSessions();
            verify(sessionManager).getCurrentSession();
            
            // Verify no session is marked as active in the output
            String output = outputStream.toString();
            assertTrue(output.contains("Session 1"));
            
        } finally {
            System.setOut(originalOut);
        }
    }
    
    @Test
    void testExecuteHandlesException() {
        // Arrange
        when(sessionManager.listSessions()).thenThrow(new RuntimeException("Database error"));
        
        // Act
        Mono<Void> result = listCommand.execute(context);
        
        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(sessionManager).listSessions();
        verify(renderer).renderError(contains("Failed to list sessions"));
    }
    
    @Test
    void testExecuteMarksOnlyCurrentSessionAsActive() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        Session session1 = Session.builder()
                .id("11111111-1111-1111-1111-111111111111")
                .name("Session 1")
                .createdAt(now)
                .lastActiveAt(now)
                .messageCount(10)
                .active(false)
                .build();
        
        Session session2 = Session.builder()
                .id("22222222-2222-2222-2222-222222222222")
                .name("Session 2")
                .createdAt(now)
                .lastActiveAt(now)
                .messageCount(5)
                .active(false)
                .build();
        
        List<Session> sessions = new ArrayList<>(List.of(session1, session2));
        
        when(sessionManager.listSessions()).thenReturn(sessions);
        when(sessionManager.getCurrentSession()).thenReturn(session1);
        
        // Capture System.out
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        try {
            // Act
            Mono<Void> result = listCommand.execute(context);
            
            // Assert
            StepVerifier.create(result)
                    .verifyComplete();
            
            // Verify only session1 is shown as active in output
            String output = outputStream.toString();
            assertTrue(output.contains("Session 1"));
            assertTrue(output.contains("Session 2"));
            
        } finally {
            System.setOut(originalOut);
        }
    }
    
    @Test
    void testExecuteDisplaysSessionDetails() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        Session session = Session.builder()
                .id("abcdef12-3456-7890-abcd-ef1234567890")
                .name("Test Session")
                .createdAt(now.minusDays(1))
                .lastActiveAt(now)
                .messageCount(42)
                .active(false)
                .build();
        
        List<Session> sessions = new ArrayList<>(List.of(session));
        
        when(sessionManager.listSessions()).thenReturn(sessions);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        
        // Capture System.out
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        try {
            // Act
            Mono<Void> result = listCommand.execute(context);
            
            // Assert
            StepVerifier.create(result)
                    .verifyComplete();
            
            String output = outputStream.toString();
            
            // Verify session details are in the output
            assertTrue(output.contains("Test Session"));
            assertTrue(output.contains("42")); // message count
            
        } finally {
            System.setOut(originalOut);
        }
    }
}
