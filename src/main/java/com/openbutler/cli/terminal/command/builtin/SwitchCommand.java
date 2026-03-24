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
 * SwitchCommand 通过 ID 或名称切换到指定的会话。
 *
 * 本命令允许用户在对话会话之间切换。
 * 它接受会话 ID（完整或部分）或会话名称作为参数。
 * 切换后，会话历史会自动加载。
 *
 * 验证要求：2.4, 6.6, 6.9, 9.2
 */
@Component
@Slf4j
public class SwitchCommand implements Command {
    
    @Override
    public String getName() {
        return "switch";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("sw", "s");
    }
    
    @Override
    public String getDescription() {
        return "切换到指定会话";
    }
    
    @Override
    public String getUsage() {
        return "switch <会话ID|会话名称> - 切换到指定的会话";
    }
    
    @Override
    public boolean requiresSession() {
        return false;
    }
    
    @Override
    public Mono<Void> execute(CommandContext context) {
        // 从参数中提取会话标识符
        String sessionIdentifier = extractSessionIdentifier(context);
        
        if (sessionIdentifier == null || sessionIdentifier.trim().isEmpty()) {
            TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
            renderer.renderError("需要提供会话ID或名称");
            renderer.renderSystem("用法: " + getUsage());
            return Mono.empty();
        }
        
        // 从上下文中获取服务
        SessionManager sessionManager = (SessionManager) context.getSessionManager();
        TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
        
        log.debug("Switching to session: {}", sessionIdentifier);
        
        try {
            // 按 ID 或名称查找会话
            Session targetSession = findSession(sessionManager, sessionIdentifier);
            
            if (targetSession == null) {
                renderer.renderError("未找到会话: " + sessionIdentifier);
                renderer.renderSystem("使用 'list' 命令查看所有可用会话");
                return Mono.empty();
            }
            
            // 检查是否已在该会话中
            Session currentSession = sessionManager.getCurrentSession();
            if (currentSession != null && currentSession.getId().equals(targetSession.getId())) {
                renderer.renderWarning("已在会话中: " + targetSession.getName());
                return Mono.empty();
            }
            
            // 切换到目标会话
            sessionManager.switchSession(targetSession.getId());
            
            // 显示成功消息
            renderer.renderSuccess(String.format(
                "已切换到会话: %s (ID: %s)",
                targetSession.getName(),
                targetSession.getId().substring(0, 8)
            ));
            
            // 显示消息数量
            renderer.renderSystem(String.format(
                "会话包含 %d 条消息",
                targetSession.getMessageCount()
            ));
            
            log.info("Successfully switched to session: {} ({})", 
                    targetSession.getName(), targetSession.getId());
            
        } catch (IllegalArgumentException e) {
            log.error("Failed to switch session", e);
            renderer.renderError("切换会话失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error switching session", e);
            renderer.renderError("发生意外错误: " + e.getMessage());
        }
        
        return Mono.empty();
    }
    
    /**
     * 从命令上下文中提取会话标识符。
     *
     * @param context 命令上下文
     * @return 会话标识符（ID 或名称），如果未提供则返回 null
     */
    private String extractSessionIdentifier(CommandContext context) {
        List<String> arguments = context.getArguments();
        
        if (arguments == null || arguments.isEmpty()) {
            return null;
        }
        
        // 将所有参数连接为单个标识符（支持带空格的名称）
        return String.join(" ", arguments);
    }
    
    /**
     * 通过 ID（完整或部分）或名称查找会话。
     *
     * @param sessionManager 会话管理器
     * @param identifier 会话标识符（ID 或名称）
     * @return 匹配的会话，如果未找到则返回 null
     */
    private Session findSession(SessionManager sessionManager, String identifier) {
        List<Session> allSessions = sessionManager.listSessions();
        
        // 首先尝试精确 ID 匹配
        for (Session session : allSessions) {
            if (session.getId().equals(identifier)) {
                return session;
            }
        }
        
        // 尝试部分 ID 匹配（前缀）
        for (Session session : allSessions) {
            if (session.getId().startsWith(identifier)) {
                return session;
            }
        }
        
        // 尝试精确名称匹配
        for (Session session : allSessions) {
            if (session.getName().equals(identifier)) {
                return session;
            }
        }
        
        // 尝试不区分大小写的名称匹配
        for (Session session : allSessions) {
            if (session.getName().equalsIgnoreCase(identifier)) {
                return session;
            }
        }
        
        return null;
    }
}
