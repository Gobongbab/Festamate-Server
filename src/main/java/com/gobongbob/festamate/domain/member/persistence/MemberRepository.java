package com.gobongbob.festamate.domain.member.persistence;

import com.gobongbob.festamate.domain.member.domain.Member;
import org.springframework.data.repository.Repository;

public interface MemberRepository extends Repository<Member, Long> {

}
