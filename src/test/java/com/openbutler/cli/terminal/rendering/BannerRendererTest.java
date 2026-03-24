package com.openbutler.cli.terminal.rendering;

import com.openbutler.cli.terminal.encoding.EncodingManager;
import com.openbutler.cli.terminal.encoding.PlatformDetector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BannerRenderer.
 * 
 * Tests verify that the banner renderer correctly displays the welcome banner
 * with version information and usage tips.
 * 
 * Validates: Requirement 3.11
 */
class BannerRendererTest {
    
    private BannerRenderer bannerRenderer;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    void setUp() {
        EncodingManager encodingManager = new EncodingManager(new PlatformDetector());
        bannerRenderer = new BannerRenderer(encodingManager);
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }
    
    /**
     * Test that renderBanner displays the application name.
     * Validates: Requirement 3.11
     */
    @Test
    void testRenderBannerDisplaysApplicationName() {
        bannerRenderer.renderBanner();
        
        String output = outputStream.toString();
        assertTrue(output.contains("OpenButler CLI"), "Banner should contain application name");
    }
    
    /**
     * Test that renderBanner displays version information.
     * Validates: Requirement 3.11
     */
    @Test
    void testRenderBannerDisplaysVersionInformation() {
        bannerRenderer.renderBanner();
        
        String output = outputStream.toString();
        assertTrue(output.contains("Version"), "Banner should contain version label");
        assertTrue(output.contains("1.0.0"), "Banner should contain version number");
    }
    
    /**
     * Test that renderBanner displays basic usage tips.
     * Validates: Requirement 3.11
     */
    @Test
    void testRenderBannerDisplaysUsageTips() {
        bannerRenderer.renderBanner();
        
        String output = outputStream.toString();
        assertTrue(output.contains("Quick Start"), "Banner should contain quick start section");
        assertTrue(output.contains("/help"), "Banner should mention help command");
        assertTrue(output.contains("/new"), "Banner should mention new command");
        assertTrue(output.contains("/exit"), "Banner should mention exit command");
    }
    
    /**
     * Test that renderBanner includes ANSI color codes.
     * Validates: Requirement 3.11
     */
    @Test
    void testRenderBannerIncludesAnsiColors() {
        bannerRenderer.renderBanner();
        
        String output = outputStream.toString();
        assertTrue(output.contains("\u001B["), "Banner should contain ANSI escape codes for colors");
    }
    
    /**
     * Test that renderBanner includes decorative borders.
     * Validates: Requirement 3.11
     */
    @Test
    void testRenderBannerIncludesBorders() {
        bannerRenderer.renderBanner();
        
        String output = outputStream.toString();
        assertTrue(output.contains("+"), "Banner should contain top border");
        assertTrue(output.contains("|"), "Banner should contain side borders");
    }
    
    /**
     * Test that renderBanner outputs to System.out.
     * Validates: Requirement 3.11
     */
    @Test
    void testRenderBannerOutputsToSystemOut() {
        bannerRenderer.renderBanner();
        
        String output = outputStream.toString();
        assertFalse(output.isEmpty(), "Banner should produce output");
        assertTrue(output.length() > 100, "Banner should be substantial in size");
    }
    
    /**
     * Test that renderBanner includes chat usage tip.
     * Validates: Requirement 3.11
     */
    @Test
    void testRenderBannerIncludesChatTip() {
        bannerRenderer.renderBanner();
        
        String output = outputStream.toString();
        assertTrue(output.contains("chat with AI") || output.contains("message"), 
                   "Banner should mention chatting with AI");
    }
    
    /**
     * Test that renderBanner can be called multiple times.
     * Validates: Requirement 3.11
     */
    @Test
    void testRenderBannerCanBeCalledMultipleTimes() {
        bannerRenderer.renderBanner();
        String firstOutput = outputStream.toString();
        
        outputStream.reset();
        
        bannerRenderer.renderBanner();
        String secondOutput = outputStream.toString();
        
        assertEquals(firstOutput, secondOutput, "Multiple calls should produce identical output");
    }
}
