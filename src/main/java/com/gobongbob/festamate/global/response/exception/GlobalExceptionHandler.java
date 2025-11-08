package com.gobongbob.festamate.global.response.exception;

import static com.gobongbob.festamate.global.response.ResponseCode.EXCEED_IMAGE_CAPACITY;
import static com.gobongbob.festamate.global.response.ResponseCode.INVALID_REQUEST;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 전역 예외 처리기입니다. body로 클라이언트에게 유용한 피드백을 제공하면서, 로깅을 추가하여 서버 측에서도 문제를 효과적으로 추적할 수 있게 해줍니다.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // @Valid 어노테이션으로 검증 실패 시 발생하는 예외를 처리합니다. -> 보류
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, //발생한 예외 객체
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        // 예외 메시지를 WARN 레벨로 로깅
        log.warn(e.getMessage(), e);

        String errMessage = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        // BindingResult에서 첫 번째 FieldError의 DefaultMessage를 가져옵니다. 이후 FieldError가 null이 아님을 보장합니다.

        return ResponseEntity.badRequest() // HTTP 상태 코드 400으로 응답을 시작
                .body(new ExceptionResponse(INVALID_REQUEST.isSuccess(), errMessage)); // 응답 body 생성
    }

    // 파일 업로드 크기 제한 초과 시 발생하는 예외를 처리합니다.
    @ExceptionHandler(SizeLimitExceededException.class) // SizeLimitExceededException 클래스의 예외가 발생했을 때 이 메소드가 처리함을 명시
    public ResponseEntity<ExceptionResponse> handleSizeLimitExceededException(SizeLimitExceededException e) {
        log.warn(e.getMessage(), e);

        String message = EXCEED_IMAGE_CAPACITY.getMessage()
                + " 입력된 이미지 용량은 " + e.getActualSize() + " byte 입니다. "
                + "(제한 용량: " + e.getPermittedSize() + " byte)";
        return ResponseEntity.badRequest() // HTTP 상태 코드 400으로 응답
                .body(new ExceptionResponse(EXCEED_IMAGE_CAPACITY.isSuccess(), message));
    }

    // 일반적인 잘못된 요청 예외를 처리합니다.
    @ExceptionHandler(BadRequestException.class) // BadRequestException 클래스의 예외가 발생했을 때 이 메소드가 처리함을 명시
    public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException e) {
        log.warn(e.getMessage(), e);

        return ResponseEntity.badRequest() // HTTP 상태 코드 400으로 응답
                .body(new ExceptionResponse(INVALID_REQUEST.isSuccess(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ExceptionResponse> handleInternalServerError(Exception e) {
        // 500 에러는 서버의 버그이므로, WARN이 아닌 ERROR 레벨로 로깅해야 합니다.
        log.error("처리되지 못한 서버 내부 오류 발생: {}", e.getMessage(), e);

        // 클라이언트에게는 상세한 오류 내용을 숨기고, 공통 응답 포맷을 반환합니다.
        return ResponseEntity
                .internalServerError() // HTTP 500
                .body(new ExceptionResponse(
                        false, // isSuccess
                        "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요." // 클라이언트에게 노출할 메시지
                ));
    }
}