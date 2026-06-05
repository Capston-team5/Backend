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

    private String sessionId;   // 어떤 세션(방)의 메시지인지 식별
    private String message;     // 대화 내용
    private String sender;      // Bot 또는 User
    private LocalDateTime timestamp;
}