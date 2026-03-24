package com.openbutler.cli.terminal.rendering;

import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 * ProgressBar wrapper class that integrates the ProgressBar library (me.tongfei:progressbar).
 * 
 * This class provides a simplified interface for creating and managing progress bars
 * in the terminal, supporting various styles and update methods.
 * 
 * Validates: Requirements 3.9, 9.3
 */
public class ProgressBar {
    
    private final me.tongfei.progressbar.ProgressBar progressBar;
    
    /**
     * Private constructor - use static factory methods to create instances.
     * 
     * @param progressBar the underlying ProgressBar instance
     */
    private ProgressBar(me.tongfei.progressbar.ProgressBar progressBar) {
        this.progressBar = progressBar;
    }
    
    /**
     * Creates a new progress bar with the specified task name and total steps.
     * 
     * @param taskName the name of the task being tracked
     * @param total the total number of steps (max value)
     * @return a new ProgressBar instance
     * 
     * Validates: Requirement 3.9
     */
    public static ProgressBar create(String taskName, long total) {
        me.tongfei.progressbar.ProgressBar pb = new ProgressBarBuilder()
                .setTaskName(taskName)
                .setInitialMax(total)
                .setStyle(ProgressBarStyle.ASCII)
                .build();
        return new ProgressBar(pb);
    }
    
    /**
     * Creates a new progress bar with custom style.
     * 
     * @param taskName the name of the task being tracked
     * @param total the total number of steps (max value)
     * @param style the progress bar style
     * @return a new ProgressBar instance
     * 
     * Validates: Requirement 3.9
     */
    public static ProgressBar create(String taskName, long total, ProgressBarStyle style) {
        me.tongfei.progressbar.ProgressBar pb = new ProgressBarBuilder()
                .setTaskName(taskName)
                .setInitialMax(total)
                .setStyle(style)
                .build();
        return new ProgressBar(pb);
    }
    
    /**
     * Creates a new indeterminate progress bar (no known total).
     * 
     * @param taskName the name of the task being tracked
     * @return a new ProgressBar instance
     * 
     * Validates: Requirement 3.9
     */
    public static ProgressBar createIndeterminate(String taskName) {
        me.tongfei.progressbar.ProgressBar pb = new ProgressBarBuilder()
                .setTaskName(taskName)
                .setInitialMax(-1)
                .setStyle(ProgressBarStyle.ASCII)
                .build();
        return new ProgressBar(pb);
    }
    
    /**
     * Updates the progress by incrementing the current value by 1.
     * 
     * Validates: Requirement 3.9
     */
    public void step() {
        progressBar.step();
    }
    
    /**
     * Updates the progress by incrementing the current value by the specified amount.
     * 
     * @param amount the amount to increment by
     * 
     * Validates: Requirement 3.9
     */
    public void stepBy(long amount) {
        progressBar.stepBy(amount);
    }
    
    /**
     * Sets the progress to a specific value.
     * 
     * @param value the new current value
     * 
     * Validates: Requirement 3.9
     */
    public void stepTo(long value) {
        progressBar.stepTo(value);
    }
    
    /**
     * Updates the extra message displayed alongside the progress bar.
     * 
     * @param message the extra message to display
     * 
     * Validates: Requirement 9.3
     */
    public void setExtraMessage(String message) {
        progressBar.setExtraMessage(message);
    }
    
    /**
     * Gets the current progress value.
     * 
     * @return the current progress value
     */
    public long getCurrent() {
        return progressBar.getCurrent();
    }
    
    /**
     * Gets the maximum progress value.
     * 
     * @return the maximum progress value
     */
    public long getMax() {
        return progressBar.getMax();
    }
    
    /**
     * Closes the progress bar and cleans up resources.
     * This should be called when the task is complete.
     * 
     * Validates: Requirement 3.9
     */
    public void close() {
        progressBar.close();
    }
    
    /**
     * Gets the underlying ProgressBar instance for advanced usage.
     * 
     * @return the underlying ProgressBar instance
     */
    public me.tongfei.progressbar.ProgressBar getUnderlyingProgressBar() {
        return progressBar;
    }
}
