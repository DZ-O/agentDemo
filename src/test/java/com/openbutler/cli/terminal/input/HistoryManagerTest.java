package com.openbutler.cli.terminal.input;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HistoryManager单元测试
 */
class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new HistoryManager();
    }

    @Test
    void testGetHistoryFile_ShouldReturnValidPath() {
        // When
        Path historyFile = historyManager.getHistoryFile();

        // Then
        assertNotNull(historyFile, "History file path should not be null");
        assertTrue(historyFile.toString().contains(".openbutler"), 
                "History file should be in .openbutler directory");
        assertTrue(historyFile.toString().contains(".openbutler_history"), 
                "History file should be named .openbutler_history");
    }

    @Test
    void testGetHistoryFile_ShouldBeInUserHomeDirectory() {
        // Given
        String userHome = System.getProperty("user.home");
        Path expectedParent = Paths.get(userHome, ".openbutler");

        // When
        Path historyFile = historyManager.getHistoryFile();

        // Then
        assertEquals(expectedParent, historyFile.getParent(), 
                "History file should be in user home .openbutler directory");
    }

    @Test
    void testGetHistorySize_ShouldReturnDefaultSize() {
        // When
        int historySize = historyManager.getHistorySize();

        // Then
        assertEquals(1000, historySize, "Default history size should be 1000");
    }

    @Test
    void testGetHistorySize_ShouldBePositive() {
        // When
        int historySize = historyManager.getHistorySize();

        // Then
        assertTrue(historySize > 0, "History size should be positive");
    }

    @Test
    void testHistoryManager_ShouldBeConsistent() {
        // Multiple calls should return the same values
        Path file1 = historyManager.getHistoryFile();
        Path file2 = historyManager.getHistoryFile();
        int size1 = historyManager.getHistorySize();
        int size2 = historyManager.getHistorySize();

        assertEquals(file1, file2, "History file path should be consistent");
        assertEquals(size1, size2, "History size should be consistent");
    }
}
