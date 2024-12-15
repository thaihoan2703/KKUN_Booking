package com.backend.KKUN_Booking.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
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
import java.util.stream.Collectors;

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

        try {
            // Parse incoming message JSON into a Map
            Map<String, String> body = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {});

            // Create headers for the request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            // Send request to Rasa and get the response
            ResponseEntity<String> response = restTemplate.postForEntity(rasaUrl, entity, String.class);

            // Parse Rasa response JSON
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());

            // List to hold formatted messages for the frontend
            List<Map<String, Object>> messages = new ArrayList<>();

            // Process each message from the Rasa response
            for (JsonNode messageNode : jsonResponse) {
                Map<String, Object> messageData = new HashMap<>();

                // Handle text messages
                if (messageNode.has("text")) {
                    messageData.put("type", "text");
                    messageData.put("content", messageNode.get("text").asText());
                }

                // Handle image messages
                if (messageNode.has("image")) {
                    messageData.put("type", "image");
                    messageData.put("content", messageNode.get("image").asText());
                }

                // Handle buttons
                if (messageNode.has("buttons")) {
                    messageData.put("type", "buttons");
                    List<Map<String, String>> buttons = new ArrayList<>();
                    for (JsonNode buttonNode : messageNode.get("buttons")) {
                        Map<String, String> button = new HashMap<>();
                        button.put("title", buttonNode.get("title").asText());
                        button.put("payload", buttonNode.get("payload").asText());
                        buttons.add(button);
                    }
                    messageData.put("content", buttons);
                }

                // Handle custom field (e.g., image group)
                if (messageNode.has("custom")) {
                    JsonNode customNode = messageNode.get("custom");
                    if (customNode.has("type") && customNode.get("type").asText().equals("image_group")) {
                        messageData.put("type", "image_group");
                        List<String> images = new ArrayList<>();
                        for (JsonNode imageNode : customNode.get("content")) {
                            images.add(imageNode.asText());
                        }
                        messageData.put("content", images);
                    }
                }

                // Add the processed message to the list
                messages.add(messageData);
            }

            // Prepare the final response body
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("messages", messages);

            return ResponseEntity.ok(responseBody);

        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid request body format", "details", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while processing the request", "details", e.getMessage()));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetSession(@RequestHeader("Session-ID") String sessionId) {
        // Xóa ngữ cảnh của phiên này
        sessionContexts.remove(sessionId);
        return ResponseEntity.ok("Session reset.");
    }
}
