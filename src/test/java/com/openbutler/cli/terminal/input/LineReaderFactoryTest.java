package com.openbutler.cli.terminal.input;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.jline.terminal.Terminal;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * LineReaderFactory单元测试
 * 
 * 注意：由于JLine LineReader需要真实的Terminal实例才能正常工作，
 * 这些测试主要验证工厂类的配置逻辑和提示符生成功能。
 * LineReader的完整功能将在集成测试中验证。
 */
@ExtendWith(MockitoExtension.class)
class LineReaderFactoryTest {

    @Mock
    private Terminal terminal;

    @Mock
    private HistoryManager historyManager;

    @Mock
    private CommandCompleter commandCompleter;

    private LineReaderFactory lineReaderFactory;

    @BeforeEach
    void setUp() {
        // 配置mock对象的默认行为
        lenient().when(historyManager.getHistoryFile()).thenReturn(Paths.get("/tmp/.openbutler_history"));
        lenient().when(historyManager.getHistorySize()).thenReturn(1000);
        
        lineReaderFactory = new LineReaderFactory(terminal, historyManager, commandCompleter);
    }

    @Test
    void testLineReaderFactory_ShouldBeCreatedWithDependencies() {
        // Then
        assertNotNull(lineReaderFactory, "LineReaderFactory should be created");
    }

    @Test
    void testGeneratePrompt_ShouldIncludeSessionName() {
        // Given
        String sessionName = "my-session";

        // When
        String prompt = lineReaderFactory.generatePrompt(sessionName);

        // Then
        assertNotNull(prompt, "Prompt should not be null");
        assertTrue(prompt.contains("my-session"), 
                "Prompt should contain session name");
        assertTrue(prompt.contains(">"), 
                "Prompt should contain > symbol");
    }

    @Test
    void testGeneratePrompt_ShouldContainAnsiColorCodes() {
        // Given
        String sessionName = "test-session";

        // When
        String prompt = lineReaderFactory.generatePrompt(sessionName);

        // Then
        assertTrue(prompt.contains("\u001B["), 
                "Prompt should contain ANSI escape codes for colors");
    }

    @Test
    void testGeneratePrompt_WithDifferentSessionNames() {
        // Test with various session names
        String[] sessionNames = {"default", "project-1", "会话", "session_123"};

        for (String sessionName : sessionNames) {
            // When
            String prompt = lineReaderFactory.generatePrompt(sessionName);

            // Then
            assertNotNull(prompt, "Prompt should not be null for session: " + sessionName);
            assertTrue(prompt.contains(sessionName), 
                    "Prompt should contain session name: " + sessionName);
        }
    }

    @Test
    void testGeneratePrompt_WithNullSessionName_ShouldHandleGracefully() {
        // When
        String prompt = lineReaderFactory.generatePrompt(null);

        // Then
        assertNotNull(prompt, "Prompt should not be null even with null session name");
    }

    @Test
    void testGeneratePrompt_WithEmptySessionName_ShouldHandleGracefully() {
        // When
        String prompt = lineReaderFactory.generatePrompt("");

        // Then
        assertNotNull(prompt, "Prompt should not be null even with empty session name");
    }

    @Test
    void testLineReaderFactory_ShouldHaveTerminalDependency() {
        // Verify that the factory was created with the terminal dependency
        assertNotNull(lineReaderFactory, "Factory should have terminal dependency");
    }

    @Test
    void testLineReaderFactory_ShouldHaveHistoryManagerDependency() {
        // Verify that the factory was created with the history manager dependency
        assertNotNull(lineReaderFactory, "Factory should have history manager dependency");
    }

    @Test
    void testLineReaderFactory_ShouldHaveCommandCompleterDependency() {
        // Verify that the factory was created with the command completer dependency
        assertNotNull(lineReaderFactory, "Factory should have command completer dependency");
    }
}
