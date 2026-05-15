package com.phishing.controller;

import com.phishing.service.ImageAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImageController {

    private final ImageAnalysisService imageAnalysisService;

    // [명세서 12번] 캡처 사진 분석
    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> analyzeImage(@RequestParam("file") MultipartFile file) {

        // 사진을 받아서 구글 비전 -> OpenAI 파이프라인으로 넘기고 JSON 상자로 반환
        Map<String, String> result = imageAnalysisService.analyzeImage(file);
        return ResponseEntity.ok(result);
    }
}