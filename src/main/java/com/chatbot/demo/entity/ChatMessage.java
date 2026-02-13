package com.chatbot.demo.entity;

import java.time.Instant;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long userId;

    @Column(name = "session_id")
    private String sessionId;

    private String role;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime timestamp = LocalDateTime.now();

    private String title;

    public ChatMessage() {
    }

    public ChatMessage(String sessionId, Long userId, String role, String message) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.role = role;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(String sessionId, String user, String userMessage) {
    }

    public String getRole() {
        return role;
    }

    public String getMessage() {
        return message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setRole(String user) {
        this.role = user;
    }

    public void setMessage(String userMessage) {
        this.message = userMessage;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTimestamp(Instant now) {
        this.timestamp = LocalDateTime.now();
    }

    public void setTitle(String userMessage) {
        this.title = userMessage;
    }

    // getters
}
