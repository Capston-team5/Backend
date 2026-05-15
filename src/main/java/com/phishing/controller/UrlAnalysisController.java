package com.phishing.controller;

import com.phishing.service.UrlAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1") // 명세서의 API 버전 반영
@RequiredArgsConstructor
public class UrlAnalysisController {

    private final UrlAnalysisService urlAnalysisService;

    // 1. [명세서 1번] URL 피싱 분석
    @PostMapping("/phishing/analyze")
    public ResponseEntity<String> analyzeUrl(@RequestParam("url") String targetUrl) {
        String result = urlAnalysisService.analyzeUrl(targetUrl);
        return ResponseEntity.ok("🤖 [AI URL 수사관 분석 결과]\n\n" + result);
    }

    // 2. [명세서 18번] 솔루션 평가 (기존 evaluate를 feedback으로 변경)
    @PostMapping("/chat/feedback")
    public ResponseEntity<String> feedback(@RequestParam Long id, @RequestParam boolean helpful) {
        urlAnalysisService.evaluateResult(id, helpful);
        return ResponseEntity.ok("평가가 반영되었습니다. 감사합니다!");
    }

    // 3. 통계 조회 (명세서에 없지만 테스트를 위해 유지, 필요시 명세서에 추가 제안)
    @GetMapping("/phishing/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(urlAnalysisService.getEvaluationStats());
    }
}