package com.phishing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageAnalysisService {

    // 1. 태경님이 기존에 만들어두신 구글 비전 OCR 서비스
    private final OcrService ocrService;

    // 2. OpenAI 피싱 분석 서비스
    private final OpenAiService openAiService;

    public Map<String, String> analyzeImage(MultipartFile imageFile) {
        Map<String, String> response = new HashMap<>();

        System.out.println("📸 1단계: 구글 비전 API로 사진에서 글자 추출 중...");

        // 🌟 주의: 'extractText' 부분은 태경님이 OcrService 안에 만들어두신 실제 메서드 이름으로 바꿔주세요!
        String extractedText = ocrService.extractTextFromImage(imageFile);

        if (extractedText == null || extractedText.trim().isEmpty()) {
            response.put("error", "❌ 사진에서 글자를 찾을 수 없거나 구글 비전 API 오류가 발생했습니다.");
            return response;
        }

        // 첫 번째 포장: 추출된 글자 담기
        response.put("extractedText", extractedText);

        System.out.println("🤖 2단계: 추출된 텍스트를 OpenAI에게 넘겨서 스미싱 판별 시작...");

        // 프롬프트 작성 (스미싱 문자에 특화되게 명령!)
        String prompt = "다음은 카카오톡이나 문자 메시지를 캡처한 화면에서 추출한 텍스트입니다. 이 내용이 단순한 은행/기관의 안내인지, 아니면 악의적인 스미싱(피싱) 시도인지 분석해서 위험도와 사유를 알려주세요:\n\n[추출된 텍스트]\n" + extractedText;
        String aiResult = openAiService.analyzePhishing(prompt);

        // 두 번째 포장: AI 분석 결과 담기
        response.put("aiResult", aiResult);

        return response;
    }
}