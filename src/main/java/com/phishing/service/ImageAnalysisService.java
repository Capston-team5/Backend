package com.phishing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageAnalysisService {

    private final OcrService ocrService;
    private final OpenAiService openAiService;

    // 🌟 이력 저장을 위한 레포지토리 주입 (실제 사용하시는 레포지토리 이름으로 변경 필요!)
    // private final AnalysisHistoryRepository historyRepository;

    // 🌟 파라미터에 Long userId 추가!
    public Map<String, String> analyzeImage(MultipartFile imageFile, Long userId) {
        Map<String, String> response = new HashMap<>();

        System.out.println("📸 1단계: 구글 비전 API로 사진에서 글자 추출 중...");

        String extractedText = ocrService.extractTextFromImage(imageFile);

        if (extractedText == null || extractedText.trim().isEmpty()) {
            response.put("error", "❌ 사진에서 글자를 찾을 수 없거나 구글 비전 API 오류가 발생했습니다.");
            return response;
        }

        response.put("extractedText", extractedText);

        System.out.println("🤖 2단계: 추출된 텍스트를 OpenAI에게 넘겨서 스미싱 판별 시작...");

        String prompt = "다음은 카카오톡이나 문자 메시지를 캡처한 화면에서 추출한 텍스트입니다. 이 내용이 단순한 은행/기관의 안내인지, 아니면 악의적인 스미싱(피싱) 시도인지 분석해서 위험도와 사유를 알려주세요:\n\n[추출된 텍스트]\n" + extractedText;
        String aiResult = openAiService.analyzePhishing(prompt);

        response.put("aiResult", aiResult);

        // 🌟 3단계: 회원일 경우 DB에 이력 저장 로직
        if (userId != null) {
            System.out.println("💾 로그인한 유저(" + userId + ")의 이미지 분석 이력을 DB에 저장합니다.");
            try {
                /* 실제 DB 저장 로직 주석 해제 및 수정
                AnalysisHistory history = new AnalysisHistory();
                history.setUserId(userId);
                history.setType("IMAGE");
                history.setTarget(imageFile.getOriginalFilename()); // 파일명 저장
                history.setAiResult(aiResult);
                historyRepository.save(history);
                */
            } catch (Exception e) {
                System.err.println("🚨 이미지 분석 이력 DB 저장 실패: " + e.getMessage());
            }
        }

        return response;
    }
}