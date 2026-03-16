package com.openbutler.core.tool;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {
    private boolean success;
    private String output;
    private String error;
    
    public static ToolResult success(String output) {
        return ToolResult.builder()
                .success(true)
                .output(output)
                .build();
    }
    
    public static ToolResult error(String error) {
        return ToolResult.builder()
                .success(false)
                .error(error)
                .build();
    }
}
