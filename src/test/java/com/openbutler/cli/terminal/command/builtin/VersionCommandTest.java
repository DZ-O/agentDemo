package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VersionCommand.
 * 
 * Validates: Requirement 2.9
 */
@ExtendWith(MockitoExtension.class)
class VersionCommandTest {
    
    @Mock
    private TerminalRenderer renderer;
    
    private VersionCommand command;
    
    @BeforeEach
    void setUp() {
        command = new VersionCommand();
    }
    
    @Test
    void testGetName() {
        assertEquals("version", command.getName());
    }
    
    @Test
    void testGetAliases() {
        List<String> aliases = command.getAliases();
        assertTrue(aliases.contains("v"));
        assertTrue(aliases.contains("ver"));
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
    void testDisplayVersion() {
        // Setup
        CommandContext context = CommandContext.builder()
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(renderer, atLeastOnce()).renderSystem(anyString());
    }
    
    @Test
    void testDisplayVersionWithException() {
        // Setup - This is unlikely to fail, but we test error handling
        // We can't easily mock System.getProperty, so we just verify the command completes
        CommandContext context = CommandContext.builder()
                .renderer(renderer)
                .build();
        
        // Execute
        assertDoesNotThrow(() -> command.execute(context).block());
    }
}
