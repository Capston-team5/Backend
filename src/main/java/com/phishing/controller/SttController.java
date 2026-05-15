package com.phishing.controller;

import com.phishing.service.VoiceAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SttController {

    private final VoiceAnalysisService voiceAnalysisService;

    @PostMapping("/voice")
    public ResponseEntity<Map<String, String>> analyzeVoice(@RequestParam("file") MultipartFile file) {

        // Map 상자를 통째로 받아서 화면(프론트엔드)으로 쏴줍니다! (JSON 형태로 자동 변환됨)
        Map<String, String> result = voiceAnalysisService.analyzeVoice(file);
        return ResponseEntity.ok(result);
    }
}