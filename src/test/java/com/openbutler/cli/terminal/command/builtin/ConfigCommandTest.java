package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.model.CLIConfig;
import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.cli.terminal.service.ConfigManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConfigCommand.
 * 
 * Validates: Requirements 2.7, 8.8, 8.9, 8.10
 */
@ExtendWith(MockitoExtension.class)
class ConfigCommandTest {
    
    @Mock
    private ConfigManager configManager;
    
    @Mock
    private TerminalRenderer renderer;
    
    private ConfigCommand command;
    private CLIConfig testConfig;
    
    @BeforeEach
    void setUp() {
        command = new ConfigCommand();
        
        testConfig = CLIConfig.builder()
                .colorScheme("default")
                .promptStyle("{{session}} > ")
                .streamingEnabled(true)
                .historySize(1000)
                .defaultSessionName("default")
                .debugMode(false)
                .autoSaveInterval(30)
                .autoCompleteEnabled(true)
                .terminalWidth(0)
                .build();
    }
    
    @Test
    void testGetName() {
        assertEquals("config", command.getName());
    }
    
    @Test
    void testGetAliases() {
        List<String> aliases = command.getAliases();
        assertTrue(aliases.contains("cfg"));
        assertTrue(aliases.contains("conf"));
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
    void testShowConfig() {
        // Setup
        when(configManager.loadConfig()).thenReturn(testConfig);
        when(configManager.getConfigPath()).thenReturn(Paths.get("/test/config.yml"));
        
        CommandContext context = CommandContext.builder()
                .arguments(List.of("show"))
                .configManager(configManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(configManager).loadConfig();
        verify(renderer, atLeastOnce()).renderSystem(anyString());
    }
    
    @Test
    void testSetConfigString() throws IOException {
        // Setup
        CommandContext context = CommandContext.builder()
                .arguments(List.of("set", "colorScheme", "dark"))
                .configManager(configManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(configManager).setConfigValue("colorScheme", "dark");
        verify(renderer).renderSuccess(anyString());
    }
    
    @Test
    void testSetConfigBoolean() throws IOException {
        // Setup
        CommandContext context = CommandContext.builder()
                .arguments(List.of("set", "debugMode", "true"))
                .configManager(configManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(configManager).setConfigValue("debugMode", true);
        verify(renderer).renderSuccess(anyString());
    }
    
    @Test
    void testSetConfigInteger() throws IOException {
        // Setup
        CommandContext context = CommandContext.builder()
                .arguments(List.of("set", "historySize", "500"))
                .configManager(configManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(configManager).setConfigValue("historySize", 500);
        verify(renderer).renderSuccess(anyString());
    }
    
    @Test
    void testSetConfigWithInvalidKey() {
        // Setup
        CommandContext context = CommandContext.builder()
                .arguments(List.of("set", "invalidKey", "value"))
                .configManager(configManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(renderer).renderError(anyString());
    }
    
    @Test
    void testSetConfigWithMissingValue() throws IOException {
        // Setup
        CommandContext context = CommandContext.builder()
                .arguments(List.of("set", "colorScheme"))
                .configManager(configManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(renderer).renderError(anyString());
        verify(configManager, never()).setConfigValue(anyString(), any());
    }
    
    @Test
    void testResetConfig() throws IOException {
        // Setup
        when(configManager.resetToDefaults()).thenReturn(testConfig);
        
        CommandContext context = CommandContext.builder()
                .arguments(List.of("reset"))
                .configManager(configManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(configManager).resetToDefaults();
        verify(renderer).renderSuccess(anyString());
    }
    
    @Test
    void testConfigWithoutSubcommand() {
        // Setup
        CommandContext context = CommandContext.builder()
                .arguments(List.of())
                .configManager(configManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(renderer).renderError(anyString());
    }
    
    @Test
    void testConfigWithInvalidSubcommand() {
        // Setup
        CommandContext context = CommandContext.builder()
                .arguments(List.of("invalid"))
                .configManager(configManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(renderer).renderError(anyString());
    }
    
    @Test
    void testSetConfigWithIOException() throws IOException {
        // Setup
        doThrow(new IOException("Write error")).when(configManager).setConfigValue(anyString(), any());
        
        CommandContext context = CommandContext.builder()
                .arguments(List.of("set", "colorScheme", "dark"))
                .configManager(configManager)
                .renderer(renderer)
                .build();
        
        // Execute
        command.execute(context).block();
        
        // Verify
        verify(renderer).renderError(anyString());
    }
}
