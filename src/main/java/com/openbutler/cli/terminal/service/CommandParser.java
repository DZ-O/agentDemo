package com.openbutler.cli.terminal.service;

import com.openbutler.cli.terminal.model.ParsedCommand;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CommandParser service for parsing user input into structured commands.
 *
 * This service handles:
 * - Detecting if input is a command (starts with /)
 * - Extracting command name and arguments
 * - Parsing positional arguments
 * - Parsing named arguments (--key=value)
 * - Parsing flag arguments (--flag)
 *
 * Validates: Requirements 2.1-2.11
 */
@Service
public class CommandParser {
    
    private static final String COMMAND_PREFIX = "/";
    private static final Pattern NAMED_ARG_PATTERN = Pattern.compile("--([a-zA-Z0-9_-]+)=(.*)");
    private static final Pattern FLAG_PATTERN = Pattern.compile("--([a-zA-Z0-9_-]+)");
    
    /**
     * Parse user input into a ParsedCommand object.
     *
     * @param input the raw user input string
     * @return ParsedCommand containing parsed command information
     */
    public ParsedCommand parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return ParsedCommand.builder()
                    .isCommand(false)
                    .rawInput(input)
                    .build();
        }
        
        String trimmedInput = input.trim();
        
        if (!isCommand(trimmedInput)) {
            // 不是命令，作为聊天消息处理
            return ParsedCommand.builder()
                    .isCommand(false)
                    .rawInput(input)
                    .build();
        }
        
        // 提取命令名称
        String commandName = extractCommandName(trimmedInput);
        
        // 提取参数
        String argsString = trimmedInput.substring(commandName.length() + 1).trim();
        
        // 将参数解析为位置参数、命名参数和标志
        List<String> positionalArgs = new ArrayList<>();
        Map<String, String> namedArgs = new HashMap<>();
        Set<String> flags = new HashSet<>();
        
        if (!argsString.isEmpty()) {
            parseArguments(argsString, positionalArgs, namedArgs, flags);
        }
        
        return ParsedCommand.builder()
                .isCommand(true)
                .commandName(commandName)
                .positionalArgs(positionalArgs)
                .namedArgs(namedArgs)
                .flags(flags)
                .rawInput(input)
                .build();
    }
    
    /**
     * Check if the input is a command (starts with /).
     *
     * @param input the user input string
     * @return true if input starts with command prefix, false otherwise
     */
    public boolean isCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return input.trim().startsWith(COMMAND_PREFIX);
    }
    
    /**
     * Extract the command name from the input.
     *
     * @param input the user input string (must be a command)
     * @return the command name without the prefix
     */
    public String extractCommandName(String input) {
        if (input == null || !isCommand(input)) {
            return null;
        }
        
        String withoutPrefix = input.trim().substring(COMMAND_PREFIX.length());
        int spaceIndex = withoutPrefix.indexOf(' ');
        
        if (spaceIndex == -1) {
            return withoutPrefix;
        }
        
        return withoutPrefix.substring(0, spaceIndex);
    }
    
    /**
     * Extract all arguments from the input.
     *
     * @param input the user input string (must be a command)
     * @return list of all arguments (positional, named, and flags combined)
     */
    public List<String> extractArguments(String input) {
        if (input == null || !isCommand(input)) {
            return Collections.emptyList();
        }
        
        String commandName = extractCommandName(input);
        String argsString = input.trim().substring(commandName.length() + 1).trim();
        
        if (argsString.isEmpty()) {
            return Collections.emptyList();
        }
        
        return tokenizeArguments(argsString);
    }
    
    /**
     * Parse arguments string into positional args, named args, and flags.
     *
     * @param argsString the arguments portion of the command
     * @param positionalArgs output list for positional arguments
     * @param namedArgs output map for named arguments
     * @param flags output set for flag arguments
     */
    private void parseArguments(String argsString, List<String> positionalArgs, 
                                Map<String, String> namedArgs, Set<String> flags) {
        List<String> tokens = tokenizeArguments(argsString);
        
        for (String token : tokens) {
            if (token.startsWith("--")) {
                // 检查是否是命名参数（--key=value）
                Matcher namedMatcher = NAMED_ARG_PATTERN.matcher(token);
                if (namedMatcher.matches()) {
                    String key = namedMatcher.group(1);
                    String value = namedMatcher.group(2);
                    namedArgs.put(key, value);
                } else {
                    // 这是一个标志（--flag）
                    Matcher flagMatcher = FLAG_PATTERN.matcher(token);
                    if (flagMatcher.matches()) {
                        String flag = flagMatcher.group(1);
                        flags.add(flag);
                    }
                }
            } else {
                // 位置参数
                positionalArgs.add(token);
            }
        }
    }
    
    /**
     * Tokenize arguments string, respecting quoted strings.
     *
     * @param argsString the arguments string to tokenize
     * @return list of tokens
     */
    private List<String> tokenizeArguments(String argsString) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = '\0';
        
        for (int i = 0; i < argsString.length(); i++) {
            char c = argsString.charAt(i);
            
            if (c == '"' || c == '\'') {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == quoteChar) {
                    inQuotes = false;
                    quoteChar = '\0';
                } else {
                    currentToken.append(c);
                }
            } else if (c == ' ' && !inQuotes) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken = new StringBuilder();
                }
            } else {
                currentToken.append(c);
            }
        }
        
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }
        
        return tokens;
    }
}
