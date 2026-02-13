package com.chatbot.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatbot.demo.dto.ChatRequest;
import com.chatbot.demo.dto.ChatResponse;
import com.chatbot.demo.dto.RenameRequest;
import com.chatbot.demo.entity.ChatMessage;
import com.chatbot.demo.entity.ChatSession;
import com.chatbot.demo.repository.ChatRepository;
import com.chatbot.demo.repository.ChatSessionRepository;
import com.chatbot.demo.service.AIService;
import com.chatbot.demo.service.JwtService;

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

        // ✅ Validate session exists and belongs to user
        ChatSession session = chatSessionRepository
                .findBySessionIdAndUserId(request.getSessionId(), userId)
                .orElseThrow(() -> new RuntimeException("Invalid session"));

        session.setLastUpdated(LocalDateTime.now());
        chatSessionRepository.save(session);

        String reply = aiService.processMessage(
                request.getSessionId(),
                request.getMessage(),
                userId
        );

        return new ChatResponse(reply);
    }

    @PostMapping("/sessions")
    public ResponseEntity<?> createSession(
            @RequestHeader("Authorization") String authHeader
    ) {

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);

        ChatSession session = new ChatSession();
        session.setSessionId(java.util.UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setTitle("New Chat");

        chatSessionRepository.save(session);

        return ResponseEntity.ok(session);
    }

    // ================= GET USER SESSIONS =================
    @GetMapping("/sessions")
    public List<Map<String, Object>> getSessions(
            @RequestHeader("Authorization") String authHeader
    ) {

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);

        List<ChatSession> sessions
                = chatSessionRepository.findByUserIdOrderByLastUpdatedDesc(userId);

        return sessions.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", s.getSessionId());
            map.put("title", s.getTitle());
            map.put("lastUpdated", s.getLastUpdated());
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
    public ResponseEntity<Void> deleteSession(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String sessionId
    ) {

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);

        ChatSession session = chatSessionRepository
                .findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // delete messages first
        chatRepository.deleteBySessionId(sessionId);

        // then delete session
        chatSessionRepository.delete(session);

        return ResponseEntity.noContent().build();
    }

    // ================= RENAME SESSION =================
    @PutMapping("/sessions/{sessionId}/rename")
    public ResponseEntity<Void> renameSession(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String sessionId,
            @RequestBody RenameRequest request
    ) {

        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserId(token);

        ChatSession session = chatSessionRepository
                .findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setTitle(request.getTitle());
        chatSessionRepository.save(session);

        return ResponseEntity.ok().build();
    }

}
