package com.openbutler.core.tool;

import com.openbutler.tools.builtin.ListDirectoryTool;
import com.openbutler.tools.builtin.ReadFileTool;
import com.openbutler.tools.builtin.RunCommandTool;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DynamicToolManager {

    private final Set<Object> enabledTools = ConcurrentHashMap.newKeySet();
    
    private final ListDirectoryTool listDirectoryTool;
    private final ReadFileTool readFileTool;
    private final RunCommandTool runCommandTool;

    public DynamicToolManager(ListDirectoryTool listDirectoryTool, 
                              ReadFileTool readFileTool, 
                              RunCommandTool runCommandTool) {
        this.listDirectoryTool = listDirectoryTool;
        this.readFileTool = readFileTool;
        this.runCommandTool = runCommandTool;
    }

    @PostConstruct
    public void init() {
        // Register default tools
        registerTool(listDirectoryTool);
        registerTool(readFileTool);
        registerTool(runCommandTool);
    }

    public void registerTool(Object toolBean) {
        enabledTools.add(toolBean);
    }

    public void unregisterTool(Object toolBean) {
        enabledTools.remove(toolBean);
    }

    public Set<Object> getEnabledTools() {
        return Collections.unmodifiableSet(enabledTools);
    }
}
