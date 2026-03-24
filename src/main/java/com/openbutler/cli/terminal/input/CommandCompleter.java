package com.openbutler.cli.terminal.input;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 命令自动补全器
 * 实现JLine Completer接口，提供命令名称自动补全功能
 */
@Component
public class CommandCompleter implements Completer {

    // 内置命令列表
    private static final List<String> BUILTIN_COMMANDS = List.of(
            "chat", "new", "list", "switch", "history", 
            "clear", "config", "help", "version", "exit"
    );

    // 补全缓存，提高性能
    private final Map<String, List<Candidate>> completionCache = new ConcurrentHashMap<>();

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String word = line.word();
        
        // 如果是命令前缀（以/开头），补全命令
        if (word.startsWith("/")) {
            String commandPrefix = word.substring(1);
            List<Candidate> cachedCandidates = completionCache.computeIfAbsent(
                    commandPrefix,
                    prefix -> BUILTIN_COMMANDS.stream()
                            .filter(cmd -> cmd.startsWith(prefix))
                            .map(cmd -> new Candidate(
                                    "/" + cmd,           // 补全值
                                    cmd,                 // 显示值
                                    null,                // 分组
                                    null,                // 描述
                                    null,                // 后缀
                                    null,                // 键
                                    true                 // 完整
                            ))
                            .collect(Collectors.toList())
            );
            candidates.addAll(cachedCandidates);
        }
    }

    /**
     * 获取所有可用命令
     * 
     * @return 命令列表
     */
    public List<String> getAllCommands() {
        return BUILTIN_COMMANDS;
    }

    /**
     * 清空补全缓存
     */
    public void clearCache() {
        completionCache.clear();
    }
}
