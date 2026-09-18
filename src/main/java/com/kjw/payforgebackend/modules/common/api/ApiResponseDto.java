package com.kjw.payforgebackend.modules.common.api;

import com.kjw.payforgebackend.modules.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 모든 API 응답의 공통 본문.
 *
 * <ul>
 *   <li>status  : HTTP 상태 코드. 응답 상태 줄과 항상 같다({@link ApiResponseStatusAdvice} 가 맞춘다).</li>
 *   <li>code    : 클라이언트가 분기에 쓰는 비즈니스 코드. 성공은 SUCCESS, 실패는 {@link ErrorCode#getCode()}.</li>
 *   <li>message : 사람이 읽는 메시지. 내부 예외 메시지는 담지 않는다.</li>
 *   <li>data    : 성공 시 결과, 입력값 오류 시 필드별 오류 목록. 없으면 null.</li>
 * </ul>
 */
public record ApiResponseDto<T>(int status, String code, String message, T data) {

    private static final String SUCCESS_CODE = "SUCCESS";
    private static final String SUCCESS_MESSAGE = "요청이 성공했습니다.";

    /** 200 OK — 조회, 수정 */
    public static <T> ApiResponseDto<T> ok(T data) {
        return success(HttpStatus.OK, data);
    }

    /** 201 Created — 생성. 컨트롤러에서 Location 헤더를 함께 내려준다. */
    public static <T> ApiResponseDto<T> created(T data) {
        return success(HttpStatus.CREATED, data);
    }

    /** 202 Accepted — 접수만 하고 처리는 비동기로 끝나는 요청 */
    public static <T> ApiResponseDto<T> accepted(T data) {
        return success(HttpStatus.ACCEPTED, data);
    }

    public static ApiResponseDto<Void> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getMessage(), null);
    }

    public static ApiResponseDto<Void> error(ErrorCode errorCode, String message) {
        return error(errorCode, message, null);
    }

    public static <T> ApiResponseDto<T> error(ErrorCode errorCode, String message, T data) {
        return new ApiResponseDto<>(errorCode.getStatus().value(), errorCode.getCode(), message, data);
    }

    /** ErrorCode 로 정의하지 않은 상태 코드를 프레임워크가 낼 때 쓴다. 코드는 HttpStatus 이름을 그대로 쓴다. */
    public static ApiResponseDto<Void> error(HttpStatus status) {
        return new ApiResponseDto<>(status.value(), status.name(), status.getReasonPhrase(), null);
    }

    private static <T> ApiResponseDto<T> success(HttpStatus status, T data) {
        return new ApiResponseDto<>(status.value(), SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }
}
