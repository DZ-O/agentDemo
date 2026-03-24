package com.openbutler.cli.terminal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.openbutler.cli.terminal.model.CLIConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ConfigManager 是 OpenButler CLI 的配置管理服务。
 *
 * 管理 CLI 配置的加载、保存、缓存和验证。
 * 配置以 YAML 格式存储在 ~/.openbutler/config.yml
 * 并使用 1 分钟 TTL 缓存来减少文件 I/O。
 *
 * 验证要求：8.1, 8.7, 8.8, 10.6
 */
@Slf4j
@Service
public class ConfigManager {
    
    private static final String CONFIG_DIR = ".openbutler";
    private static final String CONFIG_FILE = "config.yml";
    private static final long CACHE_TTL_MS = 60_000; // 1 minute
    
    private final ObjectMapper yamlMapper;
    private final Path configPath;
    private final ReadWriteLock lock;
    
    // 缓存字段
    private volatile CLIConfig cachedConfig;
    private volatile long lastLoadTime;
    
    public ConfigManager() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.configPath = Paths.get(System.getProperty("user.home"), CONFIG_DIR, CONFIG_FILE);
        this.lock = new ReentrantReadWriteLock();
        this.lastLoadTime = 0;
    }
    
    /**
     * 从文件加载配置并使用缓存。
     * 使用 1 分钟 TTL 缓存来减少文件 I/O 操作。
     *
     * 验证要求：8.1, 10.6
     *
     * @return 加载的配置，如果文件不存在则返回默认值
     */
    public CLIConfig loadConfig() {
        long now = System.currentTimeMillis();
        
        // 首先检查缓存（双重检查锁定模式）
        if (cachedConfig != null && (now - lastLoadTime) < CACHE_TTL_MS) {
            log.debug("Returning cached config (age: {}ms)", now - lastLoadTime);
            return cachedConfig;
        }
        
        lock.writeLock().lock();
        try {
            // 获取锁后再次检查
            if (cachedConfig != null && (now - lastLoadTime) < CACHE_TTL_MS) {
                return cachedConfig;
            }
            
            CLIConfig config;
            if (Files.exists(configPath)) {
                log.debug("Loading config from: {}", configPath);
                config = yamlMapper.readValue(configPath.toFile(), CLIConfig.class);
            } else {
                log.info("Config file not found, creating default config");
                config = createDefaultConfig();
                saveConfig(config);
            }
            
            // 更新缓存
            cachedConfig = config;
            lastLoadTime = System.currentTimeMillis();
            
            return config;
        } catch (IOException e) {
            log.error("Failed to load config from {}, using defaults", configPath, e);
            return createDefaultConfig();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 将配置保存为 YAML 格式到文件。
     * 保存后使缓存失效。
     *
     * 验证要求：8.1
     *
     * @param config 要保存的配置
     * @throws IOException 如果保存失败
     */
    public void saveConfig(CLIConfig config) throws IOException {
        lock.writeLock().lock();
        try {
            // 保存前验证
            if (!validateConfig(config)) {
                throw new IllegalArgumentException("Invalid configuration");
            }
            
            // 确保目录存在
            Files.createDirectories(configPath.getParent());
            
            // 写入文件
            yamlMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(configPath.toFile(), config);
            
            log.info("Config saved to: {}", configPath);
            
            // 更新缓存
            cachedConfig = config;
            lastLoadTime = System.currentTimeMillis();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 按键获取特定配置值。
     *
     * @param key 配置键
     * @param type 期望的值类型
     * @param <T> 值类型
     * @return 配置值
     */
    public <T> T getConfigValue(String key, Class<T> type) {
        CLIConfig config = loadConfig();
        
        return switch (key) {
            case "colorScheme" -> type.cast(config.getColorScheme());
            case "promptStyle" -> type.cast(config.getPromptStyle());
            case "streamingEnabled" -> type.cast(config.isStreamingEnabled());
            case "historySize" -> type.cast(config.getHistorySize());
            case "defaultSessionName" -> type.cast(config.getDefaultSessionName());
            case "debugMode" -> type.cast(config.isDebugMode());
            case "autoSaveInterval" -> type.cast(config.getAutoSaveInterval());
            case "autoCompleteEnabled" -> type.cast(config.isAutoCompleteEnabled());
            case "terminalWidth" -> type.cast(config.getTerminalWidth());
            default -> throw new IllegalArgumentException("Unknown config key: " + key);
        };
    }
    
    /**
     * 按键设置特定配置值。
     *
     * @param key 配置键
     * @param value 新值
     * @throws IOException 如果保存失败
     */
    public void setConfigValue(String key, Object value) throws IOException {
        CLIConfig config = loadConfig();
        
        switch (key) {
            case "colorScheme" -> config.setColorScheme((String) value);
            case "promptStyle" -> config.setPromptStyle((String) value);
            case "streamingEnabled" -> config.setStreamingEnabled((Boolean) value);
            case "historySize" -> config.setHistorySize((Integer) value);
            case "defaultSessionName" -> config.setDefaultSessionName((String) value);
            case "debugMode" -> config.setDebugMode((Boolean) value);
            case "autoSaveInterval" -> config.setAutoSaveInterval((Integer) value);
            case "autoCompleteEnabled" -> config.setAutoCompleteEnabled((Boolean) value);
            case "terminalWidth" -> config.setTerminalWidth((Integer) value);
            default -> throw new IllegalArgumentException("Unknown config key: " + key);
        }
        
        saveConfig(config);
    }
    
    /**
     * 验证配置值。
     *
     * 验证要求：8.8
     *
     * @param config 要验证的配置
     * @return 如果有效返回 true，否则返回 false
     */
    public boolean validateConfig(CLIConfig config) {
        if (config == null) {
            log.warn("Config is null");
            return false;
        }
        
        // 验证历史大小
        if (config.getHistorySize() < 0 || config.getHistorySize() > 10000) {
            log.warn("Invalid historySize: {} (must be 0-10000)", config.getHistorySize());
            return false;
        }
        
        // 验证自动保存间隔
        if (config.getAutoSaveInterval() < 0 || config.getAutoSaveInterval() > 3600) {
            log.warn("Invalid autoSaveInterval: {} (must be 0-3600)", config.getAutoSaveInterval());
            return false;
        }
        
        // 验证终端宽度
        if (config.getTerminalWidth() < 0 || config.getTerminalWidth() > 500) {
            log.warn("Invalid terminalWidth: {} (must be 0-500)", config.getTerminalWidth());
            return false;
        }
        
        // 验证默认会话名称不为空
        if (config.getDefaultSessionName() == null || config.getDefaultSessionName().trim().isEmpty()) {
            log.warn("Invalid defaultSessionName: cannot be null or empty");
            return false;
        }
        
        return true;
    }
    
    /**
     * 创建默认配置。
     *
     * 验证要求：8.7
     *
     * @return 默认配置
     */
    public CLIConfig createDefaultConfig() {
        return CLIConfig.builder()
                .colorScheme("default")
                .promptStyle("{{session}} > ")
                .streamingEnabled(true)
                .historySize(1000)
                .defaultSessionName("default")
                .debugMode(false)
                .autoSaveInterval(30)
                .autoCompleteEnabled(true)
                .terminalWidth(0) // 0 means auto-detect
                .build();
    }
    
    /**
     * 将配置重置为默认值。
     *
     * @return 默认配置
     * @throws IOException 如果保存失败
     */
    public CLIConfig resetToDefaults() throws IOException {
        CLIConfig defaultConfig = createDefaultConfig();
        saveConfig(defaultConfig);
        return defaultConfig;
    }
    
    /**
     * Get the configuration file path.
     * 
     * @return path to config file
     */
    public Path getConfigPath() {
        return configPath;
    }
    
    /**
     * Invalidate the configuration cache.
     * Forces next loadConfig() to read from file.
     */
    public void invalidateCache() {
        lock.writeLock().lock();
        try {
            cachedConfig = null;
            lastLoadTime = 0;
            log.debug("Config cache invalidated");
        } finally {
            lock.writeLock().unlock();
        }
    }
}
