package com.example.modules.redis.configs.subscribers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NewSubmissionsSubscriber implements MessageListener {

  SimpMessagingTemplate messagingTemplate;
  ObjectMapper objectMapper;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String body = new String(message.getBody());
      log.debug("Received Redis pub/sub message: {}", body);

      Map<String, Object> payload = objectMapper.readValue(body, Map.class);

      messagingTemplate.convertAndSend("/topic/new-submissions", payload);
      log.debug("Sent WebSocket to /topic/new-submissions -> {}", payload);
    } catch (Exception e) {
      log.error("Error processing Redis pub/sub message", e);
    }
  }
}
