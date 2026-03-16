package com.openbutler.cli;

import com.openbutler.cli.commands.OpenButlerCommands;
import com.openbutler.cli.ui.TerminalUI;
import com.openbutler.core.agent.AgentCore;
import lombok.RequiredArgsConstructor;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OpenButlerRunner implements ApplicationRunner {

    private final AgentCore agentCore;
    private final TerminalUI terminalUI;
    private final OpenButlerCommands commands;
    private final Terminal terminal;
    
    private String currentSessionId = UUID.randomUUID().toString();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        terminalUI.println("Welcome to OpenButler! (Type '/exit' to quit, '/help' for commands)");
        
        // Use JLine LineReader for better input handling (history, etc.)
        LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

        while (true) {
            String line;
            try {
                // Read line with prompt
                line = lineReader.readLine("\u001B[34m> \u001B[0m");
            } catch (org.jline.reader.UserInterruptException e) {
                // Handle Ctrl+C
                break;
            } catch (org.jline.reader.EndOfFileException e) {
                // Handle Ctrl+D
                break;
            }

            if (line == null) {
                break;
            }
            
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("/")) {
                handleCommand(line);
            } else {
                handleChat(line);
            }
            terminalUI.println(""); // New line after response
        }
    }

    private void handleCommand(String line) {
        String[] parts = line.substring(1).split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "exit":
            case "quit":
                System.exit(0);
                break;
            case "new":
            case "new-session":
                currentSessionId = UUID.randomUUID().toString();
                terminalUI.println("Started new session: " + currentSessionId);
                break;
            case "help":
                printHelp();
                break;
            default:
                terminalUI.println("\u001B[31mUnknown command: " + command + ". Type /help for available commands.\u001B[0m");
        }
    }

    private void handleChat(String input) {
        try {
            agentCore.process(currentSessionId, input)
                    .doOnNext(terminalUI::printAiResponse)
                    .blockLast();
        } catch (Exception e) {
            terminalUI.println("\u001B[31mError: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void printHelp() {
        terminalUI.println("Available commands:");
        terminalUI.println("  /new      - Start a new conversation session");
        terminalUI.println("  /exit     - Exit the application");
        terminalUI.println("  /help     - Show this help message");
        terminalUI.println("  <text>    - Any other text is sent to the AI");
    }
}
