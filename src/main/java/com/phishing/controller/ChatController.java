package com.phishing.controller;

import com.phishing.domain.ChatMessage;
import com.phishing.dto.ChatResponseDto;
import com.phishing.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat") // 🌟 프론트 명세서에 맞춰 "/api/v1/chat"으로 통일!
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 1. [명세서 반영] 챗봇 응답 및 ID 반환
    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        String message = request.get("message"); // 유저가 보낸 메시지

        // 서비스에서 로직 처리 후 DTO 받아오기
        ChatResponseDto responseDto = chatService.processAndSaveChat(sessionId, message);

        // 프론트엔드가 요청한 정확한 JSON 형태({success, message, data})로 포장하기
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "성공했습니다");
        response.put("data", responseDto); // 🌟 여기에 DTO(ID 포함)가 쏙 들어갑니다!

        return ResponseEntity.ok(response);
    }

    // 2. [명세서 반영] 대화 이력 조회 (경로를 프론트 요청인 {sessionId}/history 로 맞춤)
    @GetMapping("/{sessionId}/history")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        // ChatMessage 안에 이미 id가 포함되어 있으므로, 프론트엔드는 이 데이터를 받아
        // 각 메시지의 고유 ID를 바로 평가 버튼에 연결할 수 있습니다!
        return ResponseEntity.ok(chatService.getChatHistory(sessionId));
    }
}