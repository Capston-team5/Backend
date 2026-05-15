package com.phishing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VoiceAnalysisService {

    private final SttService sttService;
    private final OpenAiService openAiService;

    // 반환 타입을 Map으로 변경! (여러 개의 데이터를 담기 위해)
    public Map<String, String> analyzeVoice(MultipartFile audioFile) {
        Map<String, String> response = new HashMap<>();

        System.out.println("🎙️ 1단계: 네이버 STT로 음성 파일에서 텍스트 추출 중...");
        String extractedText = sttService.transcribeAudio(audioFile);

        if (extractedText == null || extractedText.contains("오류가 발생했습니다")) {
            response.put("error", "❌ STT 변환 실패: " + extractedText);
            return response;
        }

        // 1. 첫 번째 포장: 변환된 텍스트를 담습니다.
        response.put("extractedText", extractedText);

        System.out.println("🤖 2단계: 추출된 대본을 OpenAI에게 넘겨서 피싱 판별 시작...");
        String prompt = "다음은 통화 녹음 내용입니다. 이 대화가 보이스피싱인지 위험도를 분석해주세요:\n\n[통화 내용]\n" + extractedText;
        String aiResult = openAiService.analyzePhishing(prompt);

        // 2. 두 번째 포장: AI 분석 결과를 담습니다.
        response.put("aiResult", aiResult);

        return response; // 2개가 담긴 상자를 반환!
    }
}