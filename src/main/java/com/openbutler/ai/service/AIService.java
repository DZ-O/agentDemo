package com.openbutler.ai.service;

import reactor.core.publisher.Flux;

public interface AIService {
    Flux<String> streamChat(String prompt, String context);
    String chat(String prompt, String context);
    // EmbeddingResult embed(String text); // 暂时注释掉，等待 EmbeddingResult 定义或需要时再启用
}
