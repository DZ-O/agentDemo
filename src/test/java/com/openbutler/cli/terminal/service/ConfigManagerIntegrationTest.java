package com.openbutler.cli.terminal.service;

import com.openbutler.cli.terminal.model.CLIConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ConfigManager with actual YAML file I/O.
 * 
 * Tests the complete workflow of configuration management including
 * YAML serialization/deserialization and file system operations.
 * 
 * Validates: Requirements 8.1, 8.7, 8.8, 10.6
 */
class ConfigManagerIntegrationTest {
    
    @TempDir
    Path tempDir;
    
    private ConfigManager configManager;
    private String originalUserHome;
    
    @BeforeEach
    void setUp() {
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());
        configManager = new ConfigManager();
    }
    
    @AfterEach
    void tearDown() {
        System.setProperty("user.home", originalUserHome);
    }
    
    /**
     * Test complete workflow: create, save, load, modify, reload.
     * Validates: Requirement 8.1
     */
    @Test
    void testCompleteConfigWorkflow() throws IOException {
        // 1. Load config (creates default)
        CLIConfig config1 = configManager.loadConfig();
        assertEquals("default", config1.getColorScheme());
        
        // 2. Modify and save
        config1.setColorScheme("dark");
        config1.setHistorySize(2000);
        configManager.saveConfig(config1);
        
        // 3. Invalidate cache and reload
        configManager.invalidateCache();
        CLIConfig config2 = configManager.loadConfig();
        assertEquals("dark", config2.getColorScheme());
        assertEquals(2000, config2.getHistorySize());
        
        // 4. Modify using setConfigValue
        configManager.setConfigValue("promptStyle", "$ ");
        configManager.setConfigValue("streamingEnabled", false);
        
        // 5. Reload and verify
        configManager.invalidateCache();
        CLIConfig config3 = configManager.loadConfig();
        assertEquals("$ ", config3.getPromptStyle());
        assertFalse(config3.isStreamingEnabled());
    }
    
    /**
     * Test YAML file format is correct and human-readable.
     * Validates: Requirement 8.1
     */
    @Test
    void testYAMLFileFormatIsCorrect() throws IOException {
        configManager.loadConfig(); // Create default config
        
        Path configPath = configManager.getConfigPath();
        String content = Files.readString(configPath);
        
        // Verify YAML structure
        assertTrue(content.contains("colorScheme:"), "Should contain colorScheme field");
        assertTrue(content.contains("promptStyle:"), "Should contain promptStyle field");
        assertTrue(content.contains("streamingEnabled:"), "Should contain streamingEnabled field");
        assertTrue(content.contains("historySize:"), "Should contain historySize field");
        assertTrue(content.contains("defaultSessionName:"), "Should contain defaultSessionName field");
        
        // Verify values
        assertTrue(content.contains("default"), "Should contain default values");
        assertTrue(content.contains("1000"), "Should contain historySize value");
    }
    
    /**
     * Test config file is created in correct directory structure.
     * Validates: Requirement 8.1
     */
    @Test
    void testConfigDirectoryStructure() {
        configManager.loadConfig();
        
        Path configPath = configManager.getConfigPath();
        Path configDir = configPath.getParent();
        
        assertTrue(Files.exists(configDir), "Config directory should exist");
        assertTrue(Files.isDirectory(configDir), "Config directory should be a directory");
        assertEquals(".openbutler", configDir.getFileName().toString(), "Directory should be .openbutler");
        assertEquals("config.yml", configPath.getFileName().toString(), "File should be config.yml");
    }
    
    /**
     * Test cache behavior with multiple loads.
     * Validates: Requirement 10.6
     */
    @Test
    void testCacheBehaviorWithMultipleLoads() throws IOException {
        // First load
        long start1 = System.nanoTime();
        CLIConfig config1 = configManager.loadConfig();
        long duration1 = System.nanoTime() - start1;
        
        // Second load (should be cached and faster)
        long start2 = System.nanoTime();
        CLIConfig config2 = configManager.loadConfig();
        long duration2 = System.nanoTime() - start2;
        
        // Cached load should be significantly faster
        assertTrue(duration2 < duration1 / 2, 
                "Cached load should be at least 2x faster than file load");
        
        // Should return same instance
        assertSame(config1, config2, "Should return cached instance");
    }
    
    /**
     * Test handling of corrupted config file.
     * Validates: Requirement 8.1
     */
    @Test
    void testHandlingOfCorruptedConfigFile() throws IOException {
        // Create config file
        configManager.loadConfig();
        
        // Corrupt the file
        Path configPath = configManager.getConfigPath();
        Files.writeString(configPath, "invalid: yaml: content: [[[");
        
        // Invalidate cache
        configManager.invalidateCache();
        
        // Load should return default config (not throw exception)
        CLIConfig config = configManager.loadConfig();
        assertNotNull(config);
        assertEquals("default", config.getColorScheme());
    }
    
    /**
     * Test reset to defaults removes custom settings.
     * Validates: Requirement 8.7
     */
    @Test
    void testResetToDefaultsRemovesCustomSettings() throws IOException {
        // Create custom config
        CLIConfig custom = CLIConfig.builder()
                .colorScheme("custom")
                .promptStyle("# ")
                .streamingEnabled(false)
                .historySize(100)
                .defaultSessionName("custom")
                .debugMode(true)
                .autoSaveInterval(10)
                .autoCompleteEnabled(false)
                .terminalWidth(80)
                .build();
        
        configManager.saveConfig(custom);
        
        // Verify custom config is saved
        configManager.invalidateCache();
        CLIConfig loaded = configManager.loadConfig();
        assertEquals("custom", loaded.getColorScheme());
        
        // Reset to defaults
        configManager.resetToDefaults();
        
        // Verify defaults are restored
        configManager.invalidateCache();
        CLIConfig defaults = configManager.loadConfig();
        assertEquals("default", defaults.getColorScheme());
        assertEquals("{{session}} > ", defaults.getPromptStyle());
        assertTrue(defaults.isStreamingEnabled());
    }
    
    /**
     * Test validation prevents saving invalid config.
     * Validates: Requirement 8.8
     */
    @Test
    void testValidationPreventsSavingInvalidConfig() {
        CLIConfig invalid = configManager.createDefaultConfig();
        invalid.setHistorySize(-100);
        
        assertThrows(IllegalArgumentException.class, () -> {
            configManager.saveConfig(invalid);
        });
        
        // Verify file was not modified
        CLIConfig loaded = configManager.loadConfig();
        assertEquals(1000, loaded.getHistorySize(), "Should still have default value");
    }
    
    /**
     * Test all config values can be set and retrieved.
     * Validates: Requirement 8.1
     */
    @Test
    void testAllConfigValuesCanBeSetAndRetrieved() throws IOException {
        configManager.loadConfig(); // Initialize
        
        // Set all values
        configManager.setConfigValue("colorScheme", "dark");
        configManager.setConfigValue("promptStyle", "$ ");
        configManager.setConfigValue("streamingEnabled", false);
        configManager.setConfigValue("historySize", 500);
        configManager.setConfigValue("defaultSessionName", "test");
        configManager.setConfigValue("debugMode", true);
        configManager.setConfigValue("autoSaveInterval", 60);
        configManager.setConfigValue("autoCompleteEnabled", false);
        configManager.setConfigValue("terminalWidth", 120);
        
        // Retrieve all values
        assertEquals("dark", configManager.getConfigValue("colorScheme", String.class));
        assertEquals("$ ", configManager.getConfigValue("promptStyle", String.class));
        assertFalse(configManager.getConfigValue("streamingEnabled", Boolean.class));
        assertEquals(500, configManager.getConfigValue("historySize", Integer.class));
        assertEquals("test", configManager.getConfigValue("defaultSessionName", String.class));
        assertTrue(configManager.getConfigValue("debugMode", Boolean.class));
        assertEquals(60, configManager.getConfigValue("autoSaveInterval", Integer.class));
        assertFalse(configManager.getConfigValue("autoCompleteEnabled", Boolean.class));
        assertEquals(120, configManager.getConfigValue("terminalWidth", Integer.class));
    }
}
