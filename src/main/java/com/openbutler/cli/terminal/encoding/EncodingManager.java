package com.openbutler.cli.terminal.encoding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 编码管理器，负责处理跨平台字符编码
 *
 * 实现 ApplicationContextInitializer 接口在应用启动时设置正确的编码
 *
 * 验证需求: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8
 */
@Component
@Slf4j
public class EncodingManager implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private final PlatformDetector platformDetector;
    private Charset platformCharset;

    /**
     * 默认构造函数，用于ApplicationContextInitializer
     */
    public EncodingManager() {
        this.platformDetector = new PlatformDetector();
    }

    /**
     * 构造函数，用于依赖注入
     *
     * @param platformDetector 平台检测器
     */
    public EncodingManager(PlatformDetector platformDetector) {
        this.platformDetector = platformDetector;
    }

    /**
     * 在应用上下文初始化时设置平台编码
     *
     * @param applicationContext Spring应用上下文
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        setupPlatformEncoding();
    }

    /**
     * 检测并设置平台编码
     *
     * 验证需求: 4.1, 4.2, 4.3, 4.4, 4.8
     */
    public void setupPlatformEncoding() {
        try {
            // 首先尝试从控制台获取编码
            Charset consoleCharset = detectConsoleEncoding();
            log.info("Detected console encoding: {}", consoleCharset);

            if (platformDetector.isWindows()) {
                // 根据核心经验，不要对 Java 应用程序强加 UTF-8 控制台编码，应顺应 Windows 系统的默认代码页（如 GBK）
                // 即使控制台被错误设置为 chcp 65001，也不应强转为 UTF-8，以避免 IME bug 导致的中文输入乱码
                // 依赖 JLine 自身的 JNA 交互，这样输入和输出中文都能正确工作
                platformCharset = Charset.defaultCharset();
            } else if (consoleCharset != null) {
                platformCharset = consoleCharset;
            } else {
                // 如果无法检测控制台编码，根据平台选择默认编码
                if (platformDetector.isLinux() || platformDetector.isMacOS()) {
                    // Linux和macOS系统使用UTF-8编码
                    platformCharset = StandardCharsets.UTF_8;
                } else {
                    // 未知系统回退到UTF-8
                    platformCharset = StandardCharsets.UTF_8;
                }
            }

            // 设置系统属性以确保所有组件使用一致的编码
            System.setProperty("file.encoding", platformCharset.name());
            System.setProperty("sun.stdout.encoding", platformCharset.name());
            System.setProperty("sun.stderr.encoding", platformCharset.name());

        } catch (Exception e) {
            // 如果编码设置失败，回退到UTF-8
            platformCharset = StandardCharsets.UTF_8;
            System.setProperty("file.encoding", StandardCharsets.UTF_8.name());
            System.setProperty("sun.stdout.encoding", StandardCharsets.UTF_8.name());
            System.setProperty("sun.stderr.encoding", StandardCharsets.UTF_8.name());
        }
    }

    /**
     * 检测控制台编码
     *
     * @return 控制台字符集，如果无法检测则返回null
     */
    private Charset detectConsoleEncoding() {
        try {
            // 方法1: 尝试从System.console()获取编码
            Console console = System.console();
            if (console != null) {
                // Console对象存在，尝试获取其编码
                // 注意：Console的charset()方法在某些JDK版本中可能不可用
                try {
                    java.lang.reflect.Method charsetMethod = console.getClass().getMethod("charset");
                    Charset charset = (Charset) charsetMethod.invoke(console);
                    if (charset != null) {
                        return charset;
                    }
                } catch (Exception e) {
                    // 方法不可用，继续尝试其他方法
                }
            }

            // 方法2: 在Windows上检测控制台代码页
            if (platformDetector.isWindows()) {
                return detectWindowsConsoleEncoding();
            }

            // 方法3: 检查环境变量
            String lang = System.getenv("LANG");
            if (lang != null) {
                if (lang.toUpperCase().contains("UTF-8") || lang.toUpperCase().contains("UTF8")) {
                    return StandardCharsets.UTF_8;
                } else if (lang.toUpperCase().contains("GBK")) {
                    return Charset.forName("GBK");
                }
            }

            // 方法4: 使用JVM默认编码
            String fileEncoding = System.getProperty("file.encoding");
            if (fileEncoding != null && !fileEncoding.isEmpty()) {
                try {
                    return Charset.forName(fileEncoding);
                } catch (Exception e) {
                    // 编码名称无效
                }
            }

        } catch (Exception e) {
            // 检测失败，返回null
        }

        return null;
    }

    /**
     * 检测Windows控制台编码
     *
     * @return Windows控制台字符集
     */
    private Charset detectWindowsConsoleEncoding() {
        try {
            // 执行chcp命令获取当前代码页
            Process process = Runtime.getRuntime().exec("cmd.exe /c chcp");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );

            String line = reader.readLine();
            reader.close();
            process.waitFor();

            if (line != null) {
                // 输出格式: "Active code page: 936" 或 "活动代码页: 936"
                String[] parts = line.split(":");
                if (parts.length >= 2) {
                    String codePage = parts[1].trim();

                    // 根据代码页返回对应的字符集
                    switch (codePage) {
                        case "936":
                            return Charset.forName("GBK");
                        case "65001":
                            return StandardCharsets.UTF_8;
                        case "437":
                            return Charset.forName("IBM437");
                        default:
                            // 尝试使用代码页号创建字符集
                            try {
                                return Charset.forName("CP" + codePage);
                            } catch (Exception e) {
                                // 不支持的代码页
                            }
                    }
                }
            }
        } catch (Exception e) {
            // 检测失败
        }

        return null;
    }

    /**
     * 获取当前平台编码
     *
     * @return 平台字符集
     */
    public Charset getPlatformCharset() {
        if (platformCharset == null) {
            setupPlatformEncoding();
        }
        return platformCharset;
    }

    /**
     * 编码字符串用于输出
     *
     * @param text 原始文本
     * @return 编码后的文本
     *
     * 验证需求: 4.5, 4.6, 4.7
     */
    public String encodeForOutput(String text) {
        if (text == null) {
            return null;
        }

        try {
            Charset charset = getPlatformCharset();
            // 将字符串转换为字节数组，然后使用平台编码重新构建字符串
            byte[] bytes = text.getBytes(charset);
            return new String(bytes, charset);
        } catch (Exception e) {
            // 编码失败时返回原始文本
            return text;
        }
    }

    /**
     * 解码输入字符串
     *
     * @param text 编码的文本
     * @return 解码后的文本
     *
     * 验证需求: 4.5, 4.6, 4.7
     */
    public String decodeInput(String text) {
        if (text == null) {
            return null;
        }

        try {
            Charset charset = getPlatformCharset();
            // 将字符串转换为字节数组，然后使用平台编码重新构建字符串
            byte[] bytes = text.getBytes(charset);
            return new String(bytes, charset);
        } catch (Exception e) {
            // 解码失败时返回原始文本
            return text;
        }
    }

    /**
     * 检查字符是否可显示
     *
     * @param c 要检查的字符
     * @return 如果字符可显示返回true，否则返回false
     *
     * 验证需求: 4.5, 4.6, 4.7
     */
    public boolean isDisplayable(char c) {
        // 检查字符是否在可显示范围内
        // 包括：基本拉丁字母、中文字符、emoji等

        // 控制字符（除了换行、制表符等）不可显示
        if (Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t') {
            return false;
        }

        // 检查字符是否可以被当前平台编码
        try {
            Charset charset = getPlatformCharset();
            String str = String.valueOf(c);
            byte[] bytes = str.getBytes(charset);
            String decoded = new String(bytes, charset);
            // 如果编码后再解码能得到相同的字符，则认为可显示
            return decoded.equals(str);
        } catch (Exception e) {
            return false;
        }
    }
}
