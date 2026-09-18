package com.kjw.payforgebackend.modules.common.api;

/**
 * 입력값 검증 실패 항목.
 * 거부된 값(rejectedValue)은 카드번호·비밀번호가 그대로 되돌아갈 수 있어 응답에 담지 않는다.
 */
public record FieldErrorDto(String field, String reason) {
}
