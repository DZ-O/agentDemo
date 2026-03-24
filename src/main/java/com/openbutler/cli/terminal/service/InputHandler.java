package com.openbutler.cli.terminal.service;

import com.openbutler.cli.terminal.input.LineReaderFactory;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * InputHandler service for handling user input.
 *
 * This service handles:
 * - Reading user input using JLine LineReader
 * - Multi-line input support (backslash continuation)
 * - Interrupt signal handling (Ctrl+C)
 * - End-of-file handling (Ctrl+D)
 * - Integration with CommandParser
 *
 * Validates: Requirements 7.3, 7.5, 10.1
 */
@Service
public class InputHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(InputHandler.class);
    private static final String LINE_CONTINUATION = "\\";
    
    private final LineReaderFactory lineReaderFactory;
    private final CommandParser commandParser;
    
    public InputHandler(LineReaderFactory lineReaderFactory, CommandParser commandParser) {
        this.lineReaderFactory = lineReaderFactory;
        this.commandParser = commandParser;
    }
    
    /**
     * Read input from the user for the given session.
     *
     * This method handles:
     * - Multi-line input (lines ending with backslash)
     * - Ctrl+C interrupts (returns null)
     * - Ctrl+D end-of-file (returns null)
     *
     * @param sessionName the current session name for the prompt
     * @return the user input string, or null if interrupted or EOF
     */
    public String readInput(String sessionName) {
        LineReader lineReader = lineReaderFactory.createLineReader(sessionName);
        String prompt = lineReaderFactory.generatePrompt(sessionName);
        
        try {
            StringBuilder input = new StringBuilder();
            String line;
            
            // 读取第一行
            line = lineReader.readLine(prompt);
            
            if (line == null) {
                return null;
            }
            
            input.append(line);
            
            // 处理多行输入（反斜杠续行）
            while (line.trim().endsWith(LINE_CONTINUATION) && !line.trim().endsWith("\\\\")) {
                // 移除尾部空白和反斜杠
                while (input.length() > 0 && (input.charAt(input.length() - 1) == ' ' || input.charAt(input.length() - 1) == '\\')) {
                    input.setLength(input.length() - 1);
                }
                
                // 使用续行提示读取下一行
                line = lineReader.readLine("... ");
                
                if (line == null) {
                    // 多行输入期间的 EOF
                    break;
                }
                
                // 使用空格分隔符追加
                input.append(" ").append(line.trim());
            }
            
            String result = input.toString().trim();
            logger.debug("Read input: {}", result);
            return result;
            
        } catch (UserInterruptException e) {
            // 按下 Ctrl+C - 返回 null 表示中断
            logger.debug("User interrupted input (Ctrl+C)");
            return null;
            
        } catch (EndOfFileException e) {
            // 按下 Ctrl+D - 返回 null 表示 EOF/退出
            logger.debug("End of file reached (Ctrl+D)");
            return null;
        }
    }
    
    /**
     * Read input and parse it as a command.
     *
     * This is a convenience method that combines readInput and command parsing.
     *
     * @param sessionName the current session name for the prompt
     * @return the parsed command, or null if interrupted or EOF
     */
    public com.openbutler.cli.terminal.model.ParsedCommand readAndParseCommand(String sessionName) {
        String input = readInput(sessionName);
        
        if (input == null) {
            return null;
        }
        
        return commandParser.parse(input);
    }
    
    /**
     * Check if the input represents an exit signal.
     * 
     * @param input the input string to check
     * @return true if input is null (Ctrl+C or Ctrl+D), false otherwise
     */
    public boolean isExitSignal(String input) {
        return input == null;
    }
}
