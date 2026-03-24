# 实现计划: Modern CLI Refactor

## 概述

本实现计划将OpenButler的CLI系统从简单的命令行界面重构为功能丰富、样式现代、跨平台兼容的终端系统，并支持GraalVM Native Image打包。实现将采用分层架构，从底层平台支持到上层命令系统逐步构建。

## 任务

- [x] 1. 删除旧CLI代码并创建新包结构
  - 删除 src/main/java/com/openbutler/cli/commands/OpenButlerCommands.java
  - 删除 src/main/java/com/openbutler/cli/OpenButlerRunner.java
  - 删除 src/main/java/com/openbutler/cli/ui/TerminalUI.java
  - 创建新的包结构：terminal/command/builtin, terminal/service, terminal/rendering, terminal/encoding, terminal/model, terminal/input, terminal/config
  - _需求: 1.1, 1.2, 1.3, 1.4_

- [ ] 2. 实现平台层（编码管理和平台检测）
  - [x] 2.1 实现 PlatformDetector 类
    - 检测操作系统类型（Windows/Linux/macOS）
    - 提供平台信息查询方法
    - _需求: 4.1, 4.2, 4.3_
  
  - [x] 2.2 实现 EncodingManager 类
    - 实现 ApplicationContextInitializer 接口在启动时设置编码
    - 根据平台自动选择编码（Windows GBK, Linux/Mac UTF-8）
    - 实现编码回退机制
    - 提供编码/解码方法
    - _需求: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8_
  
  - [ ]* 2.3 编写 EncodingManager 的属性测试
    - **属性 8: 字符编码往返一致性**
    - **验证需求: 4.5, 4.6, 4.7**
    - 测试任意字符串（中文、emoji、特殊符号）经过编码解码后保持不变

- [ ] 3. 实现数据模型层
  - [x] 3.1 创建 Session 模型类
    - 定义会话属性：id, name, createdAt, lastActiveAt, messageCount, active, metadata
    - 实现懒加载消息列表
    - 使用 @Data 和 @Builder 注解
    - _需求: 6.1, 6.2, 6.3, 6.4, 6.10_
  
  - [x] 3.2 创建 CLIConfig 模型类
    - 定义配置属性：colorScheme, promptStyle, streamingEnabled, historySize, defaultSessionName, debugMode, autoSaveInterval, autoCompleteEnabled, terminalWidth
    - 使用 @ConfigurationProperties 注解
    - _需求: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_
  
  - [x] 3.3 创建 ColorScheme 模型类
    - 定义颜色属性和ANSI代码
    - 提供 DEFAULT 预定义主题
    - _需求: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 8.2_
  
  - [x] 3.4 创建 CommandContext 模型类
    - 包含命令执行所需的所有上下文信息
    - _需求: 2.1-2.11_
  
  - [x] 3.5 创建 ParsedCommand 模型类
    - 支持位置参数、命名参数、标志参数
    - _需求: 2.1-2.11_

- [ ] 4. 实现渲染层
  - [x] 4.1 实现 TerminalRenderer 接口和实现类
    - 实现 renderPrompt 方法（显示会话名称）
    - 实现 renderAIResponse 方法（支持流式输出）
    - 实现 renderError, renderSuccess, renderWarning, renderSystem 方法
    - 实现 clear 方法
    - 使用 Jansi 库应用ANSI颜色代码
    - _需求: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 7.9, 9.1, 9.2_
  
  - [x] 4.2 实现 BannerRenderer 类
    - 渲染欢迎横幅
    - 显示版本信息和基本使用提示
    - _需求: 3.11_
  
  - [x] 4.3 实现 TableFormatter 类
    - 使用 ASCII Table 库格式化表格
    - 支持会话列表显示
    - _需求: 3.8_
  
  - [x] 4.4 实现 ProgressBar 包装类
    - 集成 ProgressBar 库
    - 提供进度更新方法
    - _需求: 3.9, 9.3_
  
  - [x] 4.5 实现文本换行逻辑
    - 检测终端宽度
    - 实现智能换行（不截断单词）
    - _需求: 3.10_
  
  - [ ]* 4.6 编写渲染层的单元测试
    - 测试各种消息类型的颜色代码
    - 测试表格格式化
    - 测试文本换行

- [ ] 5. 实现配置管理
  - [x] 5.1 实现 ConfigManager 服务类
    - 实现配置加载和保存（YAML格式）
    - 实现配置缓存机制（TTL 1分钟）
    - 实现配置验证
    - 实现默认配置生成
    - 配置文件位置：~/.openbutler/config.yml
    - _需求: 8.1, 8.7, 8.8, 10.6_
  
  - [x] 5.2 创建 TerminalConfiguration 配置类
    - 配置 JLine Terminal bean
    - 启用 Jansi 支持
    - _需求: 3.1_
  
  - [ ]* 5.3 编写 ConfigManager 的属性测试
    - **属性 21: 配置持久化往返**
    - **属性 22: 配置项保存和加载**
    - **验证需求: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6**
    - 测试任意配置对象保存后加载保持一致
  
  - [ ]* 5.4 编写配置验证的单元测试
    - **属性 23: 配置验证拒绝无效值**
    - **验证需求: 8.8**
    - 测试无效配置值被拒绝

- [ ] 6. 实现会话管理
  - [x] 6.1 实现 SessionManager 服务类
    - 实现创建会话（生成唯一ID）
    - 实现获取当前会话
    - 实现切换会话
    - 实现列出所有会话
    - 实现删除会话
    - 实现重命名会话
    - 实现会话持久化（JSON格式）
    - 实现异步保存机制
    - 会话文件位置：~/.openbutler/sessions/{session-id}.json
    - 启动时自动创建默认会话
    - _需求: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 6.10_
  
  - [ ]* 6.2 编写 SessionManager 的属性测试
    - **属性 9: 会话唯一标识符**
    - **属性 10: 会话元数据完整性**
    - **属性 11: 会话重命名**
    - **属性 12: 会话列表完整性**
    - **属性 13: 会话切换正确性**
    - **属性 14: 会话删除**
    - **属性 16: 会话持久化往返**
    - **验证需求: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.10**
    - 测试会话ID唯一性、元数据完整性、持久化一致性
  
  - [ ]* 6.3 编写会话历史加载的单元测试
    - **属性 15: 会话历史加载**
    - **验证需求: 6.9**
    - 测试切换会话后历史消息正确加载

- [x] 7. Checkpoint - 验证核心基础设施
  - 确保所有测试通过，询问用户是否有问题

- [ ] 8. 实现输入处理层
  - [x] 8.1 实现 CommandParser 服务类
    - 检查输入是否是命令（以/开头）
    - 解析命令名称和参数
    - 支持位置参数、命名参数（--key=value）、标志参数（--flag）
    - _需求: 2.1-2.11_
  
  - [x] 8.2 实现 LineReaderFactory 类
    - 配置 JLine LineReader
    - 启用历史记录功能
    - 配置自动补全
    - 配置快捷键（Ctrl+C, Ctrl+D, Ctrl+L）
    - _需求: 7.1, 7.2, 7.3, 7.4, 7.6, 7.7, 7.8_
  
  - [x] 8.3 实现 CommandCompleter 类
    - 实现 JLine Completer 接口
    - 提供命令名称自动补全
    - 实现补全缓存
    - _需求: 7.2, 10.6_
  
  - [x] 8.4 实现 HistoryManager 类
    - 配置历史记录保存位置
    - 实现历史记录大小限制
    - _需求: 7.1, 8.5_
  
  - [x] 8.5 实现 InputHandler 服务类
    - 处理用户输入循环
    - 处理多行输入（反斜杠续行）
    - 处理中断信号（Ctrl+C）
    - _需求: 7.3, 7.5, 10.1_
  
  - [ ]* 8.6 编写输入处理的属性测试
    - **属性 2: 非命令输入默认为聊天**
    - **属性 17: 命令历史记录**
    - **属性 18: 命令自动补全**
    - **属性 19: 多行输入解析**
    - **验证需求: 2.11, 7.1, 7.2, 7.5**
    - 测试命令解析、历史记录、自动补全的正确性

- [ ] 9. 实现命令系统基础
  - [x] 9.1 创建 Command 接口
    - 定义命令方法：getName, getAliases, getDescription, getUsage, execute, requiresSession
    - execute 方法返回 Mono<Void> 支持异步
    - _需求: 2.1-2.11_
  
  - [x] 9.2 实现 CommandRegistry 服务类
    - 使用 Spring 自动注入所有 Command beans
    - 支持命令名称和别名查找
    - 实现相似命令查找（编辑距离算法）
    - _需求: 2.1-2.11, 9.5_
  
  - [ ]* 9.3 编写 CommandRegistry 的属性测试
    - **属性 1: 命令识别和路由**
    - **属性 28: 无效命令提示相似命令**
    - **验证需求: 2.1-2.10, 9.5**
    - 测试所有注册命令都能被正确识别和路由

- [ ] 10. 实现内置命令
  - [x] 10.1 实现 ChatCommand
    - 处理聊天消息（默认命令）
    - 调用 AgentCore 处理消息
    - 流式显示AI响应
    - 异步处理，支持中断
    - _需求: 2.1, 2.11, 8.4, 10.3, 10.4_
  
  - [x] 10.2 实现 NewCommand
    - 创建新会话
    - 可选参数：会话名称
    - 显示成功消息
    - _需求: 2.2, 6.8, 9.2_
  
  - [x] 10.3 实现 ListCommand
    - 列出所有会话
    - 使用表格格式显示
    - 显示会话ID、名称、消息数、最后活动时间
    - 标记当前活动会话
    - _需求: 2.3, 3.8_
  
  - [x] 10.4 实现 SwitchCommand
    - 切换到指定会话
    - 参数：会话ID或名称
    - 加载会话历史
    - 显示切换成功消息
    - _需求: 2.4, 6.6, 6.9, 9.2_
  
  - [x] 10.5 实现 HistoryCommand
    - 显示当前会话的历史消息
    - 支持限制显示数量
    - 使用颜色区分用户和AI消息
    - _需求: 2.5_
  
  - [x] 10.6 实现 ClearCommand
    - 清空终端屏幕
    - _需求: 2.6_
  
  - [x] 10.7 实现 ConfigCommand
    - 子命令：show（查看配置）、set（修改配置）、reset（重置配置）
    - 验证配置值有效性
    - 显示配置修改成功消息
    - _需求: 2.7, 8.8, 8.9, 8.10_
  
  - [x] 10.8 实现 HelpCommand
    - 列出所有可用命令
    - 显示每个命令的描述和用法
    - 支持查看特定命令的详细帮助
    - _需求: 2.8_
  
  - [x] 10.9 实现 VersionCommand
    - 显示应用版本信息
    - 显示Java版本和操作系统信息
    - _需求: 2.9_
  
  - [x] 10.10 实现 ExitCommand
    - 保存当前会话状态
    - 显示退出消息
    - 优雅关闭应用
    - _需求: 2.10, 9.8_
  
  - [ ]* 10.11 编写命令执行的单元测试
    - 测试每个命令的基本功能
    - 测试命令参数解析
    - 测试错误处理

- [x] 11. 实现CLI主入口
  - [x] 11.1 创建 OpenButlerCLI 主类
    - 实现 CommandLineRunner 接口
    - 初始化所有组件
    - 显示欢迎横幅
    - 启动输入循环
    - 处理命令执行
    - 实现优雅关闭
    - _需求: 3.11, 10.1, 10.2, 10.8_
  
  - [x] 11.2 配置 Spring Boot 主类
    - 注册 EncodingManager 为 ApplicationContextInitializer
    - 配置异步执行器
    - _需求: 4.8, 10.3_
  
  - [ ]* 11.3 编写集成测试
    - 测试应用启动流程
    - 测试命令执行流程
    - 测试会话管理流程

- [ ] 12. Checkpoint - 验证功能完整性
  - 确保所有测试通过，询问用户是否有问题

- [x] 13. 实现错误处理和日志
  - [x] 13.1 实现全局异常处理器
    - 捕获命令执行异常
    - 显示友好的错误消息
    - 记录详细错误日志
    - _需求: 9.1, 9.4, 9.6_
  
  - [x] 13.2 实现致命错误处理
    - 设置 UncaughtExceptionHandler
    - 保存会话状态
    - 优雅退出
    - _需求: 9.8_
  
  - [x] 13.3 配置日志系统
    - 配置 logback 输出到文件
    - 调试模式下显示堆栈跟踪
    - _需求: 9.6, 9.7_
  
  - [ ]* 13.4 编写错误处理的单元测试
    - **属性 26: 命令失败显示错误**
    - **属性 27: 命令成功显示反馈**
    - **属性 29: 错误日志记录**
    - **验证需求: 9.1, 9.2, 9.6**
    - 测试各种错误场景的处理

- [x] 14. 配置 GraalVM Native Image
  - [x] 14.1 添加 Maven Native Image 插件配置
    - 配置 native profile
    - 设置构建参数
    - 配置主类
    - _需求: 5.1, 5.2, 5.3, 5.4, 5.10_
  
  - [x] 14.2 创建反射配置文件
    - 配置 Command 类的反射
    - 配置 Model 类的反射
    - 配置 Spring 相关类的反射
    - 文件位置：src/main/resources/META-INF/native-image/reflect-config.json
    - _需求: 5.6_
  
  - [x] 14.3 创建资源配置文件
    - 包含配置文件（application.yml）
    - 包含日志配置（logback-spring.xml）
    - 文件位置：src/main/resources/META-INF/native-image/resource-config.json
    - _需求: 5.7_
  
  - [x] 14.4 优化原生镜像大小
    - 排除不必要的依赖
    - 配置 --no-fallback 参数
    - 验证生成的可执行文件大小 < 100MB
    - _需求: 5.9_
  
  - [ ]* 14.5 测试原生镜像构建
    - 执行 mvn clean package -Pnative
    - 验证构建成功
    - 测试生成的可执行文件
    - _需求: 5.1, 5.2, 5.3, 5.4, 5.8_

- [ ] 15. 性能优化和最终测试
  - [ ] 15.1 实现性能优化
    - 验证配置缓存工作正常
    - 验证会话历史懒加载
    - 验证命令补全缓存
    - 验证异步会话保存
    - _需求: 10.6_
  
  - [ ] 15.2 性能基准测试
    - 测试输入响应时间 < 100ms
    - 测试命令解析时间 < 500ms
    - 测试应用启动时间 < 2s
    - _需求: 10.1, 10.2, 10.8_
  
  - [ ]* 15.3 运行完整测试套件
    - 运行所有单元测试
    - 运行所有属性测试（100+ 迭代）
    - 验证代码覆盖率 > 80%
  
  - [ ] 15.4 跨平台测试
    - 在 Windows 上测试 GBK 编码
    - 在 Linux 上测试 UTF-8 编码
    - 在 macOS 上测试 UTF-8 编码
    - 测试中文、emoji、特殊符号显示
    - _需求: 4.1, 4.2, 4.3, 4.5, 4.6, 4.7_

- [ ] 16. 最终 Checkpoint
  - 确保所有测试通过，询问用户是否准备好发布

## 注意事项

- 标记 `*` 的任务为可选测试任务，可以跳过以加快MVP开发
- 每个任务都引用了具体的需求编号以确保可追溯性
- Checkpoint 任务确保增量验证
- 属性测试验证通用正确性属性
- 单元测试验证具体示例和边界情况
