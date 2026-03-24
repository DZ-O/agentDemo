package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.command.Command;
import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.model.Session;
import com.openbutler.cli.terminal.rendering.TableFormatter;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.cli.terminal.service.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * ListCommand 以格式化表格显示所有会话。
 *
 * 本命令列出所有对话会话及其详细信息：
 * - 会话 ID（前 8 个字符）
 * - 会话名称
 * - 消息数量
 * - 最后活跃时间
 * - 活跃状态（当前会话用 "*" 标记）
 *
 * 该命令优雅地处理空会话列表，不需要活动会话即可执行。
 *
 * 验证要求：2.3, 3.8
 */
@Component
@Slf4j
public class ListCommand implements Command {
    
    private final TableFormatter tableFormatter;
    
    public ListCommand() {
        this.tableFormatter = new TableFormatter();
    }
    
    @Override
    public String getName() {
        return "list";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("ls", "l");
    }
    
    @Override
    public String getDescription() {
        return "列出所有对话会话";
    }
    
    @Override
    public String getUsage() {
        return "list - 以表格形式显示所有会话";
    }
    
    @Override
    public boolean requiresSession() {
        return false;
    }
    
    @Override
    public Mono<Void> execute(CommandContext context) {
        // 从上下文中获取服务
        SessionManager sessionManager = (SessionManager) context.getSessionManager();
        TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
        
        log.debug("Listing all sessions");
        
        try {
            // 获取所有会话
            List<Session> sessions = sessionManager.listSessions();
            
            // 处理空会话列表
            if (sessions == null || sessions.isEmpty()) {
                renderer.renderSystem("未找到会话。");
                return Mono.empty();
            }
            
            // 获取当前会话以标记它
            Session currentSession = sessionManager.getCurrentSession();
            String currentSessionId = currentSession != null ? currentSession.getId() : null;
            
            // 在列表中标记当前会话
            sessions.forEach(session -> {
                if (currentSessionId != null && session.getId().equals(currentSessionId)) {
                    session.setActive(true);
                } else {
                    session.setActive(false);
                }
            });
            
            // 格式化并显示表格
            String table = tableFormatter.formatSessionTable(sessions);
            System.out.println(table);
            
            // 显示摘要
            renderer.renderSystem(String.format(
                "总会话数: %d | 当前会话标记为 'Active' 状态",
                sessions.size()
            ));
            
            log.info("Listed {} sessions", sessions.size());
            
        } catch (Exception e) {
            log.error("Failed to list sessions", e);
            renderer.renderError("列出会话失败: " + e.getMessage());
        }
        
        return Mono.empty();
    }
}
