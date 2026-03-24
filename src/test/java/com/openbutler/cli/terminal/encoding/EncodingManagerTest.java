package com.openbutler.cli.terminal.encoding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 单元测试：EncodingManager
 * 
 * 验证需求: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8
 */
class EncodingManagerTest {
    
    @Mock
    private PlatformDetector platformDetector;
    
    private EncodingManager encodingManager;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testWindowsPlatformSelectsGBKEncoding() {
        // 验证需求: 4.1
        // 测试Windows平台选择GBK编码
        when(platformDetector.isWindows()).thenReturn(true);
        when(platformDetector.isLinux()).thenReturn(false);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        Charset charset = encodingManager.getPlatformCharset();
        assertEquals("GBK", charset.name());
    }
    
    @Test
    void testLinuxPlatformSelectsUTF8Encoding() {
        // 验证需求: 4.2
        // 测试Linux平台选择UTF-8编码
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(true);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        Charset charset = encodingManager.getPlatformCharset();
        assertEquals(StandardCharsets.UTF_8, charset);
    }
    
    @Test
    void testMacOSPlatformSelectsUTF8Encoding() {
        // 验证需求: 4.3
        // 测试macOS平台选择UTF-8编码
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(false);
        when(platformDetector.isMacOS()).thenReturn(true);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        Charset charset = encodingManager.getPlatformCharset();
        assertEquals(StandardCharsets.UTF_8, charset);
    }
    
    @Test
    void testUnknownPlatformFallsBackToUTF8() {
        // 验证需求: 4.4
        // 测试未知平台回退到UTF-8编码
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(false);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        Charset charset = encodingManager.getPlatformCharset();
        assertEquals(StandardCharsets.UTF_8, charset);
    }
    
    @Test
    void testEncodingSetupSetsSystemProperties() {
        // 验证需求: 4.8
        // 测试编码设置会设置系统属性
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(true);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        // 验证系统属性被设置
        assertNotNull(System.getProperty("file.encoding"));
        assertNotNull(System.getProperty("sun.stdout.encoding"));
        assertNotNull(System.getProperty("sun.stderr.encoding"));
    }
    
    @Test
    void testEncodeForOutputHandlesChineseCharacters() {
        // 验证需求: 4.5
        // 测试正确处理中文字符的编码
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(true);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        String chineseText = "你好世界";
        String encoded = encodingManager.encodeForOutput(chineseText);
        
        assertNotNull(encoded);
        assertEquals(chineseText, encoded);
    }
    
    @Test
    void testEncodeForOutputHandlesEmoji() {
        // 验证需求: 4.6
        // 测试正确处理emoji表情符号
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(true);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        String emojiText = "Hello 😀 World 🌍";
        String encoded = encodingManager.encodeForOutput(emojiText);
        
        assertNotNull(encoded);
        // UTF-8应该能正确处理emoji
        assertEquals(emojiText, encoded);
    }
    
    @Test
    void testEncodeForOutputHandlesSpecialSymbols() {
        // 验证需求: 4.7
        // 测试正确处理特殊符号（如表格边框字符）
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(true);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        String specialText = "┌─┬─┐\n│ │ │\n└─┴─┘";
        String encoded = encodingManager.encodeForOutput(specialText);
        
        assertNotNull(encoded);
        assertEquals(specialText, encoded);
    }
    
    @Test
    void testEncodeForOutputHandlesNullInput() {
        // 测试编码方法处理null输入
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(true);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        String encoded = encodingManager.encodeForOutput(null);
        assertNull(encoded);
    }
    
    @Test
    void testDecodeInputHandlesChineseCharacters() {
        // 验证需求: 4.5
        // 测试正确处理中文字符的解码
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(true);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        String chineseText = "你好世界";
        String decoded = encodingManager.decodeInput(chineseText);
        
        assertNotNull(decoded);
        assertEquals(chineseText, decoded);
    }
    
    @Test
    void testDecodeInputHandlesNullInput() {
        // 测试解码方法处理null输入
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(true);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        String decoded = encodingManager.decodeInput(null);
        assertNull(decoded);
    }
    
    @Test
    void testEncodingRoundTripConsistency() {
        // 验证需求: 4.5, 4.6, 4.7
        // 测试编码和解码的往返一致性
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(true);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        String[] testStrings = {
            "Hello World",
            "你好世界",
            "Hello 😀 World",
            "┌─┬─┐",
            "Mixed: 中文 English 123 😀"
        };
        
        for (String original : testStrings) {
            String encoded = encodingManager.encodeForOutput(original);
            String decoded = encodingManager.decodeInput(encoded);
            assertEquals(original, decoded, "Round trip failed for: " + original);
        }
    }
    
    @Test
    void testIsDisplayableForPrintableCharacters() {
        // 验证需求: 4.5, 4.6, 4.7
        // 测试可显示字符的检测
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(true);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        // 基本拉丁字母应该可显示
        assertTrue(encodingManager.isDisplayable('A'));
        assertTrue(encodingManager.isDisplayable('z'));
        assertTrue(encodingManager.isDisplayable('0'));
        
        // 中文字符应该可显示
        assertTrue(encodingManager.isDisplayable('你'));
        assertTrue(encodingManager.isDisplayable('好'));
        
        // 特殊符号应该可显示
        assertTrue(encodingManager.isDisplayable('─'));
        assertTrue(encodingManager.isDisplayable('│'));
    }
    
    @Test
    void testIsDisplayableForControlCharacters() {
        // 测试控制字符不可显示
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(true);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        encodingManager.setupPlatformEncoding();
        
        // 控制字符不应该可显示（除了换行、制表符等）
        assertFalse(encodingManager.isDisplayable('\u0000')); // NULL
        assertFalse(encodingManager.isDisplayable('\u0001')); // SOH
        assertFalse(encodingManager.isDisplayable('\u0007')); // BELL
        
        // 但是换行、制表符等应该可显示
        assertTrue(encodingManager.isDisplayable('\n'));
        assertTrue(encodingManager.isDisplayable('\r'));
        assertTrue(encodingManager.isDisplayable('\t'));
    }
    
    @Test
    void testGetPlatformCharsetInitializesIfNeeded() {
        // 测试getPlatformCharset在需要时自动初始化
        when(platformDetector.isWindows()).thenReturn(false);
        when(platformDetector.isLinux()).thenReturn(true);
        when(platformDetector.isMacOS()).thenReturn(false);
        
        encodingManager = new EncodingManager(platformDetector);
        // 不调用setupPlatformEncoding
        
        Charset charset = encodingManager.getPlatformCharset();
        assertNotNull(charset);
        assertEquals(StandardCharsets.UTF_8, charset);
    }
    
    @Test
    void testEncodingFallbackOnException() {
        // 测试编码异常时的回退机制
        // 创建一个会抛出异常的场景
        PlatformDetector faultyDetector = mock(PlatformDetector.class);
        when(faultyDetector.isWindows()).thenThrow(new RuntimeException("Test exception"));
        
        encodingManager = new EncodingManager(faultyDetector);
        encodingManager.setupPlatformEncoding();
        
        // 应该回退到UTF-8
        Charset charset = encodingManager.getPlatformCharset();
        assertEquals(StandardCharsets.UTF_8, charset);
    }
    
    @Test
    void testDefaultConstructor() {
        // 测试默认构造函数（用于ApplicationContextInitializer）
        EncodingManager manager = new EncodingManager();
        assertNotNull(manager);
        
        // 应该能够正常工作
        manager.setupPlatformEncoding();
        assertNotNull(manager.getPlatformCharset());
    }
}
