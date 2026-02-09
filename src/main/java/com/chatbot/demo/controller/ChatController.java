package com.chatbot.demo.controller;

import com.chatbot.demo.dto.ChatRequest;
import com.chatbot.demo.dto.ChatResponse;
import com.chatbot.demo.entity.ChatMessage;
import com.chatbot.demo.repository.ChatRepository;
import com.chatbot.demo.service.AIService;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
//@CrossOrigin(origins = {
//        "https://front-end-inky-iota.vercel.app",
//        "http://localhost:3000"
//})

public class ChatController {

    private final AIService aiService;
    private final ChatRepository chatRepository;

    public ChatController(AIService aiService, ChatRepository chatRepository) {
        this.aiService = aiService;
        this.chatRepository = chatRepository;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = aiService.processMessage(
                request.getSessionId(),
                request.getMessage()
        );
        return new ChatResponse(reply);
    }

    @GetMapping("/sessions")
    public List<String> getSessions() {
        return chatRepository.findAllSessionIds();
    }

    @GetMapping("/sessions/{sessionId}")
    public List<ChatMessage> getChat(@PathVariable String sessionId) {
        return chatRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        chatRepository.deleteBySessionId(sessionId);
        return ResponseEntity.noContent().build();
    }



}
