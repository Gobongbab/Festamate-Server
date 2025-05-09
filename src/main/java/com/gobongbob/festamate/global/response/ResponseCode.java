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
    PHONE_NUMBER_DUPLICATE_AMONG_PARTICIPANTS(false,  "초대된 사용자(호스트 및 친구) 간에 중복된 전화번호가 있습니다."),

    // 파일
    EMPTY_FILE(false, "파일이 비어있습니다."),
    EXCEED_IMAGE_CAPACITY(false, "업로드 가능한 이미지 용량을 초과했습니다."),

    // 채팅방
    CHAT_ROOM_NOT_FOUND(false, "채팅방이 존재하지 않습니다."),
    NO_AUTHORITY_CHAT_ROOM(false, "채팅방을 조회할 수 있는 권한이 없습니다."),
    NO_DEPARTMENT(false, "해당하는 학과가 존재하지 않습니다."),

    // 신고
    CAN_NOT_REPORT_MYSELF(false, "자신의 방은 신고할 수 없습니다."),
    ALREADY_REPORT(false, "이미 신고한 방입니다."),
    NO_REPORT(false, "해당 신고가 존재하지 않습니다."),
    NO_REPORT_REASON(false, "해당하는 신고 사유가 없습니다."),

    // 관리자
    NO_ADMIN(false, "관리자 권한이 필요합니다.");


    private final boolean isSuccess;
    private final String message;

    ResponseCode(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }
}
