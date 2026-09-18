package com.kjw.payforgebackend.modules.common.exception;

import com.kjw.payforgebackend.modules.common.api.ApiResponseDto;
import com.kjw.payforgebackend.modules.common.api.FieldErrorDto;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 모든 예외를 {@link ApiResponseDto} 로 변환한다.
 * Spring MVC 표준 예외(404, 405, 415, 요청 본문 파싱 실패 등)는 {@link ResponseEntityExceptionHandler} 가
 * 올바른 상태 코드로 분류해 주므로 그대로 쓰고, 본문만 공통 형식으로 바꾼다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        if (errorCode.getStatus().is5xxServerError()) {
            log.error("[{}] {}", errorCode.getCode(), ex.getMessage(), ex);
        } else {
            log.warn("[{}] {}", errorCode.getCode(), ex.getMessage());
        }
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponseDto.error(errorCode, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUnexpectedException(Exception ex) {
        log.error("처리되지 않은 예외", ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponseDto.error(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }

    /** @RequestBody @Valid 검증 실패 */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<FieldErrorDto> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> new FieldErrorDto(
                        error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName(),
                        error.getDefaultMessage()))
                .toList();
        return handleExceptionInternal(ex, invalidInput(errors), headers, status, request);
    }

    /** @PathVariable, @RequestParam 등에 붙인 제약(@Positive 등) 검증 실패 */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<FieldErrorDto> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new FieldErrorDto(
                                result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .toList();
        return handleExceptionInternal(ex, invalidInput(errors), headers, status, request);
    }

    /** 부모 클래스가 처리하는 모든 표준 예외가 최종적으로 거치는 곳. 본문을 ApiResponseDto 로 바꾼다. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        if (status.is5xxServerError()) {
            log.error("[{}] {}", status.value(), ex.getMessage(), ex);
        } else {
            log.warn("[{}] {}", status.value(), ex.getMessage());
        }

        Object response = body instanceof ApiResponseDto<?>
                ? body
                : CommonErrorCode.from(status)
                        .map(ApiResponseDto::error)
                        .orElseGet(() -> ApiResponseDto.error(status));
        return super.handleExceptionInternal(ex, response, headers, statusCode, request);
    }

    private static ApiResponseDto<List<FieldErrorDto>> invalidInput(List<FieldErrorDto> errors) {
        CommonErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
        return ApiResponseDto.error(errorCode, errorCode.getMessage(), errors);
    }
}
