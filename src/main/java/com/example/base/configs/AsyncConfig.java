package com.example.base.configs;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean(name = "submissionExecutor")
  public Executor submissionExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10); // 10 threads sẵn có
    executor.setMaxPoolSize(50); // tối đa 50 threads
    executor.setQueueCapacity(1000); // tối đa 1000 task chờ
    executor.setThreadNamePrefix("SubmissionWorker-");
    executor.initialize();
    return executor;
  }
}
