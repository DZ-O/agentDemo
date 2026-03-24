package com.openbutler.cli.terminal.rendering;

import com.openbutler.cli.terminal.encoding.EncodingManager;
import org.fusesource.jansi.Ansi;
import org.springframework.stereotype.Component;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * BannerRenderer 负责在 CLI 应用程序启动时渲染带有版本信息和基本使用提示的欢迎横幅。
 *
 * 验证要求：3.11
 */
@Component
public class BannerRenderer {
    
    private static final String VERSION = "1.0.0";
    private static final String RESET = "\u001B[0m";
    
    private final EncodingManager encodingManager;
    
    public BannerRenderer(EncodingManager encodingManager) {
        this.encodingManager = encodingManager;
    }
    
    /**
     * 渲染带有版本信息和使用提示的欢迎横幅。
     *
     * 验证要求：3.11
     */
    public void renderBanner() {
        String banner = buildBanner();
        System.out.println(banner);
        System.out.flush();
    }
    
    /**
     * 构建带有 ANSI 颜色和格式的横幅字符串。
     *
     * @return 格式化后的横幅字符串
     */
    private String buildBanner() {
        StringBuilder sb = new StringBuilder();
        
        // 顶部边框 - 使用ASCII字符以确保跨平台兼容
        sb.append(Ansi.ansi().fg(Ansi.Color.CYAN).a("+============================================================+").reset()).append("\n");
        
        // 标题
        sb.append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset())
          .append(Ansi.ansi().fg(Ansi.Color.GREEN).bold().a("                    OpenButler CLI                          ").reset())
          .append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset()).append("\n");
        
        // 版本
        sb.append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset())
          .append(Ansi.ansi().fg(Ansi.Color.YELLOW).a("                      版本 ").a(VERSION).a("                            ").reset())
          .append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset()).append("\n");
        
        // 分隔符
        sb.append(Ansi.ansi().fg(Ansi.Color.CYAN).a("+============================================================+").reset()).append("\n");
        
        // 使用提示
        sb.append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset())
          .append("  ")
          .append(Ansi.ansi().fg(Ansi.Color.WHITE).a("快速开始:").reset())
          .append("                                              ")
          .append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset()).append("\n");
        
        sb.append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset())
          .append("    * 直接输入消息与AI对话                             ")
          .append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset()).append("\n");
        
        sb.append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset())
          .append("    * 使用 ")
          .append(Ansi.ansi().fg(Ansi.Color.GREEN).a("/help").reset())
          .append(" 查看所有可用命令                       ")
          .append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset()).append("\n");
        
        sb.append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset())
          .append("    * 使用 ")
          .append(Ansi.ansi().fg(Ansi.Color.GREEN).a("/new").reset())
          .append(" 创建新会话                             ")
          .append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset()).append("\n");
        
        sb.append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset())
          .append("    * 使用 ")
          .append(Ansi.ansi().fg(Ansi.Color.GREEN).a("/exit").reset())
          .append(" 退出应用                              ")
          .append(Ansi.ansi().fg(Ansi.Color.CYAN).a("|").reset()).append("\n");
        
        // 底部边框
        sb.append(Ansi.ansi().fg(Ansi.Color.CYAN).a("+============================================================+").reset()).append("\n");
        
        return sb.toString();
    }
}
