package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.command.Command;
import com.openbutler.cli.terminal.command.CommandRegistry;
import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import org.springframework.beans.factory.ObjectProvider;

/**
 * Unit tests for HelpCommand.
 * 
 * Validates: Requirement 2.8
 */
@ExtendWith(MockitoExtension.class)
class HelpCommandTest {
    
    @Mock
    private CommandRegistry commandRegistry;
    
    @Mock
    private ObjectProvider<CommandRegistry> commandRegistryProvider;
    
    @Mock
    private TerminalRenderer renderer;
    
    @Mock
    private Command mockCommand;
    
    private HelpCommand command;
    
    @BeforeEach
    void setUp() {
        lenient().when(commandRegistryProvider.getObject()).thenReturn(commandRegistry);
        command = new HelpCommand(commandRegistryProvider);
    }
    
    private void setupMockCommand() {
        // Setup mock command
        lenient().when(mockCommand.getName()).thenReturn("test");
        lenient().when(mockCommand.getAliases()).thenReturn(List.of("t"));
        lenient().when(mockCommand.getDescription()).thenReturn("Test command");
        lenient().when(mockCommand.getUsage()).thenReturn("test - Test usage");
    }
    
    @Test
    void testGetName() {
        assertEquals("help", command.getName());
    }
    
    @Test
    void testGetAliases() {
        List<String> aliases = command.getAliases();
        assertTrue(aliases.contains("h"));
        assertTrue(aliases.contains("?"));
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
    void testDisplayAllHelp() {
        // Setup
        setupMockCommand();
        when(commandRegistry.getAllCommandNames()).thenReturn(Arrays.asList("chat", "new", "list"));
        when(commandRegistry.getCommand("chat")).thenReturn(mockCommand);
        when(commandRegistry.getCommand("new")).thenReturn(mockCommand);
        when(commandRegistry.getCommand("list")).thenReturn(mockCommand);
        
        CommandContext context = CommandContext.builder()
                .arguments(List.of())
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(commandRegistry).getAllCommandNames();
        verify(commandRegistry, times(3)).getCommand(anyString());
        verify(renderer, atLeastOnce()).renderSystem(anyString());
    }
    
    @Test
    void testDisplaySpecificCommandHelp() {
        // Setup
        setupMockCommand();
        when(commandRegistry.getCommand("test")).thenReturn(mockCommand);
        
        CommandContext context = CommandContext.builder()
                .arguments(List.of("test"))
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(commandRegistry).getCommand("test");
        verify(renderer, atLeastOnce()).renderSystem(anyString());
    }
    
    @Test
    void testDisplayHelpForNonExistentCommand() {
        // Setup
        when(commandRegistry.getCommand("nonexistent")).thenReturn(null);
        
        CommandContext context = CommandContext.builder()
                .arguments(List.of("nonexistent"))
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(commandRegistry).getCommand("nonexistent");
        verify(renderer).renderError(anyString());
    }
    
    @Test
    void testDisplayHelpWithException() {
        // Setup
        when(commandRegistry.getAllCommandNames()).thenThrow(new RuntimeException("Registry error"));
        
        CommandContext context = CommandContext.builder()
                .arguments(List.of())
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(renderer).renderError(anyString());
    }
}
