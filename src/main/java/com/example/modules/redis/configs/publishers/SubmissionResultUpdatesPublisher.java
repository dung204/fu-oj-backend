package com.example.modules.redis.configs.publishers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionResultUpdatesPublisher {

  private final RedisTemplate<String, Object> redisTemplate;

  @Qualifier("submissionResultUpdatesTopic")
  private final ChannelTopic submissionResultUpdatesTopic;

  public void publishSubmissionResultUpdate(Object message) {
    try {
      redisTemplate.convertAndSend(submissionResultUpdatesTopic.getTopic(), message);
      log.info(
        "Published message to Redis topic [{}]: {}",
        submissionResultUpdatesTopic.getTopic(),
        message
      );
    } catch (Exception e) {
      log.error("Failed to publish message to Redis", e);
    }
  }
}
