package com.openbutler.cli.terminal.input;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.stereotype.Component;

/**
 * LineReader工厂类
 * 负责创建和配置JLine LineReader实例，提供历史记录、自动补全和快捷键支持
 */
@Component
public class LineReaderFactory {

    private final Terminal terminal;
    private final HistoryManager historyManager;
    private final CommandCompleter commandCompleter;

    public LineReaderFactory(Terminal terminal, 
                            HistoryManager historyManager,
                            CommandCompleter commandCompleter) {
        this.terminal = terminal;
        this.historyManager = historyManager;
        this.commandCompleter = commandCompleter;
    }

    /**
     * 创建配置好的LineReader实例
     * 
     * @param sessionName 当前会话名称，用于显示在提示符中
     * @return 配置好的LineReader实例
     */
    public LineReader createLineReader(String sessionName) {
        return LineReaderBuilder.builder()
                .terminal(terminal)
                .appName("OpenButler")
                // 配置历史记录
                .variable(LineReader.HISTORY_FILE, historyManager.getHistoryFile())
                .variable(LineReader.HISTORY_SIZE, historyManager.getHistorySize())
                // 配置自动补全
                .completer(commandCompleter)
                // 配置解析器
                .parser(new DefaultParser())
                // 配置选项
                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)  // 禁用事件扩展（!命令）
                .option(LineReader.Option.AUTO_FRESH_LINE, true)          // 自动刷新行
                .option(LineReader.Option.CASE_INSENSITIVE, true)         // 补全时不区分大小写
                .build();
    }

    /**
     * 创建带自定义提示符的LineReader
     * 
     * @param sessionName 会话名称
     * @return 配置好的LineReader
     */
    public LineReader createLineReaderWithPrompt(String sessionName) {
        LineReader lineReader = createLineReader(sessionName);
        
        // 配置快捷键绑定
        configureKeyBindings(lineReader);
        
        return lineReader;
    }

    /**
     * 配置快捷键绑定
     * 
     * @param lineReader LineReader实例
     */
    private void configureKeyBindings(LineReader lineReader) {
        // Ctrl+C: 中断当前操作（默认已支持）
        // Ctrl+D: 退出应用（默认已支持，会返回null）
        // Ctrl+L: 清空屏幕
        lineReader.getKeyMaps().get(LineReader.MAIN)
                .bind(new org.jline.reader.Reference("clear-screen"), "\u000C"); // Ctrl+L
        
        // 左右箭头编辑（默认已支持）
        // Home/End键（默认已支持）
        // 上下箭头历史导航（默认已支持）
    }

    /**
     * 生成彩色提示符
     * 
     * @param sessionName 会话名称
     * @return 格式化的提示符字符串
     */
    public String generatePrompt(String sessionName) {
        AttributedString prompt = new AttributedString(
                sessionName + " > ",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN)
        );
        return prompt.toAnsi();
    }
}
