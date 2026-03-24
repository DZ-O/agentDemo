package com.openbutler.cli.terminal.input;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CommandCompleter单元测试
 */
@ExtendWith(MockitoExtension.class)
class CommandCompleterTest {

    @Mock
    private LineReader lineReader;

    @Mock
    private ParsedLine parsedLine;

    private CommandCompleter commandCompleter;

    @BeforeEach
    void setUp() {
        commandCompleter = new CommandCompleter();
    }

    @Test
    void testComplete_WithCommandPrefix_ShouldSuggestMatchingCommands() {
        // Given
        when(parsedLine.word()).thenReturn("/ch");
        List<Candidate> candidates = new ArrayList<>();

        // When
        commandCompleter.complete(lineReader, parsedLine, candidates);

        // Then
        assertFalse(candidates.isEmpty(), "Should suggest commands starting with 'ch'");
        assertTrue(candidates.stream().anyMatch(c -> c.value().equals("/chat")),
                "Should suggest 'chat' command");
    }

    @Test
    void testComplete_WithFullCommandName_ShouldSuggestExactMatch() {
        // Given
        when(parsedLine.word()).thenReturn("/help");
        List<Candidate> candidates = new ArrayList<>();

        // When
        commandCompleter.complete(lineReader, parsedLine, candidates);

        // Then
        assertFalse(candidates.isEmpty(), "Should suggest exact match");
        assertTrue(candidates.stream().anyMatch(c -> c.value().equals("/help")),
                "Should suggest 'help' command");
    }

    @Test
    void testComplete_WithNoMatch_ShouldReturnEmptyList() {
        // Given
        when(parsedLine.word()).thenReturn("/xyz");
        List<Candidate> candidates = new ArrayList<>();

        // When
        commandCompleter.complete(lineReader, parsedLine, candidates);

        // Then
        assertTrue(candidates.isEmpty(), "Should not suggest any commands for non-matching prefix");
    }

    @Test
    void testComplete_WithoutCommandPrefix_ShouldNotSuggest() {
        // Given
        when(parsedLine.word()).thenReturn("chat");
        List<Candidate> candidates = new ArrayList<>();

        // When
        commandCompleter.complete(lineReader, parsedLine, candidates);

        // Then
        assertTrue(candidates.isEmpty(), 
                "Should not suggest commands without / prefix");
    }

    @Test
    void testComplete_WithEmptyPrefix_ShouldSuggestAllCommands() {
        // Given
        when(parsedLine.word()).thenReturn("/");
        List<Candidate> candidates = new ArrayList<>();

        // When
        commandCompleter.complete(lineReader, parsedLine, candidates);

        // Then
        assertFalse(candidates.isEmpty(), "Should suggest all commands");
        assertEquals(10, candidates.size(), "Should suggest all 10 builtin commands");
    }

    @Test
    void testComplete_ShouldCacheResults() {
        // Given
        when(parsedLine.word()).thenReturn("/ch");
        List<Candidate> candidates1 = new ArrayList<>();
        List<Candidate> candidates2 = new ArrayList<>();

        // When
        commandCompleter.complete(lineReader, parsedLine, candidates1);
        commandCompleter.complete(lineReader, parsedLine, candidates2);

        // Then
        assertEquals(candidates1.size(), candidates2.size(), 
                "Cached results should be consistent");
    }

    @Test
    void testGetAllCommands_ShouldReturnAllBuiltinCommands() {
        // When
        List<String> commands = commandCompleter.getAllCommands();

        // Then
        assertNotNull(commands, "Commands list should not be null");
        assertEquals(10, commands.size(), "Should have 10 builtin commands");
        assertTrue(commands.contains("chat"), "Should contain 'chat' command");
        assertTrue(commands.contains("new"), "Should contain 'new' command");
        assertTrue(commands.contains("list"), "Should contain 'list' command");
        assertTrue(commands.contains("switch"), "Should contain 'switch' command");
        assertTrue(commands.contains("history"), "Should contain 'history' command");
        assertTrue(commands.contains("clear"), "Should contain 'clear' command");
        assertTrue(commands.contains("config"), "Should contain 'config' command");
        assertTrue(commands.contains("help"), "Should contain 'help' command");
        assertTrue(commands.contains("version"), "Should contain 'version' command");
        assertTrue(commands.contains("exit"), "Should contain 'exit' command");
    }

    @Test
    void testClearCache_ShouldClearCompletionCache() {
        // Given
        when(parsedLine.word()).thenReturn("/ch");
        List<Candidate> candidates = new ArrayList<>();
        commandCompleter.complete(lineReader, parsedLine, candidates);

        // When
        commandCompleter.clearCache();

        // Then - should not throw and cache should be cleared
        assertDoesNotThrow(() -> commandCompleter.clearCache(),
                "Clearing cache should not throw");
    }

    @Test
    void testComplete_WithMultipleMatchingCommands() {
        // Given
        when(parsedLine.word()).thenReturn("/c");
        List<Candidate> candidates = new ArrayList<>();

        // When
        commandCompleter.complete(lineReader, parsedLine, candidates);

        // Then
        assertTrue(candidates.size() >= 3, 
                "Should suggest multiple commands starting with 'c'");
        assertTrue(candidates.stream().anyMatch(c -> c.value().equals("/chat")),
                "Should suggest 'chat'");
        assertTrue(candidates.stream().anyMatch(c -> c.value().equals("/clear")),
                "Should suggest 'clear'");
        assertTrue(candidates.stream().anyMatch(c -> c.value().equals("/config")),
                "Should suggest 'config'");
    }

    @Test
    void testComplete_CandidatesShouldHaveCorrectFormat() {
        // Given
        when(parsedLine.word()).thenReturn("/help");
        List<Candidate> candidates = new ArrayList<>();

        // When
        commandCompleter.complete(lineReader, parsedLine, candidates);

        // Then
        assertFalse(candidates.isEmpty(), "Should have candidates");
        Candidate candidate = candidates.get(0);
        assertTrue(candidate.value().startsWith("/"), 
                "Candidate value should start with /");
        assertFalse(candidate.displ().startsWith("/"), 
                "Candidate display should not start with /");
    }

    @Test
    void testComplete_WithCaseSensitivity() {
        // Given - uppercase prefix
        when(parsedLine.word()).thenReturn("/CH");
        List<Candidate> candidates = new ArrayList<>();

        // When
        commandCompleter.complete(lineReader, parsedLine, candidates);

        // Then - should not match (case sensitive)
        assertTrue(candidates.isEmpty(), 
                "Should not match with different case (completer is case sensitive)");
    }
}
