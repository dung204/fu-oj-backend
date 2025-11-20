package com.example.modules.redis.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;

@Configuration
public class RedisTopicConfig {

  @Bean
  ChannelTopic commentsTopic() {
    return new ChannelTopic("comments-events");
  }
}
