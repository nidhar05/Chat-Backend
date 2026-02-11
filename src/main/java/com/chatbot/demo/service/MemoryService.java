package com.chatbot.demo.service;

import com.chatbot.demo.entity.ChatMessage;
import com.chatbot.demo.repository.ChatRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemoryService {

    private final ChatRepository chatRepository;

    public MemoryService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    // ✅ NOW USER-SAFE
    public String buildMemorySummary(String sessionId, Long userId) {

        List<ChatMessage> history =
                chatRepository.findBySessionIdAndUserIdOrderByTimestampAsc(
                        sessionId,
                        userId
                );

        if (history.isEmpty()) {
            return "No prior patient information.";
        }

        return history.stream()
                .filter(m -> "USER".equals(m.getRole()))
                .map(m -> String.valueOf(m.getMessage())) // 🔥 FORCE String
                .collect(java.util.stream.Collectors.joining("; "));
    }

}
