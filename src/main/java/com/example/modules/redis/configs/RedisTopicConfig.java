package com.example.modules.redis.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;

@Configuration
public class RedisTopicConfig {

  @Bean
  public ChannelTopic submissionTopic() {
    // tên kênh để publish/subcribe
    return new ChannelTopic("submission-result-updates"); // bắn message về kênh này
  }
}
