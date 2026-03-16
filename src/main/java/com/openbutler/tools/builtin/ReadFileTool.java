package com.openbutler.tools.builtin;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

@Component("readFile")
public class ReadFileTool implements Function<ReadFileTool.Request, String> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Read the content of a file")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @JsonPropertyDescription("The path of the file to read")
        @JsonProperty(required = true)
        private String path;
    }

    @Override
    public String apply(Request request) {
        try {
            return Files.readString(Path.of(request.path));
        } catch (Exception e) {
            return "Error: Failed to read file: " + e.getMessage();
        }
    }
}
