package com.openbutler.cli.terminal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
*ParsedCommand模型表示已解析的用户命令及其参数。
*
*此类封装了解析用户输入的结果，区分了命令（以/开头）和常规聊天消息。它支持三种类型
*参数：
*-位置参数：有序值（例如，“arg1 arg2”）
*-命名参数：键值对（例如，--key=value）
*-标志参数：布尔标志（例如，--verbose）
*
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedCommand {

    /**
     * 是否是命令（vs 普通聊天消息）
     * True if the input starts with command prefix (/), false for chat messages
     */
    private boolean isCommand;

    /**
     * 命令名称
     * The command name (e.g., "chat", "new", "list")
     * Null for non-command inputs
     */
    private String commandName;

    /**
     * 位置参数
     * Ordered positional arguments passed to the command
     * Example: "/new session1 session2" -> ["session1", "session2"]
     */
    @Builder.Default
    private List<String> positionalArgs = new ArrayList<>();

    /**
     * 命名参数（--key=value）
     * Named arguments in key-value format
     * Example: "/config --theme=dark --size=large" -> {theme: "dark", size: "large"}
     */
    @Builder.Default
    private Map<String, String> namedArgs = new HashMap<>();

    /**
     * 标志参数（--flag）
     * Boolean flag arguments without values
     * Example: "/list --verbose --all" -> ["verbose", "all"]
     */
    @Builder.Default
    private Set<String> flags = new HashSet<>();

    /**
     * 原始输入
     * The original raw input string as entered by the user
     */
    private String rawInput;
}
