package com.openbutler.tools.builtin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RunCommandTool {

    @Tool(description = "Execute a shell command on the host system")
    public String runCommand(
            @ToolParam(description = "The command to execute") String command) {
        try {
            log.info("Running command: {}", command);
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder builder;

            if (os.contains("win")) {
                builder = new ProcessBuilder("cmd.exe", "/c", command);
            } else {
                builder = new ProcessBuilder("sh", "-c", command);
            }

            // 设置工作目录为当前用户目录
            builder.directory(new File(System.getProperty("user.dir")));
            builder.redirectErrorStream(true);

            Process process = builder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return output.toString() + "\n[Timeout]";
            }

            return output.toString();

        } catch (Exception e) {
            return "Error executing command: " + e.getMessage();
        }
    }
}
