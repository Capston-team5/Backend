package com.phishing.service;

import com.phishing.domain.ChatMessage;
import com.phishing.domain.ChatSession;
import com.phishing.dto.ChatResponseDto;
import com.phishing.repository.ChatMessageRepository;
import com.phishing.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final OpenAiService openAiService;

    @Transactional
    public ChatResponseDto processAndSaveChat(String sessionId, String userMessage, String type, String riskLevel, String preview, Long userId) {

        // 1. 세션(대화방)이 DB에 없으면 신규 생성
        if (!chatSessionRepository.existsById(sessionId)) {
            ChatSession session = new ChatSession();
            session.setSessionId(sessionId);
            session.setUserId(userId);
            session.setType(type != null ? type : "unknown");
            session.setRiskLevel(riskLevel != null ? riskLevel : "SAFE");
            session.setPreview(preview != null ? preview : userMessage);
            session.setCreatedAt(LocalDateTime.now());
            chatSessionRepository.save(session);
        }

        // 2. AI 응답 생성
        String aiReply = openAiService.analyzePhishing(userMessage);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessage(aiReply);
        chatMessage.setSender("Bot");
        chatMessage.setTimestamp(LocalDateTime.now());

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        return ChatResponseDto.builder()
                .sessionId(sessionId)
                .chatMessageId(savedMessage.getId())
                .reply(aiReply)
                .riskLevel(riskLevel != null ? riskLevel : "HIGH")
                .build();
    }

    // 유저의 전체 세션 목록 조회 (A번 항목)
    public List<ChatSession> getUserSessions(Long userId) {
        return chatSessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 특정 세션의 메시지 기록과 위험도 조회 (C번 항목)
    public Map<String, Object> getHistoryWithRiskLevel(String sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다."));

        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", sessionId);
        data.put("riskLevel", session.getRiskLevel());
        data.put("messages", messages);

        return data;
    }
}