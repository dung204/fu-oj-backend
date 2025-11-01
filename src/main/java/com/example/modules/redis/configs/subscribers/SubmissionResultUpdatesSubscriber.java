package com.example.modules.redis.configs.subscribers;

import com.example.modules.submissions.dtos.TestCaseResultDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionResultUpdatesSubscriber implements MessageListener {

  private final SimpMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String body = new String(message.getBody());
      log.debug("Received Redis pub/sub message: {}", body);

      TestCaseResultDTO payload = objectMapper.readValue(body, TestCaseResultDTO.class);

      messagingTemplate.convertAndSend(
        "/topic/submission-result-updates/" + payload.getSubmissionId(),
        payload
      );
      log.debug(
        "Sent WebSocket to /topic/submission-result-updates/{} -> {}",
        payload.getSubmissionId(),
        payload
      );
    } catch (Exception e) {
      log.error("Error processing Redis pub/sub message", e);
    }
  }
}
