package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.model.Session;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.core.agent.AgentCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChatCommand.
 * 
 * Tests cover:
 * - Command metadata (name, aliases, description, usage)
 * - Message extraction from arguments
 * - AI response streaming
 * - Error handling
 * - Empty message validation
 * - Session requirement
 */
@ExtendWith(MockitoExtension.class)
class ChatCommandTest {
    
    @Mock
    private AgentCore agentCore;
    
    @Mock
    private TerminalRenderer renderer;
    
    private ChatCommand chatCommand;
    private Session testSession;
    
    @BeforeEach
    void setUp() {
        chatCommand = new ChatCommand();
        testSession = Session.builder()
                .id("test-session-id")
                .name("Test Session")
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(0)
                .active(true)
                .build();
    }
    
    @Test
    void testGetName() {
        assertEquals("chat", chatCommand.getName());
    }
    
    @Test
    void testGetAliases() {
        List<String> aliases = chatCommand.getAliases();
        assertEquals(2, aliases.size());
        assertTrue(aliases.contains("c"));
        assertTrue(aliases.contains("talk"));
    }
    
    @Test
    void testGetDescription() {
        assertNotNull(chatCommand.getDescription());
        assertFalse(chatCommand.getDescription().isEmpty());
    }
    
    @Test
    void testGetUsage() {
        assertNotNull(chatCommand.getUsage());
        assertFalse(chatCommand.getUsage().isEmpty());
    }
    
    @Test
    void testRequiresSession() {
        assertTrue(chatCommand.requiresSession());
    }
    
    @Test
    void testExecuteWithValidMessage() {
        // Arrange
        String userMessage = "Hello AI";
        CommandContext context = CommandContext.builder()
                .rawInput("/chat Hello AI")
                .commandName("chat")
                .arguments(List.of("Hello", "AI"))
                .currentSession(testSession)
                .renderer(renderer)
                .agentCore(agentCore)
                .build();
        
        // Mock AI response stream
        Flux<String> aiResponse = Flux.just("Hello", " ", "there", "!");
        when(agentCore.process(anyString(), anyString())).thenReturn(aiResponse);
        
        // Act
        StepVerifier.create(chatCommand.execute(context))
                .verifyComplete();
        
        // Assert
        verify(agentCore).process(testSession.getId(), userMessage);
        verify(renderer, times(4)).renderAIResponse(anyString());
        verify(renderer).renderAIResponse("Hello");
        verify(renderer).renderAIResponse(" ");
        verify(renderer).renderAIResponse("there");
        verify(renderer).renderAIResponse("!");
    }
    
    @Test
    void testExecuteWithEmptyMessage() {
        // Arrange
        CommandContext context = CommandContext.builder()
                .rawInput("/chat")
                .commandName("chat")
                .arguments(List.of())
                .currentSession(testSession)
                .renderer(renderer)
                .agentCore(agentCore)
                .build();
        
        // Act
        StepVerifier.create(chatCommand.execute(context))
                .verifyComplete();
        
        // Assert
        verify(renderer).renderError("Message cannot be empty");
        verify(agentCore, never()).process(anyString(), anyString());
    }
    
    @Test
    void testExecuteWithWhitespaceOnlyMessage() {
        // Arrange
        CommandContext context = CommandContext.builder()
                .rawInput("/chat   ")
                .commandName("chat")
                .arguments(List.of("   "))
                .currentSession(testSession)
                .renderer(renderer)
                .agentCore(agentCore)
                .build();
        
        // Act
        StepVerifier.create(chatCommand.execute(context))
                .verifyComplete();
        
        // Assert
        verify(renderer).renderError("Message cannot be empty");
        verify(agentCore, never()).process(anyString(), anyString());
    }
    
    @Test
    void testExecuteWithNonCommandInput() {
        // Arrange - simulating default chat (no command prefix)
        String userMessage = "What is the weather today?";
        CommandContext context = CommandContext.builder()
                .rawInput(userMessage)
                .commandName("chat")
                .arguments(List.of())
                .currentSession(testSession)
                .renderer(renderer)
                .agentCore(agentCore)
                .build();
        
        // Mock AI response stream
        Flux<String> aiResponse = Flux.just("The", " weather", " is", " sunny");
        when(agentCore.process(anyString(), anyString())).thenReturn(aiResponse);
        
        // Act
        StepVerifier.create(chatCommand.execute(context))
                .verifyComplete();
        
        // Assert
        verify(agentCore).process(testSession.getId(), userMessage);
        verify(renderer, times(4)).renderAIResponse(anyString());
    }
    
    @Test
    void testExecuteWithMultipleWordMessage() {
        // Arrange
        CommandContext context = CommandContext.builder()
                .rawInput("/chat Tell me a joke about programming")
                .commandName("chat")
                .arguments(List.of("Tell", "me", "a", "joke", "about", "programming"))
                .currentSession(testSession)
                .renderer(renderer)
                .agentCore(agentCore)
                .build();
        
        // Mock AI response
        Flux<String> aiResponse = Flux.just("Why", " do", " programmers", "...");
        when(agentCore.process(anyString(), anyString())).thenReturn(aiResponse);
        
        // Act
        StepVerifier.create(chatCommand.execute(context))
                .verifyComplete();
        
        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(agentCore).process(eq(testSession.getId()), messageCaptor.capture());
        assertEquals("Tell me a joke about programming", messageCaptor.getValue());
    }
    
    @Test
    void testExecuteWithAIError() {
        // Arrange
        CommandContext context = CommandContext.builder()
                .rawInput("/chat Hello")
                .commandName("chat")
                .arguments(List.of("Hello"))
                .currentSession(testSession)
                .renderer(renderer)
                .agentCore(agentCore)
                .build();
        
        // Mock AI error
        RuntimeException aiError = new RuntimeException("AI service unavailable");
        when(agentCore.process(anyString(), anyString()))
                .thenReturn(Flux.error(aiError));
        
        // Act
        StepVerifier.create(chatCommand.execute(context))
                .verifyComplete();
        
        // Assert
        verify(agentCore).process(testSession.getId(), "Hello");
        verify(renderer).renderError(contains("Failed to get AI response"));
    }
    
    @Test
    void testExecuteStreamsResponseIncrementally() {
        // Arrange
        CommandContext context = CommandContext.builder()
                .rawInput("/chat test")
                .commandName("chat")
                .arguments(List.of("test"))
                .currentSession(testSession)
                .renderer(renderer)
                .agentCore(agentCore)
                .build();
        
        // Mock AI response with multiple chunks
        Flux<String> aiResponse = Flux.just("chunk1", "chunk2", "chunk3", "chunk4", "chunk5");
        when(agentCore.process(anyString(), anyString())).thenReturn(aiResponse);
        
        // Act
        StepVerifier.create(chatCommand.execute(context))
                .verifyComplete();
        
        // Assert - verify each chunk is rendered as it arrives
        verify(renderer, times(5)).renderAIResponse(anyString());
        verify(renderer).renderAIResponse("chunk1");
        verify(renderer).renderAIResponse("chunk2");
        verify(renderer).renderAIResponse("chunk3");
        verify(renderer).renderAIResponse("chunk4");
        verify(renderer).renderAIResponse("chunk5");
    }
    
    @Test
    void testExecuteWithSpecialCharacters() {
        // Arrange
        String messageWithSpecialChars = "Hello! How are you? 你好 😊";
        CommandContext context = CommandContext.builder()
                .rawInput("/chat " + messageWithSpecialChars)
                .commandName("chat")
                .arguments(List.of("Hello!", "How", "are", "you?", "你好", "😊"))
                .currentSession(testSession)
                .renderer(renderer)
                .agentCore(agentCore)
                .build();
        
        // Mock AI response
        Flux<String> aiResponse = Flux.just("Response");
        when(agentCore.process(anyString(), anyString())).thenReturn(aiResponse);
        
        // Act
        StepVerifier.create(chatCommand.execute(context))
                .verifyComplete();
        
        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(agentCore).process(eq(testSession.getId()), messageCaptor.capture());
        assertEquals(messageWithSpecialChars, messageCaptor.getValue());
    }
    
    @Test
    void testExecuteWithNullArguments() {
        // Arrange
        CommandContext context = CommandContext.builder()
                .rawInput("")
                .commandName("chat")
                .arguments(null)
                .currentSession(testSession)
                .renderer(renderer)
                .agentCore(agentCore)
                .build();
        
        // Act
        StepVerifier.create(chatCommand.execute(context))
                .verifyComplete();
        
        // Assert
        verify(renderer).renderError("Message cannot be empty");
        verify(agentCore, never()).process(anyString(), anyString());
    }
}
