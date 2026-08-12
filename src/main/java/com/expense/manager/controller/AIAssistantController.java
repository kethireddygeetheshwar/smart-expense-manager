package com.expense.manager.controller;

import com.expense.manager.dto.request.ChatRequest;
import com.expense.manager.dto.response.ChatResponse;
import com.expense.manager.entity.ChatMessage;
import com.expense.manager.service.AIAssistantService;
import com.expense.manager.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AIAssistantController {

    private final AIAssistantService aiAssistantService;
    private final UserContext userContext;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        Map<String, Object> result = aiAssistantService.processMessage(
                userContext.getCurrentUserId(), request.getMessage());

        ChatResponse response = ChatResponse.builder()
                .message((String) result.get("message"))
                .response((String) result.get("response"))
                .intent((String) result.get("intent"))
                .timestamp((Long) result.get("timestamp"))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory() {
        return ResponseEntity.ok(aiAssistantService.getChatHistory(userContext.getCurrentUserId()));
    }
}
