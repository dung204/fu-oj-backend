package com.example.modules.redis.configs;

import com.example.modules.redis.configs.subscribers.SubmissionSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisSubscriberConfig {

  private final RedisConnectionFactory connectionFactory;
  private final SubmissionSubscriber submissionSubscriber;
  private final ChannelTopic submissionTopic;

  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer() {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(submissionSubscriber, submissionTopic);
    return container;
  }
}
