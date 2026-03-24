# 设计文档

## 概述

本设计文档描述了OpenButler CLI系统的现代化重构方案。该重构将删除现有的简单CLI实现，构建一个功能丰富、样式现代、跨平台兼容的命令行系统，并支持使用GraalVM Native Image打包为独立可执行文件。

### 设计目标

1. **功能丰富**: 提供10+个内置命令，支持会话管理、配置管理、历史记录等高级功能
2. **现代化体验**: 使用ANSI颜色、表格、进度条等现代终端特性提升用户体验
3. **跨平台兼容**: 自动处理Windows GBK和Linux/Mac UTF-8编码差异
4. **原生打包**: 支持编译为原生可执行文件，无需Java运行时
5. **高性能**: 异步处理、流式输出、快速响应（<100ms输入响应，<2s启动）

### 技术栈

- **核心框架**: Spring Boot 3.2.3
- **终端库**: JLine 3.x (提供readline功能、历史记录、自动补全)
- **渲染库**: Jansi 2.x (跨平台ANSI颜色支持)
- **表格渲染**: ASCII Table (格式化表格输出)
- **原生编译**: GraalVM Native Image
- **配置管理**: Spring Boot Configuration Properties
- **测试框架**: JUnit 5 + Mockito + QuickTheories (property-based testing)

## 架构

### 整体架构

系统采用分层架构，从上到下分为：

```
┌─────────────────────────────────────────────────────────┐
│                    CLI Entry Point                       │
│              (OpenButlerCLIApplication)                  │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                   Command Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ ChatCommand  │  │ SessionCmd   │  │ ConfigCmd    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ HistoryCmd   │  │ HelpCommand  │  │ VersionCmd   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                   Service Layer                          │
│  ┌──────────────────┐  ┌──────────────────┐            │
│  │ SessionManager   │  │ ConfigManager    │            │
│  └──────────────────┘  └──────────────────┘            │
│  ┌──────────────────┐  ┌──────────────────┐            │
│  │ CommandParser    │  │ InputHandler     │            │
│  └──────────────────┘  └──────────────────┘            │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                  Rendering Layer                         │
│  ┌──────────────────┐  ┌──────────────────┐            │
│  │ TerminalRenderer │  │ ColorScheme      │            │
│  └──────────────────┘  └──────────────────┘            │
│  ┌──────────────────┐  ┌──────────────────┐            │
│  │ TableFormatter   │  │ ProgressBar      │            │
│  └──────────────────┘  └──────────────────┘            │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                 Platform Layer                           │
│  ┌──────────────────┐  ┌──────────────────┐            │
│  │ EncodingManager  │  │ PlatformDetector │            │
│  └──────────────────┘  └──────────────────┘            │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                   Core Services                          │
│              (AgentCore, MemoryService)                  │
└─────────────────────────────────────────────────────────┘
```

### 包结构设计

删除旧代码后，新的包结构如下：

```
com.openbutler.cli/
├── terminal/                    # 新的CLI根包
│   ├── OpenButlerCLI.java      # 主入口类
│   ├── command/                 # 命令层
│   │   ├── Command.java        # 命令接口
│   │   ├── CommandRegistry.java # 命令注册器
│   │   ├── builtin/            # 内置命令
│   │   │   ├── ChatCommand.java
│   │   │   ├── NewCommand.java
│   │   │   ├── ListCommand.java
│   │   │   ├── SwitchCommand.java
│   │   │   ├── HistoryCommand.java
│   │   │   ├── ClearCommand.java
│   │   │   ├── ConfigCommand.java
│   │   │   ├── HelpCommand.java
│   │   │   ├── VersionCommand.java
│   │   │   └── ExitCommand.java
│   ├── service/                 # 服务层
│   │   ├── SessionManager.java
│   │   ├── ConfigManager.java
│   │   ├── CommandParser.java
│   │   └── InputHandler.java
│   ├── rendering/               # 渲染层
│   │   ├── TerminalRenderer.java
│   │   ├── ColorScheme.java
│   │   ├── TableFormatter.java
│   │   ├── ProgressBar.java
│   │   └── BannerRenderer.java
│   ├── encoding/                # 编码层
│   │   ├── EncodingManager.java
│   │   └── PlatformDetector.java
│   ├── model/                   # 数据模型
│   │   ├── Session.java
│   │   ├── CLIConfig.java
│   │   └── CommandContext.java
│   ├── input/                   # 输入处理
│   │   ├── LineReaderFactory.java
│   │   ├── CommandCompleter.java
│   │   └── HistoryManager.java
│   └── config/                  # 配置类
│       ├── CLIProperties.java
│       └── TerminalConfiguration.java
```

### 数据流

1. **用户输入流**:
   ```
   用户输入 → LineReader → InputHandler → CommandParser 
   → Command → Service → AgentCore → 响应流
   ```

2. **渲染流**:
   ```
   响应数据 → TerminalRenderer → ColorScheme → EncodingManager 
   → Terminal输出
   ```

3. **会话管理流**:
   ```
   会话操作 → SessionManager → MemoryService → 文件系统
   ```

## 组件和接口

### 1. Command接口

所有命令的统一接口：

```java
public interface Command {
    /**
     * 命令名称（如 "chat", "new", "list"）
     */
    String getName();
    
    /**
     * 命令别名列表
     */
    List<String> getAliases();
    
    /**
     * 命令描述（用于help显示）
     */
    String getDescription();
    
    /**
     * 命令用法说明
     */
    String getUsage();
    
    /**
     * 执行命令
     * @param context 命令上下文（包含参数、会话信息等）
     * @return 执行结果（可能是Mono<Void>用于异步）
     */
    Mono<Void> execute(CommandContext context);
    
    /**
     * 是否需要活动会话
     */
    default boolean requiresSession() {
        return false;
    }
}
```

### 2. TerminalRenderer接口

终端渲染的核心接口：

```java
public interface TerminalRenderer {
    /**
     * 渲染用户提示符
     */
    void renderPrompt(String sessionName);
    
    /**
     * 渲染AI响应（支持流式）
     */
    void renderAIResponse(String chunk);
    
    /**
     * 渲染错误消息
     */
    void renderError(String message);
    
    /**
     * 渲染成功消息
     */
    void renderSuccess(String message);
    
    /**
     * 渲染警告消息
     */
    void renderWarning(String message);
    
    /**
     * 渲染系统消息
     */
    void renderSystem(String message);
    
    /**
     * 渲染表格
     */
    void renderTable(List<String> headers, List<List<String>> rows);
    
    /**
     * 渲染进度条
     */
    ProgressBar createProgressBar(String task, long total);
    
    /**
     * 渲染欢迎横幅
     */
    void renderBanner();
    
    /**
     * 清空屏幕
     */
    void clear();
}
```

### 3. SessionManager接口

会话管理核心接口：

```java
public interface SessionManager {
    /**
     * 创建新会话
     */
    Session createSession(String name);
    
    /**
     * 获取当前会话
     */
    Session getCurrentSession();
    
    /**
     * 切换到指定会话
     */
    void switchSession(String sessionId);
    
    /**
     * 列出所有会话
     */
    List<Session> listSessions();
    
    /**
     * 删除会话
     */
    void deleteSession(String sessionId);
    
    /**
     * 重命名会话
     */
    void renameSession(String sessionId, String newName);
    
    /**
     * 获取会话历史
     */
    List<Message> getSessionHistory(String sessionId);
    
    /**
     * 保存会话状态
     */
    void saveSession(Session session);
}
```

### 4. EncodingManager接口

跨平台编码管理：

```java
public interface EncodingManager {
    /**
     * 检测并设置平台编码
     */
    void setupPlatformEncoding();
    
    /**
     * 获取当前平台编码
     */
    Charset getPlatformCharset();
    
    /**
     * 编码字符串用于输出
     */
    String encodeForOutput(String text);
    
    /**
     * 解码输入字符串
     */
    String decodeInput(String text);
    
    /**
     * 检查字符是否可显示
     */
    boolean isDisplayable(char c);
}
```

### 5. ConfigManager接口

配置管理接口：

```java
public interface ConfigManager {
    /**
     * 加载配置
     */
    CLIConfig loadConfig();
    
    /**
     * 保存配置
     */
    void saveConfig(CLIConfig config);
    
    /**
     * 获取配置项
     */
    <T> T getConfigValue(String key, Class<T> type);
    
    /**
     * 设置配置项
     */
    void setConfigValue(String key, Object value);
    
    /**
     * 验证配置
     */
    boolean validateConfig(CLIConfig config);
    
    /**
     * 重置为默认配置
     */
    CLIConfig resetToDefaults();
}
```

### 6. CommandParser接口

命令解析接口：

```java
public interface CommandParser {
    /**
     * 解析用户输入
     */
    ParsedCommand parse(String input);
    
    /**
     * 检查是否是命令（以/开头）
     */
    boolean isCommand(String input);
    
    /**
     * 提取命令名称
     */
    String extractCommandName(String input);
    
    /**
     * 提取命令参数
     */
    List<String> extractArguments(String input);
}
```

## 数据模型

### Session模型

```java
@Data
@Builder
public class Session {
    /**
     * 会话唯一标识符
     */
    private String id;
    
    /**
     * 会话名称（用户可自定义）
     */
    private String name;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 最后活动时间
     */
    private LocalDateTime lastActiveAt;
    
    /**
     * 消息数量
     */
    private int messageCount;
    
    /**
     * 是否是当前活动会话
     */
    private boolean active;
    
    /**
     * 会话元数据
     */
    private Map<String, String> metadata;
}
```

### CLIConfig模型

```java
@Data
@Builder
@ConfigurationProperties(prefix = "openbutler.cli")
public class CLIConfig {
    /**
     * 主题颜色方案
     */
    private ColorScheme colorScheme;
    
    /**
     * 提示符样式
     */
    private String promptStyle;
    
    /**
     * 是否启用流式输出
     */
    private boolean streamingEnabled;
    
    /**
     * 历史记录保存数量
     */
    private int historySize;
    
    /**
     * 默认会话名称
     */
    private String defaultSessionName;
    
    /**
     * 是否启用调试模式
     */
    private boolean debugMode;
    
    /**
     * 自动保存间隔（秒）
     */
    private int autoSaveInterval;
    
    /**
     * 是否启用自动补全
     */
    private boolean autoCompleteEnabled;
    
    /**
     * 终端宽度（0表示自动检测）
     */
    private int terminalWidth;
}
```

### CommandContext模型

```java
@Data
@Builder
public class CommandContext {
    /**
     * 原始输入
     */
    private String rawInput;
    
    /**
     * 命令名称
     */
    private String commandName;
    
    /**
     * 命令参数
     */
    private List<String> arguments;
    
    /**
     * 当前会话
     */
    private Session currentSession;
    
    /**
     * 终端渲染器
     */
    private TerminalRenderer renderer;
    
    /**
     * 配置管理器
     */
    private ConfigManager configManager;
    
    /**
     * 会话管理器
     */
    private SessionManager sessionManager;
    
    /**
     * Agent核心服务
     */
    private AgentCore agentCore;
}
```

### ColorScheme模型

```java
@Data
@Builder
public class ColorScheme {
    /**
     * 用户输入颜色
     */
    private String userColor;
    
    /**
     * AI响应颜色
     */
    private String aiColor;
    
    /**
     * 错误消息颜色
     */
    private String errorColor;
    
    /**
     * 警告消息颜色
     */
    private String warningColor;
    
    /**
     * 系统消息颜色
     */
    private String systemColor;
    
    /**
     * 成功消息颜色
     */
    private String successColor;
    
    /**
     * 提示符颜色
     */
    private String promptColor;
    
    // 预定义主题
    public static ColorScheme DEFAULT = ColorScheme.builder()
        .userColor("\u001B[34m")      // 蓝色
        .aiColor("\u001B[32m")        // 绿色
        .errorColor("\u001B[31m")     // 红色
        .warningColor("\u001B[33m")   // 黄色
        .systemColor("\u001B[90m")    // 灰色
        .successColor("\u001B[32m")   // 绿色
        .promptColor("\u001B[36m")    // 青色
        .build();
}
```

### ParsedCommand模型

```java
@Data
@Builder
public class ParsedCommand {
    /**
     * 是否是命令（vs 普通聊天消息）
     */
    private boolean isCommand;
    
    /**
     * 命令名称
     */
    private String commandName;
    
    /**
     * 位置参数
     */
    private List<String> positionalArgs;
    
    /**
     * 命名参数（--key=value）
     */
    private Map<String, String> namedArgs;
    
    /**
     * 标志参数（--flag）
     */
    private Set<String> flags;
    
    /**
     * 原始输入
     */
    private String rawInput;
}
```


## 正确性属性

属性是系统所有有效执行中都应该保持为真的特征或行为——本质上是关于系统应该做什么的形式化陈述。属性作为人类可读规范和机器可验证正确性保证之间的桥梁。

### 属性反思

在分析了所有验收标准后，我识别出以下需要消除的冗余：

1. **命令支持属性合并**: 需求2.1-2.10都是测试单个命令是否被支持，这些可以合并为一个综合属性"所有定义的命令都能被正确识别和路由"
2. **颜色样式属性合并**: 需求3.2-3.6都是测试特定消息类型的颜色，这些是具体示例而非通用属性，保留为示例测试
3. **编码round trip合并**: 需求4.5-4.7都是测试编码往返，可以合并为一个属性"任何字符串经过编码和解码后保持不变"
4. **配置项属性合并**: 需求8.2-8.6都是测试单个配置项的保存和加载，可以合并为一个属性"任何配置项都能正确保存和加载"
5. **会话元数据属性**: 需求6.1-6.3都是测试会话元数据，可以合并为一个属性"所有会话都包含完整的元数据"

### 属性 1: 命令识别和路由

对于任何已注册的命令名称或别名，当用户输入该命令时，系统应该正确识别并路由到对应的命令处理器。

**验证需求: 2.1, 2.2, 2.3, 2.4, 2.5, 2.7**

### 属性 2: 非命令输入默认为聊天

对于任何不以命令前缀（/）开头的用户输入，系统应该将其解析为聊天消息并路由到chat命令处理器。

**验证需求: 2.11**

### 属性 3: ANSI颜色代码应用

对于任何消息类型（用户、AI、错误、警告、系统），渲染器输出应该包含对应的ANSI颜色代码。

**验证需求: 3.1**

### 属性 4: 文本样式应用

对于任何支持的文本样式（粗体、斜体、下划线），渲染器应该能够应用该样式并输出正确的ANSI转义序列。

**验证需求: 3.7**

### 属性 5: 表格渲染完整性

对于任何会话列表数据，表格渲染器应该生成包含所有行和列的格式化表格输出。

**验证需求: 3.8**

### 属性 6: 进度条更新

对于任何进度值（0-100%），进度条应该正确显示当前进度并在更新时刷新显示。

**验证需求: 3.9**

### 属性 7: 文本换行保持完整性

对于任何超过终端宽度的文本，换行后的输出应该包含原始文本的所有内容，且单词不被截断。

**验证需求: 3.10**

### 属性 8: 字符编码往返一致性

对于任何字符串（包括中文、emoji、特殊符号），经过编码管理器的编码和解码后，应该得到与原始字符串相等的结果。

**验证需求: 4.5, 4.6, 4.7**

### 属性 9: 会话唯一标识符

对于任何创建的会话集合，所有会话的ID应该是唯一的，不存在重复。

**验证需求: 6.1**

### 属性 10: 会话元数据完整性

对于任何创建的会话，该会话应该包含所有必需的元数据字段：ID、名称、创建时间、最后活动时间。

**验证需求: 6.2, 6.3**

### 属性 11: 会话重命名

对于任何会话和任何有效的新名称，重命名操作后，该会话的名称应该等于新名称。

**验证需求: 6.4**

### 属性 12: 会话列表完整性

对于任何已创建的会话集合，列表操作应该返回包含所有这些会话的列表。

**验证需求: 6.5**

### 属性 13: 会话切换正确性

对于任何存在的会话ID，切换到该会话后，当前会话应该等于指定的会话。

**验证需求: 6.6**

### 属性 14: 会话删除

对于任何存在的会话，删除操作后，该会话不应该出现在会话列表中。

**验证需求: 6.7**

### 属性 15: 会话历史加载

对于任何包含消息的会话，切换到该会话后，加载的历史消息应该与该会话存储的消息一致。

**验证需求: 6.9**

### 属性 16: 会话持久化往返

对于任何会话，保存到文件系统后再加载，应该得到与原始会话等价的会话对象。

**验证需求: 6.10**

### 属性 17: 命令历史记录

对于任何输入的命令序列，历史记录应该按顺序保存这些命令，且可以通过历史导航访问。

**验证需求: 7.1**

### 属性 18: 命令自动补全

对于任何命令前缀，自动补全建议应该包含所有以该前缀开头的已注册命令。

**验证需求: 7.2**

### 属性 19: 多行输入解析

对于任何使用反斜杠续行的多行输入，解析后应该得到一个连接所有行的单个命令字符串。

**验证需求: 7.5**

### 属性 20: 提示符包含会话名称

对于任何会话，渲染的提示符字符串应该包含该会话的名称。

**验证需求: 7.9**

### 属性 21: 配置持久化往返

对于任何配置对象，保存到文件后再加载，应该得到与原始配置等价的配置对象。

**验证需求: 8.1**

### 属性 22: 配置项保存和加载

对于任何有效的配置项键值对，设置后保存并重新加载，获取的值应该等于设置的值。

**验证需求: 8.2, 8.3, 8.4, 8.5, 8.6**

### 属性 23: 配置验证拒绝无效值

对于任何无效的配置值，配置验证应该返回失败，且配置不应该被更新。

**验证需求: 8.8**

### 属性 24: Config命令查看配置

对于任何当前配置状态，执行config查看命令应该返回包含所有配置项的输出。

**验证需求: 8.9**

### 属性 25: Config命令修改配置

对于任何有效的配置修改命令，执行后，对应的配置项应该被更新为新值。

**验证需求: 8.10**

### 属性 26: 命令失败显示错误

对于任何失败的命令执行，系统应该显示包含红色ANSI代码的错误消息。

**验证需求: 9.1**

### 属性 27: 命令成功显示反馈

对于任何成功的命令执行，系统应该显示包含绿色ANSI代码的成功消息。

**验证需求: 9.2**

### 属性 28: 无效命令提示相似命令

对于任何无效的命令输入，系统应该计算并提示与输入最相似的有效命令（基于编辑距离）。

**验证需求: 9.5**

### 属性 29: 错误日志记录

对于任何发生的错误，系统应该在日志文件中记录包含错误消息和时间戳的日志条目。

**验证需求: 9.6**

### 属性 30: 配置缓存减少IO

对于任何配置读取操作，如果配置已缓存且未过期，应该从缓存返回而不触发文件读取。

**验证需求: 10.6**


## 错误处理

### 错误分类

系统将错误分为以下几类：

1. **用户输入错误**: 无效命令、错误参数、格式错误
2. **系统错误**: 文件IO失败、配置损坏、内存不足
3. **网络错误**: API调用失败、超时、连接中断
4. **业务逻辑错误**: 会话不存在、权限不足、状态冲突

### 错误处理策略

#### 1. 用户输入错误

```java
// 策略：友好提示 + 建议
try {
    Command command = commandRegistry.getCommand(commandName);
} catch (CommandNotFoundException e) {
    List<String> suggestions = commandRegistry.findSimilarCommands(commandName);
    renderer.renderError("Unknown command: " + commandName);
    if (!suggestions.isEmpty()) {
        renderer.renderSystem("Did you mean: " + String.join(", ", suggestions) + "?");
    }
    renderer.renderSystem("Type 'help' to see all available commands.");
}
```

#### 2. 系统错误

```java
// 策略：记录日志 + 优雅降级
try {
    config = configManager.loadConfig();
} catch (IOException e) {
    logger.error("Failed to load config", e);
    renderer.renderWarning("Could not load configuration, using defaults");
    config = configManager.getDefaultConfig();
}
```

#### 3. 网络错误

```java
// 策略：重试 + 详细反馈
try {
    return agentCore.process(sessionId, input)
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
            .filter(throwable -> throwable instanceof TimeoutException))
        .onErrorResume(throwable -> {
            renderer.renderError("Failed to get AI response: " + throwable.getMessage());
            if (throwable instanceof TimeoutException) {
                renderer.renderSystem("The request timed out. Please try again.");
            } else if (throwable instanceof ConnectException) {
                renderer.renderSystem("Could not connect to AI service. Check your network.");
            }
            return Mono.empty();
        });
} catch (Exception e) {
    logger.error("Unexpected error in chat", e);
    renderer.renderError("An unexpected error occurred");
}
```

#### 4. 致命错误

```java
// 策略：保存状态 + 优雅退出
Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
    logger.error("Fatal error in thread " + thread.getName(), throwable);
    try {
        // 保存当前会话状态
        sessionManager.saveAllSessions();
        renderer.renderError("A fatal error occurred. Your session has been saved.");
    } catch (Exception e) {
        logger.error("Failed to save session during fatal error", e);
    }
    System.exit(1);
});
```

### 错误消息设计原则

1. **清晰性**: 使用简单明了的语言描述问题
2. **可操作性**: 提供用户可以采取的具体行动
3. **上下文**: 包含足够的上下文信息帮助理解问题
4. **一致性**: 使用统一的错误消息格式

### 错误消息模板

```
[错误类型] 简短描述
详细信息（如果有）
建议操作（如果有）
```

示例：
```
[Command Error] Unknown command: chatt
Did you mean: chat?
Type 'help' to see all available commands.
```

## 测试策略

### 双重测试方法

系统采用单元测试和基于属性的测试相结合的方法：

#### 单元测试

单元测试专注于：
- 具体示例和边界情况
- 组件集成点
- 错误条件和异常处理
- 特定的业务逻辑场景

示例：
```java
@Test
void testWelcomeBannerDisplayedOnStartup() {
    // 测试启动时显示欢迎横幅
    cli.start();
    verify(renderer).renderBanner();
}

@Test
void testWindowsPlatformSelectsGBKEncoding() {
    // 测试Windows平台选择GBK编码
    when(platformDetector.getOS()).thenReturn(OS.WINDOWS);
    encodingManager.setupPlatformEncoding();
    assertEquals(Charset.forName("GBK"), encodingManager.getPlatformCharset());
}

@Test
void testCtrlCInterruptsCurrentOperation() {
    // 测试Ctrl+C中断当前操作
    CompletableFuture<Void> operation = cli.startLongRunningOperation();
    cli.handleInterrupt();
    assertTrue(operation.isCancelled());
}
```

#### 基于属性的测试

基于属性的测试验证通用属性，使用QuickTheories库生成随机输入：

配置要求：
- 每个属性测试最少运行100次迭代
- 每个测试必须引用设计文档中的属性
- 标签格式：`Feature: modern-cli-refactor, Property {number}: {property_text}`

示例：
```java
@Test
@Tag("Feature: modern-cli-refactor, Property 8: 字符编码往返一致性")
void testEncodingRoundTripConsistency() {
    qt()
        .forAll(strings().allPossible().ofLengthBetween(0, 1000))
        .checkAssert(originalText -> {
            String encoded = encodingManager.encodeForOutput(originalText);
            String decoded = encodingManager.decodeInput(encoded);
            assertEquals(originalText, decoded);
        });
}

@Test
@Tag("Feature: modern-cli-refactor, Property 9: 会话唯一标识符")
void testSessionIdsAreUnique() {
    qt()
        .forAll(integers().between(1, 100))
        .checkAssert(count -> {
            List<Session> sessions = IntStream.range(0, count)
                .mapToObj(i -> sessionManager.createSession("Session " + i))
                .collect(Collectors.toList());
            
            Set<String> uniqueIds = sessions.stream()
                .map(Session::getId)
                .collect(Collectors.toSet());
            
            assertEquals(sessions.size(), uniqueIds.size());
        });
}

@Test
@Tag("Feature: modern-cli-refactor, Property 16: 会话持久化往返")
void testSessionPersistenceRoundTrip() {
    qt()
        .forAll(
            strings().allPossible().ofLengthBetween(1, 50),
            integers().between(0, 100)
        )
        .checkAssert((sessionName, messageCount) -> {
            // 创建会话并添加消息
            Session original = sessionManager.createSession(sessionName);
            for (int i = 0; i < messageCount; i++) {
                original.addMessage(new Message("user", "Message " + i));
            }
            
            // 保存并重新加载
            sessionManager.saveSession(original);
            Session loaded = sessionManager.loadSession(original.getId());
            
            // 验证等价性
            assertEquals(original.getId(), loaded.getId());
            assertEquals(original.getName(), loaded.getName());
            assertEquals(original.getMessageCount(), loaded.getMessageCount());
        });
}

@Test
@Tag("Feature: modern-cli-refactor, Property 18: 命令自动补全")
void testCommandAutoCompletion() {
    qt()
        .forAll(strings().allPossible().ofLengthBetween(1, 10))
        .checkAssert(prefix -> {
            List<String> suggestions = commandCompleter.complete(prefix);
            
            // 所有建议都应该以前缀开头
            assertTrue(suggestions.stream()
                .allMatch(cmd -> cmd.startsWith(prefix)));
            
            // 所有以前缀开头的命令都应该在建议中
            List<String> allCommands = commandRegistry.getAllCommandNames();
            List<String> expected = allCommands.stream()
                .filter(cmd -> cmd.startsWith(prefix))
                .collect(Collectors.toList());
            
            assertTrue(suggestions.containsAll(expected));
        });
}
```

### 测试覆盖率目标

- 代码覆盖率：>80%
- 分支覆盖率：>75%
- 所有正确性属性必须有对应的属性测试
- 所有错误处理路径必须有单元测试

### 测试环境

- 使用内存文件系统（如Jimfs）进行文件IO测试
- 使用Mock对象隔离外部依赖（AgentCore、Terminal）
- 使用TestContainers进行集成测试（如果需要）

### 持续集成

- 所有测试在每次提交时运行
- 属性测试在夜间构建中运行更多迭代（1000+）
- 性能测试在发布前运行


## 关键技术决策

### 1. 删除Spring Shell依赖

**决策**: 移除Spring Shell，使用JLine 3直接构建CLI

**理由**:
- Spring Shell对GraalVM Native Image支持有限，需要大量反射配置
- 我们需要更细粒度的控制来实现现代化终端特性
- JLine 3提供了足够的底层功能（readline、历史、补全）
- 减少依赖可以降低原生镜像大小

**影响**:
- 需要手动实现命令注册和路由
- 需要自己处理命令解析
- 更灵活的命令系统设计

### 2. 使用Jansi进行跨平台ANSI支持

**决策**: 使用Jansi库处理ANSI颜色代码

**理由**:
- Jansi在Windows上自动启用ANSI支持（通过JNI）
- 在Linux/Mac上直接透传ANSI代码
- 提供统一的API，无需平台检测
- 支持GraalVM Native Image

**实现**:
```java
@Configuration
public class TerminalConfiguration {
    @Bean
    public Terminal terminal() {
        // Jansi会自动处理Windows ANSI支持
        AnsiConsole.systemInstall();
        return TerminalBuilder.builder()
            .system(true)
            .jansi(true)
            .build();
    }
}
```

### 3. 编码管理策略

**决策**: 在应用启动时检测平台并设置系统属性

**理由**:
- Java的默认编码在Windows中文环境下是GBK
- 需要在JVM启动早期设置编码以避免乱码
- 使用系统属性确保所有组件使用一致的编码

**实现**:
```java
@Component
public class EncodingManager implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        String os = System.getProperty("os.name").toLowerCase();
        Charset charset;
        
        if (os.contains("win")) {
            charset = Charset.forName("GBK");
        } else {
            charset = StandardCharsets.UTF_8;
        }
        
        System.setProperty("file.encoding", charset.name());
        System.setProperty("sun.stdout.encoding", charset.name());
        System.setProperty("sun.stderr.encoding", charset.name());
    }
}
```

### 4. GraalVM Native Image配置

**决策**: 使用GraalVM Native Image Maven Plugin + 手动反射配置

**POM配置**:
```xml
<profile>
    <id>native</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.graalvm.buildtools</groupId>
                <artifactId>native-maven-plugin</artifactId>
                <version>0.10.1</version>
                <executions>
                    <execution>
                        <id>build-native</id>
                        <goals>
                            <goal>compile-no-fork</goal>
                        </goals>
                        <phase>package</phase>
                    </execution>
                </executions>
                <configuration>
                    <imageName>openbutler</imageName>
                    <mainClass>com.openbutler.OpenButlerApplication</mainClass>
                    <buildArgs>
                        <buildArg>--no-fallback</buildArg>
                        <buildArg>--enable-url-protocols=https</buildArg>
                        <buildArg>-H:+ReportExceptionStackTraces</buildArg>
                    </buildArgs>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

**反射配置** (src/main/resources/META-INF/native-image/reflect-config.json):
```json
[
  {
    "name": "com.openbutler.cli.terminal.command.builtin.ChatCommand",
    "allDeclaredConstructors": true,
    "allPublicMethods": true
  },
  {
    "name": "com.openbutler.cli.terminal.model.Session",
    "allDeclaredConstructors": true,
    "allDeclaredFields": true,
    "allPublicMethods": true
  }
]
```

### 5. 命令注册机制

**决策**: 使用Spring的组件扫描 + 自定义注解

**实现**:
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface CLICommand {
    String name();
    String[] aliases() default {};
}

@Service
public class CommandRegistry {
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    
    @Autowired
    public CommandRegistry(List<Command> commandBeans) {
        commandBeans.forEach(cmd -> {
            commands.put(cmd.getName(), cmd);
            cmd.getAliases().forEach(alias -> commands.put(alias, cmd));
        });
    }
    
    public Command getCommand(String name) {
        return commands.get(name);
    }
}
```

### 6. 异步处理架构

**决策**: 使用Project Reactor进行响应式编程

**理由**:
- Spring AI已经使用Reactor，保持一致性
- 非阻塞IO提升响应性
- 流式处理AI响应

**实现**:
```java
@CLICommand(name = "chat")
public class ChatCommand implements Command {
    
    @Override
    public Mono<Void> execute(CommandContext context) {
        String message = String.join(" ", context.getArguments());
        
        return context.getAgentCore()
            .process(context.getCurrentSession().getId(), message)
            .doOnNext(chunk -> context.getRenderer().renderAIResponse(chunk))
            .doOnError(error -> context.getRenderer().renderError(error.getMessage()))
            .then();
    }
}
```

### 7. 配置文件格式

**决策**: 使用YAML格式存储用户配置

**位置**: `~/.openbutler/config.yml`

**示例**:
```yaml
cli:
  theme:
    user-color: "\u001B[34m"
    ai-color: "\u001B[32m"
    error-color: "\u001B[31m"
    warning-color: "\u001B[33m"
    system-color: "\u001B[90m"
  prompt-style: "{{session}} > "
  streaming-enabled: true
  history-size: 1000
  default-session-name: "default"
  debug-mode: false
  auto-save-interval: 30
  auto-complete-enabled: true
  terminal-width: 0
```

### 8. 会话存储格式

**决策**: 使用JSON格式存储会话数据

**位置**: `~/.openbutler/sessions/{session-id}.json`

**示例**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "My Project",
  "createdAt": "2024-01-15T10:30:00Z",
  "lastActiveAt": "2024-01-15T15:45:00Z",
  "messageCount": 42,
  "metadata": {
    "tags": "project,important",
    "description": "Working on CLI refactor"
  }
}
```

### 9. 表格渲染实现

**决策**: 使用ASCII Table库进行表格渲染

**依赖**:
```xml
<dependency>
    <groupId>de.vandermeer</groupId>
    <artifactId>asciitable</artifactId>
    <version>0.3.2</version>
</dependency>
```

**实现**:
```java
public void renderSessionTable(List<Session> sessions) {
    AsciiTable table = new AsciiTable();
    table.addRule();
    table.addRow("ID", "Name", "Messages", "Last Active");
    table.addRule();
    
    sessions.forEach(session -> {
        table.addRow(
            session.getId().substring(0, 8),
            session.getName(),
            session.getMessageCount(),
            formatTimestamp(session.getLastActiveAt())
        );
        table.addRule();
    });
    
    terminal.writer().println(table.render());
}
```

### 10. 进度条实现

**决策**: 使用ProgressBar库

**依赖**:
```xml
<dependency>
    <groupId>me.tongfei</groupId>
    <artifactId>progressbar</artifactId>
    <version>0.10.0</version>
</dependency>
```

**实现**:
```java
public ProgressBar createProgressBar(String task, long total) {
    return new ProgressBarBuilder()
        .setTaskName(task)
        .setInitialMax(total)
        .setStyle(ProgressBarStyle.ASCII)
        .build();
}
```

## 性能优化

### 1. 配置缓存

```java
@Service
public class ConfigManager {
    private volatile CLIConfig cachedConfig;
    private volatile long lastLoadTime;
    private static final long CACHE_TTL = 60_000; // 1分钟
    
    public CLIConfig loadConfig() {
        long now = System.currentTimeMillis();
        if (cachedConfig != null && (now - lastLoadTime) < CACHE_TTL) {
            return cachedConfig;
        }
        
        synchronized (this) {
            if (cachedConfig != null && (now - lastLoadTime) < CACHE_TTL) {
                return cachedConfig;
            }
            cachedConfig = loadConfigFromFile();
            lastLoadTime = now;
            return cachedConfig;
        }
    }
}
```

### 2. 会话历史懒加载

```java
public class Session {
    private String id;
    private String name;
    private transient List<Message> messages; // 不序列化
    
    public List<Message> getMessages() {
        if (messages == null) {
            messages = loadMessagesFromFile();
        }
        return messages;
    }
}
```

### 3. 命令补全缓存

```java
@Service
public class CommandCompleter {
    private final List<String> allCommands;
    private final Map<String, List<String>> completionCache = new ConcurrentHashMap<>();
    
    public List<String> complete(String prefix) {
        return completionCache.computeIfAbsent(prefix, p -> 
            allCommands.stream()
                .filter(cmd -> cmd.startsWith(p))
                .collect(Collectors.toList())
        );
    }
}
```

### 4. 异步会话保存

```java
@Service
public class SessionManager {
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();
    
    public void saveSessionAsync(Session session) {
        saveExecutor.submit(() -> {
            try {
                saveSessionToFile(session);
            } catch (IOException e) {
                logger.error("Failed to save session", e);
            }
        });
    }
}
```

## 部署和打包

### Maven构建命令

```bash
# 标准JAR打包
mvn clean package

# GraalVM Native Image打包
mvn clean package -Pnative

# 跳过测试的快速构建
mvn clean package -DskipTests

# 运行属性测试（更多迭代）
mvn test -Dquicktheories.iterations=1000
```

### 运行方式

```bash
# JAR方式
java -jar target/openbutler-1.0.0.jar

# Native方式（Windows）
target/openbutler.exe

# Native方式（Linux/Mac）
./target/openbutler
```

### 分发包结构

```
openbutler-1.0.0/
├── bin/
│   ├── openbutler.exe      # Windows可执行文件
│   ├── openbutler          # Linux/Mac可执行文件
│   └── openbutler.bat      # Windows启动脚本（JAR模式）
├── lib/
│   └── openbutler-1.0.0.jar
├── config/
│   └── application.yml     # 默认配置
├── README.md
└── LICENSE
```

