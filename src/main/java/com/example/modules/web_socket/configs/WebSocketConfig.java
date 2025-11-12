package com.example.modules.web_socket.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Đây là endpoint FE sẽ connect (VD: ws://localhost:8080/ws)
    registry.addEndpoint("/ws").setAllowedOrigins("*");
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Cho phép dùng prefix /topic để subscribe
    config.enableSimpleBroker("/topic");

    // Tất cả message từ client gửi lên bắt đầu bằng /app
    config.setApplicationDestinationPrefixes("/app");
  }
}
