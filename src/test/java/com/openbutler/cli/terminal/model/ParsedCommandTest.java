package com.openbutler.cli.terminal.model;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ParsedCommand model class.
 * 
 * Tests the builder pattern, default values, and data integrity of the ParsedCommand model.
 */
class ParsedCommandTest {
    
    @Test
    void testBuilderCreatesValidCommand() {
        // Arrange & Act
        ParsedCommand command = ParsedCommand.builder()
            .isCommand(true)
            .commandName("chat")
            .rawInput("/chat hello world")
            .build();
        
        // Assert
        assertTrue(command.isCommand());
        assertEquals("chat", command.getCommandName());
        assertEquals("/chat hello world", command.getRawInput());
        assertNotNull(command.getPositionalArgs());
        assertNotNull(command.getNamedArgs());
        assertNotNull(command.getFlags());
    }
    
    @Test
    void testDefaultValuesAreInitialized() {
        // Arrange & Act
        ParsedCommand command = ParsedCommand.builder()
            .isCommand(true)
            .commandName("help")
            .rawInput("/help")
            .build();
        
        // Assert
        assertNotNull(command.getPositionalArgs(), "Positional args should be initialized");
        assertNotNull(command.getNamedArgs(), "Named args should be initialized");
        assertNotNull(command.getFlags(), "Flags should be initialized");
        assertTrue(command.getPositionalArgs().isEmpty());
        assertTrue(command.getNamedArgs().isEmpty());
        assertTrue(command.getFlags().isEmpty());
    }
    
    @Test
    void testPositionalArgumentsAreStored() {
        // Arrange
        List<String> args = Arrays.asList("arg1", "arg2", "arg3");
        
        // Act
        ParsedCommand command = ParsedCommand.builder()
            .isCommand(true)
            .commandName("new")
            .positionalArgs(args)
            .rawInput("/new arg1 arg2 arg3")
            .build();
        
        // Assert
        assertEquals(3, command.getPositionalArgs().size());
        assertEquals("arg1", command.getPositionalArgs().get(0));
        assertEquals("arg2", command.getPositionalArgs().get(1));
        assertEquals("arg3", command.getPositionalArgs().get(2));
    }
    
    @Test
    void testNamedArgumentsAreStored() {
        // Arrange
        Map<String, String> namedArgs = new HashMap<>();
        namedArgs.put("theme", "dark");
        namedArgs.put("size", "large");
        
        // Act
        ParsedCommand command = ParsedCommand.builder()
            .isCommand(true)
            .commandName("config")
            .namedArgs(namedArgs)
            .rawInput("/config --theme=dark --size=large")
            .build();
        
        // Assert
        assertEquals(2, command.getNamedArgs().size());
        assertEquals("dark", command.getNamedArgs().get("theme"));
        assertEquals("large", command.getNamedArgs().get("size"));
    }
    
    @Test
    void testFlagsAreStored() {
        // Arrange
        Set<String> flags = new HashSet<>(Arrays.asList("verbose", "all"));
        
        // Act
        ParsedCommand command = ParsedCommand.builder()
            .isCommand(true)
            .commandName("list")
            .flags(flags)
            .rawInput("/list --verbose --all")
            .build();
        
        // Assert
        assertEquals(2, command.getFlags().size());
        assertTrue(command.getFlags().contains("verbose"));
        assertTrue(command.getFlags().contains("all"));
    }
    
    @Test
    void testMixedArgumentTypes() {
        // Arrange
        List<String> positional = Arrays.asList("session1");
        Map<String, String> named = new HashMap<>();
        named.put("format", "json");
        Set<String> flags = new HashSet<>(Collections.singletonList("verbose"));
        
        // Act
        ParsedCommand command = ParsedCommand.builder()
            .isCommand(true)
            .commandName("export")
            .positionalArgs(positional)
            .namedArgs(named)
            .flags(flags)
            .rawInput("/export session1 --format=json --verbose")
            .build();
        
        // Assert
        assertEquals(1, command.getPositionalArgs().size());
        assertEquals("session1", command.getPositionalArgs().get(0));
        assertEquals(1, command.getNamedArgs().size());
        assertEquals("json", command.getNamedArgs().get("format"));
        assertEquals(1, command.getFlags().size());
        assertTrue(command.getFlags().contains("verbose"));
    }
    
    @Test
    void testNonCommandInput() {
        // Arrange & Act
        ParsedCommand command = ParsedCommand.builder()
            .isCommand(false)
            .commandName(null)
            .rawInput("Hello, how are you?")
            .build();
        
        // Assert
        assertFalse(command.isCommand());
        assertNull(command.getCommandName());
        assertEquals("Hello, how are you?", command.getRawInput());
    }
    
    @Test
    void testNoArgsConstructor() {
        // Act
        ParsedCommand command = new ParsedCommand();
        
        // Assert
        assertNotNull(command);
        assertFalse(command.isCommand());
        assertNull(command.getCommandName());
        assertNull(command.getRawInput());
    }
    
    @Test
    void testAllArgsConstructor() {
        // Arrange
        List<String> positional = Arrays.asList("arg1");
        Map<String, String> named = new HashMap<>();
        named.put("key", "value");
        Set<String> flags = new HashSet<>(Collections.singletonList("flag"));
        
        // Act
        ParsedCommand command = new ParsedCommand(
            true,
            "test",
            positional,
            named,
            flags,
            "/test arg1 --key=value --flag"
        );
        
        // Assert
        assertTrue(command.isCommand());
        assertEquals("test", command.getCommandName());
        assertEquals(1, command.getPositionalArgs().size());
        assertEquals(1, command.getNamedArgs().size());
        assertEquals(1, command.getFlags().size());
        assertEquals("/test arg1 --key=value --flag", command.getRawInput());
    }
    
    @Test
    void testSettersAndGetters() {
        // Arrange
        ParsedCommand command = new ParsedCommand();
        
        // Act
        command.setCommand(true);
        command.setCommandName("switch");
        command.setRawInput("/switch session1");
        
        List<String> args = new ArrayList<>();
        args.add("session1");
        command.setPositionalArgs(args);
        
        Map<String, String> named = new HashMap<>();
        named.put("force", "true");
        command.setNamedArgs(named);
        
        Set<String> flags = new HashSet<>();
        flags.add("quiet");
        command.setFlags(flags);
        
        // Assert
        assertTrue(command.isCommand());
        assertEquals("switch", command.getCommandName());
        assertEquals("/switch session1", command.getRawInput());
        assertEquals(1, command.getPositionalArgs().size());
        assertEquals(1, command.getNamedArgs().size());
        assertEquals(1, command.getFlags().size());
    }
    
    @Test
    void testEmptyCommand() {
        // Act
        ParsedCommand command = ParsedCommand.builder()
            .isCommand(true)
            .commandName("clear")
            .rawInput("/clear")
            .build();
        
        // Assert
        assertTrue(command.isCommand());
        assertEquals("clear", command.getCommandName());
        assertTrue(command.getPositionalArgs().isEmpty());
        assertTrue(command.getNamedArgs().isEmpty());
        assertTrue(command.getFlags().isEmpty());
    }
}
