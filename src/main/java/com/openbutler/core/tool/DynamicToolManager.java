package com.openbutler.core.tool;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DynamicToolManager {

    private final Set<String> enabledTools = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void init() {
        // Register default tools
        registerTool("listDirectory");
        registerTool("readFile");
        registerTool("runCommand");
    }

    public void registerTool(String toolName) {
        enabledTools.add(toolName);
    }

    public void unregisterTool(String toolName) {
        enabledTools.remove(toolName);
    }

    public Set<String> getEnabledTools() {
        return Collections.unmodifiableSet(enabledTools);
    }
}
