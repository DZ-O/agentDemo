package com.openbutler.ai.service;

import com.openbutler.core.system.SystemInfoProvider;
import com.openbutler.core.tool.DynamicToolManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Service
public class OpenAiService implements AIService {

    private final ChatClient chatClient;
    private final SystemInfoProvider systemInfoProvider;
    private final DynamicToolManager toolManager;

    public OpenAiService(ChatClient.Builder chatClientBuilder, 
                         SystemInfoProvider systemInfoProvider, 
                         DynamicToolManager toolManager) {
        this.systemInfoProvider = systemInfoProvider;
        this.toolManager = toolManager;
        this.chatClient = chatClientBuilder
                .defaultSystem(getSystemPrompt())
                .build();
    }
    
    private String getSystemPrompt() {
        return """
            You are OpenButler, a helpful AI assistant in a CLI environment.
            You can execute tools and help users with their tasks.
            Please format your response in Markdown.
            
            IMPORTANT: When calling tools, you MUST provide the arguments in valid JSON format. Never provide empty string as arguments.
            
            """ + systemInfoProvider.getSystemContext();
    }

    @Override
    public Flux<String> streamChat(String promptText, String context) {
        String fullPrompt = context != null && !context.isEmpty() 
                ? context + "\n" + promptText 
                : promptText;
        
        Set<String> tools = toolManager.getEnabledTools();
        
        // Workaround: Use call() instead of stream() to avoid MessageAggregator issues with tool calls
        // in Spring AI 1.0.0-M6. This ensures tools are executed correctly without crashing.
        return Mono.fromCallable(() -> chatClient.prompt()
                .user(fullPrompt)
                .functions(tools.toArray(new String[0]))
                .call()
                .content())
                .flux();
    }

    @Override
    public String chat(String promptText, String context) {
        String fullPrompt = context != null && !context.isEmpty() 
                ? context + "\n" + promptText 
                : promptText;
        
        Set<String> tools = toolManager.getEnabledTools();
                
        return chatClient.prompt()
                .user(fullPrompt)
                .functions(tools.toArray(new String[0]))
                .call()
                .content();
    }
}
