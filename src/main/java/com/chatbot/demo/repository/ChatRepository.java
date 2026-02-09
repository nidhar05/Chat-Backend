package com.chatbot.demo.repository;

import com.chatbot.demo.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId);

    @Query("""
              SELECT c.sessionId
              FROM ChatMessage c
              GROUP BY c.sessionId
              ORDER BY MAX(c.timestamp) DESC
            """)
    List<String> findAllSessionIds();

    @Modifying
    @Transactional
    void deleteBySessionId(String sessionId);
}
