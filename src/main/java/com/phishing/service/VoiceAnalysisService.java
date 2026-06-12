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

    public Map<String, String> analyzeVoice(MultipartFile audioFile, Long userId) {
        Map<String, String> response = new HashMap<>();

        String extractedText = sttService.transcribeAudio(audioFile);

        if (extractedText == null || extractedText.contains("오류가 발생했습니다")) {
            response.put("error", "STT 변환 실패: " + extractedText);
            return response;
        }

        response.put("extractedText", extractedText);

        String prompt = "다음은 통화 녹음 내용입니다. 이 대화가 보이스피싱인지 위험도를 분석해주세요:\n\n[통화 내용]\n" + extractedText;
        String aiResult = openAiService.analyzePhishing(prompt);

        response.put("aiResult", aiResult);

        if (userId != null) {
            try {
                AnalysisHistory history = new AnalysisHistory();
                history.setUserId(userId);
                history.setType("VOICE");
                history.setTarget(audioFile.getOriginalFilename());
                history.setRiskLevel("MEDIUM");
                history.setAiResult(aiResult);
                history.setAnalyzedAt(LocalDateTime.now());
                urlAnalysisRepository.save(history);
            } catch (Exception e) {
                System.err.println("음성 분석 이력 DB 저장 실패: " + e.getMessage());
            }
        }

        return response;
    }
}