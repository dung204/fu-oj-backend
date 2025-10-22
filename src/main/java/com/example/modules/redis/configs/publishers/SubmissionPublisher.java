package com.example.modules.redis.configs.publishers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionPublisher {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ChannelTopic submissionResultTopic;

  public void publishSubmissionUpdate(Object message) {
    try {
      redisTemplate.convertAndSend(submissionResultTopic.getTopic(), message);
      log.info(
        "Published message to Redis topic [{}]: {}",
        submissionResultTopic.getTopic(),
        message
      );
    } catch (Exception e) {
      log.error("Failed to publish message to Redis", e);
    }
  }
}
