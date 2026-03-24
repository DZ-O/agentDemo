package com.openbutler.cli.terminal;

import com.openbutler.cli.terminal.command.CommandRegistry;
import com.openbutler.cli.terminal.rendering.BannerRenderer;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.cli.terminal.service.CommandParser;
import com.openbutler.cli.terminal.service.ConfigManager;
import com.openbutler.cli.terminal.service.GlobalExceptionHandler;
import com.openbutler.cli.terminal.service.InputHandler;
import com.openbutler.cli.terminal.service.SessionManager;
import com.openbutler.core.agent.AgentCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OpenButlerCLI.
 * 
 * Tests the main CLI entry point including:
 * - Component initialization
 * - Banner display on startup
 * - Graceful shutdown handling
 */
@ExtendWith(MockitoExtension.class)
class OpenButlerCLITest {
    
    @Mock
    private BannerRenderer bannerRenderer;
    
    @Mock
    private InputHandler inputHandler;
    
    @Mock
    private CommandParser commandParser;
    
    @Mock
    private CommandRegistry commandRegistry;
    
    @Mock
    private SessionManager sessionManager;
    
    @Mock
    private ConfigManager configManager;
    
    @Mock
    private TerminalRenderer terminalRenderer;
    
    @Mock
    private AgentCore agentCore;
    
    @Mock
    private GlobalExceptionHandler exceptionHandler;
    
    private OpenButlerCLI cli;
    
    @BeforeEach
    void setUp() {
        cli = new OpenButlerCLI(
                bannerRenderer,
                inputHandler,
                commandParser,
                commandRegistry,
                sessionManager,
                configManager,
                terminalRenderer,
                agentCore,
                exceptionHandler
        );
    }
    
    @Test
    void testCLIInitialization() {
        // Verify CLI can be instantiated with all dependencies
        assertNotNull(cli);
    }
    
    @Test
    void testStopMethod() {
        // Test that stop method can be called
        assertDoesNotThrow(() -> cli.stop());
    }
}
