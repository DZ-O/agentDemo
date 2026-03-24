package com.openbutler.cli.terminal.encoding;

import org.springframework.stereotype.Component;

/**
 * 平台检测器，用于检测操作系统类型和提供平台信息查询
 * 
 * 支持的操作系统：
 * - Windows
 * - Linux
 * - macOS
 */
@Component
public class PlatformDetector {
    
    /**
     * 操作系统类型枚举
     */
    public enum OS {
        WINDOWS,
        LINUX,
        MACOS,
        UNKNOWN
    }
    
    private final OS currentOS;
    private final String osName;
    private final String osVersion;
    private final String osArch;
    
    /**
     * 构造函数，自动检测当前操作系统
     */
    public PlatformDetector() {
        this.osName = System.getProperty("os.name");
        this.osVersion = System.getProperty("os.version");
        this.osArch = System.getProperty("os.arch");
        this.currentOS = detectOS();
    }
    
    /**
     * 检测操作系统类型
     * 
     * @return 操作系统类型
     */
    private OS detectOS() {
        if (osName == null) {
            return OS.UNKNOWN;
        }
        
        String osNameLower = osName.toLowerCase();
        
        if (osNameLower.contains("win")) {
            return OS.WINDOWS;
        } else if (osNameLower.contains("nix") || osNameLower.contains("nux") || osNameLower.contains("aix")) {
            return OS.LINUX;
        } else if (osNameLower.contains("mac") || osNameLower.contains("darwin")) {
            return OS.MACOS;
        }
        
        return OS.UNKNOWN;
    }
    
    /**
     * 获取当前操作系统类型
     * 
     * @return 操作系统类型
     */
    public OS getOS() {
        return currentOS;
    }
    
    /**
     * 检查是否是Windows系统
     * 
     * @return 如果是Windows返回true，否则返回false
     */
    public boolean isWindows() {
        return currentOS == OS.WINDOWS;
    }
    
    /**
     * 检查是否是Linux系统
     * 
     * @return 如果是Linux返回true，否则返回false
     */
    public boolean isLinux() {
        return currentOS == OS.LINUX;
    }
    
    /**
     * 检查是否是macOS系统
     * 
     * @return 如果是macOS返回true，否则返回false
     */
    public boolean isMacOS() {
        return currentOS == OS.MACOS;
    }
    
    /**
     * 获取操作系统名称
     * 
     * @return 操作系统名称
     */
    public String getOSName() {
        return osName;
    }
    
    /**
     * 获取操作系统版本
     * 
     * @return 操作系统版本
     */
    public String getOSVersion() {
        return osVersion;
    }
    
    /**
     * 获取操作系统架构
     * 
     * @return 操作系统架构（如 x86_64, aarch64等）
     */
    public String getOSArch() {
        return osArch;
    }
    
    /**
     * 获取平台信息的字符串表示
     * 
     * @return 平台信息字符串
     */
    public String getPlatformInfo() {
        return String.format("%s %s (%s) - %s", 
            currentOS, osName, osVersion, osArch);
    }
    
    /**
     * 检查是否是Unix-like系统（Linux或macOS）
     * 
     * @return 如果是Unix-like系统返回true，否则返回false
     */
    public boolean isUnixLike() {
        return currentOS == OS.LINUX || currentOS == OS.MACOS;
    }
}
