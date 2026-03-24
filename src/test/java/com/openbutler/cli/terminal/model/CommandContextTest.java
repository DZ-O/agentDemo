package com.openbutler.cli.terminal.model;

import com.openbutler.core.agent.AgentCore;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CommandContext model.
 * 
 * Tests the builder pattern, getters/setters, and basic functionality
 * of the CommandContext class.
 */
class CommandContextTest {

    @Test
    void testCommandContextBuilder() {
        // Given
        String rawInput = "/chat Hello world";
        String commandName = "chat";
        List<String> arguments = Arrays.asList("Hello", "world");
        Session session = Session.builder()
                .id("test-session-id")
                .name("Test Session")
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .messageCount(0)
                .active(true)
                .metadata(new HashMap<>())
                .build();
        Object renderer = new Object();
        Object configManager = new Object();
        Object sessionManager = new Object();
        AgentCore agentCore = null; // Mock or null for this test
        
        // When
        CommandContext context = CommandContext.builder()
                .rawInput(rawInput)
                .commandName(commandName)
                .arguments(arguments)
                .currentSession(session)
                .renderer(renderer)
                .configManager(configManager)
                .sessionManager(sessionManager)
                .agentCore(agentCore)
                .build();
        
        // Then
        assertNotNull(context);
        assertEquals(rawInput, context.getRawInput());
        assertEquals(commandName, context.getCommandName());
        assertEquals(arguments, context.getArguments());
        assertEquals(session, context.getCurrentSession());
        assertEquals(renderer, context.getRenderer());
        assertEquals(configManager, context.getConfigManager());
        assertEquals(sessionManager, context.getSessionManager());
        assertEquals(agentCore, context.getAgentCore());
    }
    
    @Test
    void testCommandContextSetters() {
        // Given
        CommandContext context = new CommandContext();
        String rawInput = "/list";
        String commandName = "list";
        List<String> arguments = Arrays.asList();
        
        // When
        context.setRawInput(rawInput);
        context.setCommandName(commandName);
        context.setArguments(arguments);
        
        // Then
        assertEquals(rawInput, context.getRawInput());
        assertEquals(commandName, context.getCommandName());
        assertEquals(arguments, context.getArguments());
    }
    
    @Test
    void testCommandContextWithNullValues() {
        // Given & When
        CommandContext context = CommandContext.builder()
                .rawInput(null)
                .commandName(null)
                .arguments(null)
                .currentSession(null)
                .renderer(null)
                .configManager(null)
                .sessionManager(null)
                .agentCore(null)
                .build();
        
        // Then
        assertNotNull(context);
        assertNull(context.getRawInput());
        assertNull(context.getCommandName());
        assertNull(context.getArguments());
        assertNull(context.getCurrentSession());
        assertNull(context.getRenderer());
        assertNull(context.getConfigManager());
        assertNull(context.getSessionManager());
        assertNull(context.getAgentCore());
    }
    
    @Test
    void testCommandContextWithEmptyArguments() {
        // Given
        List<String> emptyArguments = Arrays.asList();
        
        // When
        CommandContext context = CommandContext.builder()
                .rawInput("/help")
                .commandName("help")
                .arguments(emptyArguments)
                .build();
        
        // Then
        assertNotNull(context.getArguments());
        assertTrue(context.getArguments().isEmpty());
    }
    
    @Test
    void testCommandContextWithMultipleArguments() {
        // Given
        List<String> arguments = Arrays.asList("arg1", "arg2", "arg3", "arg4");
        
        // When
        CommandContext context = CommandContext.builder()
                .rawInput("/config set key value")
                .commandName("config")
                .arguments(arguments)
                .build();
        
        // Then
        assertEquals(4, context.getArguments().size());
        assertEquals("arg1", context.getArguments().get(0));
        assertEquals("arg4", context.getArguments().get(3));
    }
    
    @Test
    void testCommandContextEquality() {
        // Given
        Session session = Session.builder()
                .id("session-1")
                .name("Session 1")
                .build();
        
        CommandContext context1 = CommandContext.builder()
                .rawInput("/chat test")
                .commandName("chat")
                .arguments(Arrays.asList("test"))
                .currentSession(session)
                .build();
        
        CommandContext context2 = CommandContext.builder()
                .rawInput("/chat test")
                .commandName("chat")
                .arguments(Arrays.asList("test"))
                .currentSession(session)
                .build();
        
        // Then
        assertEquals(context1, context2);
        assertEquals(context1.hashCode(), context2.hashCode());
    }
    
    @Test
    void testCommandContextToString() {
        // Given
        CommandContext context = CommandContext.builder()
                .rawInput("/version")
                .commandName("version")
                .arguments(Arrays.asList())
                .build();
        
        // When
        String toString = context.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("CommandContext"));
        assertTrue(toString.contains("version"));
    }
}
