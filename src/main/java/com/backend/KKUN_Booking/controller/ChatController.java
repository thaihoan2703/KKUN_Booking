package com.backend.KKUN_Booking.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private String rasaUrl = "http://localhost:5005/webhooks/rest/webhook";

    // Lưu trữ ngữ cảnh của từng session
    private Map<String, List<String>> sessionContexts = new HashMap<>();

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(
            @RequestHeader("Session-ID") String sessionId,
            @RequestBody String message
    ) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse message JSON thành Map
        Map<String, String> body = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {});

        // Tạo request gửi đến Rasa
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        // Nhận phản hồi từ Rasa
        ResponseEntity<String> response = restTemplate.postForEntity(rasaUrl, entity, String.class);

        // Phân tích JSON response từ Rasa
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());

        // Lưu tất cả các tin nhắn vào danh sách
        List<String> messages = new ArrayList<>();
        for (JsonNode messageNode : jsonResponse) {
            JsonNode textNode = messageNode.get("text");
            if (textNode != null) {
                messages.add(textNode.asText());
            }
        }

        // Lưu ngữ cảnh vào session
        sessionContexts.put(sessionId, messages);

        // Đóng gói phản hồi để gửi lại cho frontend
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("messages", messages);

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetSession(@RequestHeader("Session-ID") String sessionId) {
        // Xóa ngữ cảnh của phiên này
        sessionContexts.remove(sessionId);
        return ResponseEntity.ok("Session reset.");
    }
}
