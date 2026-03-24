package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.cli.terminal.service.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExitCommand.
 * 
 * Note: This test cannot fully test System.exit() behavior, but verifies
 * that the command properly saves sessions before attempting to exit.
 * 
 * Validates: Requirements 2.10, 9.8
 */
@ExtendWith(MockitoExtension.class)
class ExitCommandTest {
    
    @Mock
    private SessionManager sessionManager;
    
    @Mock
    private TerminalRenderer renderer;
    
    @Mock
    private ConfigurableApplicationContext applicationContext;
    
    private ExitCommand command;
    
    @BeforeEach
    void setUp() {
        command = new ExitCommand(applicationContext);
    }
    
    @Test
    void testGetName() {
        assertEquals("exit", command.getName());
    }
    
    @Test
    void testGetAliases() {
        List<String> aliases = command.getAliases();
        assertTrue(aliases.contains("quit"));
        assertTrue(aliases.contains("q"));
        assertTrue(aliases.contains("bye"));
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
    void testExitSavesSessions() {
        // Setup
        CommandContext context = CommandContext.builder()
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify that sessions are saved before exit
        verify(sessionManager).saveAllSessions();
        verify(sessionManager).shutdown();
        verify(renderer, atLeastOnce()).renderSystem(anyString());
        verify(renderer).renderSuccess(anyString());
    }
    
    @Test
    void testExitWithSaveException() {
        // Setup
        doThrow(new RuntimeException("Save error")).when(sessionManager).saveAllSessions();
        
        CommandContext context = CommandContext.builder()
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify error handling
        verify(sessionManager).saveAllSessions();
        verify(renderer).renderError(anyString());
        verify(renderer).renderWarning(anyString());
    }
}
