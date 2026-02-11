package com.chatbot.demo.controller;

import com.chatbot.demo.dto.ChatRequest;
import com.chatbot.demo.dto.ChatResponse;
import com.chatbot.demo.entity.ChatMessage;
import com.chatbot.demo.entity.ChatSession;
import com.chatbot.demo.repository.ChatRepository;
import com.chatbot.demo.repository.ChatSessionRepository;
import com.chatbot.demo.service.AIService;
import com.chatbot.demo.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AIService aiService;
    private final ChatRepository chatRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final JwtService jwtService;

    // ✅ ALL DEPENDENCIES INJECTED PROPERLY
    public ChatController(
            AIService aiService,
            ChatRepository chatRepository, ChatSessionRepository chatSessionRepository,
            JwtService jwtService
    ) {
        this.aiService = aiService;
        this.chatRepository = chatRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.jwtService = jwtService;
    }

    // ================= SEND CHAT =================
    @PostMapping
    public ChatResponse chat(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChatRequest request
    ) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);

        String reply = aiService.processMessage(
                request.getSessionId(),
                request.getMessage(),
                userId
        );

        return new ChatResponse(reply);
    }

    // ================= GET USER SESSIONS =================
    @GetMapping("/sessions")
    public List<Map<String, Object>> getSessions(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);

        List<Object[]> rows = chatRepository.findSessionsFromMessages(userId);

        return rows.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", r[0]);
            map.put("title", r[1]);
            map.put("lastUpdated", r[2]);
            return map;
        }).toList();
    }

    // ================= CHAT HISTORY =================
    @GetMapping("/sessions/{sessionId}")
    public List<ChatMessage> getChat(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String sessionId
    ) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);

        return chatRepository
                .findBySessionIdAndUserIdOrderByTimestampAsc(sessionId, userId);
    }


    // ================= DELETE SESSION =================
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        chatRepository.deleteBySessionId(sessionId);
        chatSessionRepository.deleteBySessionId(sessionId);
        return ResponseEntity.noContent().build();
    }

    // ================= RENAME SESSION =================
    @PutMapping("/sessions/{sessionId}/rename")
    public ResponseEntity<Void> renameSession(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body
    ) {
        chatRepository.updateTitleBySessionId(
                sessionId,
                body.get("title")
        );
        return ResponseEntity.ok().build();
    }
}
