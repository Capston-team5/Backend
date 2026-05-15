package com.phishing.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class UrlAnalysis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;       // 분석을 요청한 사용자 ID
    private String url;          // 분석한 URL
    private boolean isMalicious; // 악성 여부
    private String details;      // 상세 결과 메시지
    private LocalDateTime timestamp; // 분석 시간

    // 솔루션 평가 (true: 도움됨, false: 도움안됨, null: 아직 평가안함)
    private Boolean isHelpful;

    // AI가 분석한 결과 전체 내용
    private String aiResult;
}