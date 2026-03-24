package com.openbutler.cli.terminal.rendering;

import com.openbutler.cli.terminal.model.ColorScheme;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.stereotype.Component;

/**
 * 使用 Jansi 库的基于 ANSI 的 TerminalRenderer 实现。
 *
 * 本类提供跨平台的 ANSI 颜色支持以进行终端渲染，
 * 通过 Jansi 自动处理 Windows 控制台配置。
 *
 * 验证要求：3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.10, 7.9, 9.1, 9.2
 */
@Component
public class AnsiTerminalRenderer implements TerminalRenderer {
    
    private final ColorScheme colorScheme;
    private final TextWrapper textWrapper;
    private static final String RESET = "\u001B[0m";
    
    /**
     * 初始化 Jansi 并设置颜色方案的构造函数。
     */
    public AnsiTerminalRenderer() {
        // 安装 Jansi 以在 Windows 上启用 ANSI 支持
        AnsiConsole.systemInstall();
        this.colorScheme = ColorScheme.DEFAULT;
        this.textWrapper = new TextWrapper();
    }
    
    /**
     * 使用自定义颜色方案的构造函数。
     *
     * @param colorScheme 用于渲染的颜色方案
     */
    public AnsiTerminalRenderer(ColorScheme colorScheme) {
        AnsiConsole.systemInstall();
        this.colorScheme = colorScheme;
        this.textWrapper = new TextWrapper();
    }
    
    /**
     * Constructor with custom color scheme and terminal width.
     * 
     * @param colorScheme the color scheme to use for rendering
     * @param terminalWidth the terminal width for text wrapping (0 for auto-detection)
     */
    public AnsiTerminalRenderer(ColorScheme colorScheme, int terminalWidth) {
        AnsiConsole.systemInstall();
        this.colorScheme = colorScheme;
        this.textWrapper = new TextWrapper(terminalWidth);
    }
    
    @Override
    public void renderPrompt(String sessionName) {
        String prompt = Ansi.ansi()
                .fg(Ansi.Color.CYAN)
                .a(sessionName)
                .reset()
                .a(" > ")
                .toString();
        System.out.print(prompt);
        System.out.flush();
    }
    
    @Override
    public void renderAIResponse(String chunk) {
        String coloredChunk = colorScheme.getAiColor() + chunk + RESET;
        System.out.print(coloredChunk);
        System.out.flush();
    }
    
    @Override
    public void renderError(String message) {
        String errorMessage = Ansi.ansi()
                .fg(Ansi.Color.RED)
                .a("[ERROR] ")
                .a(message)
                .reset()
                .toString();
        System.out.println(errorMessage);
        System.out.flush();
    }
    
    @Override
    public void renderSuccess(String message) {
        String successMessage = Ansi.ansi()
                .fg(Ansi.Color.GREEN)
                .a("[SUCCESS] ")
                .a(message)
                .reset()
                .toString();
        System.out.println(successMessage);
        System.out.flush();
    }
    
    @Override
    public void renderWarning(String message) {
        String warningMessage = Ansi.ansi()
                .fg(Ansi.Color.YELLOW)
                .a("[WARNING] ")
                .a(message)
                .reset()
                .toString();
        System.out.println(warningMessage);
        System.out.flush();
    }
    
    @Override
    public void renderSystem(String message) {
        String systemMessage = colorScheme.getSystemColor() + message + RESET;
        System.out.println(systemMessage);
        System.out.flush();
    }
    
    @Override
    public void clear() {
        // ANSI escape code to clear screen and move cursor to home position
        System.out.print(Ansi.ansi().eraseScreen().cursor(1, 1));
        System.out.flush();
    }
    
    @Override
    public ProgressBar createProgressBar(String task, long total) {
        return ProgressBar.create(task, total);
    }
    
    @Override
    public String wrapText(String text) {
        return textWrapper.wrap(text);
    }
}
