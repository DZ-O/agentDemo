package com.openbutler.cli.terminal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ColorScheme model defining color properties and ANSI codes for terminal rendering.
 * 
 * This class holds ANSI color codes for different message types in the CLI system,
 * providing a consistent color scheme for user interface elements.
 * 
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 8.2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorScheme {
    
    /**
     * 用户输入颜色
     * Validates: Requirement 3.2
     */
    private String userColor;
    
    /**
     * AI响应颜色
     * Validates: Requirement 3.3
     */
    private String aiColor;
    
    /**
     * 错误消息颜色
     * Validates: Requirement 3.4
     */
    private String errorColor;
    
    /**
     * 警告消息颜色
     * Validates: Requirement 3.5
     */
    private String warningColor;
    
    /**
     * 系统消息颜色
     * Validates: Requirement 3.6
     */
    private String systemColor;
    
    /**
     * 成功消息颜色
     */
    private String successColor;
    
    /**
     * 提示符颜色
     */
    private String promptColor;
    
    /**
     * 预定义默认主题
     * Provides a default color scheme with standard ANSI color codes
     */
    public static final ColorScheme DEFAULT = ColorScheme.builder()
        .userColor("\u001B[34m")      // 蓝色 - Blue
        .aiColor("\u001B[32m")        // 绿色 - Green
        .errorColor("\u001B[31m")     // 红色 - Red
        .warningColor("\u001B[33m")   // 黄色 - Yellow
        .systemColor("\u001B[90m")    // 灰色 - Gray
        .successColor("\u001B[32m")   // 绿色 - Green
        .promptColor("\u001B[36m")    // 青色 - Cyan
        .build();
}
