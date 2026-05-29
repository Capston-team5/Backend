package com.phishing.controller;

import com.phishing.service.UrlAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UrlAnalysisService urlAnalysisService;

    // 인터넷 주소창에서 바로 접속해서 테스트하기 위한 임시 API
    @GetMapping("/api/test-url")
    public String testUrl(@RequestParam String url) {
        // 🌟 수정된 부분: 서비스가 '누가' 검사했는지 물어보므로, 가짜 유저 ID(1L)를 달아서 보냅니다!
        Long dummyUserId = 1L;
        return urlAnalysisService.analyzeUrl(url, dummyUserId);
    }
}