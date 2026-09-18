package com.kjw.payforgebackend.modules.common.api;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 응답 본문이 {@link ApiResponseDto} 면 HTTP 상태 코드를 본문의 status 로 맞춘다.
 * 상태 코드의 기준은 본문 하나다. 컨트롤러가 ApiResponseDto 를 바로 반환해도, ResponseEntity 로 감싸도 둘이 어긋나지 않는다.
 */
@RestControllerAdvice
public class ApiResponseStatusAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ApiResponseDto<?> apiResponse) {
            response.setStatusCode(HttpStatusCode.valueOf(apiResponse.status()));
        }
        return body;
    }
}
