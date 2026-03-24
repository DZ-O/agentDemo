package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.command.Command;
import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.cli.terminal.service.SessionManager;
import com.openbutler.core.memory.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * HistoryCommand 显示当前会话的消息历史。
 *
 * 本命令显示当前会话的对话历史，
 * 用颜色编码区分用户和 AI 消息。
 * 支持可选的 limit 参数来只显示最近的消息。
 *
 * 验证要求：2.5
 */
@Component
@Slf4j
public class HistoryCommand implements Command {
    
    private static final int DEFAULT_LIMIT = 50;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Override
    public String getName() {
        return "history";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("h", "hist");
    }
    
    @Override
    public String getDescription() {
        return "显示当前会话历史";
    }
    
    @Override
    public String getUsage() {
        return "history [数量] - 显示消息历史（默认: 50条消息）";
    }
    
    @Override
    public boolean requiresSession() {
        return true;
    }
    
    @Override
    public Mono<Void> execute(CommandContext context) {
        // 从参数中提取可选的 limit
        int limit = extractLimit(context);
        
        // 从上下文中获取服务
        SessionManager sessionManager = (SessionManager) context.getSessionManager();
        TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
        
        String sessionId = context.getCurrentSession().getId();
        String sessionName = context.getCurrentSession().getName();
        
        log.debug("Displaying history for session: {} (limit: {})", sessionId, limit);
        
        try {
            // 获取会话历史
            List<Message> messages = sessionManager.getSessionHistory(sessionId);
            
            // 处理空历史记录
            if (messages == null || messages.isEmpty()) {
                renderer.renderSystem("此会话中还没有消息。");
                return Mono.empty();
            }
            
            // 应用限制
            int startIndex = Math.max(0, messages.size() - limit);
            List<Message> displayMessages = messages.subList(startIndex, messages.size());
            
            // 显示标题
            System.out.println();
            renderer.renderSystem(String.format(
                "=== 会话历史: %s (显示 %d / %d 条消息) ===",
                sessionName,
                displayMessages.size(),
                messages.size()
            ));
            System.out.println();
            
            // 显示带颜色编码的消息
            for (Message message : displayMessages) {
                displayMessage(renderer, message);
            }
            
            System.out.println();
            renderer.renderSystem("=== 历史记录结束 ===");
            
            log.info("Displayed {} messages from session {}", displayMessages.size(), sessionId);
            
        } catch (Exception e) {
            log.error("Failed to display history", e);
            renderer.renderError("显示历史记录失败: " + e.getMessage());
        }
        
        return Mono.empty();
    }
    
    /**
     * 从命令上下文中提取 limit 参数。
     *
     * @param context 命令上下文
     * @return limit 值，如果未指定或无效则返回 DEFAULT_LIMIT
     */
    private int extractLimit(CommandContext context) {
        List<String> arguments = context.getArguments();
        
        if (arguments == null || arguments.isEmpty()) {
            return DEFAULT_LIMIT;
        }
        
        try {
            int limit = Integer.parseInt(arguments.get(0));
            if (limit <= 0) {
                return DEFAULT_LIMIT;
            }
            return Math.min(limit, 1000); // 上限为 1000 条消息
        } catch (NumberFormatException e) {
            log.warn("Invalid limit parameter: {}, using default", arguments.get(0));
            return DEFAULT_LIMIT;
        }
    }
    
    /**
     * 显示带适当颜色编码的单个消息。
     *
     * @param renderer 终端渲染器
     * @param message 要显示的消息
     */
    private void displayMessage(TerminalRenderer renderer, Message message) {
        String role = message.getRole();
        String content = message.getContent();
        String timestamp = message.getTimestamp() != null 
            ? message.getTimestamp().format(TIME_FORMATTER) 
            : "";
        
        // 格式: [HH:mm:ss] Role: Content
        String prefix = timestamp.isEmpty() ? "" : "[" + timestamp + "] ";
        
        if ("user".equalsIgnoreCase(role)) {
            // 用户消息为蓝色
            System.out.print("\u001B[34m"); // 蓝色
            System.out.print(prefix + "你: ");
            System.out.print("\u001B[0m"); // 重置
            System.out.println(content);
        } else if ("assistant".equalsIgnoreCase(role) || "ai".equalsIgnoreCase(role)) {
            // AI 消息为绿色
            System.out.print("\u001B[32m"); // 绿色
            System.out.print(prefix + "AI: ");
            System.out.print("\u001B[0m"); // 重置
            System.out.println(content);
        } else {
            // 系统消息为灰色
            System.out.print("\u001B[90m"); // 灰色
            System.out.print(prefix + role + ": ");
            System.out.print("\u001B[0m"); // Reset
            System.out.println(content);
        }
    }
}
