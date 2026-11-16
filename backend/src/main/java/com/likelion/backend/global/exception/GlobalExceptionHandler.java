package com.likelion.backend.global.exception;

import com.likelion.backend.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException e
    ) {
        return toResponse(e.getErrorCode());
    }

    // @Valid 검증 실패 (@NotNull 등)
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            Exception e
    ) {
        log.debug("Validation failed: {}", e.getMessage());
        return toResponse(ErrorCode.INVALID_REQUEST);
    }

    // JSON 파싱 실패 (잘못된 enum 값, 형식 오류, 본문 없음 등)
    // 쿼리 파라미터 누락 / 타입 불일치
    // null id 로 조회 시도 (findById(null) 등)
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MissingPathVariableException.class,
            MethodArgumentTypeMismatchException.class,
            InvalidDataAccessApiUsageException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            Exception e
    ) {
        log.debug("Bad request: {}", e.getMessage());
        return toResponse(ErrorCode.INVALID_REQUEST);
    }

    // Content-Type 누락 / 미지원
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException e
    ) {
        return toResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException e
    ) {
        return toResponse(ErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(
            NoResourceFoundException e
    ) {
        return toResponse(ErrorCode.RESOURCE_NOT_FOUND);
    }

    // DB 제약조건 위반 (unique 등 - 동시 요청으로 중복 검사를 통과한 경우)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(
            DataIntegrityViolationException e
    ) {
        log.warn("Data integrity violation: {}", e.getMostSpecificCause().getMessage());
        return toResponse(ErrorCode.DATA_INTEGRITY_VIOLATION);
    }

    // 그 외 처리되지 않은 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception e
    ) {
        log.error("Unhandled exception", e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(
                        ApiResponse.error(
                                errorCode.getStatus().value(),
                                errorCode.getMessage()
                        )
                );
    }
}
