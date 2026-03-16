package com.openbutler.tools.builtin;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component("runCommand")
public class RunCommandTool implements Function<RunCommandTool.Request, String> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Execute a shell command on the host system")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @JsonPropertyDescription("The command to execute")
        @JsonProperty(required = true)
        private String command;
    }

    @Override
    public String apply(Request request) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder builder;
            
            if (os.contains("win")) {
                builder = new ProcessBuilder("cmd.exe", "/c", request.command);
            } else {
                builder = new ProcessBuilder("sh", "-c", request.command);
            }
            
            // Set working directory to current user directory
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
