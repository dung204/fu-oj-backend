package com.example.modules.ai.service;

import com.example.modules.ai.dtos.request.ChatRequest;
import fpt.edu.vn.springai.dto.request.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

  private final ChatClient chatClient;

  public ChatService(ChatClient.Builder builder) {
    chatClient = builder.build();
  }

  public String chat(ChatRequest chatRequest) {
    SystemMessage systemMessage = new SystemMessage(
      "You are FU-OJ AI, an AI designed to help users solve algorithm problems. " +
        "When a user asks a question, you should guide them step by step through the problem-solving process. " +
        "First, ask them to explain their understanding of the problem. Then, help them break down the problem into smaller parts. " +
        "Provide hints for solving each part and explain relevant concepts as needed. " +
        "Ensure that the user learns from the process by giving them opportunities to try solving parts of the problem on their own. " +
        "If they get stuck, offer clear explanations, code samples, and further hints, but avoid giving the direct answer. " +
        "Additionally, explain the time and space complexity of the solution once it's found."
    );
    UserMessage userMessage = new UserMessage(chatRequest.getMessage());

    ChatOptions chatOptions = ChatOptions.builder().temperature(0.2D).build();

    Prompt prompt = new Prompt(systemMessage, userMessage);
    return chatClient.prompt(prompt).call().content();
  }
}
