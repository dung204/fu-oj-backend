package com.example.modules.ai.service;

import com.example.modules.ai.dtos.request.ChatRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatService {

  private final ChatClient chatClient;

  public ChatService(ChatClient.Builder builder) {
    chatClient = builder.build();
  }

  public String chat(ChatRequest chatRequest) {
    try {
      SystemMessage systemMessage = new SystemMessage(
        "You are FU-OJ Tutor AI, a patient teaching assistant who helps learners solve algorithm exercises. " +
          "Always start by clarifying the exercise requirements and asking the learner what they already understand. " +
          "Explicitly highlight the prerequisite concepts or skills the learner should have before tackling the exercise so they can review if needed. " +
          "Guide them to break the exercise into smaller steps, offer hints, and explain the reasoning behind each step with simple language and concrete examples. " +
          "Encourage the learner to attempt partial solutions, review their code or idea, and provide constructive feedback rather than full answers. " +
          "Only share complete solutions after confirming the learner truly needs them, and always explain time and space complexity plus potential edge cases. " +
          "Keep the tone supportive, structured, and focused on helping the learner build confidence."
      );
      UserMessage userMessage = new UserMessage(chatRequest.getMessage());

      Prompt prompt = new Prompt(systemMessage, userMessage);
      return chatClient.prompt(prompt).call().content();
    } catch (NonTransientAiException e) {
      log.error("AI service error: {}", e.getMessage(), e);

      // Generic error message for other AI service errors
      return "Sorry, an error occurred while processing your request. Please try again later.";
    } catch (Exception e) {
      log.error("Unexpected error in chat service: {}", e.getMessage(), e);
      return "Sorry, an unexpected error occurred. Please try again later.";
    }
  }
}
