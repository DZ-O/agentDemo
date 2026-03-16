package com.openbutler.cli.ui;

import org.jline.terminal.Terminal;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Component
public class TerminalUI {

    @Autowired
    @Lazy
    private Terminal terminal;

    public void print(String message) {
        terminal.writer().print(message);
        terminal.writer().flush();
    }

    public void println(String message) {
        terminal.writer().println(message);
        terminal.writer().flush();
    }

    public void printAiResponse(String chunk) {
        // Green color for AI response
        terminal.writer().print("\u001B[32m" + chunk + "\u001B[0m");
        terminal.writer().flush();
    }
    
    public void printUserPrompt() {
        terminal.writer().print("\u001B[34m> \u001B[0m");
        terminal.writer().flush();
    }
}
