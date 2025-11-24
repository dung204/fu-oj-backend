package com.example.modules.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.base.BaseServiceTest;
import com.example.modules.ai.dtos.request.ChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.retry.NonTransientAiException;

class ChatServiceTest extends BaseServiceTest {

  @Mock
  private ChatClient.Builder chatClientBuilder;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ChatClient chatClient;

  private ChatService chatService;

  @BeforeEach
  void setUp() {
    when(chatClientBuilder.build()).thenReturn(chatClient);
    chatService = new ChatService(chatClientBuilder);
  }

  @Test
  void chat_WhenSuccessful_ShouldReturnAiResponse() {
    when(chatClient.prompt(any(Prompt.class)).call().content()).thenReturn("Hello student!");

    ChatRequest chatRequest = new ChatRequest("Hi");
    String response = chatService.chat(chatRequest);

    assertEquals("Hello student!", response);
  }

  @Test
  void chat_WhenNonTransientException_ShouldReturnFriendlyMessage() {
    when(chatClient.prompt(any(Prompt.class)).call().content()).thenThrow(
      new NonTransientAiException("AI down", new RuntimeException("boom"))
    );

    ChatRequest chatRequest = new ChatRequest("Need help");
    String response = chatService.chat(chatRequest);

    assertEquals(
      "Sorry, an error occurred while processing your request. Please try again later.",
      response
    );
  }

  @Test
  void chat_WhenUnexpectedException_ShouldReturnFallbackMessage() {
    when(chatClient.prompt(any(Prompt.class)).call().content()).thenThrow(
      new RuntimeException("oops")
    );

    ChatRequest chatRequest = new ChatRequest("Need help");
    String response = chatService.chat(chatRequest);

    assertEquals("Sorry, an unexpected error occurred. Please try again later.", response);
  }
}
