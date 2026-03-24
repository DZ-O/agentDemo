package com.openbutler.cli.terminal.model;

import com.openbutler.core.agent.AgentCore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CommandContext model containing all context information needed for command execution.
 * 
 * This class encapsulates all the data and services that a command needs to execute,
 * including the raw input, parsed command details, current session, and service references.
 * 
 * Validates: Requirements 2.1-2.11 (Command execution context)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandContext {
    
    /**
     * 原始输入
     * The raw input string as entered by the user
     */
    private String rawInput;
    
    /**
     * 命令名称
     * The parsed command name (e.g., "chat", "new", "list")
     */
    private String commandName;
    
    /**
     * 命令参数
     * List of arguments passed to the command
     */
    private List<String> arguments;
    
    /**
     * 当前会话
     * The current active session
     */
    private Session currentSession;
    
    /**
     * 终端渲染器
     * Renderer for terminal output (will be defined as interface later)
     */
    private Object renderer;
    
    /**
     * 配置管理器
     * Manager for CLI configuration (will be defined as interface later)
     */
    private Object configManager;
    
    /**
     * 会话管理器
     * Manager for session operations (will be defined as interface later)
     */
    private Object sessionManager;
    
    /**
     * Agent核心服务
     * Core AI agent service for processing user messages
     */
    private AgentCore agentCore;
}
