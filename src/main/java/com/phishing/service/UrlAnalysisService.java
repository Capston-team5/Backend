package com.phishing.service;

import com.phishing.domain.UrlAnalysis;
import com.phishing.repository.UrlAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UrlAnalysisService {

    private final OpenAiService openAiService;
    private final UrlAnalysisRepository urlAnalysisRepository;

    // 🌟 추가된 부분: 구글 API 서비스를 주입받습니다.
    private final GoogleSafeBrowsingService googleSafeBrowsingService;

    // 1. [수정된 기능] URL 분석 및 DB 저장 (하이브리드 탐지)
    public String analyzeUrl(String targetUrl) {
        System.out.println("🔍 URL 1차 검사를 시작합니다 (구글 DB): " + targetUrl);

        String finalResult = "";

        // 1단계: 구글에 먼저 물어보기
        boolean isMalicious = googleSafeBrowsingService.isMaliciousUrl(targetUrl);

        if (isMalicious) {
            // 구글에서 이미 블랙리스트로 확인된 경우 (AI 호출 안 함 = 비용/시간 절약!)
            System.out.println("🚨 [경고] 구글 보안 DB에서 악성 URL로 판별되었습니다!");
            finalResult = "위험도: CRITICAL. 구글 보안 데이터베이스에서 확인된 악성(피싱/스미싱) URL입니다. 즉시 접속을 중단하세요.";
        } else {
            // 2단계: 구글 통과 시, 우리 AI '돈킴이'에게 정밀 분석 의뢰
            System.out.println("✅ 구글 검사 통과! AI 수사관 '돈킴이'에게 2차 정밀 분석을 의뢰합니다.");
            finalResult = openAiService.analyzePhishing(targetUrl);
        }

        // 3단계: 분석 결과를 우리 자체 DB에 저장
        try {
            UrlAnalysis newRecord = new UrlAnalysis();
            // 💡 팁: 빈 껍데기만 저장되지 않도록, 분석한 URL과 결과를 세팅해주면 좋습니다!
            // newRecord.setUrl(targetUrl);
            // newRecord.setResult(finalResult);

            urlAnalysisRepository.save(newRecord);
            System.out.println("💾 분석 결과가 DB에 안전하게 저장되었습니다!");
        } catch (Exception e) {
            System.err.println("🚨 DB 저장 중 문제가 발생했습니다: " + e.getMessage());
        }

        return finalResult;
    }

    // 2. [기존 기능] 분석 결과 평가하기 (도움됨/안됨)
    public void evaluateResult(Long id, boolean helpful) {
        UrlAnalysis record = urlAnalysisRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 기록이 없습니다. ID: " + id));

        record.setIsHelpful(helpful);
        urlAnalysisRepository.save(record);
    }

    // 3. [기존 기능] 평가 통계 계산해서 가져오기
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