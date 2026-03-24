package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.command.Command;
import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * VersionCommand 显示版本和系统信息。
 *
 * 本命令显示：
 * - 应用程序版本
 * - Java 版本和供应商
 * - 操作系统信息
 * - 系统架构
 *
 * 验证要求：2.9
 */
@Component
@Slf4j
public class VersionCommand implements Command {
    
    private static final String APP_VERSION = "1.0.0";
    private static final String APP_NAME = "OpenButler CLI";
    
    @Override
    public String getName() {
        return "version";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("v", "ver");
    }
    
    @Override
    public String getDescription() {
        return "显示版本和系统信息";
    }
    
    @Override
    public String getUsage() {
        return "version - 显示应用版本和系统信息";
    }
    
    @Override
    public boolean requiresSession() {
        return false;
    }
    
    @Override
    public Mono<Void> execute(CommandContext context) {
        TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
        
        log.debug("Displaying version information");
        
        try {
            System.out.println();
            renderer.renderSystem("=== " + APP_NAME + " ===");
            System.out.println();
            
            // 应用程序版本
            System.out.println("版本: " + APP_VERSION);
            System.out.println();
            
            // Java 信息
            String javaVersion = System.getProperty("java.version");
            String javaVendor = System.getProperty("java.vendor");
            String javaHome = System.getProperty("java.home");
            
            System.out.println("Java 版本: " + javaVersion);
            System.out.println("Java 供应商: " + javaVendor);
            System.out.println("Java 主目录: " + javaHome);
            System.out.println();
            
            // 操作系统信息
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String osArch = System.getProperty("os.arch");
            
            System.out.println("操作系统: " + osName);
            System.out.println("系统版本: " + osVersion);
            System.out.println("系统架构: " + osArch);
            System.out.println();
            
            // 运行时信息
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory() / (1024 * 1024);
            long totalMemory = runtime.totalMemory() / (1024 * 1024);
            long freeMemory = runtime.freeMemory() / (1024 * 1024);
            
            System.out.println("最大内存: " + maxMemory + " MB");
            System.out.println("总内存: " + totalMemory + " MB");
            System.out.println("可用内存: " + freeMemory + " MB");
            System.out.println();
            
        } catch (Exception e) {
            log.error("Failed to display version information", e);
            renderer.renderError("显示版本信息失败: " + e.getMessage());
        }
        
        return Mono.empty();
    }
}
