package com.gobongbob.festamate.global.response;

import lombok.Getter;

@Getter
public enum ResponseCode {

    // 공통
    SUCCESS(true, "요청에 성공하였습니다."),
    INTERNAL_SEVER_ERROR(false, "서버 에러가 발생하였습니다. 관리자에게 문의해 주세요."),
    INVALID_REQUEST(false, "올바르지 않은 요청입니다."),

    // 모임방
    NOT_FOUND_ROOM(false, "모임방이 존재하지 않습니다."),
    ALREADY_PARTICIPATING(false, "이미 모임방에 참여하고 있습니다."),
    NO_PARTICIPATING_ROOM(false, "참여중인 모임방이 존재하지 않습니다."),
    ALREADY_MATCHED(false, "이미 매칭이 완료된 모임방입니다."),
    ROOM_NOT_JOINABLE(false, "모임의 남은 자리가 입장 인원 수와 일치하지 않습니다."),
    ROOM_FULL(false, "모임이 가득 찼습니다."),
    PHONE_NUMBER_DUPLICATE(false, "같은 회원이 동시에 참여할 수 없습니다."),
    GENDER_NOT_MATCH(false, "성별이 일치하지 않습니다."),
    STUDENT_ID_NOT_MATCH(false, "학번이 일치하지 않습니다."),
    MUST_HOST(false, "방장이어야 합니다."),
    MUST_NORMAL(false, "일반 회원이어야 합니다."),
    CAN_NOT_UPDATE(false, "방에 방장을 제외한 다른 사용자가 입장한 상태에서는 수정할 수 없습니다."),
    NOT_ENOUGH_TICKET(false, "티켓이 부족합니다."),
    ROLE_NOT_FOUND(false, "해당하는 권한이 없습니다."),
    STATUS_NOT_FOUND(false, "해당하는 모임방 상태가 없습니다. (매칭중, 매칭완료, 종료)"),

    // 회원
    NO_MEMBER(false, "사용자가 존재하지 않습니다."),
    NO_GENDER(false, "해당하는 성별이 없습니다."),
    DUPLICATE_NICKNAME(false, "중복된 닉네임입니다."),
    DUPLICATE_STUDENT_ID(false, "중복된 학번입니다."),
    USER_NOT_FOUND(false, "해당 유저를 찾을 수 없습니다"),
    UNEXPECTED_TOKEN(false, "올바르지 않은 토큰입니다."),
    PHONE_NOT_VERIFIED(false, "전화번호 인증이 완료되지 않았습니다."),
    PHONE_NUMBER_DUPLICATE_AMONG_PARTICIPANTS(false, "초대된 사용자(호스트 및 친구) 간에 중복된 전화번호가 있습니다."),
    PROFILE_REGISTRATION_REQUIRED(false, "프로필 등록이 필요합니다."),
    USER_ALREADY_EXISTS(false, "이미 존재하는 회원입니다."),

    // 인증
    KAKAO_USER_INFO_PARSING_ERROR(false, "카카오 사용자 정보 파싱에 실패했습니다."),
    KAKAO_USER_ID_NOT_FOUND(false, "카카오 사용자 정보에서 ID를 찾을 수 없습니다."),
    KAKAO_RESPONSE_EMPTY(false, "카카오 사용자 정보 응답이 비어있습니다."),
    KAKAO_TOKEN_INVALID_OR_EXPIRED(false, "카카오 토큰이 만료되었거나 유효하지 않습니다."),
    KAKAO_API_REQUEST_FAILED(false, "카카오 API 요청에 실패했습니다."),
    AUTH_CODE_NOT_FOUND_OR_EXPIRED(false, "인증 요청이 존재하지 않거나 만료되었습니다."),
    AUTH_CODE_MISMATCH(false, "인증번호가 틀렸습니다."),
    AUTH_REQUEST_DAILY_LIMIT_EXCEEDED(false, "인증번호 전송 횟수를 초과했습니다. 하루 최대 2회까지만 전송할 수 있습니다.%s"),

    // 파일
    EMPTY_FILE(false, "파일이 비어있습니다."),
    EXCEED_IMAGE_CAPACITY(false, "업로드 가능한 이미지 용량을 초과했습니다."),

    // 채팅방
    CHAT_ROOM_NOT_FOUND(false, "채팅방이 존재하지 않습니다."),
    NO_AUTHORITY_CHAT_ROOM(false, "채팅방을 조회할 수 있는 권한이 없습니다."),
    NO_DEPARTMENT(false, "해당하는 학과가 존재하지 않습니다."),

    // 신고
    CAN_NOT_REPORT_MYSELF(false, "자신의 방은 신고할 수 없습니다."),
    ALREADY_REPORTED_ROOM(false, "이미 신고한 방입니다."),
    ALREADY_REPORTED_MEMBER(false, "이미 신고한 유저입니다."),
    NO_REPORT(false, "해당 신고가 존재하지 않습니다."),
    NO_REPORT_REASON(false, "해당하는 신고 사유가 없습니다."),
    AUTHENTICATION_INVALID(false, "인증 정보가 유효하지 않습니다."),
    USER_ACCOUNT_DISABLED_OR_BLOCKED(false, "차단되었거나 비활성화된 사용자입니다. 이 기능을 사용할 수 없습니다."),

    // 티켓
    NOT_EXIST_TICKET(false, "존재하지 않는 티켓입니다."),
    ALREADY_USED_TICKET(false, "이미 사용된 쿠폰입니다."),
    EXPIRED_TICKET(false, "만료된 쿠폰입니다."),

    // 관리자
    NO_ADMIN(false, "관리자 권한이 필요합니다.");


    private final boolean isSuccess;
    private final String message;

    ResponseCode(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }
}
