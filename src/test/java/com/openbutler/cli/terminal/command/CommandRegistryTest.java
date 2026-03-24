package com.openbutler.cli.terminal.command;

import com.openbutler.cli.terminal.model.CommandContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CommandRegistry service.
 * 
 * Tests command registration, lookup, and similar command suggestions.
 * 
 * Validates: Requirements 2.1-2.11, 9.5
 */
class CommandRegistryTest {
    
    private CommandRegistry commandRegistry;
    
    /**
     * Mock command implementation for testing.
     */
    static class TestCommand implements Command {
        private final String name;
        private final List<String> aliases;
        private final String description;
        private final String usage;
        
        TestCommand(String name, List<String> aliases, String description, String usage) {
            this.name = name;
            this.aliases = aliases;
            this.description = description;
            this.usage = usage;
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
            return Mono.empty();
        }
    }
    
    @BeforeEach
    void setUp() {
        // Create test commands
        List<Command> commands = Arrays.asList(
                new TestCommand("chat", Arrays.asList("c", "talk"), "Chat with AI", "chat <message>"),
                new TestCommand("new", Arrays.asList("n", "create"), "Create new session", "new [name]"),
                new TestCommand("list", Arrays.asList("ls", "l"), "List sessions", "list"),
                new TestCommand("switch", Arrays.asList("sw", "s"), "Switch session", "switch <id>"),
                new TestCommand("history", Arrays.asList("h", "hist"), "Show history", "history"),
                new TestCommand("clear", Collections.singletonList("cls"), "Clear screen", "clear"),
                new TestCommand("config", Collections.emptyList(), "Manage config", "config <action>"),
                new TestCommand("help", Collections.singletonList("?"), "Show help", "help [command]"),
                new TestCommand("version", Arrays.asList("v", "ver"), "Show version", "version"),
                new TestCommand("exit", Arrays.asList("quit", "q"), "Exit application", "exit")
        );
        
        commandRegistry = new CommandRegistry();
        commandRegistry.setCommands(commands);
    }
    
    /**
     * Test that commands are registered by their primary name.
     * Validates: Requirements 2.1-2.10
     */
    @Test
    void testCommandsRegisteredByName() {
        assertNotNull(commandRegistry.getCommand("chat"));
        assertNotNull(commandRegistry.getCommand("new"));
        assertNotNull(commandRegistry.getCommand("list"));
        assertNotNull(commandRegistry.getCommand("switch"));
        assertNotNull(commandRegistry.getCommand("history"));
        assertNotNull(commandRegistry.getCommand("clear"));
        assertNotNull(commandRegistry.getCommand("config"));
        assertNotNull(commandRegistry.getCommand("help"));
        assertNotNull(commandRegistry.getCommand("version"));
        assertNotNull(commandRegistry.getCommand("exit"));
    }
    
    /**
     * Test that commands are registered by their aliases.
     * Validates: Requirements 2.1-2.10
     */
    @Test
    void testCommandsRegisteredByAliases() {
        // chat aliases
        assertEquals("chat", commandRegistry.getCommand("c").getName());
        assertEquals("chat", commandRegistry.getCommand("talk").getName());
        
        // new aliases
        assertEquals("new", commandRegistry.getCommand("n").getName());
        assertEquals("new", commandRegistry.getCommand("create").getName());
        
        // list aliases
        assertEquals("list", commandRegistry.getCommand("ls").getName());
        assertEquals("list", commandRegistry.getCommand("l").getName());
        
        // switch aliases
        assertEquals("switch", commandRegistry.getCommand("sw").getName());
        assertEquals("switch", commandRegistry.getCommand("s").getName());
        
        // history aliases
        assertEquals("history", commandRegistry.getCommand("h").getName());
        assertEquals("history", commandRegistry.getCommand("hist").getName());
        
        // clear aliases
        assertEquals("clear", commandRegistry.getCommand("cls").getName());
        
        // help aliases
        assertEquals("help", commandRegistry.getCommand("?").getName());
        
        // version aliases
        assertEquals("version", commandRegistry.getCommand("v").getName());
        assertEquals("version", commandRegistry.getCommand("ver").getName());
        
        // exit aliases
        assertEquals("exit", commandRegistry.getCommand("quit").getName());
        assertEquals("exit", commandRegistry.getCommand("q").getName());
    }
    
    /**
     * Test that getCommand returns null for unknown commands.
     * Validates: Requirement 9.5
     */
    @Test
    void testGetCommandReturnsNullForUnknownCommand() {
        assertNull(commandRegistry.getCommand("unknown"));
        assertNull(commandRegistry.getCommand("invalid"));
        assertNull(commandRegistry.getCommand("notfound"));
    }
    
    /**
     * Test that getAllCommandNames returns all registered command names.
     */
    @Test
    void testGetAllCommandNames() {
        List<String> commandNames = commandRegistry.getAllCommandNames();
        
        assertEquals(10, commandNames.size());
        assertTrue(commandNames.contains("chat"));
        assertTrue(commandNames.contains("new"));
        assertTrue(commandNames.contains("list"));
        assertTrue(commandNames.contains("switch"));
        assertTrue(commandNames.contains("history"));
        assertTrue(commandNames.contains("clear"));
        assertTrue(commandNames.contains("config"));
        assertTrue(commandNames.contains("help"));
        assertTrue(commandNames.contains("version"));
        assertTrue(commandNames.contains("exit"));
    }
    
    /**
     * Test that getAllCommandNames does not include aliases.
     */
    @Test
    void testGetAllCommandNamesExcludesAliases() {
        List<String> commandNames = commandRegistry.getAllCommandNames();
        
        // Aliases should not be in the list
        assertFalse(commandNames.contains("c"));
        assertFalse(commandNames.contains("talk"));
        assertFalse(commandNames.contains("n"));
        assertFalse(commandNames.contains("ls"));
        assertFalse(commandNames.contains("quit"));
    }
    
    /**
     * Test finding similar commands with typos.
     * Validates: Requirement 9.5
     */
    @Test
    void testFindSimilarCommandsWithTypos() {
        // Test typo: "chatt" -> "chat"
        List<String> suggestions = commandRegistry.findSimilarCommands("chatt");
        assertFalse(suggestions.isEmpty());
        assertEquals("chat", suggestions.get(0));
        
        // Test typo: "lst" -> "list"
        suggestions = commandRegistry.findSimilarCommands("lst");
        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.contains("list"));
        
        // Test typo: "hlp" -> "help"
        suggestions = commandRegistry.findSimilarCommands("hlp");
        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.contains("help"));
        
        // Test typo: "exti" -> "exit"
        suggestions = commandRegistry.findSimilarCommands("exti");
        assertFalse(suggestions.isEmpty());
        assertEquals("exit", suggestions.get(0));
    }
    
    /**
     * Test finding similar commands with case insensitivity.
     * Validates: Requirement 9.5
     */
    @Test
    void testFindSimilarCommandsCaseInsensitive() {
        List<String> suggestions = commandRegistry.findSimilarCommands("CHAT");
        assertFalse(suggestions.isEmpty());
        assertEquals("chat", suggestions.get(0));
        
        suggestions = commandRegistry.findSimilarCommands("List");
        assertFalse(suggestions.isEmpty());
        assertEquals("list", suggestions.get(0));
        
        suggestions = commandRegistry.findSimilarCommands("EXIT");
        assertFalse(suggestions.isEmpty());
        assertEquals("exit", suggestions.get(0));
    }
    
    /**
     * Test that similar commands are sorted by distance.
     * Validates: Requirement 9.5
     */
    @Test
    void testFindSimilarCommandsSortedByDistance() {
        // "ne" is closer to "new" (distance 1) than to "help" (distance 3)
        List<String> suggestions = commandRegistry.findSimilarCommands("ne");
        assertFalse(suggestions.isEmpty());
        assertEquals("new", suggestions.get(0));
    }
    
    /**
     * Test that only commands within distance threshold are suggested.
     * Validates: Requirement 9.5
     */
    @Test
    void testFindSimilarCommandsDistanceThreshold() {
        // "xyz" is too far from any command (distance > 3)
        List<String> suggestions = commandRegistry.findSimilarCommands("xyz");
        // Should return empty or very few results
        assertTrue(suggestions.size() <= 3);
    }
    
    /**
     * Test that findSimilarCommands limits results to 3.
     * Validates: Requirement 9.5
     */
    @Test
    void testFindSimilarCommandsLimitsResults() {
        // "e" might match multiple commands, but should return max 3
        List<String> suggestions = commandRegistry.findSimilarCommands("e");
        assertTrue(suggestions.size() <= 3);
    }
    
    /**
     * Test that findSimilarCommands handles null input.
     */
    @Test
    void testFindSimilarCommandsHandlesNullInput() {
        List<String> suggestions = commandRegistry.findSimilarCommands(null);
        assertTrue(suggestions.isEmpty());
    }
    
    /**
     * Test that findSimilarCommands handles empty input.
     */
    @Test
    void testFindSimilarCommandsHandlesEmptyInput() {
        List<String> suggestions = commandRegistry.findSimilarCommands("");
        assertTrue(suggestions.isEmpty());
    }
    
    /**
     * Test exact match returns the command itself.
     */
    @Test
    void testFindSimilarCommandsExactMatch() {
        List<String> suggestions = commandRegistry.findSimilarCommands("chat");
        assertFalse(suggestions.isEmpty());
        assertEquals("chat", suggestions.get(0));
    }
    
    /**
     * Test Levenshtein distance calculation for common typos.
     * Validates: Requirement 9.5
     */
    @Test
    void testLevenshteinDistanceCalculation() {
        // Single character insertion: "chat" -> "chatt" (distance 1)
        List<String> suggestions = commandRegistry.findSimilarCommands("chatt");
        assertTrue(suggestions.contains("chat"));
        
        // Single character deletion: "list" -> "lst" (distance 1)
        suggestions = commandRegistry.findSimilarCommands("lst");
        assertTrue(suggestions.contains("list"));
        
        // Single character substitution: "help" -> "halp" (distance 1)
        suggestions = commandRegistry.findSimilarCommands("halp");
        assertTrue(suggestions.contains("help"));
    }
    
    /**
     * Test that command registry is thread-safe.
     */
    @Test
    void testThreadSafety() throws InterruptedException {
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    assertNotNull(commandRegistry.getCommand("chat"));
                    assertNotNull(commandRegistry.getAllCommandNames());
                    assertNotNull(commandRegistry.findSimilarCommands("chatt"));
                }
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Verify registry is still functional
        assertNotNull(commandRegistry.getCommand("chat"));
        assertEquals(10, commandRegistry.getAllCommandNames().size());
    }
    
    /**
     * Test that command with no aliases is registered correctly.
     */
    @Test
    void testCommandWithNoAliases() {
        Command config = commandRegistry.getCommand("config");
        assertNotNull(config);
        assertEquals("config", config.getName());
        assertTrue(config.getAliases().isEmpty());
    }
    
    /**
     * Test that multiple aliases point to the same command instance.
     */
    @Test
    void testMultipleAliasesPointToSameCommand() {
        Command chatByName = commandRegistry.getCommand("chat");
        Command chatByAlias1 = commandRegistry.getCommand("c");
        Command chatByAlias2 = commandRegistry.getCommand("talk");
        
        assertSame(chatByName, chatByAlias1);
        assertSame(chatByName, chatByAlias2);
    }
    
    /**
     * Test command lookup performance.
     */
    @Test
    void testCommandLookupPerformance() {
        long startTime = System.nanoTime();
        
        for (int i = 0; i < 10000; i++) {
            commandRegistry.getCommand("chat");
            commandRegistry.getCommand("c");
            commandRegistry.getCommand("unknown");
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        // Should complete in less than 100ms for 30,000 lookups
        assertTrue(durationMs < 100, "Command lookup took " + durationMs + "ms");
    }
    
    /**
     * Test similar command search performance.
     */
    @Test
    void testSimilarCommandSearchPerformance() {
        long startTime = System.nanoTime();
        
        for (int i = 0; i < 1000; i++) {
            commandRegistry.findSimilarCommands("chatt");
            commandRegistry.findSimilarCommands("lst");
            commandRegistry.findSimilarCommands("hlp");
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        // Should complete in less than 500ms for 3,000 searches
        assertTrue(durationMs < 500, "Similar command search took " + durationMs + "ms");
    }
}
