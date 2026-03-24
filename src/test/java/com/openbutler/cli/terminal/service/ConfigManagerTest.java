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
 * Unit tests for ConfigManager service.
 * 
 * Tests configuration loading, saving, caching, validation, and default generation.
 * 
 * Validates: Requirements 8.1, 8.7, 8.8, 10.6
 */
class ConfigManagerTest {
    
    @TempDir
    Path tempDir;
    
    private ConfigManager configManager;
    private String originalUserHome;
    
    @BeforeEach
    void setUp() {
        // Override user.home to use temp directory
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());
        
        configManager = new ConfigManager();
    }
    
    @AfterEach
    void tearDown() {
        // Restore original user.home
        System.setProperty("user.home", originalUserHome);
    }
    
    /**
     * Test loading config when file doesn't exist creates default config.
     * Validates: Requirements 8.1, 8.7
     */
    @Test
    void testLoadConfigCreatesDefaultWhenFileNotExists() {
        CLIConfig config = configManager.loadConfig();
        
        assertNotNull(config);
        assertEquals("default", config.getColorScheme());
        assertEquals("{{session}} > ", config.getPromptStyle());
        assertTrue(config.isStreamingEnabled());
        assertEquals(1000, config.getHistorySize());
        assertEquals("default", config.getDefaultSessionName());
        assertFalse(config.isDebugMode());
        assertEquals(30, config.getAutoSaveInterval());
        assertTrue(config.isAutoCompleteEnabled());
        assertEquals(0, config.getTerminalWidth());
        
        // Verify file was created
        assertTrue(Files.exists(configManager.getConfigPath()));
    }
    
    /**
     * Test saving and loading config round-trip.
     * Validates: Requirement 8.1
     */
    @Test
    void testSaveAndLoadConfigRoundTrip() throws IOException {
        CLIConfig config = CLIConfig.builder()
                .colorScheme("dark")
                .promptStyle("$ ")
                .streamingEnabled(false)
                .historySize(500)
                .defaultSessionName("test-session")
                .debugMode(true)
                .autoSaveInterval(60)
                .autoCompleteEnabled(false)
                .terminalWidth(120)
                .build();
        
        configManager.saveConfig(config);
        
        // Invalidate cache to force reload from file
        configManager.invalidateCache();
        
        CLIConfig loaded = configManager.loadConfig();
        
        assertEquals("dark", loaded.getColorScheme());
        assertEquals("$ ", loaded.getPromptStyle());
        assertFalse(loaded.isStreamingEnabled());
        assertEquals(500, loaded.getHistorySize());
        assertEquals("test-session", loaded.getDefaultSessionName());
        assertTrue(loaded.isDebugMode());
        assertEquals(60, loaded.getAutoSaveInterval());
        assertFalse(loaded.isAutoCompleteEnabled());
        assertEquals(120, loaded.getTerminalWidth());
    }
    
    /**
     * Test config caching reduces file I/O.
     * Validates: Requirement 10.6
     */
    @Test
    void testConfigCachingReducesIO() throws IOException {
        // First load creates file
        CLIConfig config1 = configManager.loadConfig();
        assertNotNull(config1);
        
        // Modify file directly
        Path configPath = configManager.getConfigPath();
        String content = Files.readString(configPath);
        String modified = content.replace("colorScheme: \"default\"", "colorScheme: \"modified\"");
        Files.writeString(configPath, modified);
        
        // Second load should return cached value (not modified)
        CLIConfig config2 = configManager.loadConfig();
        assertEquals("default", config2.getColorScheme(), "Should return cached value");
        
        // Note: Cache expiry test is covered by testCacheInvalidationForcesReload
        // to avoid long-running tests. The 1-minute TTL is validated through
        // manual testing and integration tests.
    }
    
    /**
     * Test cache invalidation forces reload.
     * Validates: Requirement 10.6
     */
    @Test
    void testCacheInvalidationForcesReload() throws IOException {
        // First load
        CLIConfig config1 = configManager.loadConfig();
        assertEquals("default", config1.getColorScheme());
        
        // Modify file directly
        Path configPath = configManager.getConfigPath();
        String content = Files.readString(configPath);
        String modified = content.replace("colorScheme: \"default\"", "colorScheme: \"modified\"");
        Files.writeString(configPath, modified);
        
        // Invalidate cache
        configManager.invalidateCache();
        
        // Load should read from file immediately
        CLIConfig config2 = configManager.loadConfig();
        assertEquals("modified", config2.getColorScheme());
    }
    
    /**
     * Test getting specific config values.
     * Validates: Requirement 8.1
     */
    @Test
    void testGetConfigValue() {
        configManager.loadConfig(); // Create default config
        
        assertEquals("default", configManager.getConfigValue("colorScheme", String.class));
        assertEquals("{{session}} > ", configManager.getConfigValue("promptStyle", String.class));
        assertTrue(configManager.getConfigValue("streamingEnabled", Boolean.class));
        assertEquals(1000, configManager.getConfigValue("historySize", Integer.class));
        assertEquals("default", configManager.getConfigValue("defaultSessionName", String.class));
    }
    
    /**
     * Test setting specific config values.
     * Validates: Requirement 8.1
     */
    @Test
    void testSetConfigValue() throws IOException {
        configManager.loadConfig(); // Create default config
        
        configManager.setConfigValue("colorScheme", "dark");
        configManager.setConfigValue("historySize", 2000);
        configManager.setConfigValue("streamingEnabled", false);
        
        assertEquals("dark", configManager.getConfigValue("colorScheme", String.class));
        assertEquals(2000, configManager.getConfigValue("historySize", Integer.class));
        assertFalse(configManager.getConfigValue("streamingEnabled", Boolean.class));
    }
    
    /**
     * Test getting unknown config key throws exception.
     */
    @Test
    void testGetUnknownConfigKeyThrowsException() {
        configManager.loadConfig();
        
        assertThrows(IllegalArgumentException.class, () -> {
            configManager.getConfigValue("unknownKey", String.class);
        });
    }
    
    /**
     * Test setting unknown config key throws exception.
     */
    @Test
    void testSetUnknownConfigKeyThrowsException() {
        configManager.loadConfig();
        
        assertThrows(IllegalArgumentException.class, () -> {
            configManager.setConfigValue("unknownKey", "value");
        });
    }
    
    /**
     * Test validation accepts valid config.
     * Validates: Requirement 8.8
     */
    @Test
    void testValidationAcceptsValidConfig() {
        CLIConfig config = configManager.createDefaultConfig();
        assertTrue(configManager.validateConfig(config));
    }
    
    /**
     * Test validation rejects null config.
     * Validates: Requirement 8.8
     */
    @Test
    void testValidationRejectsNullConfig() {
        assertFalse(configManager.validateConfig(null));
    }
    
    /**
     * Test validation rejects invalid history size.
     * Validates: Requirement 8.8
     */
    @Test
    void testValidationRejectsInvalidHistorySize() {
        CLIConfig config = configManager.createDefaultConfig();
        
        config.setHistorySize(-1);
        assertFalse(configManager.validateConfig(config));
        
        config.setHistorySize(10001);
        assertFalse(configManager.validateConfig(config));
        
        config.setHistorySize(5000);
        assertTrue(configManager.validateConfig(config));
    }
    
    /**
     * Test validation rejects invalid auto save interval.
     * Validates: Requirement 8.8
     */
    @Test
    void testValidationRejectsInvalidAutoSaveInterval() {
        CLIConfig config = configManager.createDefaultConfig();
        
        config.setAutoSaveInterval(-1);
        assertFalse(configManager.validateConfig(config));
        
        config.setAutoSaveInterval(3601);
        assertFalse(configManager.validateConfig(config));
        
        config.setAutoSaveInterval(60);
        assertTrue(configManager.validateConfig(config));
    }
    
    /**
     * Test validation rejects invalid terminal width.
     * Validates: Requirement 8.8
     */
    @Test
    void testValidationRejectsInvalidTerminalWidth() {
        CLIConfig config = configManager.createDefaultConfig();
        
        config.setTerminalWidth(-1);
        assertFalse(configManager.validateConfig(config));
        
        config.setTerminalWidth(501);
        assertFalse(configManager.validateConfig(config));
        
        config.setTerminalWidth(120);
        assertTrue(configManager.validateConfig(config));
    }
    
    /**
     * Test validation rejects empty default session name.
     * Validates: Requirement 8.8
     */
    @Test
    void testValidationRejectsEmptyDefaultSessionName() {
        CLIConfig config = configManager.createDefaultConfig();
        
        config.setDefaultSessionName(null);
        assertFalse(configManager.validateConfig(config));
        
        config.setDefaultSessionName("");
        assertFalse(configManager.validateConfig(config));
        
        config.setDefaultSessionName("   ");
        assertFalse(configManager.validateConfig(config));
        
        config.setDefaultSessionName("valid-name");
        assertTrue(configManager.validateConfig(config));
    }
    
    /**
     * Test saving invalid config throws exception.
     * Validates: Requirement 8.8
     */
    @Test
    void testSavingInvalidConfigThrowsException() {
        CLIConfig config = configManager.createDefaultConfig();
        config.setHistorySize(-1);
        
        assertThrows(IllegalArgumentException.class, () -> {
            configManager.saveConfig(config);
        });
    }
    
    /**
     * Test reset to defaults.
     * Validates: Requirement 8.7
     */
    @Test
    void testResetToDefaults() throws IOException {
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
        
        // Reset to defaults
        CLIConfig defaults = configManager.resetToDefaults();
        
        assertEquals("default", defaults.getColorScheme());
        assertEquals("{{session}} > ", defaults.getPromptStyle());
        assertTrue(defaults.isStreamingEnabled());
        assertEquals(1000, defaults.getHistorySize());
        assertEquals("default", defaults.getDefaultSessionName());
        assertFalse(defaults.isDebugMode());
        assertEquals(30, defaults.getAutoSaveInterval());
        assertTrue(defaults.isAutoCompleteEnabled());
        assertEquals(0, defaults.getTerminalWidth());
        
        // Verify file was updated
        configManager.invalidateCache();
        CLIConfig loaded = configManager.loadConfig();
        assertEquals("default", loaded.getColorScheme());
    }
    
    /**
     * Test default config has all required fields.
     * Validates: Requirement 8.7
     */
    @Test
    void testDefaultConfigHasAllRequiredFields() {
        CLIConfig config = configManager.createDefaultConfig();
        
        assertNotNull(config.getColorScheme());
        assertNotNull(config.getPromptStyle());
        assertNotNull(config.getDefaultSessionName());
        assertTrue(config.getHistorySize() > 0);
        assertTrue(config.getAutoSaveInterval() >= 0);
        assertTrue(config.getTerminalWidth() >= 0);
    }
    
    /**
     * Test config file is created in correct location.
     * Validates: Requirement 8.1
     */
    @Test
    void testConfigFileLocationIsCorrect() {
        configManager.loadConfig();
        
        Path expectedPath = tempDir.resolve(".openbutler").resolve("config.yml");
        assertEquals(expectedPath, configManager.getConfigPath());
        assertTrue(Files.exists(expectedPath));
    }
    
    /**
     * Test concurrent access to config is thread-safe.
     * Validates: Requirement 10.6
     */
    @Test
    void testConcurrentAccessIsThreadSafe() throws InterruptedException {
        configManager.loadConfig(); // Initialize
        
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    if (index % 2 == 0) {
                        configManager.loadConfig();
                    } else {
                        configManager.setConfigValue("historySize", 1000 + index);
                    }
                } catch (IOException e) {
                    fail("Thread " + index + " failed: " + e.getMessage());
                }
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Verify config is still valid
        CLIConfig config = configManager.loadConfig();
        assertNotNull(config);
        assertTrue(configManager.validateConfig(config));
    }
}
