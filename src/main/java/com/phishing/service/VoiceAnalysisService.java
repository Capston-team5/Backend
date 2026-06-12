package com.phishing.service;

import com.phishing.domain.AnalysisHistory;
import com.phishing.repository.UrlAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VoiceAnalysisService {

    private final SttService sttService;
    private final OpenAiService openAiService;
    private final UrlAnalysisRepository urlAnalysisRepository;

    public Map<String, Object> analyzeVoice(MultipartFile audioFile, Long userId) {
        Map<String, Object> response = new HashMap<>();

        String extractedText = sttService.transcribeAudio(audioFile);

        if (extractedText == null || extractedText.contains("오류가 발생했습니다")) {
            response.put("error", "STT 변환 실패: " + extractedText);
            return response;
        }

        String prompt = "다음은 통화 녹음 내용입니다. 이 대화가 보이스피싱인지 위험도(SAFE/LOW/MEDIUM/HIGH/CRITICAL)와 사유를 간결하게 알려주세요:\n\n[통화 내용]\n" + extractedText;
        String aiResult = openAiService.analyzePhishing(prompt);

        String riskLevel = parseRiskLevel(aiResult);
        String phishingType = parsePhishingType(aiResult);

        response.put("convertedText", extractedText);
        response.put("riskLevel", riskLevel);
        response.put("phishingType", phishingType);
        response.put("message", aiResult);

        if (userId != null) {
            try {
                AnalysisHistory history = new AnalysisHistory();
                history.setUserId(userId);
                history.setType("VOICE");
                history.setTarget(audioFile.getOriginalFilename());
                history.setRiskLevel(riskLevel);
                history.setPhishingType(phishingType);
                history.setAiResult(aiResult);
                history.setAnalyzedAt(LocalDateTime.now());
                urlAnalysisRepository.save(history);
            } catch (Exception e) {
                System.err.println("음성 분석 이력 DB 저장 실패: " + e.getMessage());
            }
        }

        return response;
    }

    private String parseRiskLevel(String text) {
        if (text == null) return "MEDIUM";
        String upper = text.toUpperCase();
        if (upper.contains("CRITICAL") || upper.contains("매우 위험")) return "CRITICAL";
        if (upper.contains("HIGH") || upper.contains("높은")) return "HIGH";
        if (upper.contains("LOW") || upper.contains("낮은")) return "LOW";
        if (upper.contains("SAFE") || upper.contains("안전")) return "SAFE";
        return "MEDIUM";
    }

    private String parsePhishingType(String text) {
        if (text == null) return "기타";
        if (text.contains("보이스피싱") || text.contains("보이스 피싱")) return "보이스 피싱";
        if (text.contains("스미싱")) return "스미싱";
        if (text.contains("파밍")) return "파밍";
        if (text.contains("피싱")) return "피싱";
        return "기타";
    }
}