package com.openbutler.ai.service;

import reactor.core.publisher.Flux;

public interface AIService {
    Flux<String> streamChat(String prompt, String context);
    String chat(String prompt, String context);
    // EmbeddingResult embed(String text); // Commented out until EmbeddingResult is defined or needed
}
