package com.example.modules.redis.configs;

import com.example.modules.certificates.dtos.CourseUpdatedEventDTO;
import com.example.modules.certificates.dtos.SubmissionAcceptedEventDTO;
import com.example.modules.certificates.listeners.CourseUpdatedEventListener;
import com.example.modules.certificates.listeners.SubmissionAcceptedEventListener;
import com.example.modules.certificates.publishers.CourseUpdatedEventPublisher;
import com.example.modules.certificates.publishers.SubmissionAcceptedEventPublisher;
import java.time.Duration;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.data.redis.stream.Subscription;

@Configuration
public class RedisStreamConfig {

  @Bean
  StreamMessageListenerContainer<String, ?> streamMessageListenerContainer(
    RedisConnectionFactory redisConnectionFactory
  ) {
    StreamMessageListenerContainer<String, ?> container = StreamMessageListenerContainer.create(
      redisConnectionFactory,
      StreamMessageListenerContainerOptions.builder().pollTimeout(Duration.ofSeconds(1)).build()
    );
    container.start();
    return container;
  }

  @Bean
  Subscription submissionAcceptedSubscription(
    StreamMessageListenerContainer<
      String,
      ObjectRecord<String, SubmissionAcceptedEventDTO>
    > container,
    SubmissionAcceptedEventListener listener
  ) {
    return container.receive(
      Consumer.from(
        SubmissionAcceptedEventListener.GROUP_NAME,
        "worker-%s".formatted(UUID.randomUUID())
      ),
      StreamOffset.create(SubmissionAcceptedEventPublisher.STREAM_KEY, ReadOffset.lastConsumed()),
      listener
    );
  }

  @Bean
  Subscription courseUpdatedSubscription(
    StreamMessageListenerContainer<String, ObjectRecord<String, CourseUpdatedEventDTO>> container,
    CourseUpdatedEventListener listener
  ) {
    return container.receive(
      Consumer.from(
        CourseUpdatedEventListener.GROUP_NAME,
        "worker-%s".formatted(UUID.randomUUID())
      ),
      StreamOffset.create(CourseUpdatedEventPublisher.STREAM_KEY, ReadOffset.lastConsumed()),
      listener
    );
  }
}
