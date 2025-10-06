package com.example.modules.redis.configs.subscribers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionSubscriber implements MessageListener {

  private final SimpMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String body = new String(message.getBody());
      log.info("Received Redis pub/sub message: {}", body);

      Map<String, Object> payload = objectMapper.readValue(body, Map.class);
      Long userId = Long.valueOf(payload.get("userId").toString());

      // gửi socket đến FE (ví dụ: /topic/submissions/1)
      messagingTemplate.convertAndSend("/topic/submissions/" + userId, payload);
      log.info("Sent WebSocket to /topic/submissions/{} -> {}", userId, payload);
    } catch (Exception e) {
      log.error("Error processing Redis pub/sub message", e);
    }
  }
}
