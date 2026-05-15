package com.phishing.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class AdminDto {

    @Getter
    @NoArgsConstructor
    public static class LoginRequest {
        // 관리자 로그인 요청 DTO
        // POST /api/v1/admin/login 요청 바디

        private String adminId;         // 관리자 아이디
        private String password;        // 관리자 비밀번호
    }

    @Getter
    public static class LoginResponse {
        // 관리자 로그인 응답 DTO

        private String accessToken;     // JWT 토큰
        private String adminId;         // 관리자 아이디

        public LoginResponse(String accessToken, String adminId) {
            this.accessToken = accessToken;
            this.adminId = adminId;
        }
    }
}