package com.gobongbob.festamate.global.config.webSocket;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.global.util.TokenProvider;
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
        StompCommand command = accessor.getCommand();
        log.debug("Command: " + command);

        if (command == StompCommand.CONNECT || command == StompCommand.SEND) {
            String tokenHeader = accessor.getFirstNativeHeader("Authorization");
            if (tokenHeader == null) {
                log.error("No token found in message from session: " + accessor.getSessionId());
                throw new IllegalArgumentException("No token found");
            }

            String token = tokenHeader.replace("Bearer ", "");
            tokenProvider.validateToken(token);

            Authentication authentication = tokenProvider.getAuthentication(token);
            setAuthentication(authentication, accessor);
        }

        return message;
    }

    private static void setAuthentication(Authentication authentication, StompHeaderAccessor accessor) {
        CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        accessor.setUser(memberDetails);
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
}
