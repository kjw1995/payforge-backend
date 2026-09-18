package com.kjw.payforgebackend.modules.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 오류 코드 규약. 공통 오류는 {@link CommonErrorCode}, 도메인 오류는 모듈마다 enum 으로 구현한다.
 * 예) PaymentErrorCode.PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 내역을 찾을 수 없습니다.")
 */
public interface ErrorCode {

    HttpStatus getStatus();

    String getCode();

    String getMessage();
}
