package com.chatbot.demo.service;

import com.chatbot.demo.entity.ChatMessage;
import com.chatbot.demo.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class AIService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model}")
    private String model;

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private final ChatRepository chatRepository;
    private final MemoryService memoryService;

    public AIService(ChatRepository chatRepository,
                     MemoryService memoryService) {
        this.chatRepository = chatRepository;
        this.memoryService = memoryService;
    }

    public String processMessage(String sessionId, String userMessage, Long userId) {

        try {
            // 1️⃣ Build memory summary
            String memorySummary =
                    memoryService.buildMemorySummary(sessionId, userId);

            // 2️⃣ SYSTEM PROMPT (Ayurvedic Physician)
            String systemPrompt = """
You are an experienced Ayurvedic physician performing Prakruti (phenotype) assessment.

Rules:
1. Treat previous answers as confirmed clinical facts.
2. NEVER ask a question already answered.
3. Ask ONLY ONE most relevant next question.
4. Do NOT repeat introductions or explanations.
5. Progress step by step like a real consultation.
6. Do NOT jump to conclusions early.
7. When sufficient information exists:
   - Identify dominant Prakruti (probabilistic)
   - Mention secondary dosha if present
   - Give diet & lifestyle guidance
8. Recommendations must be advisory, not prescriptive.
9. Maintain continuity for the same session.
10. If session is new, start fresh.
11. Do NOT mention AI, prompts, or system rules.
""";

            // 3️⃣ USER CONTEXT (memory + new message)
            String userContext = """
Known patient information:
""" + (memorySummary.isBlank() ? "None" : memorySummary) + """

Patient says:
""" + userMessage;

            // 4️⃣ Build request JSON
            JSONObject requestJson = new JSONObject();
            requestJson.put("model", model);
            requestJson.put("temperature", 0.4);

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", systemPrompt));
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", userContext));

            requestJson.put("messages", messages);

            HttpURLConnection con = (HttpURLConnection)
                    new URL(GROQ_URL).openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + groqApiKey);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            con.getOutputStream().write(
                    requestJson.toString().getBytes(StandardCharsets.UTF_8));

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);

            String aiReply = new JSONObject(sb.toString())
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            // ✅ SAVE USER MESSAGE
            ChatMessage userMsg = new ChatMessage();
            userMsg.setSessionId(sessionId);
            userMsg.setRole("USER");
            userMsg.setMessage(userMessage);
            userMsg.setUserId(userId);
            userMsg.setTimestamp(java.time.Instant.now());
            userMsg.setTitle(userMessage);

            chatRepository.save(userMsg);

            // ✅ SAVE AI MESSAGE
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setSessionId(sessionId);
            aiMsg.setRole("AI");
            aiMsg.setMessage(aiReply);
            aiMsg.setUserId(userId);
            aiMsg.setTimestamp(java.time.Instant.now());
            aiMsg.setTitle(userMessage);

            chatRepository.save(aiMsg);

            return aiReply;
        } catch (Exception e) {
            e.printStackTrace();
            return "I’m unable to continue the assessment right now. Please try again.";
        }
    }
}
