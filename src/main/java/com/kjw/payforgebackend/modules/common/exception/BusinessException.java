package com.kjw.payforgebackend.modules.common.exception;

import lombok.Getter;

/**
 * 서비스 로직에서 의도적으로 던지는 예외. HTTP 상태와 응답 코드는 {@link ErrorCode} 가 정한다.
 * 메시지는 클라이언트에 그대로 내려가므로 내부 정보(SQL, 스택, 카드번호 등)를 담지 않는다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
