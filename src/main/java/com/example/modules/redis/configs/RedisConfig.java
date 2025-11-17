package com.example.modules.redis.configs;

import com.example.modules.comments.listeners.CommentEventListener;
import com.example.modules.submission_results.listeners.SubmissionResultUpdatesEventListener;
import com.example.modules.submissions.listeners.NewSubmissionsEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

  @Value("${spring.data.redis.host:localhost}")
  private String host;

  @Value("${spring.data.redis.port:6379}")
  private int port;

  @Value("${spring.data.redis.password:#{null}}")
  private String password;

  private final ObjectMapper objectMapper;
  private final CommentEventListener commentEventListener;
  private final SubmissionResultUpdatesEventListener submissionResultUpdatesEventListener;
  private final NewSubmissionsEventListener newSubmissionsEventListener;

  @Qualifier("submissionResultUpdatesTopic")
  private final ChannelTopic submissionResultUpdatesTopic;

  @Qualifier("newSubmissionsTopic")
  private final ChannelTopic newSubmissionsTopic;

  @Qualifier("commentsTopic")
  private final ChannelTopic commentsTopic;

  @Bean
  JedisConnectionFactory jedisConnectionFactory() {
    final RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);

    if (password != null) config.setPassword(password);

    return new JedisConnectionFactory(config);
  }

  @Bean
  RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();

    template.setConnectionFactory(jedisConnectionFactory());
    template.setKeySerializer(new StringRedisSerializer());

    GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(
      objectMapper
    );

    template.setValueSerializer(jsonRedisSerializer);
    template.setHashValueSerializer(jsonRedisSerializer);

    return template;
  }

  @Bean
  RedisMessageListenerContainer redisMessageListenerContainer() {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(jedisConnectionFactory());
    container.addMessageListener(commentEventListener, commentsTopic);
    container.addMessageListener(
      submissionResultUpdatesEventListener,
      submissionResultUpdatesTopic
    );
    container.addMessageListener(newSubmissionsEventListener, newSubmissionsTopic);

    return container;
  }
}
