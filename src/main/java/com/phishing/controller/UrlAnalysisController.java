package com.phishing.controller;

import com.phishing.dto.AnalysisHistoryResponseDto;
import com.phishing.service.UrlAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1") // 💡 여기서 이미 "/api/v1"을 깔고 들어갑니다!
@RequiredArgsConstructor
public class UrlAnalysisController {

    private final UrlAnalysisService urlAnalysisService;

    // 1. [명세서 1번] URL 피싱 분석
    @PostMapping("/phishing/analyze")
    public ResponseEntity<String> analyzeUrl(@RequestParam("url") String targetUrl) {
        // 🌟 수정된 부분: 시큐리티 연동 전까지 임시로 1번 유저(1L) 아이디를 달아서 보냅니다!
        Long dummyUserId = 1L;
        String result = urlAnalysisService.analyzeUrl(targetUrl, dummyUserId);
        return ResponseEntity.ok("🤖 [AI URL 수사관 분석 결과]\n\n" + result);
    }

    // 2. [명세서 18번] 솔루션 평가 (기존 evaluate를 feedback으로 변경)
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

    // 🌟 프론트엔드가 요청한 "진짜" 통합 이력 조회 API (본게임!)
    @GetMapping("/analysis/history")
    public ResponseEntity<List<AnalysisHistoryResponseDto>> getHistory() {
        // 실제로는 로그인한 유저 ID를 가져와야 하지만, 일단 테스트를 위해 1번 유저로 고정
        Long dummyUserId = 1L;
        List<AnalysisHistoryResponseDto> historyList = urlAnalysisService.getUserHistory(dummyUserId);
        return ResponseEntity.ok(historyList);
    }

    // 🛠️ 껍데기 테스트용 임시 API (경로 중복 버그 수정: /api/v1 제거)
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