package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.command.Command;
import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * ClearCommand 清空终端屏幕。
 *
 * 本命令提供一种简单的清空终端显示的方法，
 * 提高可读性并减少长时间会话中的混乱。
 *
 * 验证要求：2.6
 */
@Component
@Slf4j
public class ClearCommand implements Command {
    
    @Override
    public String getName() {
        return "clear";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("cls", "cl");
    }
    
    @Override
    public String getDescription() {
        return "清空终端屏幕";
    }
    
    @Override
    public String getUsage() {
        return "clear - 清空终端屏幕";
    }
    
    @Override
    public boolean requiresSession() {
        return false;
    }
    
    @Override
    public Mono<Void> execute(CommandContext context) {
        // 从上下文中获取渲染器
        TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
        
        log.debug("Clearing terminal screen");
        
        try {
            // 使用渲染器清空屏幕
            renderer.clear();
            
            log.debug("Terminal screen cleared");
            
        } catch (Exception e) {
            log.error("Failed to clear screen", e);
            renderer.renderError("清空屏幕失败: " + e.getMessage());
        }
        
        return Mono.empty();
    }
}
