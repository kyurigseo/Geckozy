package com.likelion.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 Bad Request
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST"),
    INVALID_PAGE(HttpStatus.BAD_REQUEST, "INVALID_PAGE"),
    SENSOR_ENCLOSURE_MISMATCH(HttpStatus.BAD_REQUEST, "SENSOR_ENCLOSURE_MISMATCH"),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"),
    UNAUTHORIZED_SENSOR(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_SENSOR"),

    // 404 Not Found
    SPECIES_NOT_FOUND(HttpStatus.NOT_FOUND, "SPECIES_NOT_FOUND"),
    LIZARD_NOT_FOUND(HttpStatus.NOT_FOUND, "LIZARD_NOT_FOUND"),
    ENCLOSURE_NOT_FOUND(HttpStatus.NOT_FOUND, "ENCLOSURE_NOT_FOUND"),
    GUIDEBOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "GUIDEBOOK_NOT_FOUND"),
    GUIDEBOOK_PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "GUIDEBOOK_PAGE_NOT_FOUND"),
    SCRAP_NOT_FOUND(HttpStatus.NOT_FOUND, "SCRAP_NOT_FOUND"),
    SENSOR_NOT_FOUND(HttpStatus.NOT_FOUND, "SENSOR_NOT_FOUND"),

    // 409 Conflict
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "DUPLICATE_USERNAME"),
    ENCLOSURE_ALREADY_EXISTS(HttpStatus.CONFLICT, "ENCLOSURE_ALREADY_EXISTS"),
    ALREADY_SCRAPPED(HttpStatus.CONFLICT, "ALREADY_SCRAPPED"),
    SENSOR_ALREADY_CONNECTED(HttpStatus.CONFLICT, "SENSOR_ALREADY_CONNECTED"),

    // 422 Unprocessable Entity
    INVALID_SENSOR_VALUE(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_SENSOR_VALUE"),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR"
    );

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}