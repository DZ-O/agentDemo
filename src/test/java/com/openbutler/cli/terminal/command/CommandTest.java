package com.openbutler.cli.terminal.command;

import com.openbutler.cli.terminal.model.CommandContext;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Command interface to verify it can be implemented correctly.
 * 
 * Validates: Requirements 2.1-2.11
 */
class CommandTest {
    
    /**
     * Mock implementation of Command for testing purposes.
     */
    static class MockCommand implements Command {
        private final String name;
        private final List<String> aliases;
        private final String description;
        private final String usage;
        private final boolean requiresSession;
        private boolean executed = false;
        
        MockCommand(String name, List<String> aliases, String description, String usage, boolean requiresSession) {
            this.name = name;
            this.aliases = aliases;
            this.description = description;
            this.usage = usage;
            this.requiresSession = requiresSession;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public List<String> getAliases() {
            return aliases;
        }
        
        @Override
        public String getDescription() {
            return description;
        }
        
        @Override
        public String getUsage() {
            return usage;
        }
        
        @Override
        public Mono<Void> execute(CommandContext context) {
            return Mono.fromRunnable(() -> executed = true);
        }
        
        @Override
        public boolean requiresSession() {
            return requiresSession;
        }
        
        public boolean isExecuted() {
            return executed;
        }
    }
    
    @Test
    void testCommandInterfaceCanBeImplemented() {
        // Given: A mock command implementation
        MockCommand command = new MockCommand(
            "test",
            Arrays.asList("t", "tst"),
            "Test command",
            "test [options]",
            false
        );
        
        // Then: All methods should return expected values
        assertEquals("test", command.getName());
        assertEquals(Arrays.asList("t", "tst"), command.getAliases());
        assertEquals("Test command", command.getDescription());
        assertEquals("test [options]", command.getUsage());
        assertFalse(command.requiresSession());
    }
    
    @Test
    void testCommandExecuteReturnsMonoVoid() {
        // Given: A mock command
        MockCommand command = new MockCommand(
            "test",
            Collections.emptyList(),
            "Test command",
            "test",
            false
        );
        
        // When: Execute is called
        CommandContext context = CommandContext.builder().build();
        Mono<Void> result = command.execute(context);
        
        // Then: Result should be a Mono<Void> that completes successfully
        assertNotNull(result);
        
        // Block and verify execution completes
        result.block();
        assertTrue(command.isExecuted());
    }
    
    @Test
    void testCommandWithSessionRequirement() {
        // Given: A command that requires a session
        MockCommand command = new MockCommand(
            "chat",
            Collections.emptyList(),
            "Chat command",
            "chat <message>",
            true
        );
        
        // Then: requiresSession should return true
        assertTrue(command.requiresSession());
    }
    
    @Test
    void testCommandWithoutSessionRequirement() {
        // Given: A command that doesn't require a session
        MockCommand command = new MockCommand(
            "help",
            Collections.emptyList(),
            "Help command",
            "help [command]",
            false
        );
        
        // Then: requiresSession should return false (default behavior)
        assertFalse(command.requiresSession());
    }
    
    @Test
    void testCommandWithEmptyAliases() {
        // Given: A command with no aliases
        MockCommand command = new MockCommand(
            "version",
            Collections.emptyList(),
            "Version command",
            "version",
            false
        );
        
        // Then: getAliases should return an empty list
        assertTrue(command.getAliases().isEmpty());
    }
    
    @Test
    void testCommandWithMultipleAliases() {
        // Given: A command with multiple aliases
        MockCommand command = new MockCommand(
            "exit",
            Arrays.asList("quit", "q", "bye"),
            "Exit command",
            "exit",
            false
        );
        
        // Then: getAliases should return all aliases
        assertEquals(3, command.getAliases().size());
        assertTrue(command.getAliases().contains("quit"));
        assertTrue(command.getAliases().contains("q"));
        assertTrue(command.getAliases().contains("bye"));
    }
}
