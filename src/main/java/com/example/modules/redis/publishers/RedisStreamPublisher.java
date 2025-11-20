package com.example.modules.redis.publishers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisStreamPublisher {

  RedisTemplate<String, Object> redisTemplate;
  ObjectMapper objectMapper;

  public void send(String streamKey, Object object) {
    try {
      String jsonPayload = objectMapper.writeValueAsString(object);

      MapRecord<String, String, String> record = StreamRecords.newRecord()
        .ofStrings(Map.of("payload", jsonPayload))
        .withStreamKey(streamKey);

      RecordId recordId = redisTemplate.opsForStream().add(record);
      log.info("Pushed JSON to stream [{}]: id={}", streamKey, recordId);
    } catch (Exception e) {
      log.error("Error pushing to stream", e);
    }
  }
}
