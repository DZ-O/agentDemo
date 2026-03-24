package com.openbutler.cli.terminal.service;

import com.openbutler.cli.terminal.model.ParsedCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CommandParser service.
 * 
 * Tests command detection, parsing of command names, positional arguments,
 * named arguments (--key=value), and flag arguments (--flag).
 * 
 * Validates: Requirements 2.1-2.11
 */
class CommandParserTest {
    
    private CommandParser parser;
    
    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }
    
    // ========== Command Detection Tests ==========
    
    /**
     * Test that input starting with / is recognized as a command.
     * Validates: Requirement 2.1-2.10
     */
    @Test
    void testIsCommandReturnsTrueForCommandInput() {
        assertTrue(parser.isCommand("/chat"));
        assertTrue(parser.isCommand("/new"));
        assertTrue(parser.isCommand("/list"));
        assertTrue(parser.isCommand("/help"));
        assertTrue(parser.isCommand("  /chat  ")); // with whitespace
    }
    
    /**
     * Test that input not starting with / is not recognized as a command.
     * Validates: Requirement 2.11
     */
    @Test
    void testIsCommandReturnsFalseForNonCommandInput() {
        assertFalse(parser.isCommand("hello world"));
        assertFalse(parser.isCommand("chat hello"));
        assertFalse(parser.isCommand(""));
        assertFalse(parser.isCommand("   "));
        assertFalse(parser.isCommand(null));
    }
    
    // ========== Command Name Extraction Tests ==========
    
    /**
     * Test extracting command name from simple command.
     * Validates: Requirement 2.1-2.10
     */
    @Test
    void testExtractCommandNameFromSimpleCommand() {
        assertEquals("chat", parser.extractCommandName("/chat"));
        assertEquals("new", parser.extractCommandName("/new"));
        assertEquals("list", parser.extractCommandName("/list"));
        assertEquals("help", parser.extractCommandName("/help"));
    }
    
    /**
     * Test extracting command name from command with arguments.
     * Validates: Requirement 2.1-2.10
     */
    @Test
    void testExtractCommandNameFromCommandWithArguments() {
        assertEquals("chat", parser.extractCommandName("/chat hello world"));
        assertEquals("new", parser.extractCommandName("/new session1"));
        assertEquals("switch", parser.extractCommandName("/switch abc123"));
    }
    
    /**
     * Test extracting command name handles whitespace.
     */
    @Test
    void testExtractCommandNameHandlesWhitespace() {
        assertEquals("chat", parser.extractCommandName("  /chat  "));
        assertEquals("new", parser.extractCommandName("/new   session1"));
    }
    
    /**
     * Test extracting command name from non-command returns null.
     */
    @Test
    void testExtractCommandNameFromNonCommandReturnsNull() {
        assertNull(parser.extractCommandName("hello world"));
        assertNull(parser.extractCommandName(null));
    }
    
    // ========== Argument Extraction Tests ==========
    
    /**
     * Test extracting arguments from command with no arguments.
     */
    @Test
    void testExtractArgumentsFromCommandWithNoArgs() {
        List<String> args = parser.extractArguments("/help");
        assertTrue(args.isEmpty());
    }
    
    /**
     * Test extracting arguments from command with positional arguments.
     */
    @Test
    void testExtractArgumentsFromCommandWithPositionalArgs() {
        List<String> args = parser.extractArguments("/new session1 session2");
        assertEquals(2, args.size());
        assertEquals("session1", args.get(0));
        assertEquals("session2", args.get(1));
    }
    
    /**
     * Test extracting arguments from command with named arguments.
     */
    @Test
    void testExtractArgumentsFromCommandWithNamedArgs() {
        List<String> args = parser.extractArguments("/config --theme=dark --size=large");
        assertEquals(2, args.size());
        assertTrue(args.contains("--theme=dark"));
        assertTrue(args.contains("--size=large"));
    }
    
    /**
     * Test extracting arguments from command with flags.
     */
    @Test
    void testExtractArgumentsFromCommandWithFlags() {
        List<String> args = parser.extractArguments("/list --verbose --all");
        assertEquals(2, args.size());
        assertTrue(args.contains("--verbose"));
        assertTrue(args.contains("--all"));
    }
    
    /**
     * Test extracting arguments from non-command returns empty list.
     */
    @Test
    void testExtractArgumentsFromNonCommandReturnsEmpty() {
        List<String> args = parser.extractArguments("hello world");
        assertTrue(args.isEmpty());
    }
    
    // ========== Full Parsing Tests ==========
    
    /**
     * Test parsing simple command with no arguments.
     * Validates: Requirement 2.1-2.10
     */
    @Test
    void testParseSimpleCommandWithNoArgs() {
        ParsedCommand cmd = parser.parse("/help");
        
        assertTrue(cmd.isCommand());
        assertEquals("help", cmd.getCommandName());
        assertTrue(cmd.getPositionalArgs().isEmpty());
        assertTrue(cmd.getNamedArgs().isEmpty());
        assertTrue(cmd.getFlags().isEmpty());
        assertEquals("/help", cmd.getRawInput());
    }
    
    /**
     * Test parsing command with positional arguments.
     * Validates: Requirement 2.1-2.10
     */
    @Test
    void testParseCommandWithPositionalArgs() {
        ParsedCommand cmd = parser.parse("/new session1 session2");
        
        assertTrue(cmd.isCommand());
        assertEquals("new", cmd.getCommandName());
        assertEquals(2, cmd.getPositionalArgs().size());
        assertEquals("session1", cmd.getPositionalArgs().get(0));
        assertEquals("session2", cmd.getPositionalArgs().get(1));
        assertTrue(cmd.getNamedArgs().isEmpty());
        assertTrue(cmd.getFlags().isEmpty());
    }
    
    /**
     * Test parsing command with named arguments.
     * Validates: Requirement 2.1-2.10
     */
    @Test
    void testParseCommandWithNamedArgs() {
        ParsedCommand cmd = parser.parse("/config --theme=dark --size=large");
        
        assertTrue(cmd.isCommand());
        assertEquals("config", cmd.getCommandName());
        assertTrue(cmd.getPositionalArgs().isEmpty());
        assertEquals(2, cmd.getNamedArgs().size());
        assertEquals("dark", cmd.getNamedArgs().get("theme"));
        assertEquals("large", cmd.getNamedArgs().get("size"));
        assertTrue(cmd.getFlags().isEmpty());
    }
    
    /**
     * Test parsing command with flag arguments.
     * Validates: Requirement 2.1-2.10
     */
    @Test
    void testParseCommandWithFlags() {
        ParsedCommand cmd = parser.parse("/list --verbose --all");
        
        assertTrue(cmd.isCommand());
        assertEquals("list", cmd.getCommandName());
        assertTrue(cmd.getPositionalArgs().isEmpty());
        assertTrue(cmd.getNamedArgs().isEmpty());
        assertEquals(2, cmd.getFlags().size());
        assertTrue(cmd.getFlags().contains("verbose"));
        assertTrue(cmd.getFlags().contains("all"));
    }
    
    /**
     * Test parsing command with mixed argument types.
     * Validates: Requirement 2.1-2.10
     */
    @Test
    void testParseCommandWithMixedArgs() {
        ParsedCommand cmd = parser.parse("/export session1 --format=json --verbose");
        
        assertTrue(cmd.isCommand());
        assertEquals("export", cmd.getCommandName());
        assertEquals(1, cmd.getPositionalArgs().size());
        assertEquals("session1", cmd.getPositionalArgs().get(0));
        assertEquals(1, cmd.getNamedArgs().size());
        assertEquals("json", cmd.getNamedArgs().get("format"));
        assertEquals(1, cmd.getFlags().size());
        assertTrue(cmd.getFlags().contains("verbose"));
    }
    
    /**
     * Test parsing non-command input (chat message).
     * Validates: Requirement 2.11
     */
    @Test
    void testParseNonCommandInput() {
        ParsedCommand cmd = parser.parse("Hello, how are you?");
        
        assertFalse(cmd.isCommand());
        assertNull(cmd.getCommandName());
        assertTrue(cmd.getPositionalArgs().isEmpty());
        assertTrue(cmd.getNamedArgs().isEmpty());
        assertTrue(cmd.getFlags().isEmpty());
        assertEquals("Hello, how are you?", cmd.getRawInput());
    }
    
    /**
     * Test parsing empty input.
     */
    @Test
    void testParseEmptyInput() {
        ParsedCommand cmd = parser.parse("");
        
        assertFalse(cmd.isCommand());
        assertNull(cmd.getCommandName());
        assertEquals("", cmd.getRawInput());
    }
    
    /**
     * Test parsing null input.
     */
    @Test
    void testParseNullInput() {
        ParsedCommand cmd = parser.parse(null);
        
        assertFalse(cmd.isCommand());
        assertNull(cmd.getCommandName());
        assertNull(cmd.getRawInput());
    }
    
    /**
     * Test parsing whitespace-only input.
     */
    @Test
    void testParseWhitespaceOnlyInput() {
        ParsedCommand cmd = parser.parse("   ");
        
        assertFalse(cmd.isCommand());
        assertNull(cmd.getCommandName());
    }
    
    // ========== Edge Cases and Special Scenarios ==========
    
    /**
     * Test parsing command with quoted arguments containing spaces.
     */
    @Test
    void testParseCommandWithQuotedArguments() {
        ParsedCommand cmd = parser.parse("/new \"my session\" 'another session'");
        
        assertTrue(cmd.isCommand());
        assertEquals("new", cmd.getCommandName());
        assertEquals(2, cmd.getPositionalArgs().size());
        assertEquals("my session", cmd.getPositionalArgs().get(0));
        assertEquals("another session", cmd.getPositionalArgs().get(1));
    }
    
    /**
     * Test parsing command with named argument containing spaces in value.
     */
    @Test
    void testParseCommandWithQuotedNamedArgValue() {
        ParsedCommand cmd = parser.parse("/config --name=\"My Config\"");
        
        assertTrue(cmd.isCommand());
        assertEquals("config", cmd.getCommandName());
        assertEquals(1, cmd.getNamedArgs().size());
        assertEquals("My Config", cmd.getNamedArgs().get("name"));
    }
    
    /**
     * Test parsing command with multiple spaces between arguments.
     */
    @Test
    void testParseCommandWithMultipleSpaces() {
        ParsedCommand cmd = parser.parse("/new   session1    session2");
        
        assertTrue(cmd.isCommand());
        assertEquals("new", cmd.getCommandName());
        assertEquals(2, cmd.getPositionalArgs().size());
        assertEquals("session1", cmd.getPositionalArgs().get(0));
        assertEquals("session2", cmd.getPositionalArgs().get(1));
    }
    
    /**
     * Test parsing command with leading/trailing whitespace.
     */
    @Test
    void testParseCommandWithLeadingTrailingWhitespace() {
        ParsedCommand cmd = parser.parse("  /chat hello world  ");
        
        assertTrue(cmd.isCommand());
        assertEquals("chat", cmd.getCommandName());
        assertEquals(2, cmd.getPositionalArgs().size());
        assertEquals("hello", cmd.getPositionalArgs().get(0));
        assertEquals("world", cmd.getPositionalArgs().get(1));
    }
    
    /**
     * Test parsing command with hyphenated flag names.
     */
    @Test
    void testParseCommandWithHyphenatedFlags() {
        ParsedCommand cmd = parser.parse("/list --show-all --include-hidden");
        
        assertTrue(cmd.isCommand());
        assertEquals("list", cmd.getCommandName());
        assertEquals(2, cmd.getFlags().size());
        assertTrue(cmd.getFlags().contains("show-all"));
        assertTrue(cmd.getFlags().contains("include-hidden"));
    }
    
    /**
     * Test parsing command with underscored named arguments.
     */
    @Test
    void testParseCommandWithUnderscoredNamedArgs() {
        ParsedCommand cmd = parser.parse("/config --color_scheme=dark --auto_save=true");
        
        assertTrue(cmd.isCommand());
        assertEquals("config", cmd.getCommandName());
        assertEquals(2, cmd.getNamedArgs().size());
        assertEquals("dark", cmd.getNamedArgs().get("color_scheme"));
        assertEquals("true", cmd.getNamedArgs().get("auto_save"));
    }
    
    /**
     * Test parsing command with numeric values in named arguments.
     */
    @Test
    void testParseCommandWithNumericNamedArgs() {
        ParsedCommand cmd = parser.parse("/config --history-size=1000 --timeout=30");
        
        assertTrue(cmd.isCommand());
        assertEquals("config", cmd.getCommandName());
        assertEquals(2, cmd.getNamedArgs().size());
        assertEquals("1000", cmd.getNamedArgs().get("history-size"));
        assertEquals("30", cmd.getNamedArgs().get("timeout"));
    }
    
    /**
     * Test parsing command with special characters in values.
     */
    @Test
    void testParseCommandWithSpecialCharactersInValues() {
        ParsedCommand cmd = parser.parse("/config --url=https://example.com/api --path=/home/user");
        
        assertTrue(cmd.isCommand());
        assertEquals("config", cmd.getCommandName());
        assertEquals(2, cmd.getNamedArgs().size());
        assertEquals("https://example.com/api", cmd.getNamedArgs().get("url"));
        assertEquals("/home/user", cmd.getNamedArgs().get("path"));
    }
    
    /**
     * Test parsing command with empty named argument value.
     */
    @Test
    void testParseCommandWithEmptyNamedArgValue() {
        ParsedCommand cmd = parser.parse("/config --name=");
        
        assertTrue(cmd.isCommand());
        assertEquals("config", cmd.getCommandName());
        assertEquals(1, cmd.getNamedArgs().size());
        assertEquals("", cmd.getNamedArgs().get("name"));
    }
    
    /**
     * Test parsing command with single character command name.
     */
    @Test
    void testParseCommandWithSingleCharacterName() {
        ParsedCommand cmd = parser.parse("/h");
        
        assertTrue(cmd.isCommand());
        assertEquals("h", cmd.getCommandName());
    }
    
    /**
     * Test parsing command with only slash returns empty command name.
     */
    @Test
    void testParseCommandWithOnlySlash() {
        ParsedCommand cmd = parser.parse("/");
        
        assertTrue(cmd.isCommand());
        assertEquals("", cmd.getCommandName());
    }
    
    /**
     * Test parsing preserves raw input exactly.
     */
    @Test
    void testParsePreservesRawInput() {
        String input = "  /chat  hello   world  ";
        ParsedCommand cmd = parser.parse(input);
        
        assertEquals(input, cmd.getRawInput());
    }
    
    /**
     * Test parsing command with all argument types in complex order.
     */
    @Test
    void testParseCommandWithComplexArgumentOrder() {
        ParsedCommand cmd = parser.parse("/command pos1 --flag1 pos2 --name=value --flag2 pos3");
        
        assertTrue(cmd.isCommand());
        assertEquals("command", cmd.getCommandName());
        
        // Positional args
        assertEquals(3, cmd.getPositionalArgs().size());
        assertEquals("pos1", cmd.getPositionalArgs().get(0));
        assertEquals("pos2", cmd.getPositionalArgs().get(1));
        assertEquals("pos3", cmd.getPositionalArgs().get(2));
        
        // Named args
        assertEquals(1, cmd.getNamedArgs().size());
        assertEquals("value", cmd.getNamedArgs().get("name"));
        
        // Flags
        assertEquals(2, cmd.getFlags().size());
        assertTrue(cmd.getFlags().contains("flag1"));
        assertTrue(cmd.getFlags().contains("flag2"));
    }
    
    /**
     * Test parsing command with duplicate flags (last one wins).
     */
    @Test
    void testParseCommandWithDuplicateFlags() {
        ParsedCommand cmd = parser.parse("/list --verbose --all --verbose");
        
        assertTrue(cmd.isCommand());
        assertEquals("list", cmd.getCommandName());
        // Set should contain unique flags
        assertEquals(2, cmd.getFlags().size());
        assertTrue(cmd.getFlags().contains("verbose"));
        assertTrue(cmd.getFlags().contains("all"));
    }
    
    /**
     * Test parsing command with duplicate named args (last one wins).
     */
    @Test
    void testParseCommandWithDuplicateNamedArgs() {
        ParsedCommand cmd = parser.parse("/config --theme=dark --theme=light");
        
        assertTrue(cmd.isCommand());
        assertEquals("config", cmd.getCommandName());
        assertEquals(1, cmd.getNamedArgs().size());
        // Map should contain last value
        assertEquals("light", cmd.getNamedArgs().get("theme"));
    }
}
