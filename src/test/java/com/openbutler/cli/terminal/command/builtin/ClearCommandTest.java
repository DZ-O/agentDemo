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
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClearCommand.
 * 
 * Validates: Requirement 2.6
 */
@ExtendWith(MockitoExtension.class)
class ClearCommandTest {
    
    @Mock
    private TerminalRenderer renderer;
    
    private ClearCommand command;
    
    @BeforeEach
    void setUp() {
        command = new ClearCommand();
    }
    
    @Test
    void testGetName() {
        assertEquals("clear", command.getName());
    }
    
    @Test
    void testGetAliases() {
        List<String> aliases = command.getAliases();
        assertTrue(aliases.contains("cls"));
        assertTrue(aliases.contains("cl"));
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
    void testClearScreen() {
        // Setup
        CommandContext context = CommandContext.builder()
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(renderer).clear();
    }
    
    @Test
    void testClearScreenWithException() {
        // Setup
        doThrow(new RuntimeException("Terminal error")).when(renderer).clear();
        
        CommandContext context = CommandContext.builder()
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(renderer).clear();
        verify(renderer).renderError(anyString());
    }
}
