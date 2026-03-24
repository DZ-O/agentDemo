package com.openbutler.cli.terminal.config;

import org.fusesource.jansi.AnsiConsole;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * 终端配置类
 * 配置 JLine Terminal bean 并启用 Jansi 支持
 */
@Configuration
public class TerminalConfiguration {

    /**
     * 配置 JLine Terminal bean
     * 启用 Jansi 支持以实现跨平台 ANSI 颜色代码支持
     * 
     * @return 配置好的 Terminal 实例
     * @throws IOException 如果终端创建失败
     */
    @Bean
    public Terminal terminal() throws IOException {
        // 安装 Jansi，在 Windows 上自动启用 ANSI 支持
        AnsiConsole.systemInstall();
        
        // 构建 Terminal，启用系统终端和 Jansi 支持
        return TerminalBuilder.builder()
                .system(true)      // 使用系统终端
                .jansi(true)       // 启用 Jansi 支持
                .build();
    }
}
