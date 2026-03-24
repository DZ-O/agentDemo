package com.openbutler.cli.terminal.config;

import org.jline.terminal.Terminal;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TerminalConfiguration 单元测试
 */
class TerminalConfigurationTest {

    @Test
    void testTerminalBeanCreation() throws IOException {
        // Given
        TerminalConfiguration config = new TerminalConfiguration();
        
        // When
        Terminal terminal = config.terminal();
        
        // Then
        assertNotNull(terminal, "Terminal bean should not be null");
    }

    @Test
    void testTerminalIsSystemTerminal() throws IOException {
        // Given
        TerminalConfiguration config = new TerminalConfiguration();
        
        // When
        Terminal terminal = config.terminal();
        
        // Then
        assertNotNull(terminal.getType(), "Terminal type should not be null");
    }

    @Test
    void testTerminalHasWriter() throws IOException {
        // Given
        TerminalConfiguration config = new TerminalConfiguration();
        
        // When
        Terminal terminal = config.terminal();
        
        // Then
        assertNotNull(terminal.writer(), "Terminal writer should not be null");
    }

    @Test
    void testTerminalHasReader() throws IOException {
        // Given
        TerminalConfiguration config = new TerminalConfiguration();
        
        // When
        Terminal terminal = config.terminal();
        
        // Then
        assertNotNull(terminal.reader(), "Terminal reader should not be null");
    }

    @Test
    void testTerminalName() throws IOException {
        // Given
        TerminalConfiguration config = new TerminalConfiguration();
        
        // When
        Terminal terminal = config.terminal();
        
        // Then
        assertNotNull(terminal.getName(), "Terminal name should not be null");
    }
}
