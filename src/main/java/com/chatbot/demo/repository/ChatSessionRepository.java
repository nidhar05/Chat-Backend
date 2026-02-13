package com.chatbot.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatbot.demo.entity.ChatSession;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByUserId(Long userId);

    Optional<ChatSession> findBySessionIdAndUserId(String sessionId, Long userId);

    void deleteBySessionId(String sessionId);

    Optional<ChatSession> findBySessionId(String sessionId);

}
