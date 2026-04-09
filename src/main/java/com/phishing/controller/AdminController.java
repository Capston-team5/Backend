package com.phishing.controller;

import com.phishing.dto.AdminDto;
import com.phishing.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController                         // JSON 응답을 반환하는 Controller
@RequestMapping("/api/v1/admin")        // 기본 URL 경로
@RequiredArgsConstructor                // final 필드 생성자 자동 생성
@Tag(name = "관리자 API", description = "관리자 로그인, 로그아웃")
public class AdminController {

    private final AdminService adminService;    // 비즈니스 로직 처리용

    @PostMapping("/login")                      // POST /api/v1/admin/login
    @Operation(summary = "관리자 로그인", description = "관리자 계정으로 로그인합니다")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AdminDto.LoginRequest request) {

        AdminDto.LoginResponse response = adminService.login(request);
        return ResponseEntity.ok(success("로그인 성공했습니다", response));
    }

    @PostMapping("/logout")                     // POST /api/v1/admin/logout
    @Operation(summary = "관리자 로그아웃", description = "관리자 계정에서 로그아웃합니다")
    public ResponseEntity<Map<String, Object>> logout() {
        // JWT는 서버에서 토큰 삭제 불가
        // 클라이언트에서 토큰 삭제하면 됨
        return ResponseEntity.ok(success("로그아웃 되었습니다", null));
    }

    // 공통 응답 형식 생성 메서드
    private Map<String, Object> success(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
}