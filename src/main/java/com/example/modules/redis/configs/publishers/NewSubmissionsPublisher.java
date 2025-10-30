package com.example.modules.redis.configs.publishers;

import com.example.modules.submissions.dtos.SubmissionResponseDTO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NewSubmissionsPublisher {

  RedisTemplate<String, Object> redisTemplate;

  @Qualifier("newSubmissionsTopic")
  ChannelTopic newSubmissionsTopic;

  public void publishNewSubmission(SubmissionResponseDTO submission) {
    redisTemplate.convertAndSend(newSubmissionsTopic.getTopic(), submission);
  }
}
