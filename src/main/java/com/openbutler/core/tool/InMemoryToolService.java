package com.openbutler.core.tool;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryToolService implements ToolService {

    private final Map<String, ToolInfo> tools = new ConcurrentHashMap<>();
    private final Map<String, ToolExecutor> executors = new ConcurrentHashMap<>();

    @Override
    public ToolResult execute(String toolName, Map<String, Object> parameters) {
        ToolExecutor executor = executors.get(toolName);
        if (executor == null) {
            return ToolResult.error("Tool not found: " + toolName);
        }
        try {
            return executor.execute(parameters);
        } catch (Exception e) {
            return ToolResult.error("Tool execution failed: " + e.getMessage());
        }
    }

    @Override
    public List<ToolInfo> listAvailableTools() {
        return new ArrayList<>(tools.values());
    }

    @Override
    public ToolInfo getToolInfo(String toolName) {
        return tools.get(toolName);
    }

    @Override
    public void registerTool(ToolInfo toolInfo, ToolExecutor executor) {
        tools.put(toolInfo.getName(), toolInfo);
        executors.put(toolInfo.getName(), executor);
    }
}
