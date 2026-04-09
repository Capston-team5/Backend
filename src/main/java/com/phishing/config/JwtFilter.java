package com.phishing.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    // OncePerRequestFilter = 요청당 한 번만 실행되는 필터

    private final JwtUtil jwtUtil;  // JWT 검증용

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        // Header에서 Authorization 값 가져오기
        // 예) Authorization: Bearer eyJhbGci...

        if (header != null && header.startsWith("Bearer ")) {
            // "Bearer " 로 시작하면 토큰 있는 것
            String token = header.substring(7);
            // "Bearer " 7글자 제거 후 토큰만 추출

            if (jwtUtil.validateToken(token)) {
                // 토큰 유효하면
                Long userId = jwtUtil.getUserId(token);     // userId 추출
                String role = jwtUtil.getRole(token);       // role 추출

                // Spring Security에 인증 정보 저장
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,                                         // principal (userId)
                                null,                                           // credentials
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)) // 권한
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // 이후 Controller에서 userId 꺼낼 수 있음
            }
        }

        filterChain.doFilter(request, response);
        // 다음 필터로 넘기기
    }
}