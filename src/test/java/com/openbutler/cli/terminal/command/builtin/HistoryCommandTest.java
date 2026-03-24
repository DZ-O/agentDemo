package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.model.Session;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.cli.terminal.service.SessionManager;
import com.openbutler.core.memory.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HistoryCommand.
 * 
 * Validates: Requirement 2.5
 */
@ExtendWith(MockitoExtension.class)
class HistoryCommandTest {
    
    @Mock
    private SessionManager sessionManager;
    
    @Mock
    private TerminalRenderer renderer;
    
    private HistoryCommand command;
    private Session session;
    
    @BeforeEach
    void setUp() {
        command = new HistoryCommand();
        
        session = Session.builder()
                .id("test-session-id")
                .name("Test Session")
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(5)
                .active(true)
                .metadata(new HashMap<>())
                .build();
    }
    
    @Test
    void testGetName() {
        assertEquals("history", command.getName());
    }
    
    @Test
    void testGetAliases() {
        List<String> aliases = command.getAliases();
        assertTrue(aliases.contains("h"));
        assertTrue(aliases.contains("hist"));
    }
    
    @Test
    void testGetDescription() {
        assertNotNull(command.getDescription());
        assertFalse(command.getDescription().isEmpty());
    }
    
    @Test
    void testRequiresSession() {
        assertTrue(command.requiresSession());
    }
    
    @Test
    void testDisplayHistoryWithMessages() {
        // Setup
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder()
                .sessionId("test-session-id")
                .role("user")
                .content("Hello")
                .timestamp(LocalDateTime.now())
                .build());
        messages.add(Message.builder()
                .sessionId("test-session-id")
                .role("assistant")
                .content("Hi there!")
                .timestamp(LocalDateTime.now())
                .build());
        
        when(sessionManager.getSessionHistory("test-session-id")).thenReturn(messages);
        
        CommandContext context = CommandContext.builder()
                .currentSession(session)
                .arguments(List.of())
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(sessionManager).getSessionHistory("test-session-id");
        verify(renderer, atLeastOnce()).renderSystem(anyString());
    }
    
    @Test
    void testDisplayHistoryWithLimit() {
        // Setup
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            messages.add(Message.builder()
                    .sessionId("test-session-id")
                    .role("user")
                    .content("Message " + i)
                    .timestamp(LocalDateTime.now())
                    .build());
        }
        
        when(sessionManager.getSessionHistory("test-session-id")).thenReturn(messages);
        
        CommandContext context = CommandContext.builder()
                .currentSession(session)
                .arguments(List.of("10"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(sessionManager).getSessionHistory("test-session-id");
        verify(renderer, atLeastOnce()).renderSystem(anyString());
    }
    
    @Test
    void testDisplayHistoryWithEmptyHistory() {
        // Setup
        when(sessionManager.getSessionHistory("test-session-id")).thenReturn(new ArrayList<>());
        
        CommandContext context = CommandContext.builder()
                .currentSession(session)
                .arguments(List.of())
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(sessionManager).getSessionHistory("test-session-id");
        verify(renderer).renderSystem("No messages in this session yet.");
    }
    
    @Test
    void testDisplayHistoryWithInvalidLimit() {
        // Setup
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder()
                .sessionId("test-session-id")
                .role("user")
                .content("Hello")
                .timestamp(LocalDateTime.now())
                .build());
        
        when(sessionManager.getSessionHistory("test-session-id")).thenReturn(messages);
        
        CommandContext context = CommandContext.builder()
                .currentSession(session)
                .arguments(List.of("invalid"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute (should use default limit)
        command.execute(context).block();
        
        // Verify
        verify(sessionManager).getSessionHistory("test-session-id");
        verify(renderer, atLeastOnce()).renderSystem(anyString());
    }
    
    @Test
    void testDisplayHistoryWithException() {
        // Setup
        when(sessionManager.getSessionHistory("test-session-id"))
                .thenThrow(new RuntimeException("Database error"));
        
        CommandContext context = CommandContext.builder()
                .currentSession(session)
                .arguments(List.of())
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(renderer).renderError(anyString());
    }
}
