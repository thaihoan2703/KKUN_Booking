package com.backend.KKUN_Booking.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private String rasaUrl = "http://localhost:5005/webhooks/rest/webhook";

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody String message) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse chuỗi JSON sang Map
        Map<String, String> body = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {});

        // Tạo request gửi đến Rasa
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        // Nhận phản hồi từ Rasa
        ResponseEntity<String> response = restTemplate.postForEntity(rasaUrl, entity, String.class);
        return ResponseEntity.ok(response.getBody());
    }

}
