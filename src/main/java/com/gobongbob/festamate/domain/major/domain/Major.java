package com.gobongbob.festamate.domain.major.domain;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Major {
    COMPUTER_SCIENCE("소프트웨어융합대학", "AI컴퓨터공학부"),
    BUSINESS_ADMINISTRATION("소프트웨어융합대학", "경영학부"),
    INDUSTRIAL_MANAGEMENT_INFORMATION("소프트웨어융합대학", "컴퓨터공학과");

    /*
    추후 더 다양한 학과가 추가될 예정입니다.
     */

    private final String college;
    private final String department;

    public static Major findByDepartment(String department) {
        return Arrays.stream(Major.values())
                .filter(major -> major.getDepartment().equals(department))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 학과가 존재하지 않습니다."));
    }
}
