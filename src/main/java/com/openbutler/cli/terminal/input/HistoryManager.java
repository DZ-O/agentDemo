package com.openbutler.cli.terminal.input;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 历史记录管理器
 * 负责配置命令历史记录的保存位置和大小限制
 */
@Component
public class HistoryManager {

    private static final String HISTORY_FILE_NAME = ".openbutler_history";
    private static final int DEFAULT_HISTORY_SIZE = 1000;

    private final Path historyFile;
    private final int historySize;

    public HistoryManager() {
        // 历史记录文件保存在用户主目录下的.openbutler目录
        String userHome = System.getProperty("user.home");
        Path openButlerDir = Paths.get(userHome, ".openbutler");
        this.historyFile = openButlerDir.resolve(HISTORY_FILE_NAME);
        this.historySize = DEFAULT_HISTORY_SIZE;
    }

    /**
     * 获取历史记录文件路径
     * 
     * @return 历史记录文件路径
     */
    public Path getHistoryFile() {
        return historyFile;
    }

    /**
     * 获取历史记录大小限制
     * 
     * @return 历史记录条数
     */
    public int getHistorySize() {
        return historySize;
    }
}
