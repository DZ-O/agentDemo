package com.openbutler.tools.builtin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
public class ReadFileTool {

    @Tool(description = "Read the content of a file")
    public String readFile(
            @ToolParam(description = "The path of the file to read") String path) {
        log.info("Executing ReadFileTool with path: {}", path);
        try {
            return Files.readString(Path.of(path));
        } catch (Exception e) {
            return "Error: Failed to read file: " + e.getMessage();
        }
    }
}
