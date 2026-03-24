package com.openbutler.cli.terminal.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ColorScheme model class.
 * 
 * Tests the color properties and DEFAULT theme configuration.
 */
class ColorSchemeTest {
    
    @Test
    void testColorSchemeBuilder() {
        // Given
        String userColor = "\u001B[34m";
        String aiColor = "\u001B[32m";
        String errorColor = "\u001B[31m";
        String warningColor = "\u001B[33m";
        String systemColor = "\u001B[90m";
        String successColor = "\u001B[32m";
        String promptColor = "\u001B[36m";
        
        // When
        ColorScheme scheme = ColorScheme.builder()
            .userColor(userColor)
            .aiColor(aiColor)
            .errorColor(errorColor)
            .warningColor(warningColor)
            .systemColor(systemColor)
            .successColor(successColor)
            .promptColor(promptColor)
            .build();
        
        // Then
        assertEquals(userColor, scheme.getUserColor());
        assertEquals(aiColor, scheme.getAiColor());
        assertEquals(errorColor, scheme.getErrorColor());
        assertEquals(warningColor, scheme.getWarningColor());
        assertEquals(systemColor, scheme.getSystemColor());
        assertEquals(successColor, scheme.getSuccessColor());
        assertEquals(promptColor, scheme.getPromptColor());
    }
    
    @Test
    void testDefaultColorSchemeExists() {
        // When
        ColorScheme defaultScheme = ColorScheme.DEFAULT;
        
        // Then
        assertNotNull(defaultScheme);
    }
    
    @Test
    void testDefaultColorSchemeHasUserColor() {
        // When
        ColorScheme defaultScheme = ColorScheme.DEFAULT;
        
        // Then
        assertNotNull(defaultScheme.getUserColor());
        assertEquals("\u001B[34m", defaultScheme.getUserColor()); // Blue
    }
    
    @Test
    void testDefaultColorSchemeHasAiColor() {
        // When
        ColorScheme defaultScheme = ColorScheme.DEFAULT;
        
        // Then
        assertNotNull(defaultScheme.getAiColor());
        assertEquals("\u001B[32m", defaultScheme.getAiColor()); // Green
    }
    
    @Test
    void testDefaultColorSchemeHasErrorColor() {
        // When
        ColorScheme defaultScheme = ColorScheme.DEFAULT;
        
        // Then
        assertNotNull(defaultScheme.getErrorColor());
        assertEquals("\u001B[31m", defaultScheme.getErrorColor()); // Red
    }
    
    @Test
    void testDefaultColorSchemeHasWarningColor() {
        // When
        ColorScheme defaultScheme = ColorScheme.DEFAULT;
        
        // Then
        assertNotNull(defaultScheme.getWarningColor());
        assertEquals("\u001B[33m", defaultScheme.getWarningColor()); // Yellow
    }
    
    @Test
    void testDefaultColorSchemeHasSystemColor() {
        // When
        ColorScheme defaultScheme = ColorScheme.DEFAULT;
        
        // Then
        assertNotNull(defaultScheme.getSystemColor());
        assertEquals("\u001B[90m", defaultScheme.getSystemColor()); // Gray
    }
    
    @Test
    void testDefaultColorSchemeHasSuccessColor() {
        // When
        ColorScheme defaultScheme = ColorScheme.DEFAULT;
        
        // Then
        assertNotNull(defaultScheme.getSuccessColor());
        assertEquals("\u001B[32m", defaultScheme.getSuccessColor()); // Green
    }
    
    @Test
    void testDefaultColorSchemeHasPromptColor() {
        // When
        ColorScheme defaultScheme = ColorScheme.DEFAULT;
        
        // Then
        assertNotNull(defaultScheme.getPromptColor());
        assertEquals("\u001B[36m", defaultScheme.getPromptColor()); // Cyan
    }
    
    @Test
    void testColorSchemeEquality() {
        // Given
        ColorScheme scheme1 = ColorScheme.builder()
            .userColor("\u001B[34m")
            .aiColor("\u001B[32m")
            .errorColor("\u001B[31m")
            .warningColor("\u001B[33m")
            .systemColor("\u001B[90m")
            .successColor("\u001B[32m")
            .promptColor("\u001B[36m")
            .build();
        
        ColorScheme scheme2 = ColorScheme.builder()
            .userColor("\u001B[34m")
            .aiColor("\u001B[32m")
            .errorColor("\u001B[31m")
            .warningColor("\u001B[33m")
            .systemColor("\u001B[90m")
            .successColor("\u001B[32m")
            .promptColor("\u001B[36m")
            .build();
        
        // Then
        assertEquals(scheme1, scheme2);
    }
    
    @Test
    void testColorSchemeSetters() {
        // Given
        ColorScheme scheme = new ColorScheme();
        
        // When
        scheme.setUserColor("\u001B[34m");
        scheme.setAiColor("\u001B[32m");
        scheme.setErrorColor("\u001B[31m");
        scheme.setWarningColor("\u001B[33m");
        scheme.setSystemColor("\u001B[90m");
        scheme.setSuccessColor("\u001B[32m");
        scheme.setPromptColor("\u001B[36m");
        
        // Then
        assertEquals("\u001B[34m", scheme.getUserColor());
        assertEquals("\u001B[32m", scheme.getAiColor());
        assertEquals("\u001B[31m", scheme.getErrorColor());
        assertEquals("\u001B[33m", scheme.getWarningColor());
        assertEquals("\u001B[90m", scheme.getSystemColor());
        assertEquals("\u001B[32m", scheme.getSuccessColor());
        assertEquals("\u001B[36m", scheme.getPromptColor());
    }
}
