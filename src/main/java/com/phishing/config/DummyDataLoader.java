package com.phishing.config;

import com.phishing.domain.ChatMessage;
import com.phishing.repository.ChatMessageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDateTime;

@Configuration
public class DummyDataLoader {

    @Bean
    public CommandLineRunner loadData(ChatMessageRepository repository) {
        return args -> {
            repository.deleteAll();

            ChatMessage msg1 = new ChatMessage();
            msg1.setUserId("taekyung");
            msg1.setMessage("안녕하세요, 피싱 의심 번호 신고하려 하는데 어떻게 해야할까요?");
            msg1.setSender("User");
            msg1.setTimestamp(LocalDateTime.now().minusMinutes(10));
            repository.save(msg1);

            ChatMessage msg2 = new ChatMessage();
            msg2.setUserId("taekyung");
            msg2.setMessage("잠시만 기다려주세요.");
            msg2.setSender("Bot");
            msg2.setTimestamp(LocalDateTime.now().minusMinutes(9));
            repository.save(msg2);

            System.out.println("✅ 테스트용 대화 데이터 2건 장전 완료!");
        };
    }
}