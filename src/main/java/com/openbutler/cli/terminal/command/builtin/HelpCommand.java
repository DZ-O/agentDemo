package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.command.Command;
import com.openbutler.cli.terminal.command.CommandRegistry;
import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * HelpCommand 显示所有命令的帮助信息。
 *
 * 本命令列出所有可用命令及其描述和用法。
 * 支持通过传递命令名称来查看特定命令的详细帮助。
 *
 * 验证要求：2.8
 */
@Component
@Slf4j
public class HelpCommand implements Command {
    
    private final ObjectProvider<CommandRegistry> commandRegistryProvider;
    
    public HelpCommand(ObjectProvider<CommandRegistry> commandRegistryProvider) {
        this.commandRegistryProvider = commandRegistryProvider;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public List<String> getAliases() {
        return List.of("h", "?");
    }

    @Override
    public String getDescription() {
        return "显示帮助信息";
    }

    @Override
    public String getUsage() {
        return "help [命令名] - 显示所有命令或指定命令的帮助信息";
    }

    @Override
    public boolean requiresSession() {
        return false;
    }

    @Override
    public Mono<Void> execute(CommandContext context) {
        List<String> arguments = context.getArguments();
        TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();

        // 检查是否请求特定命令的帮助
        if (arguments != null && !arguments.isEmpty()) {
            String commandName = arguments.get(0);
            return displayCommandHelp(commandName, renderer);
        }

        // 显示所有命令的帮助
        return displayAllHelp(renderer);
    }

    /**
     * 显示所有可用命令的帮助。
     */
    private Mono<Void> displayAllHelp(TerminalRenderer renderer) {
        log.debug("Displaying help for all commands");

        try {
            List<String> commandNames = commandRegistryProvider.getObject().getAllCommandNames();
            
            System.out.println();
            renderer.renderSystem("=== OpenButler CLI - 可用命令 ===");
            System.out.println();
            
            // 显示每个命令
            for (String commandName : commandNames) {
                Command command = commandRegistryProvider.getObject().getCommand(commandName);
                if (command != null) {
                    displayCommandSummary(command);
                }
            }

            System.out.println();
            renderer.renderSystem("提示: 输入 'help <命令名>' 查看特定命令的详细帮助");
            renderer.renderSystem("提示: 直接输入消息（无需命令前缀）即可与AI对话");

        } catch (Exception e) {
            log.error("Failed to display help", e);
            renderer.renderError("显示帮助信息失败: " + e.getMessage());
        }

        return Mono.empty();
    }

    /**
     * Displays help for a specific command.
     */
    private Mono<Void> displayCommandHelp(String commandName, TerminalRenderer renderer) {
        log.debug("Displaying help for command: {}", commandName);
        
        try {
            Command command = commandRegistryProvider.getObject().getCommand(commandName);

            if (command == null) {
                renderer.renderError("未知命令: " + commandName);
                renderer.renderSystem("输入 'help' 查看所有可用命令");
                return Mono.empty();
            }

            System.out.println();
            renderer.renderSystem("=== 帮助: " + command.getName() + " ===");
            System.out.println();

            System.out.println("描述: " + command.getDescription());
            System.out.println();
            System.out.println("用法:");
            System.out.println("  " + command.getUsage());

            if (!command.getAliases().isEmpty()) {
                System.out.println();
                System.out.println("别名: " + String.join(", ", command.getAliases()));
            }

            System.out.println();

        } catch (Exception e) {
            log.error("Failed to display command help", e);
            renderer.renderError("显示命令帮助失败: " + e.getMessage());
        }

        return Mono.empty();
    }

    /**
     * Displays a summary line for a command.
     */
    private void displayCommandSummary(Command command) {
        String name = command.getName();
        String aliases = command.getAliases().isEmpty()
            ? ""
            : " (" + String.join(", ", command.getAliases()) + ")";
        String description = command.getDescription();

        // 格式: /command (alias1, alias2) - Description
        System.out.printf("  \u001B[36m/%s\u001B[0m%s - %s%n", name, aliases, description);
    }
}
