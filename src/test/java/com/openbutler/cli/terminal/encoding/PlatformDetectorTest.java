package com.openbutler.cli.terminal.encoding;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 单元测试：PlatformDetector
 * 
 * 验证需求: 4.1, 4.2, 4.3
 */
class PlatformDetectorTest {
    
    @Test
    void testPlatformDetectorInitialization() {
        // 测试平台检测器能够成功初始化
        PlatformDetector detector = new PlatformDetector();
        
        assertNotNull(detector.getOS());
        assertNotNull(detector.getOSName());
        assertNotNull(detector.getOSVersion());
        assertNotNull(detector.getOSArch());
    }
    
    @Test
    void testGetOSReturnsValidType() {
        // 测试getOS返回有效的操作系统类型
        PlatformDetector detector = new PlatformDetector();
        PlatformDetector.OS os = detector.getOS();
        
        assertTrue(
            os == PlatformDetector.OS.WINDOWS ||
            os == PlatformDetector.OS.LINUX ||
            os == PlatformDetector.OS.MACOS ||
            os == PlatformDetector.OS.UNKNOWN
        );
    }
    
    @Test
    void testPlatformCheckMethodsAreConsistent() {
        // 测试平台检查方法与getOS()返回值一致
        PlatformDetector detector = new PlatformDetector();
        PlatformDetector.OS os = detector.getOS();
        
        switch (os) {
            case WINDOWS:
                assertTrue(detector.isWindows());
                assertFalse(detector.isLinux());
                assertFalse(detector.isMacOS());
                assertFalse(detector.isUnixLike());
                break;
            case LINUX:
                assertFalse(detector.isWindows());
                assertTrue(detector.isLinux());
                assertFalse(detector.isMacOS());
                assertTrue(detector.isUnixLike());
                break;
            case MACOS:
                assertFalse(detector.isWindows());
                assertFalse(detector.isLinux());
                assertTrue(detector.isMacOS());
                assertTrue(detector.isUnixLike());
                break;
            case UNKNOWN:
                assertFalse(detector.isWindows());
                assertFalse(detector.isLinux());
                assertFalse(detector.isMacOS());
                assertFalse(detector.isUnixLike());
                break;
        }
    }
    
    @Test
    void testGetPlatformInfoReturnsNonEmptyString() {
        // 测试getPlatformInfo返回非空字符串
        PlatformDetector detector = new PlatformDetector();
        String platformInfo = detector.getPlatformInfo();
        
        assertNotNull(platformInfo);
        assertFalse(platformInfo.isEmpty());
        
        // 验证包含操作系统类型
        assertTrue(platformInfo.contains(detector.getOS().toString()));
    }
    
    @Test
    void testOSNameMatchesSystemProperty() {
        // 测试getOSName返回的值与系统属性一致
        PlatformDetector detector = new PlatformDetector();
        
        assertEquals(System.getProperty("os.name"), detector.getOSName());
    }
    
    @Test
    void testOSVersionMatchesSystemProperty() {
        // 测试getOSVersion返回的值与系统属性一致
        PlatformDetector detector = new PlatformDetector();
        
        assertEquals(System.getProperty("os.version"), detector.getOSVersion());
    }
    
    @Test
    void testOSArchMatchesSystemProperty() {
        // 测试getOSArch返回的值与系统属性一致
        PlatformDetector detector = new PlatformDetector();
        
        assertEquals(System.getProperty("os.arch"), detector.getOSArch());
    }
    
    @Test
    void testUnixLikeDetection() {
        // 测试Unix-like系统检测
        PlatformDetector detector = new PlatformDetector();
        
        boolean isUnixLike = detector.isUnixLike();
        boolean isLinuxOrMac = detector.isLinux() || detector.isMacOS();
        
        assertEquals(isLinuxOrMac, isUnixLike);
    }
    
    @Test
    void testOnlyOneOSTypeIsTrue() {
        // 测试只有一个操作系统类型检查方法返回true
        PlatformDetector detector = new PlatformDetector();
        
        int trueCount = 0;
        if (detector.isWindows()) trueCount++;
        if (detector.isLinux()) trueCount++;
        if (detector.isMacOS()) trueCount++;
        
        // 应该只有一个或零个（UNKNOWN情况）为true
        assertTrue(trueCount <= 1);
    }
}
