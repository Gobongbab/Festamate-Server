package com.gobongbob.festamate.global.config;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.member.domain.Member;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
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

        if (Objects.requireNonNull(accessor.getCommand()) == StompCommand.SEND) {
            String tokenHeader = accessor.getFirstNativeHeader("Authorization");
            if (tokenHeader == null) {
                log.error("No token found in message from session: " + sessionId);
                throw new IllegalArgumentException("No token found");
            }

            String token = tokenHeader.replace("Bearer ", "");
            tokenProvider.validateToken(token);

            Long userId = tokenProvider.getUserId(token);
            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("No user found"));
            UserDetails userDetails = new CustomMemberDetails(member);

            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            ));
        }

        return message;
    }
}
