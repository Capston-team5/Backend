package com.phishing.controller;

import com.phishing.service.ImageAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // 🌟 시큐리티 추가
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImageController {

    private final ImageAnalysisService imageAnalysisService;

    // [명세서 12번] 캡처 사진 분석
    @PostMapping("/analysis/image")
    public ResponseEntity<Map<String, String>> analyzeImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) { // 🌟 문지기에게 신분증 받기

        // 🌟 유저 ID 안전하게 뽑아내기 (비회원이면 null)
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            try {
                userId = Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                userId = null;
            }
        }

        // 🌟 파라미터에 userId 추가 (서비스 계층에서도 받을 수 있게 수정 필요!)
        Map<String, String> result = imageAnalysisService.analyzeImage(file, userId);
        return ResponseEntity.ok(result);
    }
}