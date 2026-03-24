package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.command.Command;
import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.model.Session;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.cli.terminal.service.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * NewCommand 创建具有可选自定义名称的新会话。
 *
 * 本命令允许用户创建新的对话会话。
 * 如果未提供名称，则使用默认名称。
 * 创建后，命令会自动切换到新会话。
 *
 * 验证要求：2.2, 6.8, 9.2
 */
@Component
@Slf4j
public class NewCommand implements Command {
    
    private static final String DEFAULT_SESSION_NAME = "会话";
    
    @Override
    public String getName() {
        return "new";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("n", "create");
    }
    
    @Override
    public String getDescription() {
        return "创建新的对话会话";
    }
    
    @Override
    public String getUsage() {
        return "new [会话名称] - 创建新会话（可选自定义名称）";
    }
    
    @Override
    public boolean requiresSession() {
        return false;
    }
    
    @Override
    public Mono<Void> execute(CommandContext context) {
        // 从参数中提取可选的会话名称
        String sessionName = extractSessionName(context);
        
        // 从上下文中获取服务
        SessionManager sessionManager = (SessionManager) context.getSessionManager();
        TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
        
        log.debug("Creating new session with name: {}", sessionName);
        
        try {
            // 创建新会话
            Session newSession = sessionManager.createSession(sessionName);
            
            // 切换到新会话
            sessionManager.switchSession(newSession.getId());
            
            // 显示成功消息
            renderer.renderSuccess(String.format(
                "已创建新会话: %s (ID: %s)",
                newSession.getName(),
                newSession.getId().substring(0, 8)
            ));
            
            log.info("Successfully created and switched to new session: {} ({})", 
                    newSession.getName(), newSession.getId());
            
        } catch (Exception e) {
            log.error("Failed to create new session", e);
            renderer.renderError("创建新会话失败: " + e.getMessage());
        }
        
        return Mono.empty();
    }
    
    /**
     * 从命令上下文中提取会话名称。
     * 如果未提供名称，则生成带时间戳的默认名称。
     *
     * @param context 命令上下文
     * @return 要使用的会话名称
     */
    private String extractSessionName(CommandContext context) {
        List<String> arguments = context.getArguments();
        
        if (arguments == null || arguments.isEmpty()) {
            // 使用时间戳生成默认名称
            return DEFAULT_SESSION_NAME + " " + System.currentTimeMillis();
        }
        
        // 将所有参数连接为单个会话名称
        return String.join(" ", arguments);
    }
}
