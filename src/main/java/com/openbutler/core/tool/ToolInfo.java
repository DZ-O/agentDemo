package com.openbutler.core.tool;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolInfo {
    private String name;
    private String description;
    private String parameters; // JSON schema or description
    private String pluginId;
}
