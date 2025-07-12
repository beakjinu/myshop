
package com.example.myShop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackService {

    //슬랙 인커밍 웹훅 URL 넣어 줘야 함. **슬랙 자유채널 url 대체하면 자유채널로 메세지 보내짐.(api발급 안해도 됨)**
    //테스트 하려면 Slack api에서 발급 받아서 대체하면 됨.
    //https://hooks.slack.com/services/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 -> 이건 제 jinu 채널 url

    private static final String SLACK_WEBHOOK_URL = "https://hooks.slack.com/services/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";
    //Http요청
    private final RestTemplate restTemplate = new RestTemplate();

    //메세지 전송 메서드
    public void sendMessage(String message) {
        try {
            //헤더
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 바디
            Map<String, String> body = new HashMap<>();
            body.put("text", message);

            //위 헤더랑 바디
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            //Post방식으로 위 url에 메세지 보냄
            ResponseEntity<String> response = restTemplate.postForEntity(SLACK_WEBHOOK_URL, request, String.class);

        } catch (Exception e) {
            log.error("Slack 메시지 전송 실패", e);
        }
    }
}