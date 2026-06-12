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

    private final JwtUtil jwtUtil;

    // 🌟 추가된 핵심 로직: 특정 조건에서는 이 필터(검색대)를 아예 건너뜁니다!
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return request.getMethod().equals("OPTIONS") ||
                path.equals("/api/v1/analysis/url") ||
                path.equals("/api/v1/analysis/image") ||
                path.equals("/api/v1/analysis/voice") ||
                path.equals("/api/v1/analysis/history/test") ||
                path.startsWith("/api/v1/phishing") ||
                path.equals("/api/v1/users/login") ||
                path.equals("/api/v1/users") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserId(token);
                    String role = jwtUtil.getRole(token);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // 토큰 오류 시 인증 없이 다음 필터로 넘김
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}