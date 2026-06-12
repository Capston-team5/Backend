package com.phishing.service;

import com.phishing.domain.AnalysisHistory;
import com.phishing.dto.AnalysisHistoryResponseDto;
import com.phishing.repository.UrlAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlAnalysisService {

    private final OpenAiService openAiService;
    private final UrlAnalysisRepository urlAnalysisRepository;
    private final GoogleSafeBrowsingService googleSafeBrowsingService;

    // 1. [기존 기능 수정] URL 분석 및 DB 저장 (하이브리드 탐지)
    // 💡 변경점: 이제 누가(userId) 검사했는지 파라미터로 받아야 합니다.
    public String analyzeUrl(String targetUrl, Long userId) {
        System.out.println("🔍 URL 1차 검사를 시작합니다 (구글 DB): " + targetUrl);
        String finalResult = "";
        String riskLevel = "SAFE";

        boolean isMalicious = googleSafeBrowsingService.isMaliciousUrl(targetUrl);

        if (isMalicious) {
            System.out.println("🚨 [경고] 구글 보안 DB에서 악성 URL로 판별되었습니다!");
            finalResult = "위험도: CRITICAL. 구글 보안 데이터베이스에서 확인된 악성 URL입니다.";
            riskLevel = "CRITICAL";
        } else {
            System.out.println("✅ 구글 검사 통과! AI 수사관 '돈킴이'에게 2차 정밀 분석을 의뢰합니다.");
            finalResult = openAiService.analyzePhishing("분석할 URL: " + targetUrl);
            riskLevel = parseRiskLevel(finalResult);
        }

        // 🌟 프론트엔드 통합 요청에 맞춰 DB 저장 데이터 세팅!
        try {
            AnalysisHistory newRecord = new AnalysisHistory();
            newRecord.setUserId(userId);     // 검사 요청한 유저 ID
            newRecord.setType("URL");        // 분석 종류
            newRecord.setTarget(targetUrl);  // URL 주소
            newRecord.setRiskLevel(riskLevel); // 위험 등급
            newRecord.setAnalyzedAt(LocalDateTime.now()); // 분석 시각
            newRecord.setAiResult(finalResult);

            urlAnalysisRepository.save(newRecord);
            System.out.println("💾 URL 분석 결과가 DB에 안전하게 저장되었습니다!");
        } catch (Exception e) {
            System.err.println("🚨 DB 저장 중 문제가 발생했습니다: " + e.getMessage());
        }
        return finalResult;
    }

    // 🌟 [신규 기능] 특정 유저의 통합 분석 이력 조회 (프론트엔드 명세서 완벽 대응)
    public List<AnalysisHistoryResponseDto> getUserHistory(Long userId) {
        // 1. DB에서 유저 ID로 모든 기록을 가져옵니다.
        List<AnalysisHistory> histories = urlAnalysisRepository.findByUserIdOrderByAnalyzedAtDesc(userId);

        // 프론트엔드가 요청한 날짜 포맷 (예: 2026-03-13 19:41:25)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 2. DB 데이터를 프론트엔드 전용 DTO 그릇으로 예쁘게 옮겨 담습니다.
        return histories.stream().map(history ->
                AnalysisHistoryResponseDto.builder()
                        .id(history.getId())
                        .type(history.getType())
                        .target(history.getTarget())
                        .riskLevel(history.getRiskLevel())
                        .riskScore(history.getRiskScore())
                        .phishingType(history.getPhishingType())
                        .analyzedAt(history.getAnalyzedAt() != null ? history.getAnalyzedAt().format(formatter) : null)
                        .build()
        ).collect(Collectors.toList());
    }

    // 3. [기존 기능] 분석 결과 평가하기 (도움됨/안됨)
    public void evaluateResult(Long id, boolean helpful) {
        AnalysisHistory record = urlAnalysisRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 기록이 없습니다. ID: " + id));

        record.setIsHelpful(helpful);
        urlAnalysisRepository.save(record);
    }

    private String parseRiskLevel(String aiResult) {
        if (aiResult == null) return "MEDIUM";
        String lower = aiResult.toLowerCase();
        if (lower.contains("critical") || lower.contains("매우 위험") || lower.contains("90") || lower.contains("95") || lower.contains("100")) return "CRITICAL";
        if (lower.contains("high") || lower.contains("높은") || lower.contains("위험") || lower.contains("7") || lower.contains("8")) return "HIGH";
        if (lower.contains("low") || lower.contains("낮은") || lower.contains("안전") || lower.contains("1") || lower.contains("2")) return "LOW";
        if (lower.contains("safe") || lower.contains("정상")) return "SAFE";
        return "MEDIUM";
    }

    // 4. [기존 기능] 평가 통계 계산해서 가져오기
    public Map<String, Object> getEvaluationStats() {
        long totalCount = urlAnalysisRepository.count();
        long helpfulCount = urlAnalysisRepository.countByIsHelpful(true);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAnalyses", totalCount);
        stats.put("helpfulCount", helpfulCount);
        stats.put("satisfactionRate", totalCount > 0 ? (double) helpfulCount / totalCount * 100 : 0);

        return stats;
    }
}