package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.command.Command;
import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.core.agent.AgentCore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * ChatCommand 处理聊天消息并流式传输 AI 响应。
 *
 * 当用户输入不以命令前缀开头时，这是默认命令。
 * 它通过 AgentCore 处理用户消息并将 AI 响应流式传输到终端。
 *
 * 验证要求：2.1, 2.11, 8.4, 10.3, 10.4
 */
@Component
@Slf4j
public class ChatCommand implements Command {
    
    @Override
    public String getName() {
        return "chat";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("c", "talk");
    }
    
    @Override
    public String getDescription() {
        return "向AI助手发送消息";
    }
    
    @Override
    public String getUsage() {
        return "chat <消息内容> 或直接输入消息（无需命令前缀）";
    }
    
    @Override
    public boolean requiresSession() {
        return true;
    }
    
    @Override
    public Mono<Void> execute(CommandContext context) {
        // 从参数中提取消息
        String message = extractMessage(context);
        
        if (message == null || message.trim().isEmpty()) {
            TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
            renderer.renderError("消息内容不能为空");
            return Mono.empty();
        }
        
        // 从上下文中获取服务
        AgentCore agentCore = context.getAgentCore();
        TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
        String sessionId = context.getCurrentSession().getId();
        
        log.debug("Processing chat message for session {}: {}", sessionId, message);
        
        // 调用 AgentCore.process() 并流式传输响应
        return agentCore.process(sessionId, message)
                .doOnNext(chunk -> {
                    // 将每个数据块流式传输到终端
                    renderer.renderAIResponse(chunk);
                })
                .doOnComplete(() -> {
                    // 响应完成后添加换行符
                    System.out.println();
                    log.debug("Chat response completed for session {}", sessionId);
                })
                .onErrorResume(error -> {
                    // 处理错误并显示错误消息
                    log.error("Error processing chat message", error);
                    renderer.renderError("获取AI响应失败: " + error.getMessage());
                    return Flux.empty();
                })
                .then();
    }
    
    /**
     * 从命令上下文中提取消息。
     * 处理显式聊天命令和默认（非命令）输入。
     *
     * @param context 命令上下文
     * @return 提取的消息，如果没有消息则返回 null
     */
    private String extractMessage(CommandContext context) {
        List<String> arguments = context.getArguments();
        
        if (arguments == null || arguments.isEmpty()) {
            // 如果没有参数，检查 rawInput 是否为非命令消息
            String rawInput = context.getRawInput();
            if (rawInput != null && !rawInput.trim().isEmpty() && !rawInput.trim().startsWith("/")) {
                // 这是一条非命令消息（默认聊天）
                return rawInput.trim();
            }
            // 未提供消息
            return null;
        }
        
        // 将所有参数连接为单个消息
        return String.join(" ", arguments);
    }
}
