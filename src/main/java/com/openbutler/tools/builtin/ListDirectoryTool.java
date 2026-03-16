package com.openbutler.tools.builtin;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.function.Function;

@Component("listDirectory")
public class ListDirectoryTool implements Function<ListDirectoryTool.Request, String> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("List files in a directory")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @JsonPropertyDescription("The directory path to list files from. Defaults to current directory if not specified.")
        @JsonProperty(required = false)
        private String path;
    }

    @Override
    public String apply(Request request) {
        String pathStr = request.path != null && !request.path.isEmpty() ? request.path : ".";
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
