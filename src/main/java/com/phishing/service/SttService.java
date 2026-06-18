package com.phishing.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Service
public class SttService {

    // ⭐ 네이버 클라우드에서 발급받은 ID와 Secret을 여기에 붙여넣어 주세요!
    @Value("${naver.stt.client.id}")
    private String CLIENT_ID;
    @Value("${naver.stt.client.secret}")
    private String CLIENT_SECRET;

    public String transcribeAudio(MultipartFile file) {
        try {
            // 언어 설정: Kor (한국어 보이스피싱 특화!)
            String apiUrl = "https://naveropenapi.apigw.ntruss.com/recog/v1/stt?lang=Kor";

            // 1. 통행증(헤더) 설정: "나 파일 보낼 거고, 내 인증키는 이거야!"
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // 파일을 쪼개서 보냄
            headers.set("X-NCP-APIGW-API-KEY-ID", CLIENT_ID);
            headers.set("X-NCP-APIGW-API-KEY", CLIENT_SECRET);

            // 2. 소포 포장하기 (파일을 컴퓨터가 읽기 편한 바이트 배열로 변환)
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

            // 3. 네이버 서버로 접수하고 결과 받기
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);

            // 4. 받아온 결과에서 'text'만 쏙 뽑아내기
            return (String) response.getBody().get("text");

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 413) {
                return "오류: 음성 파일이 너무 깁니다. 60초 이하의 파일만 분석할 수 있습니다.";
            }
            e.printStackTrace();
            return "음성 분석 중 오류가 발생했습니다. API 키나 파일 형식을 확인해주세요!";
        } catch (Exception e) {
            e.printStackTrace();
            return "음성 분석 중 오류가 발생했습니다. API 키나 파일 형식을 확인해주세요!";
        }
    }
}