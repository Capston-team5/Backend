package com.phishing.controller;

import com.phishing.service.ImageAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImageController {

    private final ImageAnalysisService imageAnalysisService;

    @PostMapping("/analysis/image")
    public ResponseEntity<Map<String, Object>> analyzeImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        Long userId = null;
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            try {
                userId = Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                userId = null;
            }
        }

        Map<String, String> result = imageAnalysisService.analyzeImage(file, userId);

        Map<String, Object> response = new HashMap<>();
        if (result.containsKey("error")) {
            response.put("success", false);
            response.put("message", result.get("error"));
            return ResponseEntity.ok(response);
        }

        response.put("success", true);
        response.put("message", "분석이 완료되었습니다");
        response.put("data", result);
        return ResponseEntity.ok(response);
    }
}