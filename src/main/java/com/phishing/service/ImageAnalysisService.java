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
public class ImageAnalysisService {

    private final OcrService ocrService;
    private final OpenAiService openAiService;
    private final UrlAnalysisRepository urlAnalysisRepository;

    public Map<String, String> analyzeImage(MultipartFile imageFile, Long userId) {
        Map<String, String> response = new HashMap<>();

        String extractedText = ocrService.extractTextFromImage(imageFile);

        if (extractedText == null || extractedText.trim().isEmpty()) {
            response.put("error", "사진에서 글자를 찾을 수 없거나 구글 비전 API 오류가 발생했습니다.");
            return response;
        }

        response.put("extractedText", extractedText);

        String prompt = "다음은 카카오톡이나 문자 메시지를 캡처한 화면에서 추출한 텍스트입니다. 이 내용이 단순한 은행/기관의 안내인지, 아니면 악의적인 스미싱(피싱) 시도인지 분석해서 위험도와 사유를 알려주세요:\n\n[추출된 텍스트]\n" + extractedText;
        String aiResult = openAiService.analyzePhishing(prompt);

        response.put("aiResult", aiResult);

        if (userId != null) {
            try {
                AnalysisHistory history = new AnalysisHistory();
                history.setUserId(userId);
                history.setType("IMAGE");
                history.setTarget(imageFile.getOriginalFilename());
                history.setRiskLevel("MEDIUM");
                history.setAiResult(aiResult);
                history.setAnalyzedAt(LocalDateTime.now());
                urlAnalysisRepository.save(history);
            } catch (Exception e) {
                System.err.println("이미지 분석 이력 DB 저장 실패: " + e.getMessage());
            }
        }

        return response;
    }
}