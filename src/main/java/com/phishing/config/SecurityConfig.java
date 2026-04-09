package com.phishing.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;  // JWT 필터에 주입할 JwtUtil

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // BCrypt = 비밀번호 암호화 알고리즘
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())               // CSRF 비활성화 (REST API라서)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // STATELESS = 세션 사용 안 함 (JWT 사용하니까)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/users",                    // 회원가입
                                "/api/v1/users/login",              // 로그인
                                "/api/v1/admin/login",              // 관리자 로그인
                                "/swagger-ui.html",                 // Swagger UI
                                "/swagger-ui/**",                   // Swagger UI
                                "/api-docs/**"                      // Swagger API 문서
                        ).permitAll()                           // 위 경로는 인증 없이 접근 가능
                        .anyRequest().authenticated()           // 나머지는 인증 필요
                )
                .addFilterBefore(new JwtFilter(jwtUtil),    // JWT 필터 등록
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}