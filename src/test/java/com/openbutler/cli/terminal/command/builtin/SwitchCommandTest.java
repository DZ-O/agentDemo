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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SwitchCommand.
 * 
 * Validates: Requirements 2.4, 6.6, 6.9, 9.2
 */
@ExtendWith(MockitoExtension.class)
class SwitchCommandTest {
    
    @Mock
    private SessionManager sessionManager;
    
    @Mock
    private TerminalRenderer renderer;
    
    private SwitchCommand command;
    private Session session1;
    private Session session2;
    
    @BeforeEach
    void setUp() {
        command = new SwitchCommand();
        
        session1 = Session.builder()
                .id("11111111-1111-1111-1111-111111111111")
                .name("Session 1")
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(5)
                .active(true)
                .metadata(new HashMap<>())
                .build();
        
        session2 = Session.builder()
                .id("22222222-2222-2222-2222-222222222222")
                .name("Session 2")
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(10)
                .active(false)
                .metadata(new HashMap<>())
                .build();
    }
    
    @Test
    void testGetName() {
        assertEquals("switch", command.getName());
    }
    
    @Test
    void testGetAliases() {
        List<String> aliases = command.getAliases();
        assertTrue(aliases.contains("sw"));
        assertTrue(aliases.contains("s"));
    }
    
    @Test
    void testGetDescription() {
        assertNotNull(command.getDescription());
        assertFalse(command.getDescription().isEmpty());
    }
    
    @Test
    void testDoesNotRequireSession() {
        assertFalse(command.requiresSession());
    }
    
    @Test
    void testSwitchByFullId() {
        // Setup
        when(sessionManager.listSessions()).thenReturn(Arrays.asList(session1, session2));
        when(sessionManager.getCurrentSession()).thenReturn(session1);
        
        CommandContext context = CommandContext.builder()
                .arguments(List.of("22222222-2222-2222-2222-222222222222"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(sessionManager).switchSession("22222222-2222-2222-2222-222222222222");
        verify(renderer).renderSuccess(anyString());
    }
    
    @Test
    void testSwitchByPartialId() {
        // Setup
        when(sessionManager.listSessions()).thenReturn(Arrays.asList(session1, session2));
        when(sessionManager.getCurrentSession()).thenReturn(session1);
        
        CommandContext context = CommandContext.builder()
                .arguments(List.of("22222222"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(sessionManager).switchSession("22222222-2222-2222-2222-222222222222");
        verify(renderer).renderSuccess(anyString());
    }
    
    @Test
    void testSwitchByName() {
        // Setup
        when(sessionManager.listSessions()).thenReturn(Arrays.asList(session1, session2));
        when(sessionManager.getCurrentSession()).thenReturn(session1);
        
        CommandContext context = CommandContext.builder()
                .arguments(List.of("Session", "2"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(sessionManager).switchSession("22222222-2222-2222-2222-222222222222");
        verify(renderer).renderSuccess(anyString());
    }
    
    @Test
    void testSwitchToNonExistentSession() {
        // Setup
        when(sessionManager.listSessions()).thenReturn(Arrays.asList(session1, session2));
        
        CommandContext context = CommandContext.builder()
                .arguments(List.of("nonexistent"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(sessionManager, never()).switchSession(anyString());
        verify(renderer).renderError(anyString());
    }
    
    @Test
    void testSwitchToCurrentSession() {
        // Setup
        when(sessionManager.listSessions()).thenReturn(Arrays.asList(session1, session2));
        when(sessionManager.getCurrentSession()).thenReturn(session1);
        
        CommandContext context = CommandContext.builder()
                .arguments(List.of("11111111"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(sessionManager, never()).switchSession(anyString());
        verify(renderer).renderWarning(anyString());
    }
    
    @Test
    void testSwitchWithoutArguments() {
        // Setup
        CommandContext context = CommandContext.builder()
                .arguments(List.of())
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(sessionManager, never()).switchSession(anyString());
        verify(renderer).renderError(anyString());
    }
    
    @Test
    void testSwitchWithException() {
        // Setup
        when(sessionManager.listSessions()).thenReturn(Arrays.asList(session1, session2));
        when(sessionManager.getCurrentSession()).thenReturn(session1);
        doThrow(new IllegalArgumentException("Session not found"))
                .when(sessionManager).switchSession(anyString());
        
        CommandContext context = CommandContext.builder()
                .arguments(List.of("22222222"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(renderer).renderError(anyString());
    }
}
