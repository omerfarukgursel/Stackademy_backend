package com.stackademy.proje.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Sunucudan istemciye giden mesajlar için (Subscription)
        config.enableSimpleBroker("/topic");

        // İstemciden sunucuya gelen mesajlar için prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // İstemcinin bağlanacağı endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // CORS için
                .withSockJS();

        // SockJS olmadan bağlantı için (Mobil uygulamalar için gerekebilir)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
