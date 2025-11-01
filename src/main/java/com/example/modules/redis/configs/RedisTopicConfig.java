package com.example.modules.redis.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;

@Configuration
public class RedisTopicConfig {

  @Bean
  ChannelTopic submissionResultUpdatesTopic() {
    return new ChannelTopic("submission-result-updates"); // bắn message về kênh này
  }

  @Bean
  public ChannelTopic commentsTopic() {
    return new ChannelTopic("comments-events");
  }

  @Bean
  ChannelTopic newSubmissionsTopic() {
    return new ChannelTopic("new-submissions");
  }
}
