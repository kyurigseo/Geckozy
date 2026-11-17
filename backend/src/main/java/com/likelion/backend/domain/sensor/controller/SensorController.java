package com.likelion.backend.domain.sensor.controller;

import com.likelion.backend.domain.sensor.dto.*;
import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.service.SensorService;
import com.likelion.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enclosures/{enclosureId}/sensor")
@RequiredArgsConstructor
public class SensorController {

    public static final String DEVICE_ID_HEADER = "X-Device-Id";

    private final SensorService sensorService;

    @Operation(summary = "IoT 센서 연결")
    @PostMapping
    public ResponseEntity<ApiResponse<SensorConnectResponse>> connect(
            @AuthenticationPrincipal Long currentUserId,
            @PathVariable Long enclosureId,
            @RequestBody @Valid SensorConnectRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(sensorService.connect(currentUserId, enclosureId, request)));
    }

    @Operation(summary = "IoT 센서 연결 해제")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> disconnect(
            @AuthenticationPrincipal Long currentUserId,
            @PathVariable Long enclosureId
    ) {
        sensorService.disconnect(currentUserId, enclosureId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "IoT 센서 데이터 수집",
            description = "아두이노 등 센서 장치 전용. JWT 대신 X-Device-Id 헤더에 연결 시 등록한 deviceIdentifier를 넣는다.")
    @PostMapping("/readings")
    public ResponseEntity<ApiResponse<SensorReadingResponse>> saveReading(
            @PathVariable Long enclosureId,
            @Parameter(description = "센서 연결 시 등록한 deviceIdentifier")
            @RequestHeader(value = DEVICE_ID_HEADER, required = false) String deviceId,
            @RequestBody @Valid SensorReadingRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(sensorService.saveReading(enclosureId, deviceId, request)));
    }

    @Operation(summary = "센서 연결 상태 확인", description = "마지막 수신 후 30분 이상 지나면 DISCONNECTED")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<SensorStatusResponse>> getStatus(
            @AuthenticationPrincipal Long currentUserId,
            @PathVariable Long enclosureId
    ) {
        SensorStatusResponse response = sensorService.getStatus(currentUserId, enclosureId);
        String message = response.status() == Sensor.Status.DISCONNECTED ? "SENSOR_DISCONNECTED" : "SUCCESS";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}
