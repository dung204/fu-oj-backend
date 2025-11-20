package com.example.modules.redis.configs;

import com.example.modules.redis.configs.listeners.RedisStreamListener;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.data.redis.stream.Subscription;

@Configuration
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisStreamConfig {

  RedisConnectionFactory redisConnectionFactory;
  RedisTemplate<String, Object> redisTemplate;
  List<RedisStreamListener<?>> listeners;

  @Bean
  Subscription subscription() {
    StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
      StreamMessageListenerContainerOptions.builder().pollTimeout(Duration.ofSeconds(1)).build();

    StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
      StreamMessageListenerContainer.create(redisConnectionFactory, options);

    for (RedisStreamListener<?> listener : listeners) {
      String streamKey = listener.getStreamKey();
      String group = listener.getConsumerGroup();

      createConsumerGroupIfNotExists(streamKey, group);

      // Ép kiểu về StreamListener raw là đủ, vì RedisStreamListener đã implement đúng interface
      @SuppressWarnings("unchecked")
      StreamListener<String, MapRecord<String, String, String>> typedListener = (StreamListener<
        String,
        MapRecord<String, String, String>
      >) listener;

      container.receive(
        Consumer.from(group, "worker-" + UUID.randomUUID()),
        StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
        typedListener
      );

      log.info("Registered listener for stream: {}", streamKey);
    }

    container.start();
    return null;
  }

  private void createConsumerGroupIfNotExists(String streamKey, String group) {
    try {
      if (Boolean.FALSE.equals(redisTemplate.hasKey(streamKey))) {
        redisTemplate.opsForStream().createGroup(streamKey, group);
      } else {
        redisTemplate.opsForStream().createGroup(streamKey, group);
      }
    } catch (Exception e) {
      // Ignore
    }
  }
}
