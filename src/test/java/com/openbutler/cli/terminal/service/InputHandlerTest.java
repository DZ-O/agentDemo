package com.openbutler.cli.terminal.service;

import com.openbutler.cli.terminal.input.LineReaderFactory;
import com.openbutler.cli.terminal.model.ParsedCommand;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InputHandler service.
 * 
 * Tests input reading, multi-line input handling, interrupt handling,
 * and EOF handling.
 * 
 * Validates: Requirements 7.3, 7.5, 10.1
 */
@ExtendWith(MockitoExtension.class)
class InputHandlerTest {
    
    @Mock
    private LineReaderFactory lineReaderFactory;
    
    @Mock
    private CommandParser commandParser;
    
    @Mock
    private LineReader lineReader;
    
    private InputHandler inputHandler;
    
    @BeforeEach
    void setUp() {
        inputHandler = new InputHandler(lineReaderFactory, commandParser);
        
        // Setup default mock behavior with lenient stubbing
        lenient().when(lineReaderFactory.createLineReader(anyString())).thenReturn(lineReader);
        lenient().when(lineReaderFactory.generatePrompt(anyString())).thenReturn("test > ");
    }
    
    // ========== Basic Input Reading Tests ==========
    
    /**
     * Test reading simple single-line input.
     * Validates: Requirement 7.3
     */
    @Test
    void testReadInput_SimpleSingleLine() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenReturn("hello world");
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertEquals("hello world", result);
        verify(lineReaderFactory).createLineReader(sessionName);
        verify(lineReaderFactory).generatePrompt(sessionName);
        verify(lineReader).readLine("test > ");
    }
    
    /**
     * Test reading command input.
     * Validates: Requirement 7.3
     */
    @Test
    void testReadInput_CommandInput() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenReturn("/help");
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertEquals("/help", result);
    }
    
    /**
     * Test reading input with leading/trailing whitespace.
     * Validates: Requirement 7.3
     */
    @Test
    void testReadInput_WithWhitespace() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenReturn("  hello world  ");
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertEquals("hello world", result); // Should be trimmed
    }
    
    /**
     * Test reading empty input.
     * Validates: Requirement 7.3
     */
    @Test
    void testReadInput_EmptyInput() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenReturn("");
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertEquals("", result);
    }
    
    // ========== Multi-line Input Tests ==========
    
    /**
     * Test reading multi-line input with backslash continuation.
     * Validates: Requirement 7.5
     */
    @Test
    void testReadInput_MultiLineWithBackslash() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenReturn("hello \\");
        when(lineReader.readLine("... ")).thenReturn("world");
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertEquals("hello world", result);
        verify(lineReader).readLine("test > ");
        verify(lineReader).readLine("... ");
    }
    
    /**
     * Test reading multi-line input with multiple continuations.
     * Validates: Requirement 7.5
     */
    @Test
    void testReadInput_MultipleLineContinuations() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenReturn("line1 \\");
        when(lineReader.readLine("... "))
            .thenReturn("line2 \\")
            .thenReturn("line3");
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertEquals("line1 line2 line3", result);
        verify(lineReader).readLine("test > ");
        verify(lineReader, times(2)).readLine("... ");
    }
    
    /**
     * Test reading multi-line command input.
     * Validates: Requirement 7.5
     */
    @Test
    void testReadInput_MultiLineCommand() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenReturn("/chat hello \\");
        when(lineReader.readLine("... ")).thenReturn("world");
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertEquals("/chat hello world", result);
    }
    
    /**
     * Test that backslash not at end of line is preserved.
     * Validates: Requirement 7.5
     */
    @Test
    void testReadInput_BackslashInMiddle() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenReturn("hello\\world");
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertEquals("hello\\world", result);
        verify(lineReader, times(1)).readLine(anyString()); // Only one read
    }
    
    /**
     * Test EOF during multi-line input.
     * Validates: Requirement 7.5
     */
    @Test
    void testReadInput_EOFDuringMultiLine() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenReturn("hello \\");
        when(lineReader.readLine("... ")).thenReturn(null); // EOF
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertEquals("hello", result); // Returns what was read before EOF
    }
    
    // ========== Interrupt Handling Tests ==========
    
    /**
     * Test handling Ctrl+C interrupt.
     * Validates: Requirement 7.3
     */
    @Test
    void testReadInput_UserInterrupt() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenThrow(new UserInterruptException("Ctrl+C"));
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertNull(result); // Should return null on interrupt
    }
    
    /**
     * Test handling Ctrl+C during multi-line input.
     * Validates: Requirement 7.3
     */
    @Test
    void testReadInput_UserInterruptDuringMultiLine() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenReturn("hello \\");
        when(lineReader.readLine("... ")).thenThrow(new UserInterruptException("Ctrl+C"));
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertNull(result); // Should return null on interrupt
    }
    
    /**
     * Test isExitSignal returns true for null input.
     * Validates: Requirement 7.3
     */
    @Test
    void testIsExitSignal_NullInput() {
        // When/Then
        assertTrue(inputHandler.isExitSignal(null));
    }
    
    /**
     * Test isExitSignal returns false for non-null input.
     * Validates: Requirement 7.3
     */
    @Test
    void testIsExitSignal_NonNullInput() {
        // When/Then
        assertFalse(inputHandler.isExitSignal("hello"));
        assertFalse(inputHandler.isExitSignal(""));
        assertFalse(inputHandler.isExitSignal("/exit"));
    }
    
    // ========== EOF Handling Tests ==========
    
    /**
     * Test handling Ctrl+D (EOF).
     * Validates: Requirement 7.4
     */
    @Test
    void testReadInput_EndOfFile() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenThrow(new EndOfFileException());
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertNull(result); // Should return null on EOF
    }
    
    /**
     * Test handling null return from readLine (EOF).
     * Validates: Requirement 7.4
     */
    @Test
    void testReadInput_NullFromReadLine() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenReturn(null);
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertNull(result);
    }
    
    // ========== Command Parsing Integration Tests ==========
    
    /**
     * Test readAndParseCommand with simple command.
     * Validates: Requirement 10.1
     */
    @Test
    void testReadAndParseCommand_SimpleCommand() {
        // Given
        String sessionName = "test-session";
        String input = "/help";
        ParsedCommand expectedCommand = ParsedCommand.builder()
                .isCommand(true)
                .commandName("help")
                .rawInput(input)
                .build();
        
        when(lineReader.readLine("test > ")).thenReturn(input);
        when(commandParser.parse(input)).thenReturn(expectedCommand);
        
        // When
        ParsedCommand result = inputHandler.readAndParseCommand(sessionName);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isCommand());
        assertEquals("help", result.getCommandName());
        verify(commandParser).parse(input);
    }
    
    /**
     * Test readAndParseCommand with chat message.
     * Validates: Requirement 10.1
     */
    @Test
    void testReadAndParseCommand_ChatMessage() {
        // Given
        String sessionName = "test-session";
        String input = "hello world";
        ParsedCommand expectedCommand = ParsedCommand.builder()
                .isCommand(false)
                .rawInput(input)
                .build();
        
        when(lineReader.readLine("test > ")).thenReturn(input);
        when(commandParser.parse(input)).thenReturn(expectedCommand);
        
        // When
        ParsedCommand result = inputHandler.readAndParseCommand(sessionName);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isCommand());
        verify(commandParser).parse(input);
    }
    
    /**
     * Test readAndParseCommand with multi-line input.
     * Validates: Requirements 7.5, 10.1
     */
    @Test
    void testReadAndParseCommand_MultiLineInput() {
        // Given
        String sessionName = "test-session";
        String combinedInput = "hello world";
        ParsedCommand expectedCommand = ParsedCommand.builder()
                .isCommand(false)
                .rawInput(combinedInput)
                .build();
        
        when(lineReader.readLine("test > ")).thenReturn("hello \\");
        when(lineReader.readLine("... ")).thenReturn("world");
        when(commandParser.parse(combinedInput)).thenReturn(expectedCommand);
        
        // When
        ParsedCommand result = inputHandler.readAndParseCommand(sessionName);
        
        // Then
        assertNotNull(result);
        verify(commandParser).parse(combinedInput);
    }
    
    /**
     * Test readAndParseCommand returns null on interrupt.
     * Validates: Requirements 7.3, 10.1
     */
    @Test
    void testReadAndParseCommand_UserInterrupt() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenThrow(new UserInterruptException("Ctrl+C"));
        
        // When
        ParsedCommand result = inputHandler.readAndParseCommand(sessionName);
        
        // Then
        assertNull(result);
        verify(commandParser, never()).parse(anyString());
    }
    
    /**
     * Test readAndParseCommand returns null on EOF.
     * Validates: Requirements 7.4, 10.1
     */
    @Test
    void testReadAndParseCommand_EndOfFile() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenThrow(new EndOfFileException());
        
        // When
        ParsedCommand result = inputHandler.readAndParseCommand(sessionName);
        
        // Then
        assertNull(result);
        verify(commandParser, never()).parse(anyString());
    }
    
    // ========== Session Name Tests ==========
    
    /**
     * Test that session name is passed to LineReaderFactory.
     * Validates: Requirement 7.9
     */
    @Test
    void testReadInput_UsesSessionName() {
        // Given
        String sessionName = "my-project";
        when(lineReader.readLine(anyString())).thenReturn("test");
        
        // When
        inputHandler.readInput(sessionName);
        
        // Then
        verify(lineReaderFactory).createLineReader(sessionName);
        verify(lineReaderFactory).generatePrompt(sessionName);
    }
    
    /**
     * Test with different session names.
     * Validates: Requirement 7.9
     */
    @Test
    void testReadInput_DifferentSessionNames() {
        // Given
        String[] sessionNames = {"default", "project-1", "会话", "session_123"};
        when(lineReader.readLine(anyString())).thenReturn("test");
        
        for (String sessionName : sessionNames) {
            // When
            inputHandler.readInput(sessionName);
            
            // Then
            verify(lineReaderFactory).createLineReader(sessionName);
            verify(lineReaderFactory).generatePrompt(sessionName);
        }
    }
    
    // ========== Edge Cases ==========
    
    /**
     * Test reading input with only whitespace.
     */
    @Test
    void testReadInput_OnlyWhitespace() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenReturn("   ");
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertEquals("", result); // Should be trimmed to empty
    }
    
    /**
     * Test reading very long input.
     */
    @Test
    void testReadInput_VeryLongInput() {
        // Given
        String sessionName = "test-session";
        String longInput = "a".repeat(10000);
        when(lineReader.readLine("test > ")).thenReturn(longInput);
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertEquals(longInput, result);
    }
    
    /**
     * Test reading input with special characters.
     */
    @Test
    void testReadInput_SpecialCharacters() {
        // Given
        String sessionName = "test-session";
        String specialInput = "Hello! @#$%^&*() 你好 🎉";
        when(lineReader.readLine("test > ")).thenReturn(specialInput);
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertEquals(specialInput, result);
    }
    
    /**
     * Test reading input with newlines (should not happen in single line, but test anyway).
     */
    @Test
    void testReadInput_WithNewlines() {
        // Given
        String sessionName = "test-session";
        String inputWithNewlines = "hello\nworld";
        when(lineReader.readLine("test > ")).thenReturn(inputWithNewlines);
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        assertEquals(inputWithNewlines, result);
    }
    
    /**
     * Test multiple consecutive backslashes at end.
     */
    @Test
    void testReadInput_MultipleBackslashesAtEnd() {
        // Given
        String sessionName = "test-session";
        when(lineReader.readLine("test > ")).thenReturn("hello \\\\");
        lenient().when(lineReader.readLine("... ")).thenReturn("world");
        
        // When
        String result = inputHandler.readInput(sessionName);
        
        // Then
        // Double backslash at end should NOT trigger continuation
        assertEquals("hello \\\\", result);
        verify(lineReader, times(1)).readLine("test > ");
        verify(lineReader, never()).readLine("... ");
    }
    
    /**
     * Test that InputHandler can be created with dependencies.
     */
    @Test
    void testInputHandler_Creation() {
        // When
        InputHandler handler = new InputHandler(lineReaderFactory, commandParser);
        
        // Then
        assertNotNull(handler);
    }
    
    /**
     * Test reading input with null session name (edge case).
     */
    @Test
    void testReadInput_NullSessionName() {
        // Given
        when(lineReaderFactory.createLineReader(null)).thenReturn(lineReader);
        when(lineReaderFactory.generatePrompt(null)).thenReturn("test > ");
        when(lineReader.readLine("test > ")).thenReturn("test");
        
        // When
        String result = inputHandler.readInput(null);
        
        // Then
        assertEquals("test", result);
        verify(lineReaderFactory).createLineReader(null);
    }
    
    /**
     * Test reading input with empty session name.
     */
    @Test
    void testReadInput_EmptySessionName() {
        // Given
        when(lineReader.readLine(anyString())).thenReturn("test");
        
        // When
        String result = inputHandler.readInput("");
        
        // Then
        assertEquals("test", result);
        verify(lineReaderFactory).createLineReader("");
    }
}
