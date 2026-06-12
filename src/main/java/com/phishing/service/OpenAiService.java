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

    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();

    public String analyzePhishing(String input) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(OPENAI_API_KEY);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-4o-mini");

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "당신은 보이스피싱 및 악성 URL 탐지 전문가입니다. 주어진 내용의 피싱 위험도(%)와 그 사유를 3줄 이내로 분석해주세요.");
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", input);
            messages.add(userMessage);

            body.put("messages", messages);
            body.put("temperature", 0.2);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            return (String) message.get("content");

        } catch (Exception e) {
            e.printStackTrace();
            return "AI 분석 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}