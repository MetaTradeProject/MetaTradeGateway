package com.freesia.metatradegateway;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/meta-trade/stomp")
                .addInterceptors(new WebSocketInterceptor())
                .setHandshakeHandler(new WebSocketHandShaker())
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/meta-trade/subscribe");
        registry.setApplicationDestinationPrefixes("/meta-trade/post");
        registry.setUserDestinationPrefix("/meta-trade/user");
    }
}
