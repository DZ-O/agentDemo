package com.openbutler.core.tool;

import java.util.Map;

@FunctionalInterface
public interface ToolExecutor {
    ToolResult execute(Map<String, Object> parameters);
}
