package com.gobongbob.festamate.domain.member.dto.request;

import com.gobongbob.festamate.domain.major.domain.Major;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;

// 프로필 등록용 DTO
public record ProfileRegisterRequest(
        String name,
        String studentId,
        String phoneNumber,
        String phoneVerificationCodes,
        Gender gender,
        String college,
        String department,
        String kakaoAccessToken, // 카카오 access token
        String nickname         // 회원이 등록할 닉네임
) {

    // Member 엔티티로 변환하는 메서드
    public Member toEntity(Member existingMember) {
        existingMember.registerProfile(
                name,
                makeRandomNickname(),
                studentId,
                phoneNumber,
                gender,
                Major.findByDepartment(department)
        );

        return existingMember;
    }

    private String makeRandomNickname() {
        // 귀엽고 친근한 형용사 배열
        String[] adjectives = {
                "귀여운", "깜찍한", "아기자기한", "달콤한", "포근한", "따뜻한", "반짝이는", "사랑스러운",
                "푹신한", "동글동글한", "폭신폭신한", "말랑말랑한", "앙증맞은", "반짝반짝한", "꼬마", "작은",
                "뽀짝뽀짝한", "방글방글한", "사랑스런", "보들보들한", "몽글몽글한", "새콤달콤한", "두근두근한",
                "아삭아삭한", "달달한", "향긋한", "포실포실한", "보송보송한", "쪼꼬미", "아름다운", "예쁜",
                "귀염둥이", "깨끗한", "산뜻한", "싱그러운", "상큼한", "촉촉한", "반들반들한", "빛나는",
                "환한", "따스한", "부드러운", "순수한", "해맑은", "명랑한", "재잘재잘한", "활발한",
                "방긋한", "아롱아롱한", "소곤소곤한", "둥글둥글한", "반짝거리는", "쫑긋한", "아담한", "조그마한",
                "쪼그만", "귀엽살짝한", "통통한", "오동통한", "쫑긋쫑긋한", "아기같은", "꼬물꼬물한", "꼬물락꼬물락한",
                "꼬마난쟁이", "깔끔한", "윤기나는", "쭈글쭈글한", "꼬불꼬불한", "꾸덕꾸덕한", "달콤새콤한",
                "달콤쌉싸름한", "달콤씁쓸한", "달콤향긋한", "달콤고소한", "달콤짭짤한", "달콤바삭한", "달콤쫄깃한",
                "달콤촉촉한", "달콤포슬포슬한", "달콤아삭한", "달콤쫀득한", "달콤폭신한", "달콤말랑한", "달콤보들한",
                "달콤푹신한", "달콤몽글한"
        };

        // 귀엽고 친근한 명사 배열
        String[] nouns = {
                "병아리", "토끼", "곰돌이", "강아지", "고양이", "판다", "쿠키", "마카롱", "푸딩", "솜사탕",
                "구름", "별님", "꽃잎", "무지개", "도토리", "방울", "햇님", "달님", "사탕", "젤리",
                "송이", "단추", "리본", "모찌", "만두", "딸기", "복숭아", "수박", "멜론", "사과",
                "바나나", "포도", "오렌지", "레몬", "블루베리", "당근", "감자", "옥수수", "콩알", "팥알",
                "밤톨", "호두", "꿀벌", "나비", "잠자리", "참새", "오리", "거북이", "다람쥐", "햄스터",
                "펭귄", "코끼리", "기린", "원숭이", "하마", "물개", "바다표범", "돌고래", "고래",
                "불가사리", "조개", "소라", "멍게", "가리비", "새우", "게", "문어", "오징어", "해파리",
                "물방울", "눈송이", "바람개비", "풍선", "요술봉", "종이접기", "인형", "장난감", "연필", "크레파스",
                "지우개", "색연필", "물감", "크레용", "붓", "도화지", "색종이", "풀", "가위", "테이프",
                "스티커", "도장", "책갈피", "책", "동화", "이야기", "노래", "춤", "그림", "사진", "팬케이크", "와플",
                "머핀", "도넛", "타르트", "파이", "케이크", "빵", "비스킷", "크로와상", "베이글", "프레첼", "마들렌",
                "에클레어", "슈크림", "크레페", "롤케이크", "치즈케이크", "티라미수",
                "아이스크림", "소르베", "밀크셰이크", "스무디", "주스", "레모네이드", "차", "커피",
                "코코아", "핫초코", "밀크티", "버블티", "에이드", "소다", "사이다", "콜라", "라떼", "모카",
                "캐러멜", "초콜릿", "젤리빈", "마시멜로", "누가", "캔디", "롤리팝", "껌",
                "팝콘", "감자칩", "나초", "크래커", "와플칩", "누들", "파스타", "스파게티",
                "라면", "우동", "소바", "국수", "쌀국수", "딤섬", "군만두", "찐만두", "물만두",
                "떡볶이", "순대", "튀김", "김밥", "주먹밥", "삼각김밥", "초밥", "롤", "샌드위치", "햄버거",
                "핫도그", "피자", "타코", "부리토", "케밥", "샐러드", "스프", "찌개", "국", "탕",
                "짜장면", "짬뽕", "볶음밥", "덮밥", "비빔밥", "오므라이스", "카레", "스테이크", "돈까스", "치킨"
        };
        // 닉네임 뒤에 붙일 랜덤 4자리 숫자 생성
        int randomNumber = (int) (Math.random() * 9000) + 1000;

        // 랜덤한 인덱스 생성
        int randomAdjectiveIndex = (int) (Math.random() * adjectives.length);
        int randomNounIndex = (int) (Math.random() * nouns.length);

        // 랜덤한 형용사와 명사 조합
        String randomNickname =
                adjectives[randomAdjectiveIndex] + nouns[randomNounIndex] + randomNumber;

        return randomNickname;
    }
}