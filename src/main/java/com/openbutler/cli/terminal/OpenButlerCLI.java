package com.openbutler.cli.terminal;

import com.openbutler.cli.terminal.command.Command;
import com.openbutler.cli.terminal.command.CommandRegistry;
import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.model.ParsedCommand;
import com.openbutler.cli.terminal.model.Session;
import com.openbutler.cli.terminal.rendering.BannerRenderer;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.cli.terminal.service.CommandParser;
import com.openbutler.cli.terminal.service.ConfigManager;
import com.openbutler.cli.terminal.service.GlobalExceptionHandler;
import com.openbutler.cli.terminal.service.InputHandler;
import com.openbutler.cli.terminal.service.SessionManager;
import com.openbutler.core.agent.AgentCore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * OpenButlerCLI 是 CLI 应用的主入口点。
 *
 * 本类实现 CommandLineRunner 在 Spring Boot 启动时启动 CLI 循环。它负责：
 * - 显示欢迎横幅
 * - 启动输入循环
 * - 解析和路由命令
 * - 处理优雅关闭（Ctrl+C, Ctrl+D）
 * - 退出时保存会话状态
 *
 * 验证要求：3.11, 10.1, 10.2, 10.8
 */
@Component
@Slf4j
public class OpenButlerCLI implements CommandLineRunner {
    
    private final BannerRenderer bannerRenderer;
    private final InputHandler inputHandler;
    private final CommandParser commandParser;
    private final CommandRegistry commandRegistry;
    private final SessionManager sessionManager;
    private final ConfigManager configManager;
    private final TerminalRenderer terminalRenderer;
    private final AgentCore agentCore;
    private final GlobalExceptionHandler exceptionHandler;
    
    private volatile boolean running = true;
    
    public OpenButlerCLI(
            BannerRenderer bannerRenderer,
            InputHandler inputHandler,
            CommandParser commandParser,
            CommandRegistry commandRegistry,
            SessionManager sessionManager,
            ConfigManager configManager,
            TerminalRenderer terminalRenderer,
            AgentCore agentCore,
            GlobalExceptionHandler exceptionHandler) {
        this.bannerRenderer = bannerRenderer;
        this.inputHandler = inputHandler;
        this.commandParser = commandParser;
        this.commandRegistry = commandRegistry;
        this.sessionManager = sessionManager;
        this.configManager = configManager;
        this.terminalRenderer = terminalRenderer;
        this.agentCore = agentCore;
        this.exceptionHandler = exceptionHandler;
    }
    
    /**
     * CLI 应用的主入口点。
     * 在应用上下文初始化后由 Spring Boot 调用。
     *
     * 验证要求：3.11, 10.1, 10.2, 9.8
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("Starting OpenButler CLI...");
        
        // 注册致命错误处理器
        registerFatalErrorHandler();
        
        // 注册关闭钩子以实现优雅退出
        registerShutdownHook();
        
        // 显示欢迎横幅
        bannerRenderer.renderBanner();
        
        // 启动输入循环
        startInputLoop();
        
        log.info("OpenButler CLI stopped");
    }
    
    /**
     * 启动主输入循环。
     * 持续读取用户输入、解析命令并执行。
     *
     * 验证要求：10.1, 10.2
     */
    private void startInputLoop() {
        while (running) {
            try {
                // 获取当前会话
                Session currentSession = sessionManager.getCurrentSession();
                
                // 读取用户输入
                String input = inputHandler.readInput(currentSession.getName());
                
                // 检查退出信号（Ctrl+C 或 Ctrl+D）
                if (input == null) {
                    log.debug("Exit signal received");
                    handleExit();
                    break;
                }
                
                // 跳过空输入
                if (input.trim().isEmpty()) {
                    continue;
                }
                
                // 解析命令
                ParsedCommand parsedCommand = commandParser.parse(input);
                
                // 执行命令
                executeCommand(parsedCommand, currentSession);
                
            } catch (Exception e) {
                log.error("Error in input loop", e);
                terminalRenderer.renderError("发生意外错误: " + e.getMessage());
            }
        }
    }
    
    /**
     * 执行解析后的命令。
     * 将命令路由到相应的处理器并执行。
     * 使用 GlobalExceptionHandler 捕获和处理所有异常。
     *
     * 验证要求：2.1-2.11, 9.1, 9.4, 9.6
     */
    private void executeCommand(ParsedCommand parsedCommand, Session currentSession) {
        String commandName = null;
        
        try {
            List<String> arguments;
            
            if (parsedCommand.isCommand()) {
                // 显式命令（以 / 开头）
                commandName = parsedCommand.getCommandName();
                arguments = parsedCommand.getPositionalArgs();
            } else {
                // 非命令输入默认为聊天
                commandName = "chat";
                arguments = List.of();
            }
            
            // 查找命令
            Command command = commandRegistry.getCommand(commandName);
            
            if (command == null) {
                // 命令未找到 - 提供相似命令建议
                terminalRenderer.renderError("未知命令: " + commandName);
                List<String> suggestions = commandRegistry.findSimilarCommands(commandName);
                if (!suggestions.isEmpty()) {
                    terminalRenderer.renderSystem("您是否想输入: " + String.join(", ", suggestions) + "?");
                }
                terminalRenderer.renderSystem("输入 '/help' 查看所有可用命令。");
                return;
            }
            
            // 检查命令是否需要会话
            if (command.requiresSession() && currentSession == null) {
                terminalRenderer.renderError("此命令需要活动会话");
                return;
            }
            
            // 构建命令上下文
            CommandContext context = CommandContext.builder()
                    .rawInput(parsedCommand.getRawInput())
                    .commandName(commandName)
                    .arguments(arguments)
                    .currentSession(currentSession)
                    .renderer(terminalRenderer)
                    .configManager(configManager)
                    .sessionManager(sessionManager)
                    .agentCore(agentCore)
                    .build();
            
            // 执行命令
            Mono<Void> execution = command.execute(context);
            
            // 阻塞等待完成（CLI 上下文中的同步执行）
            execution.block();
            
        } catch (Exception e) {
            // 使用全局异常处理器处理所有命令执行错误
            log.error("Error executing command: {}", commandName, e);
            exceptionHandler.handleCommandException(e, commandName != null ? commandName : "unknown");
        }
    }
    
    /**
     * 处理优雅退出。
     * 保存会话状态并停止输入循环。
     *
     * 验证要求：10.8
     */
    private void handleExit() {
        log.info("Handling graceful exit...");
        
        try {
            // 保存所有会话
            sessionManager.saveAllSessions();
            
            // 显示退出消息
            terminalRenderer.renderSystem("再见！您的会话已保存。");
            
        } catch (Exception e) {
            log.error("Error during exit", e);
            terminalRenderer.renderError("保存会话状态失败");
        } finally {
            running = false;
        }
    }
    
    /**
     * 注册致命错误处理器以处理未捕获的异常。
     * 确保即使发生致命错误也能保存会话状态。
     *
     * 验证要求：9.8
     */
    private void registerFatalErrorHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            log.error("Fatal error in thread {}", thread.getName(), throwable);
            
            // 使用异常处理器显示错误
            exceptionHandler.handleFatalError(throwable, thread.getName());
            
            try {
                // 退出前保存所有会话
                sessionManager.saveAllSessions();
                log.info("Session state saved after fatal error");
            } catch (Exception e) {
                log.error("Failed to save session state during fatal error", e);
                System.err.println("警告: 无法保存会话状态");
            }
            
            // 以错误代码退出
            System.exit(1);
        });
        
        log.info("Fatal error handler registered");
    }
    
    /**
     * 注册关闭钩子以处理 JVM 终止。
     * 确保即使应用程序意外终止也能保存会话状态。
     *
     * 验证要求：10.8
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook triggered");
            
            if (running) {
                try {
                    // 保存所有会话
                    sessionManager.saveAllSessions();
                    log.info("Session state saved during shutdown");
                } catch (Exception e) {
                    log.error("Failed to save session state during shutdown", e);
                }
            }
        }, "shutdown-hook"));
    }
    
    /**
     * Stops the CLI application.
     * Can be called by commands (e.g., ExitCommand) to trigger shutdown.
     */
    public void stop() {
        running = false;
    }
}
