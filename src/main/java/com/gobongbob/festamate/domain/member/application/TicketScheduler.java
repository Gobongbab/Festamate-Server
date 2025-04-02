package com.gobongbob.festamate.domain.member.application;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketScheduler {

    private final MemberRepository memberRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // 매일 00:00:00에 실행
    public void resetRemainingTickets() {
        memberRepository.findAll()
                .forEach(Member::initTicket);
    }
}
