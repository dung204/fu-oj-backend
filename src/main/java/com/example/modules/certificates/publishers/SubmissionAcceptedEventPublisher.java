package com.example.modules.certificates.publishers;

import com.example.modules.certificates.dtos.SubmissionAcceptedEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionAcceptedEventPublisher {

  public static final String STREAM_KEY = "certificates:events:submission-accepted";

  private final RedisTemplate<String, Object> redisTemplate;

  public void publish(String studentId, String exerciseId) {
    try {
      SubmissionAcceptedEventDTO event = new SubmissionAcceptedEventDTO(studentId, exerciseId);
      ObjectRecord<String, SubmissionAcceptedEventDTO> record = StreamRecords.newRecord()
        .ofObject(event)
        .withStreamKey(STREAM_KEY);

      redisTemplate.opsForStream().add(record);
      log.info(
        "Published submission accepted event to stream for student '{}', exercise '{}'",
        studentId,
        exerciseId
      );
    } catch (Exception e) {
      log.error(
        "Failed to publish submission accepted event to stream for student '{}', exercise '{}': {}",
        studentId,
        exerciseId,
        e
      );
    }
  }
}
