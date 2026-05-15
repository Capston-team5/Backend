package com.phishing.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;      // 사용자 ID
    private String message;     // 대화 내용
    private String sender;      // 보낸 사람 (User 또는 Bot)
    private LocalDateTime timestamp; // 보낸 시간
}