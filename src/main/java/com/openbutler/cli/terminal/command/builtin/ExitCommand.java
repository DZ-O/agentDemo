package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.command.Command;
import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.cli.terminal.service.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * ExitCommand 保存当前会话并优雅地退出应用程序。
 *
 * 本命令确保在关闭前持久化所有会话数据。
 * 它显示告别消息并以退出代码 0 终止应用程序。
 *
 * 验证要求：2.10, 9.8
 */
@Component
@Slf4j
public class ExitCommand implements Command {
    
    private final ConfigurableApplicationContext applicationContext;
    
    public ExitCommand(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public String getName() {
        return "exit";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("quit", "q", "bye");
    }
    
    @Override
    public String getDescription() {
        return "保存会话并退出应用";
    }
    
    @Override
    public String getUsage() {
        return "exit - 保存当前会话并优雅退出";
    }
    
    @Override
    public boolean requiresSession() {
        return false;
    }
    
    @Override
    public Mono<Void> execute(CommandContext context) {
        SessionManager sessionManager = (SessionManager) context.getSessionManager();
        TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
        
        log.info("Exit command received, shutting down gracefully");
        
        try {
            // 保存所有会话
            renderer.renderSystem("正在保存会话...");
            sessionManager.saveAllSessions();
            
            // 关闭会话管理器
            sessionManager.shutdown();
            
            // 显示告别消息
            System.out.println();
            renderer.renderSuccess("所有会话已成功保存");
            renderer.renderSystem("感谢使用 OpenButler CLI。再见！");
            System.out.println();
            
            log.info("Graceful shutdown completed");
            
            // 关闭 Spring 应用上下文以触发关闭
            // 这将使应用程序优雅地退出
            applicationContext.close();
            
        } catch (Exception e) {
            log.error("Error during shutdown", e);
            renderer.renderError("关闭时出错: " + e.getMessage());
            renderer.renderWarning("部分数据可能未保存");
        }
        
        return Mono.empty();
    }
}
