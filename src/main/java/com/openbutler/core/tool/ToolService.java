package com.openbutler.core.tool;

import java.util.List;
import java.util.Map;

public interface ToolService {
    ToolResult execute(String toolName, Map<String, Object> parameters);
    List<ToolInfo> listAvailableTools();
    ToolInfo getToolInfo(String toolName);
    void registerTool(ToolInfo toolInfo, ToolExecutor executor);
}
