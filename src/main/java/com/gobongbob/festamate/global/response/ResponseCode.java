package com.gobongbob.festamate.global.response;

import lombok.Getter;

@Getter
public enum ResponseCode {

    SUCCESS(true, "요청에 성공하였습니다."),
    INTERNAL_SEVER_ERROR(false, "서버 에러가 발생하였습니다. 관리자에게 문의해 주세요."),
    INVALID_REQUEST(false, "올바르지 않은 요청입니다."),
    NOT_FOUND_ROOM(false, "모임방이 존재하지 않습니다."),
    ALREADY_PARTICIPATING(false, "이미 모임방에 참여하고 있습니다."),
    NO_PARTICIPATING_ROOM(false, "참여중인 모임방이 존재하지 않습니다."),
    FULL_ROOM(false, "모임방이 꽉 찼습니다."),
    MUST_HOST(false, "방장이어야 합니다."),
    MUST_NORMAL(false, "일반 회원이어야 합니다."),
    CAN_NOT_UPDATE(false, "방에 방장을 제외한 다른 사용자가 입장한 상태에서는 수정할 수 없습니다."),
    NO_GENDER(false, "해당하는 성별이 없습니다."),
    EMPTY_FILE(false, "파일이 비어있습니다."),
    UNEXPECTED_TOKEN(false, "올바르지 않은 토큰입니다."),
    USER_NOT_FOUND(false, "해당 유저를 찾을 수 없습니다"),
    CHAT_ROOM_NOT_FOUND(false, "채팅방이 존재하지 않습니다."),
    NO_AUTHORITY_CHAT_ROOM(false, "채팅방을 조회할 수 있는 권한이 없습니다."),
    NO_DEPARTMENT(false, "해당하는 학과가 존재하지 않습니다."),
    NO_MEMBER(false, "사용자가 존재하지 않습니다."),
    DUPLICATE_NICKNAME(false, "중복된 닉네임입니다."),
    NOT_ENOUGH_TICKET(false, "티켓이 부족합니다."),
    CAN_NOT_REPORT_MYSELF(false, "자신의 방은 신고할 수 없습니다."),
    ALREADY_REPORT(false, "이미 신고한 방입니다."),
    NO_REPORT(false, "해당 신고가 존재하지 않습니다."),
    NO_REPORT_REASON(false, "해당하는 신고 사유가 없습니다."),
    NO_ADMIN(false, "관리자 권한이 필요합니다."),

    EXCEED_IMAGE_CAPACITY(false, "업로드 가능한 이미지 용량을 초과했습니다.");

    private final boolean isSuccess;
    private final String message;

    ResponseCode(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }
}
