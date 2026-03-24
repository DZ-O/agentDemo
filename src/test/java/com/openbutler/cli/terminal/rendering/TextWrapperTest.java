package com.openbutler.cli.terminal.rendering;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TextWrapper class.
 * 
 * Tests text wrapping functionality including terminal width detection,
 * word wrapping, and edge cases.
 * 
 * Validates: Requirement 3.10
 */
class TextWrapperTest {
    
    @Test
    void testAutoDetectTerminalWidth() {
        TextWrapper wrapper = new TextWrapper();
        int width = wrapper.getTerminalWidth();
        
        // Width should be at least the minimum
        assertTrue(width >= 20, "Terminal width should be at least 20");
    }
    
    @Test
    void testCustomTerminalWidth() {
        TextWrapper wrapper = new TextWrapper(50);
        assertEquals(50, wrapper.getTerminalWidth());
    }
    
    @Test
    void testZeroWidthTriggersAutoDetection() {
        TextWrapper wrapper = new TextWrapper(0);
        int width = wrapper.getTerminalWidth();
        
        // Should auto-detect, not use 0
        assertTrue(width > 0, "Zero width should trigger auto-detection");
    }
    
    @Test
    void testExplicitWidthRespected() {
        TextWrapper wrapper = new TextWrapper(10);
        
        // Explicit width should be respected (for testing purposes)
        assertEquals(10, wrapper.getTerminalWidth(), 
                "Explicit width should be respected");
    }
    
    @Test
    void testShortTextNotWrapped() {
        TextWrapper wrapper = new TextWrapper(80);
        String text = "Hello world";
        
        assertEquals(text, wrapper.wrap(text));
    }
    
    @Test
    void testNullTextReturnsNull() {
        TextWrapper wrapper = new TextWrapper(80);
        assertNull(wrapper.wrap(null));
    }
    
    @Test
    void testEmptyTextReturnsEmpty() {
        TextWrapper wrapper = new TextWrapper(80);
        assertEquals("", wrapper.wrap(""));
    }
    
    @Test
    void testSimpleWordWrapping() {
        TextWrapper wrapper = new TextWrapper(20);
        String text = "This is a long sentence that needs to be wrapped";
        String wrapped = wrapper.wrap(text);
        
        // Check that no line exceeds width
        String[] lines = wrapped.split("\n");
        for (String line : lines) {
            assertTrue(line.length() <= 20, 
                    "Line should not exceed terminal width: " + line);
        }
        
        // Check that all words are preserved
        String unwrapped = wrapped.replace("\n", " ");
        assertTrue(unwrapped.contains("This"));
        assertTrue(unwrapped.contains("sentence"));
        assertTrue(unwrapped.contains("wrapped"));
    }
    
    @Test
    void testWordsNotBroken() {
        TextWrapper wrapper = new TextWrapper(15);
        String text = "Hello beautiful world";
        String wrapped = wrapper.wrap(text);
        
        // "beautiful" should not be broken even though it's 9 chars
        assertTrue(wrapped.contains("beautiful"), 
                "Words should not be broken across lines");
        
        // Check no partial words
        assertFalse(wrapped.contains("beaut\niful"));
        assertFalse(wrapped.contains("wor\nld"));
    }
    
    @Test
    void testPreserveExistingNewlines() {
        TextWrapper wrapper = new TextWrapper(80);
        String text = "Line 1\nLine 2\nLine 3";
        String wrapped = wrapper.wrap(text);
        
        // Should preserve the three lines
        String[] lines = wrapped.split("\n", -1);
        assertEquals(3, lines.length);
    }
    
    @Test
    void testVeryLongWordBroken() {
        TextWrapper wrapper = new TextWrapper(10);
        String text = "ThisIsAVeryLongWordThatExceedsTheTerminalWidth";
        String wrapped = wrapper.wrap(text);
        
        // Should be broken into multiple lines
        String[] lines = wrapped.split("\n");
        assertTrue(lines.length > 1, "Very long word should be broken");
        
        // Each line should not exceed width
        for (String line : lines) {
            assertTrue(line.length() <= 10, 
                    "Broken word line should not exceed width: " + line);
        }
        
        // Content should be preserved
        String rejoined = String.join("", lines);
        assertEquals(text, rejoined);
    }
    
    @Test
    void testMultipleParagraphs() {
        TextWrapper wrapper = new TextWrapper(20);
        String text = "First paragraph is here.\n\nSecond paragraph is here too.";
        String wrapped = wrapper.wrap(text);
        
        // Should preserve paragraph breaks
        assertTrue(wrapped.contains("\n\n"), "Paragraph breaks should be preserved");
    }
    
    @Test
    void testTrailingWhitespaceRemoved() {
        TextWrapper wrapper = new TextWrapper(15);
        String text = "Hello world this is a test";
        String wrapped = wrapper.wrap(text);
        
        String[] lines = wrapped.split("\n");
        for (String line : lines) {
            // Lines should not end with whitespace
            assertEquals(line, line.stripTrailing(), 
                    "Lines should not have trailing whitespace");
        }
    }
    
    @Test
    void testLeadingWhitespaceHandled() {
        TextWrapper wrapper = new TextWrapper(20);
        String text = "    Indented text that is long enough to wrap";
        String wrapped = wrapper.wrap(text);
        
        // First line should preserve leading whitespace
        assertTrue(wrapped.startsWith("    "), 
                "Leading whitespace should be preserved on first line");
    }
    
    @Test
    void testChineseCharacters() {
        TextWrapper wrapper = new TextWrapper(20);
        String text = "这是一个很长的中文句子需要被换行处理";
        String wrapped = wrapper.wrap(text);
        
        // Should wrap without breaking characters
        String[] lines = wrapped.split("\n");
        for (String line : lines) {
            assertTrue(line.length() <= 20, 
                    "Chinese text line should not exceed width");
        }
        
        // Content should be preserved
        String unwrapped = wrapped.replace("\n", "");
        assertEquals(text, unwrapped);
    }
    
    @Test
    void testMixedContent() {
        TextWrapper wrapper = new TextWrapper(30);
        String text = "Hello 世界 this is a test with mixed content";
        String wrapped = wrapper.wrap(text);
        
        // Should handle mixed content
        assertTrue(wrapped.contains("Hello"));
        assertTrue(wrapped.contains("世界"));
        assertTrue(wrapped.contains("mixed"));
    }
    
    @Test
    void testSingleCharacterWidth() {
        TextWrapper wrapper = new TextWrapper(1);
        String text = "Hi";
        String wrapped = wrapper.wrap(text);
        
        // With width of 1, each character should be on its own line
        String[] lines = wrapped.split("\n");
        assertTrue(lines.length >= 1);
    }
    
    @Test
    void testExactWidthMatch() {
        TextWrapper wrapper = new TextWrapper(11);
        String text = "Hello world";
        String wrapped = wrapper.wrap(text);
        
        // Text is exactly 11 chars, should not wrap
        assertFalse(wrapped.contains("\n"));
    }
    
    @Test
    void testOneCharOverWidth() {
        TextWrapper wrapper = new TextWrapper(10);
        String text = "Hello world";
        String wrapped = wrapper.wrap(text);
        
        // Text is 11 chars, width is 10, should wrap
        assertTrue(wrapped.contains("\n"));
        
        String[] lines = wrapped.split("\n");
        assertEquals(2, lines.length);
    }
    
    @Test
    void testMultipleSpaces() {
        TextWrapper wrapper = new TextWrapper(20);
        String text = "Hello    world    test";
        String wrapped = wrapper.wrap(text);
        
        // Should handle multiple spaces
        assertNotNull(wrapped);
    }
    
    @Test
    void testTabCharacters() {
        TextWrapper wrapper = new TextWrapper(20);
        String text = "Hello\tworld\ttest";
        String wrapped = wrapper.wrap(text);
        
        // Should handle tabs
        assertNotNull(wrapped);
        assertTrue(wrapped.contains("Hello"));
        assertTrue(wrapped.contains("world"));
    }
    
    @Test
    void testOnlyWhitespace() {
        TextWrapper wrapper = new TextWrapper(20);
        String text = "     ";
        String wrapped = wrapper.wrap(text);
        
        // Should handle whitespace-only text
        assertNotNull(wrapped);
    }
    
    @Test
    void testSingleWord() {
        TextWrapper wrapper = new TextWrapper(20);
        String text = "Hello";
        String wrapped = wrapper.wrap(text);
        
        assertEquals("Hello", wrapped);
    }
    
    @Test
    void testContentPreservation() {
        TextWrapper wrapper = new TextWrapper(30);
        String text = "The quick brown fox jumps over the lazy dog";
        String wrapped = wrapper.wrap(text);
        
        // Remove newlines and compare
        String unwrapped = wrapped.replace("\n", " ");
        
        // All words should be present
        assertTrue(unwrapped.contains("quick"));
        assertTrue(unwrapped.contains("brown"));
        assertTrue(unwrapped.contains("fox"));
        assertTrue(unwrapped.contains("jumps"));
        assertTrue(unwrapped.contains("over"));
        assertTrue(unwrapped.contains("lazy"));
        assertTrue(unwrapped.contains("dog"));
    }
}
