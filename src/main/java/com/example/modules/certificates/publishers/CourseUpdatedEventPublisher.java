package com.example.modules.certificates.publishers;

import com.example.modules.certificates.dtos.CourseUpdatedEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseUpdatedEventPublisher {

  public static final String STREAM_KEY = "certificates:events:course-updated";

  private final RedisTemplate<String, Object> redisTemplate;

  public void publish(String courseId) {
    try {
      CourseUpdatedEventDTO event = new CourseUpdatedEventDTO(courseId);
      ObjectRecord<String, CourseUpdatedEventDTO> record = StreamRecords.newRecord()
        .ofObject(event)
        .withStreamKey(STREAM_KEY);

      redisTemplate.opsForStream().add(record);
      log.info("Published course updated event to stream for course '{}'", courseId);
    } catch (Exception e) {
      log.error(
        "Failed to publish course updated event to stream for course '{}': {}",
        courseId,
        e
      );
    }
  }
}
