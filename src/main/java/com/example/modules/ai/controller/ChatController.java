package com.example.modules.ai.controller;

import com.example.modules.ai.dtos.request.ChatRequest;
import com.example.modules.ai.dtos.response.ChatResponse;
import com.example.modules.ai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @PostMapping("/chat")
  public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest chatRequest) {
    ChatResponse chatResponse = new ChatResponse();
    chatResponse.setMessageResponse(chatService.chat(chatRequest));
    return ResponseEntity.ok(chatResponse);
  }
}
