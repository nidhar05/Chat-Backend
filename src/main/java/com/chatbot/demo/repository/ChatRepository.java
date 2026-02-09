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
            SELECT c.sessionId, MAX(c.title), MAX(c.timestamp)
            FROM ChatMessage c
            WHERE c.sessionId IS NOT NULL
            GROUP BY c.sessionId
            ORDER BY MAX(c.timestamp) DESC
            """)
    List<Object[]> findAllSessionsWithTitle();


    @Modifying
    @Transactional
    void deleteBySessionId(String sessionId);

    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage c SET c.title = :title WHERE c.sessionId = :sessionId")
    void updateTitleBySessionId(String sessionId, String title);

}
