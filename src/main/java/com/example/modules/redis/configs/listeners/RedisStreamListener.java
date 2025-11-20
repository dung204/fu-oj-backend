package com.example.modules.redis.configs.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;

@Slf4j
@RequiredArgsConstructor
public abstract class RedisStreamListener<T>
  implements StreamListener<String, MapRecord<String, String, String>> {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public abstract String getStreamKey();

  public abstract String getConsumerGroup();

  public abstract Class<T> getTargetType();

  @Override
  public void onMessage(MapRecord<String, String, String> message) {
    String streamKey = getStreamKey();
    String consumerGroup = getConsumerGroup();
    RecordId recordId = message.getId();

    try {
      Map<String, String> body = message.getValue();
      String jsonPayload = body.get("payload");

      if (jsonPayload == null) {
        log.warn("Message {} body is missing 'payload' key", recordId);
        ack(streamKey, consumerGroup, recordId); // Ack để bỏ qua message lỗi
        return;
      }

      if (jsonPayload.startsWith("\"")) {
        try {
          jsonPayload = objectMapper.readValue(jsonPayload, String.class);
        } catch (JsonProcessingException e) {
          log.warn("Failed to unwrap double-encoded JSON: {}", e.getMessage());
        }
      }

      T dto = objectMapper.readValue(jsonPayload, getTargetType());

      process(recordId.getValue(), dto);

      ack(streamKey, consumerGroup, recordId);
    } catch (Exception e) {
      log.error(
        "Failed to process message {}. It will be retired later. Error: {}",
        message.getId(),
        e.getMessage()
      );
    }
  }

  private void ack(String streamKey, String group, RecordId recordId) {
    redisTemplate.opsForStream().acknowledge(streamKey, group, recordId);
  }

  protected abstract void process(String messageId, T dto);
}
