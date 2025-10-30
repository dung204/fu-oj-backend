package com.example.modules.redis.configs.subscribers;

import com.example.modules.redis.event_type.comment.CommentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentSubscriber implements MessageListener {

  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate simpMessagingTemplate;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String body = new String(message.getBody());
      log.info("Received comment event from Redis topic: {}", body);
      CommentEvent event = objectMapper.readValue(body, CommentEvent.class);
      String destination = "/topic/comments/" + event.getExerciseId();
      if (event.getParentId() != null) {
        destination += "/" + event.getParentId();
      }
      //send websocket to frontend
      simpMessagingTemplate.convertAndSend(destination, event);
      log.info("Sending comment event to WebSocket destination: {}", destination);
    } catch (Exception e) {
      log.error("Error processing comment event: ", e);
    }
  }
}
