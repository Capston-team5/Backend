package com.phishing.service;

import com.phishing.config.JwtUtil;
import com.phishing.domain.Admin;
import com.phishing.dto.AdminDto;
import com.phishing.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.phishing.config.JwtUtil;

@Service                        // 이 클래스가 Service 역할임을 표시
@RequiredArgsConstructor        // final 필드 생성자 자동 생성
@Transactional                  // DB 작업 중 오류 나면 자동으로 롤백
public class AdminService {

    private final AdminRepository adminRepository;      // 관리자 DB 접근용
    private final PasswordEncoder passwordEncoder;      // 비밀번호 암호화용
    private final JwtUtil jwtUtil;  // JWT 토큰 생성용

    // 관리자 로그인
    public AdminDto.LoginResponse login(AdminDto.LoginRequest request) {

        // 관리자 아이디로 찾기
        Admin admin = adminRepository.findByAdminId(request.getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 틀렸습니다"));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 틀렸습니다");
            // 보안상 아이디/비밀번호 중 뭐가 틀렸는지 알려주지 않음
        }

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateToken(admin.getId(), "ADMIN");

        return new AdminDto.LoginResponse(accessToken, admin.getAdminId());
    }
}