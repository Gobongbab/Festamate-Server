package com.gobongbob.festamate.global.config.webSocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketInterceptor webSocketInterceptor; // autowire
    private final WebSocketErrorHandler chatErrorHandler;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/api");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns(
                        "https://festamate-web.vercel.app",
                        "http://localhost:5173",
                        "http://localhost:8080",
                        "https://www.festamate.shop",
                        "https://stomp-practice.vercel.app"
                )
                .withSockJS();

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "https://festamate-web.vercel.app",
                        "http://localhost:5173",
                        "http://localhost:8080",
                        "https://www.festamate.shop",
                        "https://stomp-practice.vercel.app"
                );

        registry.setErrorHandler(chatErrorHandler);
    }
}
