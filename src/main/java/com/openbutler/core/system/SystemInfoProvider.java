package com.openbutler.core.system;

import org.springframework.stereotype.Component;

@Component
public class SystemInfoProvider {

    public String getSystemContext() {
        String os = System.getProperty("os.name");
        String user = System.getProperty("user.name");
        String cwd = System.getProperty("user.dir");
        String javaVersion = System.getProperty("java.version");

        return String.format("""
                Current System Information:
                - OS: %s
                - User: %s
                - Current Working Directory: %s
                - Java Version: %s
                """, os, user, cwd, javaVersion);
    }
}
