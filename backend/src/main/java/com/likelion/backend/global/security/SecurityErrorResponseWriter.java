package com.likelion.backend.global.security;

import com.likelion.backend.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Security 필터 단계(컨트롤러 진입 전)에서 발생한 401/403 을
 * ApiResponse 와 같은 형식의 JSON 으로 내려준다.
 */
final class SecurityErrorResponseWriter {

    private SecurityErrorResponseWriter() {
    }

    static void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        int status = errorCode.getStatus().value();
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":" + status
                        + ",\"message\":\"" + errorCode.getMessage() + "\""
                        + ",\"data\":null}"
        );
    }
}
