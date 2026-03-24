package com.openbutler.cli.terminal.rendering;

import me.tongfei.progressbar.ProgressBarStyle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProgressBar wrapper class.
 * 
 * Validates: Requirements 3.9, 9.3
 */
class ProgressBarTest {
    
    private ProgressBar progressBar;
    
    @AfterEach
    void cleanup() {
        if (progressBar != null) {
            progressBar.close();
        }
    }
    
    @Test
    void testCreateProgressBarWithTaskNameAndTotal() {
        // Validates: Requirement 3.9
        progressBar = ProgressBar.create("Test Task", 100);
        
        assertNotNull(progressBar);
        assertEquals(0, progressBar.getCurrent());
        assertEquals(100, progressBar.getMax());
    }
    
    @Test
    void testCreateProgressBarWithCustomStyle() {
        // Validates: Requirement 3.9
        progressBar = ProgressBar.create("Test Task", 100, ProgressBarStyle.COLORFUL_UNICODE_BLOCK);
        
        assertNotNull(progressBar);
        assertEquals(0, progressBar.getCurrent());
        assertEquals(100, progressBar.getMax());
    }
    
    @Test
    void testCreateIndeterminateProgressBar() {
        // Validates: Requirement 3.9
        progressBar = ProgressBar.createIndeterminate("Indeterminate Task");
        
        assertNotNull(progressBar);
        assertEquals(0, progressBar.getCurrent());
        // Indeterminate progress bars may have max of 0 or -1 depending on implementation
        assertTrue(progressBar.getMax() <= 0);
    }
    
    @Test
    void testStepIncrementsProgressByOne() {
        // Validates: Requirement 3.9
        progressBar = ProgressBar.create("Test Task", 100);
        
        progressBar.step();
        assertEquals(1, progressBar.getCurrent());
        
        progressBar.step();
        assertEquals(2, progressBar.getCurrent());
    }
    
    @Test
    void testStepByIncrementsProgressByAmount() {
        // Validates: Requirement 3.9
        progressBar = ProgressBar.create("Test Task", 100);
        
        progressBar.stepBy(10);
        assertEquals(10, progressBar.getCurrent());
        
        progressBar.stepBy(25);
        assertEquals(35, progressBar.getCurrent());
    }
    
    @Test
    void testStepToSetsProgressToSpecificValue() {
        // Validates: Requirement 3.9
        progressBar = ProgressBar.create("Test Task", 100);
        
        progressBar.stepTo(50);
        assertEquals(50, progressBar.getCurrent());
        
        progressBar.stepTo(75);
        assertEquals(75, progressBar.getCurrent());
    }
    
    @Test
    void testSetExtraMessageUpdatesDisplayMessage() {
        // Validates: Requirement 9.3
        progressBar = ProgressBar.create("Test Task", 100);
        
        // Should not throw exception
        assertDoesNotThrow(() -> progressBar.setExtraMessage("Processing item 1"));
        assertDoesNotThrow(() -> progressBar.setExtraMessage("Processing item 2"));
    }
    
    @Test
    void testProgressBarCanReachMaximum() {
        // Validates: Requirement 3.9
        progressBar = ProgressBar.create("Test Task", 10);
        
        progressBar.stepTo(10);
        assertEquals(10, progressBar.getCurrent());
        assertEquals(10, progressBar.getMax());
    }
    
    @Test
    void testProgressBarWithZeroTotal() {
        // Edge case: progress bar with zero total
        progressBar = ProgressBar.create("Empty Task", 0);
        
        assertNotNull(progressBar);
        assertEquals(0, progressBar.getCurrent());
        assertEquals(0, progressBar.getMax());
    }
    
    @Test
    void testProgressBarWithLargeTotal() {
        // Edge case: progress bar with large total
        progressBar = ProgressBar.create("Large Task", 1_000_000);
        
        assertNotNull(progressBar);
        assertEquals(0, progressBar.getCurrent());
        assertEquals(1_000_000, progressBar.getMax());
        
        progressBar.stepBy(500_000);
        assertEquals(500_000, progressBar.getCurrent());
    }
    
    @Test
    void testMultipleStepOperations() {
        // Validates: Requirement 3.9
        progressBar = ProgressBar.create("Test Task", 100);
        
        progressBar.step();
        progressBar.stepBy(9);
        progressBar.step();
        progressBar.stepTo(50);
        progressBar.stepBy(25);
        
        assertEquals(75, progressBar.getCurrent());
    }
    
    @Test
    void testGetUnderlyingProgressBar() {
        progressBar = ProgressBar.create("Test Task", 100);
        
        me.tongfei.progressbar.ProgressBar underlying = progressBar.getUnderlyingProgressBar();
        assertNotNull(underlying);
    }
    
    @Test
    void testCloseProgressBar() {
        // Validates: Requirement 3.9
        progressBar = ProgressBar.create("Test Task", 100);
        
        // Should not throw exception
        assertDoesNotThrow(() -> progressBar.close());
    }
    
    @Test
    void testProgressBarWithEmptyTaskName() {
        // Edge case: empty task name
        progressBar = ProgressBar.create("", 100);
        
        assertNotNull(progressBar);
        assertEquals(0, progressBar.getCurrent());
        assertEquals(100, progressBar.getMax());
    }
    
    @Test
    void testProgressBarWithLongTaskName() {
        // Edge case: long task name
        String longTaskName = "This is a very long task name that might be truncated in the display";
        progressBar = ProgressBar.create(longTaskName, 100);
        
        assertNotNull(progressBar);
        assertEquals(0, progressBar.getCurrent());
        assertEquals(100, progressBar.getMax());
    }
}
