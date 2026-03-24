# OpenButler CLI 运行指南

## 快速开始

### 方法 1: 使用开发模式（推荐用于开发）

```bash
run-dev.bat
```

这个脚本会：
- **自动检测终端编码**（GBK或UTF-8）
- 设置正确的JVM编码参数
- 使用Maven直接运行应用（无需打包）

### 方法 2: 使用打包后的JAR

```bash
run.bat
```

这个脚本会：
- **自动检测终端编码**（GBK或UTF-8）
- 如果JAR不存在，自动构建
- 使用正确的编码参数运行JAR

### 方法 3: 手动运行

#### 开发模式
```bash
# 在GBK终端（chcp 936）
set MAVEN_OPTS=-Dfile.encoding=GBK -Dsun.stdout.encoding=GBK -Dsun.stderr.encoding=GBK
mvn spring-boot:run

# 在UTF-8终端（chcp 65001）
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8
mvn spring-boot:run
```

#### 生产模式
```bash
# 先构建
mvn clean package -DskipTests

# 在GBK终端运行
java -Dfile.encoding=GBK -Dsun.stdout.encoding=GBK -Dsun.stderr.encoding=GBK -jar target/openbutler-1.0.0.jar

# 在UTF-8终端运行
java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -jar target/openbutler-1.0.0.jar
```

## 编码说明

### 自动编码检测

启动脚本（`run-dev.bat` 和 `run.bat`）会**自动检测**当前终端的代码页：
- **代码页 936** → 使用 GBK 编码
- **代码页 65001** → 使用 UTF-8 编码
- **其他代码页** → 默认使用 UTF-8 编码

### 查看当前终端编码

```bash
chcp
```

输出示例：
- `Active code page: 936` → GBK编码
- `Active code page: 65001` → UTF-8编码

### 切换终端编码

```bash
# 切换到GBK
chcp 936

# 切换到UTF-8
chcp 65001
```

**注意**: 切换编码后需要重启应用才能生效。

## 环境要求

- Java 17 或更高版本
- Maven 3.6 或更高版本
- Windows 操作系统（脚本为Windows批处理文件）

## 配置

### API密钥配置

在运行前，需要配置OpenAI API密钥：

#### 方法1: 环境变量（推荐）
```bash
set OPENAI_API_KEY=your-api-key-here
```

#### 方法2: 配置文件
编辑 `src/main/resources/application.yml`:
```yaml
openai:
  api-key: your-api-key-here
```

## 启动成功的标志

当应用成功启动时，你会看到：

```
+============================================================+
|                    OpenButler CLI                          |
|                      Version 1.0.0                         |
+============================================================+
|  Quick Start:                                              |
|    * Type your message to chat with AI                    |
|    * Use /help to see all available commands               |
|    * Use /new to create a new session                     |
|    * Use /exit to quit the application                    |
+============================================================+

default >
```

## 可用命令

启动后，你可以使用以下命令：

- **直接输入文字** - 与 AI 聊天（不需要命令前缀）
- `/help` 或 `/h` - 查看所有可用命令
- `/new [名称]` - 创建新会话
- `/list` - 列出所有会话
- `/switch <会话ID>` - 切换到指定会话
- `/history [数量]` - 查看当前会话历史
- `/clear` - 清空终端屏幕
- `/config show` - 查看当前配置
- `/config set <键> <值>` - 修改配置
- `/version` - 显示版本信息
- `/exit` - 退出应用

## 常见问题

### 1. 中文显示乱码

**原因**: JVM编码参数与终端编码不匹配

**解决方案**:
1. **推荐**: 使用提供的启动脚本（`run-dev.bat` 或 `run.bat`），它们会自动检测编码
2. 如果仍然乱码，手动指定编码参数（见上面的"方法3: 手动运行"）
3. 确保终端编码与JVM参数一致：
   - GBK终端（`chcp 936`）→ 使用GBK参数
   - UTF-8终端（`chcp 65001`）→ 使用UTF-8参数

### 2. 应用启动后立即退出

**原因**: 可能是依赖冲突或配置错误

**解决方案**:
1. 检查日志输出
2. 确保所有依赖正确安装：`mvn clean install`
3. 检查Java版本：`java -version`（需要Java 17+）

### 3. Maven构建失败

**解决方案**:
```bash
# 清理并重新构建
mvn clean install

# 跳过测试构建
mvn clean package -DskipTests
```

### 4. 找不到JAR文件

**解决方案**:
```bash
# 手动构建JAR
mvn clean package -DskipTests
```

JAR文件会生成在 `target/openbutler-1.0.0.jar`

## 快捷键

- **Ctrl+C** - 中断当前操作
- **Ctrl+D** - 退出应用
- **Ctrl+L** - 清空屏幕
- **上/下箭头** - 浏览命令历史
- **Tab** - 自动补全命令
- **左/右箭头** - 编辑当前输入
- **Home/End** - 移动到行首/行尾

## 配置文件位置

- **配置文件**: `~/.openbutler/config.yml`
- **会话文件**: `~/.openbutler/sessions/`
- **日志文件**: `~/.openbutler/logs/`
- **命令历史**: `~/.openbutler/.history`

## 退出应用

有三种方式退出应用：

1. 输入 `/exit` 命令
2. 按 `Ctrl+D`
3. 按 `Ctrl+C`（可能需要按两次）

退出时，应用会自动保存当前会话状态。

## GraalVM Native Image

如果要构建原生可执行文件，请参考 `NATIVE_BUILD.md`。

## 更多信息

- 查看 [设计文档](.kiro/specs/modern-cli-refactor/design.md) 了解架构设计
- 查看 [需求文档](.kiro/specs/modern-cli-refactor/requirements.md) 了解功能需求
- 查看 [任务列表](.kiro/specs/modern-cli-refactor/tasks.md) 了解实现进度
- 查看 [原生构建指南](NATIVE_BUILD.md) 了解 GraalVM Native Image 构建

## 支持

如果遇到问题，请查看日志文件：

- **应用日志**: `~/.openbutler/logs/application.log`
- **错误日志**: `~/.openbutler/logs/error.log`
