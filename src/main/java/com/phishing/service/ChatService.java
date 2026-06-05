package com.phishing.service;

import com.phishing.domain.ChatMessage;
import com.phishing.dto.ChatResponseDto;
import com.phishing.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatResponseDto processAndSaveChat(String sessionId, String userMessage) {
        // 1. 가짜 AI 응답 데이터
        String aiReply = "피싱번호로 등록된 기록이 있는 번호입니다";
        String riskLevel = "HIGH";

        // 2. DB에 저장할 엔티티 생성
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUserId(sessionId);
        chatMessage.setMessage(aiReply);
        chatMessage.setSender("Bot");
        chatMessage.setTimestamp(LocalDateTime.now());

        // 3. DB 저장 및 고유 ID 확보
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // 4. 프론트엔드 명세서에 맞춘 DTO 반환
        return ChatResponseDto.builder()
                .sessionId(sessionId)
                .chatMessageId(savedMessage.getId())
                .reply(aiReply)
                .riskLevel(riskLevel)
                .build();
    }

    public List<ChatMessage> getChatHistory(String sessionId) {
        return chatMessageRepository.findByUserIdOrderByTimestampAsc(sessionId);
    }
}