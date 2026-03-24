package com.openbutler.cli.terminal.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * CommandRegistry 服务，用于管理和查找 CLI 命令。
 *
 * 本服务：
 * - 通过 Spring 自动注入所有 Command bean
 * - 按名称和别名注册命令
 * - 提供按名称查找命令
 * - 使用 Levenshtein 距离实现相似命令建议
 *
 * 使用 ConcurrentHashMap 实现线程安全。
 *
 * 验证要求：2.1-2.11, 9.5
 */
@Service
public class CommandRegistry {
    
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    private final Map<String, Command> uniqueCommands = new ConcurrentHashMap<>();
    
    /**
     * 默认构造函数。
     */
    public CommandRegistry() {
    }
    
    /**
     * 注入所有 Command bean。
     * 按其名称和所有别名注册每个命令。
     *
     * @param commandBeans Spring 上下文中所有 Command bean 的列表
     */
    @Autowired
    public void setCommands(List<Command> commandBeans) {
        commandBeans.forEach(cmd -> {
            // 按命令名称注册
            commands.put(cmd.getName(), cmd);
            uniqueCommands.put(cmd.getName(), cmd);
            
            // 按所有别名注册
            cmd.getAliases().forEach(alias -> commands.put(alias, cmd));
        });
    }
    
    /**
     * 按名称或别名获取命令。
     *
     * @param name 命令名称或别名
     * @return Command 实例，如果未找到则返回 null
     */
    public Command getCommand(String name) {
        return commands.get(name);
    }
    
    /**
     * 获取所有已注册的命令名称（不包括别名）。
     *
     * @return 所有命令名称的列表
     */
    public List<String> getAllCommandNames() {
        return new ArrayList<>(uniqueCommands.keySet());
    }
    
    /**
     * 基于编辑距离（Levenshtein 距离）查找相似命令。
     * 返回按相似度排序的命令（最相似的在前）。
     *
     * @param input 输入的命令名称
     * @return 相似命令名称列表（最多 3 个建议）
     */
    public List<String> findSimilarCommands(String input) {
        if (input == null || input.isEmpty()) {
            return Collections.emptyList();
        }
        
        return uniqueCommands.keySet().stream()
                .map(cmdName -> new CommandDistance(cmdName, levenshteinDistance(input, cmdName)))
                .filter(cd -> cd.distance <= 3) // 仅在距离 <= 3 时提供建议
                .sorted(Comparator.comparingInt(cd -> cd.distance))
                .limit(3)
                .map(cd -> cd.commandName)
                .collect(Collectors.toList());
    }
    
    /**
     * 计算两个字符串之间的 Levenshtein 距离。
     * 这是将一个字符串更改 为另一个字符串所需的最少单字符编辑次数（插入、删除或替换）。
     *
     * @param s1 第一个字符串
     * @param s2 第二个字符串
     * @return Levenshtein 距离
     */
    private int levenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return Integer.MAX_VALUE;
        }
        
        String str1 = s1.toLowerCase();
        String str2 = s2.toLowerCase();
        
        int len1 = str1.length();
        int len2 = str2.length();
        
        // 创建二维数组以存储距离
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        // 初始化基本情况
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        // 填充 dp 表
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j],      // 删除
                            Math.min(
                                    dp[i][j - 1],      // 插入
                                    dp[i - 1][j - 1]   // 替换
                            )
                    );
                }
            }
        }
        
        return dp[len1][len2];
    }
    
    /**
     * 辅助类，用于存储命令名称及其与输入的距离。
     */
    private static class CommandDistance {
        final String commandName;
        final int distance;
        
        CommandDistance(String commandName, int distance) {
            this.commandName = commandName;
            this.distance = distance;
        }
    }
}
