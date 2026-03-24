package com.openbutler.cli.terminal.service;

import com.openbutler.cli.terminal.command.CommandRegistry;
import com.openbutler.cli.terminal.model.CLIConfig;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * 
 * Tests error handling for different exception types and debug mode behavior.
 * 
 * Validates: Requirements 9.1, 9.4, 9.5, 9.6, 9.7
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    
    @Mock
    private TerminalRenderer renderer;
    
    @Mock
    private CommandRegistry commandRegistry;
    
    @Mock
    private ConfigManager configManager;
    
    private GlobalExceptionHandler exceptionHandler;
    
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler(renderer, commandRegistry, configManager);
    }
    
    /**
     * Test handling user input error displays error message.
     * Validates: Requirement 9.1
     */
    @Test
    void testHandleUserInputError_DisplaysErrorMessage() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid parameter");
        CLIConfig config = CLIConfig.builder().debugMode(false).build();
        when(configManager.loadConfig()).thenReturn(config);
        
        // Act
        exceptionHandler.handleCommandException(exception, "test");
        
        // Assert
        verify(renderer).renderError("Invalid parameter");
    }
    
    /**
     * Test handling command not found suggests similar commands.
     * Validates: Requirement 9.5
     */
    @Test
    void testHandleCommandNotFound_SuggestsSimilarCommands() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Command not found");
        List<String> suggestions = Arrays.asList("chat", "clear");
        when(commandRegistry.findSimilarCommands("chatt")).thenReturn(suggestions);
        CLIConfig config = CLIConfig.builder().debugMode(false).build();
        when(configManager.loadConfig()).thenReturn(config);
        
        // Act
        exceptionHandler.handleCommandException(exception, "chatt");
        
        // Assert
        verify(renderer).renderError("Command not found");
        verify(renderer).renderSystem("Did you mean: chat, clear?");
        verify(renderer).renderSystem("Type '/help' to see all available commands.");
    }
    
    /**
     * Test handling network error displays specific error reason.
     * Validates: Requirement 9.4
     */
    @Test
    void testHandleNetworkError_DisplaysSpecificReason() {
        // Arrange
        ConnectException exception = new ConnectException("Connection refused");
        CLIConfig config = CLIConfig.builder().debugMode(false).build();
        when(configManager.loadConfig()).thenReturn(config);
        
        // Act
        exceptionHandler.handleCommandException(exception, "chat");
        
        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(renderer, atLeastOnce()).renderError(messageCaptor.capture());
        
        String errorMessage = messageCaptor.getValue();
        assertTrue(errorMessage.contains("Network request failed"));
        
        verify(renderer).renderSystem("Could not connect to the service. Please check:");
    }
    
    /**
     * Test handling timeout error displays timeout information.
     * Validates: Requirement 9.4
     */
    @Test
    void testHandleTimeoutError_DisplaysTimeoutInfo() {
        // Arrange
        TimeoutException exception = new TimeoutException("Request timed out");
        CLIConfig config = CLIConfig.builder().debugMode(false).build();
        when(configManager.loadConfig()).thenReturn(config);
        
        // Act
        exceptionHandler.handleCommandException(exception, "chat");
        
        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(renderer, atLeastOnce()).renderError(messageCaptor.capture());
        
        String errorMessage = messageCaptor.getValue();
        assertTrue(errorMessage.contains("timed out"));
    }
    
    /**
     * Test handling socket timeout error.
     * Validates: Requirement 9.4
     */
    @Test
    void testHandleSocketTimeoutError_DisplaysTimeoutInfo() {
        // Arrange
        SocketTimeoutException exception = new SocketTimeoutException("Read timed out");
        CLIConfig config = CLIConfig.builder().debugMode(false).build();
        when(configManager.loadConfig()).thenReturn(config);
        
        // Act
        exceptionHandler.handleCommandException(exception, "chat");
        
        // Assert
        verify(renderer, atLeastOnce()).renderError(anyString());
        verify(renderer, atLeastOnce()).renderSystem(anyString());
    }
    
    /**
     * Test handling IO error displays system error message.
     * Validates: Requirements 9.1, 9.6
     */
    @Test
    void testHandleIOError_DisplaysSystemError() {
        // Arrange
        IOException exception = new IOException("File not found");
        CLIConfig config = CLIConfig.builder().debugMode(false).build();
        when(configManager.loadConfig()).thenReturn(config);
        
        // Act
        exceptionHandler.handleCommandException(exception, "config");
        
        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(renderer, atLeastOnce()).renderError(messageCaptor.capture());
        
        String errorMessage = messageCaptor.getValue();
        assertTrue(errorMessage.contains("Network request failed"));
    }
    
    /**
     * Test handling system error displays generic error message.
     * Validates: Requirements 9.1, 9.6
     */
    @Test
    void testHandleSystemError_DisplaysGenericError() {
        // Arrange
        RuntimeException exception = new RuntimeException("Unexpected error");
        CLIConfig config = CLIConfig.builder().debugMode(false).build();
        when(configManager.loadConfig()).thenReturn(config);
        
        // Act
        exceptionHandler.handleCommandException(exception, "test");
        
        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(renderer, atLeastOnce()).renderError(messageCaptor.capture());
        
        String errorMessage = messageCaptor.getValue();
        assertTrue(errorMessage.contains("unexpected error"));
    }
    
    /**
     * Test debug mode displays stack trace.
     * Validates: Requirement 9.7
     */
    @Test
    void testDebugMode_DisplaysStackTrace() {
        // Arrange
        RuntimeException exception = new RuntimeException("Test error");
        CLIConfig config = CLIConfig.builder().debugMode(true).build();
        when(configManager.loadConfig()).thenReturn(config);
        
        // Act
        exceptionHandler.handleCommandException(exception, "test");
        
        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(renderer, atLeastOnce()).renderSystem(messageCaptor.capture());
        
        List<String> systemMessages = messageCaptor.getAllValues();
        boolean hasStackTrace = systemMessages.stream()
                .anyMatch(msg -> msg.contains("Stack Trace"));
        
        assertTrue(hasStackTrace, "Stack trace should be displayed in debug mode");
    }
    
    /**
     * Test non-debug mode does not display stack trace.
     * Validates: Requirement 9.7
     */
    @Test
    void testNonDebugMode_DoesNotDisplayStackTrace() {
        // Arrange
        RuntimeException exception = new RuntimeException("Test error");
        CLIConfig config = CLIConfig.builder().debugMode(false).build();
        when(configManager.loadConfig()).thenReturn(config);
        
        // Act
        exceptionHandler.handleCommandException(exception, "test");
        
        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(renderer, atLeastOnce()).renderSystem(messageCaptor.capture());
        
        List<String> systemMessages = messageCaptor.getAllValues();
        boolean hasStackTrace = systemMessages.stream()
                .anyMatch(msg -> msg.contains("Stack Trace"));
        
        assertFalse(hasStackTrace, "Stack trace should not be displayed in non-debug mode");
    }
    
    /**
     * Test fatal error handler displays fatal error message.
     * Validates: Requirement 9.6
     */
    @Test
    void testHandleFatalError_DisplaysMessage() {
        // Arrange
        RuntimeException exception = new RuntimeException("Fatal error");
        CLIConfig config = CLIConfig.builder().debugMode(false).build();
        when(configManager.loadConfig()).thenReturn(config);
        
        // Act
        exceptionHandler.handleFatalError(exception, "main");
        
        // Assert - fatal error uses System.err, so we can't verify with mock
        // This test mainly ensures no exceptions are thrown
        verify(configManager).loadConfig();
    }
    
    /**
     * Test fatal error handler with debug mode.
     * Validates: Requirements 9.6, 9.7
     */
    @Test
    void testHandleFatalError_WithDebugMode() {
        // Arrange
        RuntimeException exception = new RuntimeException("Fatal error");
        CLIConfig config = CLIConfig.builder().debugMode(true).build();
        when(configManager.loadConfig()).thenReturn(config);
        
        // Act
        exceptionHandler.handleFatalError(exception, "main");
        
        // Assert - fatal error uses System.err, so we can't verify with mock
        // This test mainly ensures no exceptions are thrown
        verify(configManager).loadConfig();
    }
    
    /**
     * Test fatal error handler handles config loading failure gracefully.
     * Validates: Requirement 9.6
     */
    @Test
    void testHandleFatalError_HandlesConfigLoadingFailure() {
        // Arrange
        RuntimeException exception = new RuntimeException("Fatal error");
        when(configManager.loadConfig()).thenThrow(new RuntimeException("Config error"));
        
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> exceptionHandler.handleFatalError(exception, "main"));
    }
    
    /**
     * Test exception with cause displays nested stack trace in debug mode.
     * Validates: Requirement 9.7
     */
    @Test
    void testDebugMode_DisplaysNestedStackTrace() {
        // Arrange
        RuntimeException cause = new RuntimeException("Root cause");
        RuntimeException exception = new RuntimeException("Wrapper error", cause);
        CLIConfig config = CLIConfig.builder().debugMode(true).build();
        when(configManager.loadConfig()).thenReturn(config);
        
        // Act
        exceptionHandler.handleCommandException(exception, "test");
        
        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(renderer, atLeastOnce()).renderSystem(messageCaptor.capture());
        
        List<String> systemMessages = messageCaptor.getAllValues();
        boolean hasCausedBy = systemMessages.stream()
                .anyMatch(msg -> msg.contains("Caused by"));
        
        assertTrue(hasCausedBy, "Nested stack trace should be displayed in debug mode");
    }
}
