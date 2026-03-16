package com.openbutler.core.memory;

import java.util.List;

public interface MemoryService {
    void saveConversation(String sessionId, String userInput, String aiResponse);
    List<Conversation> getConversationHistory(String sessionId);
    String getRelevantContext(String currentInput);
    void updateUserPreference(String key, String value);
    List<String> getRecentMessages(String sessionId, int limit);
}
