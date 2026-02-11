package com.chatbot.demo.repository;

import com.chatbot.demo.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByUserId(Long userId);

    Optional<ChatSession> findBySessionId(String sessionId);

    void deleteBySessionId(String sessionId);
}
