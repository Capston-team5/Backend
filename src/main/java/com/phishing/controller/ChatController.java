package com.phishing.controller;

import com.phishing.domain.ChatMessage;
import com.phishing.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String userId) {
        return ResponseEntity.ok(chatService.getChatHistory(userId));
    }
}