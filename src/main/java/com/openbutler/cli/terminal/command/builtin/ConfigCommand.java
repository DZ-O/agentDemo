package com.openbutler.cli.terminal.command.builtin;

import com.openbutler.cli.terminal.command.Command;
import com.openbutler.cli.terminal.model.CLIConfig;
import com.openbutler.cli.terminal.model.CommandContext;
import com.openbutler.cli.terminal.rendering.TerminalRenderer;
import com.openbutler.cli.terminal.service.ConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

/**
 * ConfigCommand 管理 CLI 配置。
 *
 * 本命令提供查看、修改和重置配置的子命令：
 * - show: 显示当前配置
 * - set: 修改配置值
 * - reset: 重置配置为默认值
 *
 * 验证要求：2.7, 8.8, 8.9, 8.10
 */
@Component
@Slf4j
public class ConfigCommand implements Command {
    
    @Override
    public String getName() {
        return "config";
    }
    
    @Override
    public List<String> getAliases() {
        return List.of("cfg", "conf");
    }
    
    @Override
    public String getDescription() {
        return "查看或修改CLI配置";
    }
    
    @Override
    public String getUsage() {
        return "config <show|set|reset> [键] [值]\n" +
               "  config show - 显示当前配置\n" +
               "  config set <键> <值> - 设置配置值\n" +
               "  config reset - 重置配置为默认值";
    }
    
    @Override
    public boolean requiresSession() {
        return false;
    }
    
    @Override
    public Mono<Void> execute(CommandContext context) {
        List<String> arguments = context.getArguments();
        
        if (arguments == null || arguments.isEmpty()) {
            TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
            renderer.renderError("需要提供子命令");
            renderer.renderSystem("用法: " + getUsage());
            return Mono.empty();
        }
        
        String subcommand = arguments.get(0).toLowerCase();
        
        return switch (subcommand) {
            case "show" -> executeShow(context);
            case "set" -> executeSet(context, arguments);
            case "reset" -> executeReset(context);
            default -> {
                TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
                renderer.renderError("未知子命令: " + subcommand);
                renderer.renderSystem("用法: " + getUsage());
                yield Mono.empty();
            }
        };
    }
    
    /**
     * 执行 'show' 子命令以显示当前配置。
     *
     * 验证要求：8.9
     */
    private Mono<Void> executeShow(CommandContext context) {
        ConfigManager configManager = (ConfigManager) context.getConfigManager();
        TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
        
        log.debug("Displaying current configuration");
        
        try {
            CLIConfig config = configManager.loadConfig();
            
            System.out.println();
            renderer.renderSystem("=== 当前配置 ===");
            System.out.println();
            
            System.out.println("colorScheme: " + config.getColorScheme());
            System.out.println("promptStyle: " + config.getPromptStyle());
            System.out.println("streamingEnabled: " + config.isStreamingEnabled());
            System.out.println("historySize: " + config.getHistorySize());
            System.out.println("defaultSessionName: " + config.getDefaultSessionName());
            System.out.println("debugMode: " + config.isDebugMode());
            System.out.println("autoSaveInterval: " + config.getAutoSaveInterval());
            System.out.println("autoCompleteEnabled: " + config.isAutoCompleteEnabled());
            System.out.println("terminalWidth: " + config.getTerminalWidth());
            
            System.out.println();
            renderer.renderSystem("配置文件: " + configManager.getConfigPath());
            
        } catch (Exception e) {
            log.error("Failed to display configuration", e);
            renderer.renderError("显示配置失败: " + e.getMessage());
        }
        
        return Mono.empty();
    }
    
    /**
     * 执行 'set' 子命令以修改配置值。
     *
     * 验证要求：8.8, 8.10
     */
    private Mono<Void> executeSet(CommandContext context, List<String> arguments) {
        ConfigManager configManager = (ConfigManager) context.getConfigManager();
        TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
        
        if (arguments.size() < 3) {
            renderer.renderError("需要提供键和值");
            renderer.renderSystem("用法: config set <键> <值>");
            return Mono.empty();
        }
        
        String key = arguments.get(1);
        String value = String.join(" ", arguments.subList(2, arguments.size()));
        
        log.debug("Setting config: {} = {}", key, value);
        
        try {
            // 根据键类型解析值
            Object parsedValue = parseConfigValue(key, value);
            
            // 设置配置值
            configManager.setConfigValue(key, parsedValue);
            
            renderer.renderSuccess(String.format("配置已更新: %s = %s", key, value));
            
            log.info("Configuration updated: {} = {}", key, value);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid configuration: {}", e.getMessage());
            renderer.renderError("无效的配置: " + e.getMessage());
            renderer.renderSystem("有效的键: colorScheme, promptStyle, streamingEnabled, historySize, " +
                    "defaultSessionName, debugMode, autoSaveInterval, autoCompleteEnabled, terminalWidth");
        } catch (IOException e) {
            log.error("Failed to save configuration", e);
            renderer.renderError("保存配置失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error setting configuration", e);
            renderer.renderError("发生意外错误: " + e.getMessage());
        }
        
        return Mono.empty();
    }
    
    /**
     * Execute the 'reset' subcommand to reset configuration to defaults.
     */
    private Mono<Void> executeReset(CommandContext context) {
        ConfigManager configManager = (ConfigManager) context.getConfigManager();
        TerminalRenderer renderer = (TerminalRenderer) context.getRenderer();
        
        log.debug("Resetting configuration to defaults");
        
        try {
            configManager.resetToDefaults();
            renderer.renderSuccess("配置已重置为默认值");
            
            log.info("Configuration reset to defaults");
            
        } catch (IOException e) {
            log.error("Failed to reset configuration", e);
            renderer.renderError("重置配置失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error resetting configuration", e);
            renderer.renderError("发生意外错误: " + e.getMessage());
        }
        
        return Mono.empty();
    }
    
    /**
     * Parses a configuration value string into the appropriate type.
     * 
     * @param key the configuration key
     * @param value the value string
     * @return the parsed value
     * @throws IllegalArgumentException if the value cannot be parsed
     */
    private Object parseConfigValue(String key, String value) {
        return switch (key) {
            case "colorScheme", "promptStyle", "defaultSessionName" -> value;
            case "streamingEnabled", "debugMode", "autoCompleteEnabled" -> 
                Boolean.parseBoolean(value);
            case "historySize", "autoSaveInterval", "terminalWidth" -> {
                try {
                    yield Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("键 " + key + " 的值必须是数字");
                }
            }
            default -> throw new IllegalArgumentException("未知的配置键: " + key);
        };
    }
}
