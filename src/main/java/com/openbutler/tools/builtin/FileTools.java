package com.openbutler.tools.builtin;

import com.openbutler.core.tool.ToolExecutor;
import com.openbutler.core.tool.ToolInfo;
import com.openbutler.core.tool.ToolResult;
import com.openbutler.core.tool.ToolService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileTools {

    private final ToolService toolService;

    @PostConstruct
    public void registerTools() {
        registerListDir();
        registerReadFile();
    }

    private void registerListDir() {
        ToolInfo info = ToolInfo.builder()
                .name("ls")
                .description("List files in a directory")
                .parameters("{\"path\": \"string\"}")
                .pluginId("builtin")
                .build();

        ToolExecutor executor = (params) -> {
            String pathStr = (String) params.getOrDefault("path", ".");
            File dir = new File(pathStr);
            if (!dir.exists() || !dir.isDirectory()) {
                return ToolResult.error("Directory not found: " + pathStr);
            }
            
            StringBuilder sb = new StringBuilder();
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    sb.append(f.getName())
                      .append(f.isDirectory() ? "/" : "")
                      .append("\n");
                }
            }
            return ToolResult.success(sb.toString());
        };

        toolService.registerTool(info, executor);
    }

    private void registerReadFile() {
        ToolInfo info = ToolInfo.builder()
                .name("cat")
                .description("Read file content")
                .parameters("{\"path\": \"string\"}")
                .pluginId("builtin")
                .build();

        ToolExecutor executor = (params) -> {
            String pathStr = (String) params.getOrDefault("path", "");
            try {
                String content = Files.readString(Path.of(pathStr));
                return ToolResult.success(content);
            } catch (Exception e) {
                return ToolResult.error("Failed to read file: " + e.getMessage());
            }
        };

        toolService.registerTool(info, executor);
    }
}
