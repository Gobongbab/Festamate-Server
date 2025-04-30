package com.gobongbob.festamate.global.config;

import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.global.util.TokenProvider;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        String sessionId = accessor.getSessionId();
        log.info("Session ID: " + sessionId);
        log.info("Message: " + message);
        log.info("Accessor: " + accessor);
        log.info("Command: " + accessor.getCommand());
        log.info("Session Attributes: " + accessor.getSessionAttributes());

        if (Objects.requireNonNull(accessor.getCommand()) == StompCommand.CONNECT ||
                accessor.getCommand() == StompCommand.SEND) {
            String tokenHeader = accessor.getFirstNativeHeader("Authorization");
            if (tokenHeader == null) {
                log.error("No token found in message from session: " + sessionId);
                throw new IllegalArgumentException("No token found");
            }

            String token = tokenHeader.replace("Bearer ", "");
            tokenProvider.validateToken(token);

            Authentication authentication = tokenProvider.getAuthentication(token);
            setAuthentication(authentication, accessor);
        }

        return message;
    }
//
//    @Override
//    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//        String sessionId = accessor.getSessionId();
//
//        switch (Objects.requireNonNull(accessor.getCommand())) {
//            case CONNECT -> log.info("CONNECT: " + message);
//            case CONNECTED -> log.info("CONNECTED: " + message);
//            case DISCONNECT -> log.info("DISCONNECT: " + message);
//            case SUBSCRIBE -> log.info("SUBSCRIBE: " + message);
//            case UNSUBSCRIBE -> log.info("UNSUBSCRIBE: " + sessionId);
//            case SEND -> log.info("SEND: " + sessionId);
//            case MESSAGE -> log.info("MESSAGE: " + sessionId);
//            case ERROR -> log.info("ERROR: " + sessionId);
//            default -> log.info("UNKNOWN: " + sessionId);
//        }
//
//    }

    private void setAuthentication(
            Authentication authentication,
            StompHeaderAccessor headerAccessor
    ) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        headerAccessor.setUser(authentication);
    }
}
