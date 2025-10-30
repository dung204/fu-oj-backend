package com.example.modules.redis.configs.publishers;

import com.example.modules.redis.event_type.comment.CommentEvent;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentPublisher {

  private final RedisTemplate<String, Object> redisTemplate;

  @Qualifier("commentsTopic")
  private final ChannelTopic commentsTopic;

  public void publishCommentEvent(CommentEvent commentEvent) {
    try {
      redisTemplate.convertAndSend(commentsTopic.getTopic(), commentEvent);
      log.info(
        "Published comment event to Redis topic {}: {}",
        commentsTopic.getTopic(),
        commentEvent
      );
    } catch (Exception e) {
      log.error("Error publishing comment event to Redis", e);
    }
  }
}
