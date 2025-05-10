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
    @Scheduled(cron = "0 0 15 * * ?") // 배포 서버(UTC) 기준으로 설정, 한국 시간으로 자정에 스케줄러 실행
    public void resetRemainingTickets() {
        memberRepository.findAll()
                .forEach(Member::initTicket);
    }
}
