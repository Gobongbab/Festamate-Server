package com.gobongbob.festamate.global.aop;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UserStatusCheckAspect {

    // @CheckActiveUser 어노테이션이 붙은 메서드를 대상으로 지정
    @Pointcut("@annotation(com.gobongbob.festamate.global.aop.CheckActiveUser)")
    // 어노테이션 경로 확인
    public void checkActiveUserPointcut() {
    }

    // 대상 메서드 실행 전에 사용자 상태 확인
    @Before("checkActiveUserPointcut()")
    public void checkUserStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomMemberDetails memberDetails)) {
            throw new AccessDeniedException("인증 정보가 유효하지 않습니다."); // 인증 안 된 접근은 여기서 차단
        }

        // isEnabled()로 상태 확인 (false이면 차단)
        if (!memberDetails.isEnabled()) {
            throw new AccessDeniedException("차단되었거나 비활성화된 사용자입니다. 이 기능을 사용할 수 없습니다.");
        }
    }
}
