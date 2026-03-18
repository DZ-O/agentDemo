package com.openbutler.core.agent;

import com.openbutler.ai.service.AIService;
import com.openbutler.core.memory.MemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentCore {
    private final AIService aiService;
    private final MemoryService memoryService;

    public Flux<String> process(String sessionId, String userInput) {
        log.info("Processing user input for session {}: {}", sessionId, userInput);
        
        // 1. Get Context (Recent messages)
        List<String> history = memoryService.getRecentMessages(sessionId, 10);
        String context = String.join("\n", history);
        log.debug("Loaded {} context messages", history.size());
        
        // 2. Call AI
        // We use a StringBuilder to accumulate the full response for saving to memory
        StringBuilder fullResponse = new StringBuilder();
        
        return aiService.streamChat(userInput, context)
                .doOnNext(chunk -> fullResponse.append(chunk))
                .doOnComplete(() -> {
                    // 3. Save conversation to memory
                    String response = fullResponse.toString();
                    log.info("AI response completed. Length: {}", response.length());
                    memoryService.saveConversation(sessionId, userInput, response);
                    log.info("Conversation saved to memory");
                })
                .doOnError(e -> log.error("Error during AI processing", e));
    }
}
