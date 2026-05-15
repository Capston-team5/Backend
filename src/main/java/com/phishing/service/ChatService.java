package com.phishing.service;

import com.phishing.domain.ChatMessage;
import com.phishing.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;

    public List<ChatMessage> getChatHistory(String userId) {
        return chatMessageRepository.findByUserIdOrderByTimestampAsc(userId);
    }
}