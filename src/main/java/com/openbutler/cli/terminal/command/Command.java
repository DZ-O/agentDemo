package com.openbutler.cli.terminal.command;

import com.openbutler.cli.terminal.model.CommandContext;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Command interface defining the contract for all CLI commands.
 * 
 * This interface provides the foundation for the command system, supporting:
 * - Command metadata (name, aliases, description, usage)
 * - Reactive programming with Mono<Void> for async execution
 * - Session requirement indication
 * 
 * Validates: Requirements 2.1-2.11
 */
public interface Command {
    
    /**
     * Get the command name (e.g., "chat", "new", "list").
     * 
     * @return the command name
     */
    String getName();
    
    /**
     * Get the command aliases (alternative names for the command).
     * 
     * @return list of command aliases
     */
    List<String> getAliases();
    
    /**
     * Get the command description (used for help display).
     * 
     * @return the command description
     */
    String getDescription();
    
    /**
     * Get the command usage information.
     * 
     * @return the command usage string
     */
    String getUsage();
    
    /**
     * Execute the command with the given context.
     * 
     * @param context the command context containing parameters, session info, and services
     * @return a Mono<Void> for async execution
     */
    Mono<Void> execute(CommandContext context);
    
    /**
     * Indicates whether this command requires an active session.
     * 
     * @return true if the command requires a session, false otherwise
     */
    default boolean requiresSession() {
        return false;
    }
}
