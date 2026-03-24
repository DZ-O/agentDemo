package com.openbutler.cli.terminal.rendering;

/**
 * TerminalRenderer interface defining core rendering operations for the CLI system.
 * 
 * This interface provides methods for rendering various types of messages and UI elements
 * in the terminal with appropriate styling and formatting.
 * 
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 7.9, 9.1, 9.2
 */
public interface TerminalRenderer {
    
    /**
     * 渲染用户提示符
     * Renders the user prompt with the current session name.
     * 
     * Validates: Requirement 7.9
     * 
     * @param sessionName the name of the current session to display in the prompt
     */
    void renderPrompt(String sessionName);
    
    /**
     * 渲染AI响应（支持流式）
     * Renders AI response text, supporting streaming output.
     * 
     * Validates: Requirement 3.3
     * 
     * @param chunk a chunk of AI response text to render
     */
    void renderAIResponse(String chunk);
    
    /**
     * 渲染错误消息
     * Renders an error message in red color.
     * 
     * Validates: Requirements 3.4, 9.1
     * 
     * @param message the error message to display
     */
    void renderError(String message);
    
    /**
     * 渲染成功消息
     * Renders a success message in green color.
     * 
     * Validates: Requirement 9.2
     * 
     * @param message the success message to display
     */
    void renderSuccess(String message);
    
    /**
     * 渲染警告消息
     * Renders a warning message in yellow color.
     * 
     * Validates: Requirement 3.5
     * 
     * @param message the warning message to display
     */
    void renderWarning(String message);
    
    /**
     * 渲染系统消息
     * Renders a system message in gray color.
     * 
     * Validates: Requirement 3.6
     * 
     * @param message the system message to display
     */
    void renderSystem(String message);
    
    /**
     * 清空屏幕
     * Clears the terminal screen.
     * 
     * @see <a href="https://en.wikipedia.org/wiki/ANSI_escape_code">ANSI Escape Codes</a>
     */
    void clear();
    
    /**
     * 创建进度条
     * Creates a progress bar for tracking long-running operations.
     * 
     * Validates: Requirement 3.9
     * 
     * @param task the name of the task being tracked
     * @param total the total number of steps (max value)
     * @return a new ProgressBar instance
     */
    ProgressBar createProgressBar(String task, long total);
    
    /**
     * 换行文本以适应终端宽度
     * Wraps text to fit within terminal width without breaking words.
     * 
     * Validates: Requirement 3.10
     * 
     * @param text the text to wrap
     * @return the wrapped text with newlines inserted at appropriate positions
     */
    String wrapText(String text);
}
