package com.openbutler.cli.commands;

import com.openbutler.cli.ui.TerminalUI;
import com.openbutler.core.agent.AgentCore;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.UUID;
import java.util.Scanner;

@ShellComponent
@RequiredArgsConstructor
public class OpenButlerCommands {

    private final AgentCore agentCore;
    private final TerminalUI terminalUI;
    private String currentSessionId = UUID.randomUUID().toString();

    @ShellMethod(key = "chat", value = "Start a chat session with OpenButler")
    public void chat(@ShellOption(defaultValue = "") String input) {
        if (!input.isEmpty()) {
            processInput(input);
        } else {
            terminalUI.println("Please provide input for chat command.");
        }
    }

    private void processInput(String input) {
        try {
            agentCore.process(currentSessionId, input)
                    .doOnNext(terminalUI::printAiResponse)
                    .blockLast(); // Block until stream completes
        } catch (Exception e) {
            terminalUI.println("\u001B[31mError: " + e.getMessage() + "\u001B[0m");
        }
    }
    
    @ShellMethod(key = "new-session", value = "Start a new conversation session")
    public String newSession() {
        currentSessionId = UUID.randomUUID().toString();
        return "Started new session: " + currentSessionId;
    }
}
