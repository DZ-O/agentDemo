package com.openbutler.cli.terminal.rendering;

import com.openbutler.cli.terminal.model.ColorScheme;
import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AnsiTerminalRenderer.
 * 
 * Tests verify that the renderer correctly applies ANSI color codes and formats
 * messages according to their type.
 * 
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 7.9, 9.1, 9.2
 */
class AnsiTerminalRendererTest {
    
    private AnsiTerminalRenderer renderer;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        renderer = new AnsiTerminalRenderer();
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }
    
    /**
     * Test that renderPrompt displays session name with cyan color.
     * Validates: Requirement 7.9
     */
    @Test
    void testRenderPromptDisplaysSessionName() {
        String sessionName = "test-session";
        renderer.renderPrompt(sessionName);
        
        String output = outputStream.toString();
        assertTrue(output.contains(sessionName), "Prompt should contain session name");
        assertTrue(output.contains(">"), "Prompt should contain '>' character");
    }
    
    /**
     * Test that renderPrompt applies cyan color code.
     * Validates: Requirements 3.1, 7.9
     */
    @Test
    void testRenderPromptAppliesCyanColor() {
        renderer.renderPrompt("session");
        
        String output = outputStream.toString();
        // Jansi uses ANSI color codes
        assertTrue(output.contains("\u001B["), "Output should contain ANSI escape codes");
    }
    
    /**
     * Test that renderAIResponse applies green color.
     * Validates: Requirements 3.1, 3.3
     */
    @Test
    void testRenderAIResponseAppliesGreenColor() {
        String aiMessage = "Hello from AI";
        renderer.renderAIResponse(aiMessage);
        
        String output = outputStream.toString();
        assertTrue(output.contains(aiMessage), "Output should contain AI message");
        assertTrue(output.contains("\u001B[32m"), "Output should contain green ANSI code");
    }
    
    /**
     * Test that renderAIResponse supports streaming (no newline).
     * Validates: Requirement 3.3
     */
    @Test
    void testRenderAIResponseSupportsStreaming() {
        renderer.renderAIResponse("chunk1");
        renderer.renderAIResponse("chunk2");
        
        String output = outputStream.toString();
        assertTrue(output.contains("chunk1"), "Output should contain first chunk");
        assertTrue(output.contains("chunk2"), "Output should contain second chunk");
        // Should not have newlines between chunks
        assertFalse(output.contains("chunk1\nchunk2"), "Chunks should not be separated by newline");
    }
    
    /**
     * Test that renderError applies red color.
     * Validates: Requirements 3.1, 3.4, 9.1
     */
    @Test
    void testRenderErrorAppliesRedColor() {
        String errorMessage = "Something went wrong";
        renderer.renderError(errorMessage);
        
        String output = outputStream.toString();
        assertTrue(output.contains(errorMessage), "Output should contain error message");
        assertTrue(output.contains("[ERROR]"), "Output should contain ERROR prefix");
        // Check for red color in Jansi format
        assertTrue(output.contains("\u001B["), "Output should contain ANSI escape codes");
    }
    
    /**
     * Test that renderSuccess applies green color.
     * Validates: Requirements 3.1, 9.2
     */
    @Test
    void testRenderSuccessAppliesGreenColor() {
        String successMessage = "Operation completed";
        renderer.renderSuccess(successMessage);
        
        String output = outputStream.toString();
        assertTrue(output.contains(successMessage), "Output should contain success message");
        assertTrue(output.contains("[SUCCESS]"), "Output should contain SUCCESS prefix");
        assertTrue(output.contains("\u001B["), "Output should contain ANSI escape codes");
    }
    
    /**
     * Test that renderWarning applies yellow color.
     * Validates: Requirements 3.1, 3.5
     */
    @Test
    void testRenderWarningAppliesYellowColor() {
        String warningMessage = "This is a warning";
        renderer.renderWarning(warningMessage);
        
        String output = outputStream.toString();
        assertTrue(output.contains(warningMessage), "Output should contain warning message");
        assertTrue(output.contains("[WARNING]"), "Output should contain WARNING prefix");
        assertTrue(output.contains("\u001B["), "Output should contain ANSI escape codes");
    }
    
    /**
     * Test that renderSystem applies gray color.
     * Validates: Requirements 3.1, 3.6
     */
    @Test
    void testRenderSystemAppliesGrayColor() {
        String systemMessage = "System notification";
        renderer.renderSystem(systemMessage);
        
        String output = outputStream.toString();
        assertTrue(output.contains(systemMessage), "Output should contain system message");
        assertTrue(output.contains("\u001B[90m"), "Output should contain gray ANSI code");
    }
    
    /**
     * Test that clear method outputs ANSI clear screen code.
     */
    @Test
    void testClearOutputsAnsiClearCode() {
        renderer.clear();
        
        String output = outputStream.toString();
        assertTrue(output.contains("\u001B["), "Clear should output ANSI escape codes");
    }
    
    /**
     * Test that custom color scheme is applied.
     * Validates: Requirement 3.1
     */
    @Test
    void testCustomColorSchemeIsApplied() {
        ColorScheme customScheme = ColorScheme.builder()
                .aiColor("\u001B[35m")  // Magenta
                .systemColor("\u001B[36m")  // Cyan
                .build();
        
        AnsiTerminalRenderer customRenderer = new AnsiTerminalRenderer(customScheme);
        System.setOut(new PrintStream(outputStream));
        
        customRenderer.renderAIResponse("test");
        String output = outputStream.toString();
        assertTrue(output.contains("\u001B[35m"), "Should use custom AI color (magenta)");
    }
    
    /**
     * Test that all message types include ANSI reset code.
     * Validates: Requirement 3.1
     */
    @Test
    void testAllMessagesIncludeResetCode() {
        renderer.renderAIResponse("ai");
        renderer.renderSystem("system");
        
        String output = outputStream.toString();
        assertTrue(output.contains("\u001B[0m"), "Output should contain ANSI reset code");
    }
    
    /**
     * Test that createProgressBar returns a valid ProgressBar instance.
     * Validates: Requirement 3.9
     */
    @Test
    void testCreateProgressBarReturnsValidInstance() {
        ProgressBar progressBar = renderer.createProgressBar("Test Task", 100);
        
        assertNotNull(progressBar, "createProgressBar should return a non-null instance");
        assertEquals(0, progressBar.getCurrent(), "Initial progress should be 0");
        assertEquals(100, progressBar.getMax(), "Max should be set to 100");
        
        progressBar.close();
    }
    
    /**
     * Test that createProgressBar with different parameters works correctly.
     * Validates: Requirement 3.9
     */
    @Test
    void testCreateProgressBarWithDifferentParameters() {
        ProgressBar progressBar1 = renderer.createProgressBar("Task 1", 50);
        ProgressBar progressBar2 = renderer.createProgressBar("Task 2", 200);
        
        assertNotNull(progressBar1);
        assertNotNull(progressBar2);
        assertEquals(50, progressBar1.getMax());
        assertEquals(200, progressBar2.getMax());
        
        progressBar1.close();
        progressBar2.close();
    }
    
    /**
     * Test that wrapText wraps long text without breaking words.
     * Validates: Requirement 3.10
     */
    @Test
    void testWrapTextWrapsLongText() {
        AnsiTerminalRenderer rendererWithWidth = new AnsiTerminalRenderer(ColorScheme.DEFAULT, 20);
        String longText = "This is a very long sentence that needs to be wrapped";
        String wrapped = rendererWithWidth.wrapText(longText);
        
        assertNotNull(wrapped, "Wrapped text should not be null");
        assertTrue(wrapped.contains("\n"), "Long text should be wrapped with newlines");
        
        // Verify no line exceeds width
        String[] lines = wrapped.split("\n");
        for (String line : lines) {
            assertTrue(line.length() <= 20, 
                    "No line should exceed terminal width: " + line);
        }
    }
    
    /**
     * Test that wrapText preserves content.
     * Validates: Requirement 3.10
     */
    @Test
    void testWrapTextPreservesContent() {
        AnsiTerminalRenderer rendererWithWidth = new AnsiTerminalRenderer(ColorScheme.DEFAULT, 30);
        String text = "The quick brown fox jumps over the lazy dog";
        String wrapped = rendererWithWidth.wrapText(text);
        
        // Remove newlines and verify all words are present
        String unwrapped = wrapped.replace("\n", " ");
        assertTrue(unwrapped.contains("quick"));
        assertTrue(unwrapped.contains("brown"));
        assertTrue(unwrapped.contains("fox"));
        assertTrue(unwrapped.contains("lazy"));
        assertTrue(unwrapped.contains("dog"));
    }
    
    /**
     * Test that wrapText doesn't break words.
     * Validates: Requirement 3.10
     */
    @Test
    void testWrapTextDoesNotBreakWords() {
        AnsiTerminalRenderer rendererWithWidth = new AnsiTerminalRenderer(ColorScheme.DEFAULT, 15);
        String text = "Hello beautiful world";
        String wrapped = rendererWithWidth.wrapText(text);
        
        // "beautiful" should not be broken
        assertTrue(wrapped.contains("beautiful"), 
                "Words should not be broken across lines");
        assertFalse(wrapped.contains("beaut\niful"));
    }
}
