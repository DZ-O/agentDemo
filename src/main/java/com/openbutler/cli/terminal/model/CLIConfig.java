package com.openbutler.cli.terminal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CLI configuration model for OpenButler CLI system.
 * 
 * This class holds all configuration properties for the CLI including
 * color schemes, prompt styles, streaming settings, and other user preferences.
 * 
 * Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "openbutler.cli")
public class CLIConfig {
    
    /**
     * 主题颜色方案
     * Validates: Requirement 8.2
     */
    private String colorScheme;
    
    /**
     * 提示符样式
     * Validates: Requirement 8.3
     */
    private String promptStyle;
    
    /**
     * 是否启用流式输出
     * Validates: Requirement 8.4
     */
    private boolean streamingEnabled;
    
    /**
     * 历史记录保存数量
     * Validates: Requirement 8.5
     */
    private int historySize;
    
    /**
     * 默认会话名称
     * Validates: Requirement 8.6
     */
    private String defaultSessionName;
    
    /**
     * 是否启用调试模式
     */
    private boolean debugMode;
    
    /**
     * 自动保存间隔（秒）
     */
    private int autoSaveInterval;
    
    /**
     * 是否启用自动补全
     */
    private boolean autoCompleteEnabled;
    
    /**
     * 终端宽度（0表示自动检测）
     */
    private int terminalWidth;
}
