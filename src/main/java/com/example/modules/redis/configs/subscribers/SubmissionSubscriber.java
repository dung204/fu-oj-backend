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
      String userId;
      if (payload.containsKey("userId")) {
        userId = String.valueOf(String.valueOf(payload.get("userId")));
      } else {
        log.error("Payload does not contain userId: {}", payload);
        return;
      }

      // gửi socket đến FE (ví dụ: /topic/submission-result-updates/1)
      messagingTemplate.convertAndSend("/topic/submission-result-updates/" + userId, payload);
      log.info("Sent WebSocket to /topic/submission-result-updates/{} -> {}", userId, payload);
    } catch (Exception e) {
      log.error("Error processing Redis pub/sub message", e);
    }
  }
}
