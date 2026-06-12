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
    public Map<String, Object> analyzeUrl(String targetUrl, Long userId) {
        String aiResult = "";
        String riskLevel = "SAFE";
        String phishingType = "기타";
        String recommendation = "";

        boolean isMalicious = googleSafeBrowsingService.isMaliciousUrl(targetUrl);

        if (isMalicious) {
            // 1차: Safe Browsing이 악성 판단 → AI가 2차 상세 분석
            aiResult = openAiService.analyzeUrl(targetUrl);
            riskLevel = parseRiskLevel(aiResult);
            phishingType = parsePhishingType(aiResult);
            recommendation = parseRecommendation(aiResult);
        } else {
            // 1차: Safe Browsing이 안전 판단 → AI 분석 없이 SAFE 반환
            aiResult = "구글 Safe Browsing 검사 결과 안전한 URL입니다.";
            riskLevel = "SAFE";
            phishingType = "정상";
            recommendation = "안전한 URL로 판단됩니다.";
        }

        int riskScore = riskLevelToScore(riskLevel);

        try {
            AnalysisHistory newRecord = new AnalysisHistory();
            newRecord.setUserId(userId);
            newRecord.setType("URL");
            newRecord.setTarget(targetUrl);
            newRecord.setRiskLevel(riskLevel);
            newRecord.setRiskScore(riskScore);
            newRecord.setPhishingType(phishingType);
            newRecord.setAnalyzedAt(LocalDateTime.now());
            newRecord.setAiResult(aiResult);
            urlAnalysisRepository.save(newRecord);
        } catch (Exception e) {
            System.err.println("DB 저장 중 문제가 발생했습니다: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("riskScore", riskScore);
        result.put("riskLevel", riskLevel);
        result.put("phishingType", phishingType);
        result.put("recommendation", recommendation);
        result.put("detectedKeywords", aiResult);
        return result;
    }

    private int riskLevelToScore(String riskLevel) {
        return switch (riskLevel) {
            case "CRITICAL" -> 95;
            case "HIGH"     -> 75;
            case "MEDIUM"   -> 50;
            case "LOW"      -> 25;
            default         -> 5;
        };
    }

    private String parsePhishingType(String text) {
        if (text == null) return "기타";
        if (text.contains("스미싱")) return "스미싱";
        if (text.contains("파밍")) return "파밍";
        if (text.contains("보이스피싱") || text.contains("보이스 피싱")) return "보이스피싱";
        if (text.contains("피싱")) return "피싱";
        return "기타";
    }

    private String parseRecommendation(String text) {
        if (text == null) return "주의하세요.";
        if (text.contains("접속") && text.contains("중단")) return "즉시 접속을 중단하세요.";
        if (text.contains("클릭") && (text.contains("금지") || text.contains("하지 마"))) return "링크를 클릭하지 마세요.";
        if (text.contains("안전") || text.contains("정상")) return "안전한 URL로 판단됩니다.";
        return "주의가 필요합니다. 개인정보를 입력하지 마세요.";
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
        // "위험도: CRITICAL" 형식을 우선 파싱
        if (aiResult.contains("위험도: CRITICAL") || aiResult.contains("위험도:CRITICAL")) return "CRITICAL";
        if (aiResult.contains("위험도: HIGH") || aiResult.contains("위험도:HIGH")) return "HIGH";
        if (aiResult.contains("위험도: MEDIUM") || aiResult.contains("위험도:MEDIUM")) return "MEDIUM";
        if (aiResult.contains("위험도: LOW") || aiResult.contains("위험도:LOW")) return "LOW";
        if (aiResult.contains("위험도: SAFE") || aiResult.contains("위험도:SAFE")) return "SAFE";
        // 폴백: 단어 자체로 판단
        String upper = aiResult.toUpperCase();
        if (upper.contains("CRITICAL")) return "CRITICAL";
        if (upper.contains("HIGH")) return "HIGH";
        if (upper.contains("SAFE")) return "SAFE";
        if (upper.contains("LOW")) return "LOW";
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