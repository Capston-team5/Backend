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

    // 🌟 이력 저장을 위한 레포지토리 주입 (실제 사용하시는 레포지토리 이름으로 변경 필요!)
    // private final AnalysisHistoryRepository historyRepository;

    // 🌟 파라미터에 Long userId 추가!
    public Map<String, String> analyzeVoice(MultipartFile audioFile, Long userId) {
        Map<String, String> response = new HashMap<>();

        System.out.println("🎙️ 1단계: 네이버 STT로 음성 파일에서 텍스트 추출 중...");
        String extractedText = sttService.transcribeAudio(audioFile);

        if (extractedText == null || extractedText.contains("오류가 발생했습니다")) {
            response.put("error", "❌ STT 변환 실패: " + extractedText);
            return response;
        }

        response.put("extractedText", extractedText);

        System.out.println("🤖 2단계: 추출된 대본을 OpenAI에게 넘겨서 피싱 판별 시작...");
        String prompt = "다음은 통화 녹음 내용입니다. 이 대화가 보이스피싱인지 위험도를 분석해주세요:\n\n[통화 내용]\n" + extractedText;
        String aiResult = openAiService.analyzePhishing(prompt);

        response.put("aiResult", aiResult);

        // 🌟 3단계: 회원일 경우 DB에 이력 저장 로직
        if (userId != null) {
            System.out.println("💾 로그인한 유저(" + userId + ")의 음성 분석 이력을 DB에 저장합니다.");
            try {
                /* 실제 DB 저장 로직 주석 해제 및 수정
                AnalysisHistory history = new AnalysisHistory();
                history.setUserId(userId);
                history.setType("VOICE");
                history.setTarget(audioFile.getOriginalFilename()); // 파일명 저장
                history.setAiResult(aiResult);
                historyRepository.save(history);
                */
            } catch (Exception e) {
                System.err.println("🚨 음성 분석 이력 DB 저장 실패: " + e.getMessage());
            }
        }

        return response;
    }
}