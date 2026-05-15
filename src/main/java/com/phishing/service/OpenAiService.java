package com.phishing.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    // ⭐ 여기에 아까 발급받으신 OpenAI API 키(sk-...)를 꼭 넣어주세요!
    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;
    private final String API_URL = "https://api.openai.com/v1/chat/completions";

    public String analyzePhishing(String targetUrl) {
        try {
            // 1. 헤더 설정 (OpenAI 입장권)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(OPENAI_API_KEY);

            // 2. 바디 설정 (어떤 모델을 쓸지, 뭐라고 질문할지)
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-3.5-turbo"); // 빠르고 가성비 좋은 임시 모델

            List<Map<String, String>> messages = new ArrayList<>();

            // AI에게 역할 부여
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "당신은 보이스피싱 및 악성 URL 탐지 전문가입니다. 주어진 URL의 피싱 위험도(%)와 그 사유를 3줄 이내로 분석해주세요.");
            messages.add(systemMessage);

            // 실제 검사할 URL 전달
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "분석할 URL: " + targetUrl);
            messages.add(userMessage);

            body.put("messages", messages);
            body.put("temperature", 0.2); // 진지하고 일관된 답변을 위해 낮게 설정

            // 3. OpenAI 서버로 전송!
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

            // 4. 응답에서 알맹이(텍스트)만 쏙 뽑아내기
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            return (String) message.get("content");

        } catch (Exception e) {
            e.printStackTrace();
            return "AI 분석 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}