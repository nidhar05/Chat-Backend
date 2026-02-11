package com.chatbot.demo.repository;

import com.chatbot.demo.entity.ChatMessage;
import com.chatbot.demo.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    // ✅ get messages of a session
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId, Long userId);

    // ✅ delete a session
    @Modifying
    @Transactional
    void deleteBySessionId(String sessionId);

    // ✅ rename a session (update all messages)
    @Modifying
    @Transactional
    @Query("""
        UPDATE ChatMessage c
        SET c.title = :title
        WHERE c.sessionId = :sessionId
    """)
    void updateTitleBySessionId(
            @Param("sessionId") String sessionId,
            @Param("title") String title
    );

    // ✅ LIST SESSIONS (THIS FIXES YOUR EMPTY [] ISSUE)
    @Query("""
        SELECT c.sessionId, MAX(c.title), MAX(c.timestamp)
        FROM ChatMessage c
        WHERE c.userId = :userId
        GROUP BY c.sessionId
        ORDER BY MAX(c.timestamp) DESC
    """)
    List<Object[]> findSessionsFromMessages(@Param("userId") Long userId);

    List<ChatMessage> findBySessionIdAndUserIdOrderByTimestampAsc(String sessionId, Long userId);
}

