package com.phishing.controller;

import com.phishing.dto.AnalysisHistoryResponseDto;
import com.phishing.service.UrlAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UrlAnalysisController {

    private final UrlAnalysisService urlAnalysisService;

    // 🌟 수정 1: 경로를 프리패스 명단에 맞춰 /analysis/url 로 통일!
    @PostMapping("/analysis/url")
    public ResponseEntity<String> analyzeUrl(@RequestParam("url") String targetUrl, Authentication authentication) {

        // 🌟 수정 2: 비회원도 안전하게 통과할 수 있는 유연한 ID 추출 로직 (이미지/음성과 동일)
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            try {
                userId = Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                userId = null;
            }
        }

        String result = urlAnalysisService.analyzeUrl(targetUrl, userId);
        return ResponseEntity.ok("🤖 [AI URL 수사관 분석 결과]\n\n" + result);
    }

    // 2. [명세서 18번] 솔루션 평가
    @PostMapping("/chat/feedback")
    public ResponseEntity<String> feedback(@RequestParam Long id, @RequestParam boolean helpful) {
        urlAnalysisService.evaluateResult(id, helpful);
        return ResponseEntity.ok("평가가 반영되었습니다. 감사합니다!");
    }

    // 3. 통계 조회
    @GetMapping("/phishing/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(urlAnalysisService.getEvaluationStats());
    }

    // 🌟 프론트엔드가 요청한 통합 이력 조회 API (본게임!)
    @GetMapping("/analysis/history")
    public ResponseEntity<List<AnalysisHistoryResponseDto>> getHistory(Authentication authentication) {
        // 이력 조회는 "회원 전용" 기능이므로 기존 코드 유지 (비회원은 시큐리티가 미리 차단함)
        Long userId = Long.parseLong(authentication.getName());
        List<AnalysisHistoryResponseDto> historyList = urlAnalysisService.getUserHistory(userId);
        return ResponseEntity.ok(historyList);
    }

    // 🛠️ 껍데기 테스트용 임시 API
    @GetMapping("/analysis/history/test")
    public ResponseEntity<AnalysisHistoryResponseDto> testDto() {
        AnalysisHistoryResponseDto dummyData = AnalysisHistoryResponseDto.builder()
                .id(2L)
                .type("IMAGE")
                .target("screenshot_0312.png")
                .riskLevel("CRITICAL")
                .riskScore(null)
                .phishingType("스미싱")
                .analyzedAt("2026-03-12 11:02:10")
                .build();

        return ResponseEntity.ok(dummyData);
    }
}