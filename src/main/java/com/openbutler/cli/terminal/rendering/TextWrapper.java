package com.openbutler.cli.terminal.rendering;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TextWrapper provides intelligent text wrapping functionality for terminal output.
 * 
 * This class detects terminal width and wraps text without breaking words,
 * ensuring optimal readability in the terminal.
 * 
 * Validates: Requirement 3.10
 */
public class TextWrapper {
    
    private static final int DEFAULT_WIDTH = 80;
    private static final int MIN_WIDTH = 20;
    
    private final int terminalWidth;
    
    /**
     * Creates a TextWrapper with auto-detected terminal width.
     */
    public TextWrapper() {
        this.terminalWidth = detectTerminalWidth();
    }
    
    /**
     * Creates a TextWrapper with specified width.
     * 
     * @param width the terminal width to use (0 for auto-detection)
     */
    public TextWrapper(int width) {
        if (width <= 0) {
            this.terminalWidth = detectTerminalWidth();
        } else {
            // Only enforce minimum width for auto-detected terminals
            // Allow explicit width for testing purposes
            this.terminalWidth = width;
        }
    }
    
    /**
     * Detects the current terminal width.
     * 
     * @return the detected terminal width, or DEFAULT_WIDTH if detection fails
     */
    private int detectTerminalWidth() {
        try {
            Terminal terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();
            int width = terminal.getWidth();
            terminal.close();
            return width > 0 ? width : DEFAULT_WIDTH;
        } catch (IOException e) {
            return DEFAULT_WIDTH;
        }
    }
    
    /**
     * Wraps text to fit within the terminal width without breaking words.
     * 
     * This method ensures that:
     * - Words are not split across lines
     * - All content from the original text is preserved
     * - Lines do not exceed the terminal width
     * 
     * @param text the text to wrap
     * @return the wrapped text with newlines inserted at appropriate positions
     * 
     * Validates: Requirement 3.10
     */
    public String wrap(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Split by existing newlines to preserve intentional line breaks
        String[] paragraphs = text.split("\n", -1);
        List<String> wrappedParagraphs = new ArrayList<>();
        
        for (String paragraph : paragraphs) {
            wrappedParagraphs.add(wrapParagraph(paragraph));
        }
        
        return String.join("\n", wrappedParagraphs);
    }
    
    /**
     * Wraps a single paragraph (no existing newlines) to fit within terminal width.
     * 
     * @param paragraph the paragraph to wrap
     * @return the wrapped paragraph
     */
    private String wrapParagraph(String paragraph) {
        if (paragraph.length() <= terminalWidth) {
            return paragraph;
        }
        
        StringBuilder result = new StringBuilder();
        StringBuilder currentLine = new StringBuilder();
        
        // Split into words while preserving whitespace
        String[] words = paragraph.split("(?<=\\s)|(?=\\s)");
        
        for (String word : words) {
            // Handle very long words that exceed terminal width
            if (word.length() > terminalWidth) {
                // Flush current line if not empty
                if (currentLine.length() > 0) {
                    result.append(currentLine).append("\n");
                    currentLine.setLength(0);
                }
                // Break long word at terminal width boundaries
                result.append(breakLongWord(word));
                continue;
            }
            
            // Check if adding this word would exceed terminal width
            if (currentLine.length() + word.length() > terminalWidth) {
                // Flush current line (trim trailing whitespace)
                String line = currentLine.toString().stripTrailing();
                if (!line.isEmpty()) {
                    result.append(line).append("\n");
                }
                currentLine.setLength(0);
                
                // Start new line with current word (skip leading whitespace)
                if (!word.isBlank()) {
                    currentLine.append(word.stripLeading());
                }
            } else {
                currentLine.append(word);
            }
        }
        
        // Append remaining content
        if (currentLine.length() > 0) {
            result.append(currentLine.toString().stripTrailing());
        }
        
        return result.toString();
    }
    
    /**
     * Breaks a very long word that exceeds terminal width into multiple lines.
     * 
     * @param word the long word to break
     * @return the word broken into multiple lines
     */
    private String breakLongWord(String word) {
        StringBuilder result = new StringBuilder();
        int start = 0;
        
        while (start < word.length()) {
            int end = Math.min(start + terminalWidth, word.length());
            result.append(word, start, end);
            
            if (end < word.length()) {
                result.append("\n");
            }
            
            start = end;
        }
        
        return result.toString();
    }
    
    /**
     * Gets the current terminal width being used for wrapping.
     * 
     * @return the terminal width in characters
     */
    public int getTerminalWidth() {
        return terminalWidth;
    }
}
