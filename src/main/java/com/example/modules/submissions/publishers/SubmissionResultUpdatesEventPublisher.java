package com.example.modules.submissions.publishers;

import com.example.modules.submissions.dtos.SubmissionResultUpdateEventDTO;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubmissionResultUpdatesEventPublisher {

  public static final String STREAM_KEY = "submissions:events:submission-result-updates";

  RedisTemplate<String, Object> redisTemplate;

  public void publish(
    String submissionId,
    String sourceCode,
    String languageCode,
    List<String> testInputs,
    List<String> expectedOutputs
  ) {
    try {
      SubmissionResultUpdateEventDTO event = new SubmissionResultUpdateEventDTO(
        submissionId,
        sourceCode,
        languageCode,
        testInputs,
        expectedOutputs
      );
      ObjectRecord<String, SubmissionResultUpdateEventDTO> record = StreamRecords.newRecord()
        .ofObject(event)
        .withStreamKey(STREAM_KEY);

      redisTemplate.opsForStream().add(record);
      log.info("Published new submission event to stream for submission '{}'", submissionId);
    } catch (Exception e) {
      log.error(
        "Failed to publish new submission event to stream for submission '{}': {}",
        submissionId,
        e
      );
    }
  }
}
