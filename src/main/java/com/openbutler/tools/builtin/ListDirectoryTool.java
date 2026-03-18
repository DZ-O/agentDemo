package com.openbutler.tools.builtin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class ListDirectoryTool {

    @Tool(description = "List files in a directory")
    public String listDirectory(
            @ToolParam(description = "The directory path to list files from. Defaults to current directory if not specified.", required = false) 
            String path) {
        
        String pathStr = path != null && !path.isEmpty() ? path : ".";
        log.info("Executing ListDirectoryTool with path: {}", pathStr);
        
        File dir = new File(pathStr);
        if (!dir.exists() || !dir.isDirectory()) {
            return "Error: Directory not found: " + pathStr;
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
        return sb.toString();
    }
}
