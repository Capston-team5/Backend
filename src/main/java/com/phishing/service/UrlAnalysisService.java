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
            aiResult = "구글 보안 데이터베이스에서 확인된 악성 URL입니다.";
            riskLevel = "CRITICAL";
            phishingType = "피싱";
            recommendation = "즉시 접속을 중단하고 해당 URL을 공유하지 마세요.";
        } else {
            aiResult = openAiService.analyzePhishing("다음 URL을 분석해주세요. 위험도(SAFE/LOW/MEDIUM/HIGH/CRITICAL), 피싱 종류, 권고사항을 알려주세요: " + targetUrl);
            riskLevel = parseRiskLevel(aiResult);
            phishingType = parsePhishingType(aiResult);
            recommendation = parseRecommendation(aiResult);
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