package com.kjw.payforgebackend.modules.common.exception;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kjw.payforgebackend.modules.common.api.ApiResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandlerTest.TestController.class)
@DisplayName("공통 응답 / 예외 매핑")
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("조회 성공은 200 과 SUCCESS")
    void ok() throws Exception {
        mockMvc.perform(get("/test/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("item-1"));
    }

    @Test
    @DisplayName("생성 성공은 201 과 Location 헤더")
    void created() throws Exception {
        mockMvc.perform(post("/test/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"pen\",\"price\":1000}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/test/items/1"))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.name").value("pen"));
    }

    @Test
    @DisplayName("ApiResponseDto 를 ResponseEntity 없이 반환해도 HTTP 상태는 본문 status 를 따른다")
    void statusFollowsBody() throws Exception {
        mockMvc.perform(post("/test/items/1/approve"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value(202));
    }

    @Test
    @DisplayName("BusinessException 은 ErrorCode 의 상태와 코드로 응답한다")
    void businessException() throws Exception {
        mockMvc.perform(get("/test/items/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("아이템(id=404)을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("@RequestBody 검증 실패는 400 INVALID_INPUT 과 필드 목록")
    void requestBodyValidation() throws Exception {
        mockMvc.perform(post("/test/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.data[*].field").value(containsInAnyOrder("name", "price")))
                .andExpect(content().string(not(containsString("rejectedValue"))));
    }

    @Test
    @DisplayName("@PathVariable 제약 위반도 400 INVALID_INPUT")
    void pathVariableValidation() throws Exception {
        mockMvc.perform(get("/test/items/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.data[0].field").value("id"));
    }

    @Test
    @DisplayName("본문 JSON 파싱 실패는 400 BAD_REQUEST")
    void malformedJson() throws Exception {
        mockMvc.perform(post("/test/items").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("없는 경로는 404 NOT_FOUND")
    void noResource() throws Exception {
        mockMvc.perform(get("/test/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("지원하지 않는 메서드는 405 와 Allow 헤더")
    void methodNotAllowed() throws Exception {
        mockMvc.perform(put("/test/items/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "GET"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    @DisplayName("지원하지 않는 Content-Type 은 415")
    void unsupportedMediaType() throws Exception {
        mockMvc.perform(post("/test/items").contentType(MediaType.TEXT_PLAIN).content("pen"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    @DisplayName("예상하지 못한 예외는 500 이고 내부 메시지를 노출하지 않는다")
    void unexpectedException() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(content().string(not(containsString("secret"))));
    }

    record ItemRequest(@NotBlank String name, @Positive Integer price) {
    }

    record ItemResponse(Long id, String name) {
    }

    @RestController
    static class TestController {

        @GetMapping("/test/items/{id}")
        ApiResponseDto<ItemResponse> get(@PathVariable @Positive Long id) {
            if (id == 404) {
                throw new BusinessException(CommonErrorCode.NOT_FOUND, "아이템(id=" + id + ")을 찾을 수 없습니다.");
            }
            return ApiResponseDto.ok(new ItemResponse(id, "item-" + id));
        }

        @PostMapping("/test/items")
        ResponseEntity<ApiResponseDto<ItemResponse>> create(@RequestBody @Valid ItemRequest request) {
            ItemResponse item = new ItemResponse(1L, request.name());
            return ResponseEntity.created(URI.create("/test/items/" + item.id()))
                    .body(ApiResponseDto.created(item));
        }

        @PostMapping("/test/items/{id}/approve")
        ApiResponseDto<Void> approve(@PathVariable Long id) {
            return ApiResponseDto.accepted(null);
        }

        @GetMapping("/test/boom")
        ApiResponseDto<Void> boom() {
            throw new IllegalStateException("secret internal detail");
        }
    }
}
