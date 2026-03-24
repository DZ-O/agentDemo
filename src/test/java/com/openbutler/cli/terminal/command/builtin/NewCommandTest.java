package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.model.Session;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.cli.terminal.service.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NewCommand.
 * 
 * Tests cover:
 * - Command metadata (name, aliases, description, usage)
 * - Session creation with custom name
 * - Session creation with default name
 * - Automatic session switching
 * - Success message display
 * - Error handling
 * - Session requirement
 */
@ExtendWith(MockitoExtension.class)
class NewCommandTest {
    
    @Mock
    private SessionManager sessionManager;
    
    @Mock
    private TerminalRenderer renderer;
    
    private NewCommand newCommand;
    
    @BeforeEach
    void setUp() {
        newCommand = new NewCommand();
    }
    
    @Test
    void testGetName() {
        assertEquals("new", newCommand.getName());
    }
    
    @Test
    void testGetAliases() {
        List<String> aliases = newCommand.getAliases();
        assertEquals(2, aliases.size());
        assertTrue(aliases.contains("n"));
        assertTrue(aliases.contains("create"));
    }
    
    @Test
    void testGetDescription() {
        assertNotNull(newCommand.getDescription());
        assertFalse(newCommand.getDescription().isEmpty());
    }
    
    @Test
    void testGetUsage() {
        assertNotNull(newCommand.getUsage());
        assertFalse(newCommand.getUsage().isEmpty());
    }
    
    @Test
    void testDoesNotRequireSession() {
        assertFalse(newCommand.requiresSession());
    }
    
    @Test
    void testExecuteWithCustomName() {
        // Arrange
        String customName = "My Project";
        CommandContext context = CommandContext.builder()
                .rawInput("/new My Project")
                .commandName("new")
                .arguments(List.of("My", "Project"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        Session newSession = Session.builder()
                .id("new-session-id-12345678")
                .name(customName)
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(0)
                .active(false)
                .build();
        
        when(sessionManager.createSession(customName)).thenReturn(newSession);
        
        // Act
        StepVerifier.create(newCommand.execute(context))
                .verifyComplete();
        
        // Assert
        verify(sessionManager).createSession(customName);
        verify(sessionManager).switchSession(newSession.getId());
        
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(renderer).renderSuccess(messageCaptor.capture());
        
        String successMessage = messageCaptor.getValue();
        assertTrue(successMessage.contains(customName));
        assertTrue(successMessage.contains("new-sess")); // First 8 chars of ID
    }
    
    @Test
    void testExecuteWithDefaultName() {
        // Arrange
        CommandContext context = CommandContext.builder()
                .rawInput("/new")
                .commandName("new")
                .arguments(List.of())
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        Session newSession = Session.builder()
                .id("default-session-id")
                .name("Session 1234567890")
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(0)
                .active(false)
                .build();
        
        when(sessionManager.createSession(anyString())).thenReturn(newSession);
        
        // Act
        StepVerifier.create(newCommand.execute(context))
                .verifyComplete();
        
        // Assert
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionManager).createSession(nameCaptor.capture());
        
        String capturedName = nameCaptor.getValue();
        assertTrue(capturedName.startsWith("Session "));
        
        verify(sessionManager).switchSession(newSession.getId());
        verify(renderer).renderSuccess(anyString());
    }
    
    @Test
    void testExecuteWithMultipleWordName() {
        // Arrange
        String multiWordName = "Important Work Project";
        CommandContext context = CommandContext.builder()
                .rawInput("/new Important Work Project")
                .commandName("new")
                .arguments(List.of("Important", "Work", "Project"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        Session newSession = Session.builder()
                .id("multi-word-session-id")
                .name(multiWordName)
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(0)
                .active(false)
                .build();
        
        when(sessionManager.createSession(multiWordName)).thenReturn(newSession);
        
        // Act
        StepVerifier.create(newCommand.execute(context))
                .verifyComplete();
        
        // Assert
        verify(sessionManager).createSession(multiWordName);
        verify(sessionManager).switchSession(newSession.getId());
        verify(renderer).renderSuccess(contains(multiWordName));
    }
    
    @Test
    void testExecuteWithSpecialCharactersInName() {
        // Arrange
        String specialName = "Project-2024 (v1.0)";
        CommandContext context = CommandContext.builder()
                .rawInput("/new " + specialName)
                .commandName("new")
                .arguments(List.of("Project-2024", "(v1.0)"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        Session newSession = Session.builder()
                .id("special-session-id")
                .name(specialName)
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(0)
                .active(false)
                .build();
        
        when(sessionManager.createSession(specialName)).thenReturn(newSession);
        
        // Act
        StepVerifier.create(newCommand.execute(context))
                .verifyComplete();
        
        // Assert
        verify(sessionManager).createSession(specialName);
        verify(sessionManager).switchSession(newSession.getId());
        verify(renderer).renderSuccess(contains(specialName));
    }
    
    @Test
    void testExecuteWithChineseCharactersInName() {
        // Arrange
        String chineseName = "我的项目";
        CommandContext context = CommandContext.builder()
                .rawInput("/new 我的项目")
                .commandName("new")
                .arguments(List.of("我的项目"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        Session newSession = Session.builder()
                .id("chinese-session-id")
                .name(chineseName)
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(0)
                .active(false)
                .build();
        
        when(sessionManager.createSession(chineseName)).thenReturn(newSession);
        
        // Act
        StepVerifier.create(newCommand.execute(context))
                .verifyComplete();
        
        // Assert
        verify(sessionManager).createSession(chineseName);
        verify(sessionManager).switchSession(newSession.getId());
        verify(renderer).renderSuccess(contains(chineseName));
    }
    
    @Test
    void testExecuteAutomaticallySwitchesToNewSession() {
        // Arrange
        CommandContext context = CommandContext.builder()
                .rawInput("/new Test")
                .commandName("new")
                .arguments(List.of("Test"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        Session newSession = Session.builder()
                .id("switch-test-id")
                .name("Test")
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(0)
                .active(false)
                .build();
        
        when(sessionManager.createSession("Test")).thenReturn(newSession);
        
        // Act
        StepVerifier.create(newCommand.execute(context))
                .verifyComplete();
        
        // Assert - verify switchSession is called with the new session's ID
        verify(sessionManager).switchSession("switch-test-id");
    }
    
    @Test
    void testExecuteDisplaysSessionIdInSuccessMessage() {
        // Arrange
        CommandContext context = CommandContext.builder()
                .rawInput("/new Display Test")
                .commandName("new")
                .arguments(List.of("Display", "Test"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        Session newSession = Session.builder()
                .id("abcdef12-3456-7890-abcd-ef1234567890")
                .name("Display Test")
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(0)
                .active(false)
                .build();
        
        when(sessionManager.createSession("Display Test")).thenReturn(newSession);
        
        // Act
        StepVerifier.create(newCommand.execute(context))
                .verifyComplete();
        
        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(renderer).renderSuccess(messageCaptor.capture());
        
        String successMessage = messageCaptor.getValue();
        assertTrue(successMessage.contains("Display Test"));
        assertTrue(successMessage.contains("abcdef12")); // First 8 chars of ID
    }
    
    @Test
    void testExecuteWithSessionCreationError() {
        // Arrange
        CommandContext context = CommandContext.builder()
                .rawInput("/new Error Test")
                .commandName("new")
                .arguments(List.of("Error", "Test"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        RuntimeException error = new RuntimeException("Failed to create session file");
        when(sessionManager.createSession(anyString())).thenThrow(error);
        
        // Act
        StepVerifier.create(newCommand.execute(context))
                .verifyComplete();
        
        // Assert
        verify(sessionManager).createSession("Error Test");
        verify(sessionManager, never()).switchSession(anyString());
        verify(renderer).renderError(contains("Failed to create new session"));
    }
    
    @Test
    void testExecuteWithSessionSwitchError() {
        // Arrange
        CommandContext context = CommandContext.builder()
                .rawInput("/new Switch Error")
                .commandName("new")
                .arguments(List.of("Switch", "Error"))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        Session newSession = Session.builder()
                .id("error-session-id")
                .name("Switch Error")
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(0)
                .active(false)
                .build();
        
        when(sessionManager.createSession("Switch Error")).thenReturn(newSession);
        doThrow(new RuntimeException("Failed to switch session"))
                .when(sessionManager).switchSession(anyString());
        
        // Act
        StepVerifier.create(newCommand.execute(context))
                .verifyComplete();
        
        // Assert
        verify(sessionManager).createSession("Switch Error");
        verify(sessionManager).switchSession("error-session-id");
        verify(renderer).renderError(contains("Failed to create new session"));
    }
    
    @Test
    void testExecuteWithNullArguments() {
        // Arrange
        CommandContext context = CommandContext.builder()
                .rawInput("/new")
                .commandName("new")
                .arguments(null)
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        Session newSession = Session.builder()
                .id("null-args-session-id")
                .name("Session 1234567890")
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(0)
                .active(false)
                .build();
        
        when(sessionManager.createSession(anyString())).thenReturn(newSession);
        
        // Act
        StepVerifier.create(newCommand.execute(context))
                .verifyComplete();
        
        // Assert - should use default name
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(sessionManager).createSession(nameCaptor.capture());
        assertTrue(nameCaptor.getValue().startsWith("Session "));
        verify(renderer).renderSuccess(anyString());
    }
    
    @Test
    void testExecuteWithEmptyStringArgument() {
        // Arrange
        CommandContext context = CommandContext.builder()
                .rawInput("/new ")
                .commandName("new")
                .arguments(List.of(""))
                .sessionManager(sessionManager)
                .renderer(renderer)
                .build();
        
        Session newSession = Session.builder()
                .id("empty-arg-session-id")
                .name("")
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(0)
                .active(false)
                .build();
        
        when(sessionManager.createSession("")).thenReturn(newSession);
        
        // Act
        StepVerifier.create(newCommand.execute(context))
                .verifyComplete();
        
        // Assert - should accept empty string as valid name
        verify(sessionManager).createSession("");
        verify(sessionManager).switchSession("empty-arg-session-id");
        verify(renderer).renderSuccess(anyString());
    }
}
