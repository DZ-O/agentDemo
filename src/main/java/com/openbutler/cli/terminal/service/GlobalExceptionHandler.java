package com.openbutler.cli.terminal.service;

import com.openbutler.cli.terminal.command.CommandRegistry;
import com.openbutler.cli.terminal.model.CLIConfig;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Global exception handler for CLI command execution.
 * 
 * This service provides centralized error handling for all command execution errors,
 * displaying user-friendly error messages and logging detailed error information.
 * 
 * Validates: Requirements 9.1, 9.4, 9.5, 9.6, 9.7
 */
@Slf4j
@Service
public class GlobalExceptionHandler {
    
    private final TerminalRenderer renderer;
    private final CommandRegistry commandRegistry;
    private final ConfigManager configManager;
    
    public GlobalExceptionHandler(
            TerminalRenderer renderer,
            CommandRegistry commandRegistry,
            ConfigManager configManager) {
        this.renderer = renderer;
        this.commandRegistry = commandRegistry;
        this.configManager = configManager;
    }
    
    /**
     * Handle command execution exception.
     * Displays user-friendly error message and logs detailed error information.
     * 
     * Validates: Requirements 9.1, 9.4, 9.6, 9.7
     * 
     * @param throwable the exception that occurred
     * @param commandName the name of the command that failed
     */
    public void handleCommandException(Throwable throwable, String commandName) {
        // 记录详细错误信息
        log.error("Command execution failed: {}", commandName, throwable);
        
        // 确定错误类别并显示相应消息
        if (throwable instanceof IllegalArgumentException) {
            handleUserInputError(throwable, commandName);
        } else if (throwable instanceof IOException || throwable instanceof ConnectException) {
            handleNetworkError(throwable);
        } else if (throwable instanceof TimeoutException || throwable instanceof SocketTimeoutException) {
            handleTimeoutError(throwable);
        } else {
            handleSystemError(throwable);
        }
        
        // 在调试模式下显示堆栈跟踪
        CLIConfig config = configManager.loadConfig();
        if (config.isDebugMode()) {
            displayStackTrace(throwable);
        }
    }
    
    /**
     * Handle user input errors (invalid commands, wrong parameters).
     * Provides suggestions for similar valid commands.
     * 
     * Validates: Requirements 9.1, 9.5
     * 
     * @param throwable the exception that occurred
     * @param commandName the name of the command that failed
     */
    private void handleUserInputError(Throwable throwable, String commandName) {
        renderer.renderError(throwable.getMessage());
        
        // 如果命令未找到，提供相似命令建议
        if (throwable.getMessage().contains("not found") || throwable.getMessage().contains("Unknown")) {
            List<String> suggestions = commandRegistry.findSimilarCommands(commandName);
            if (!suggestions.isEmpty()) {
                renderer.renderSystem("Did you mean: " + String.join(", ", suggestions) + "?");
            }
            renderer.renderSystem("Type '/help' to see all available commands.");
        }
    }
    
    /**
     * Handle network errors (API failures, connection issues).
     * Provides specific error reason and actionable suggestions.
     * 
     * Validates: Requirement 9.4
     * 
     * @param throwable the exception that occurred
     */
    private void handleNetworkError(Throwable throwable) {
        renderer.renderError("Network request failed: " + throwable.getMessage());
        
        if (throwable instanceof ConnectException) {
            renderer.renderSystem("Could not connect to the service. Please check:");
            renderer.renderSystem("  - Your internet connection");
            renderer.renderSystem("  - Service availability");
            renderer.renderSystem("  - Firewall settings");
        } else if (throwable instanceof IOException) {
            renderer.renderSystem("An I/O error occurred. Please try again.");
        }
    }
    
    /**
     * Handle timeout errors.
     * Provides specific timeout information and retry suggestions.
     * 
     * Validates: Requirement 9.4
     * 
     * @param throwable the exception that occurred
     */
    private void handleTimeoutError(Throwable throwable) {
        renderer.renderError("Request timed out: " + throwable.getMessage());
        renderer.renderSystem("The operation took too long to complete. Please:");
        renderer.renderSystem("  - Try again with a simpler request");
        renderer.renderSystem("  - Check your network connection");
        renderer.renderSystem("  - Wait a moment and retry");
    }
    
    /**
     * Handle system errors (file IO failures, unexpected errors).
     * Logs detailed information and provides generic error message.
     * 
     * Validates: Requirements 9.1, 9.6
     * 
     * @param throwable the exception that occurred
     */
    private void handleSystemError(Throwable throwable) {
        renderer.renderError("An unexpected error occurred: " + throwable.getMessage());
        renderer.renderSystem("This error has been logged. If the problem persists, please:");
        renderer.renderSystem("  - Check the log file at ~/.openbutler/logs/application.log");
        renderer.renderSystem("  - Report the issue with the error details");
    }
    
    /**
     * Display stack trace in debug mode.
     * 
     * Validates: Requirement 9.7
     * 
     * @param throwable the exception to display
     */
    private void displayStackTrace(Throwable throwable) {
        renderer.renderSystem("\n--- Stack Trace (Debug Mode) ---");
        
        // 显示异常类和消息
        renderer.renderSystem(throwable.getClass().getName() + ": " + throwable.getMessage());
        
        // 显示堆栈跟踪元素
        for (StackTraceElement element : throwable.getStackTrace()) {
            renderer.renderSystem("  at " + element.toString());
        }
        
        // 如果存在原因，显示原因
        if (throwable.getCause() != null) {
            renderer.renderSystem("\nCaused by:");
            displayStackTrace(throwable.getCause());
        }
        
        renderer.renderSystem("--- End Stack Trace ---\n");
    }
    
    /**
     * Handle fatal error with minimal processing.
     * Used by UncaughtExceptionHandler to avoid recursive errors.
     * 
     * Validates: Requirement 9.6
     * 
     * @param throwable the fatal exception
     * @param threadName the name of the thread where error occurred
     */
    public void handleFatalError(Throwable throwable, String threadName) {
        // 记录致命错误
        log.error("Fatal error in thread {}", threadName, throwable);
        
        // 显示简单错误消息（避免可能失败的复杂渲染）
        System.err.println("\n[FATAL ERROR] A critical error occurred in thread: " + threadName);
        System.err.println("Error: " + throwable.getMessage());
        System.err.println("Your session will be saved and the application will exit.");
        
        // 如果可用，显示堆栈跟踪
        CLIConfig config = null;
        try {
            config = configManager.loadConfig();
        } catch (Exception e) {
            // 在致命错误处理期间忽略配置加载错误
        }
        
        if (config != null && config.isDebugMode()) {
            System.err.println("\n--- Stack Trace ---");
            throwable.printStackTrace(System.err);
            System.err.println("--- End Stack Trace ---");
        }
    }
}
