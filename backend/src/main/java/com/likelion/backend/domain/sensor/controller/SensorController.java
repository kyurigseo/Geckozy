package com.likelion.backend.domain.sensor.controller;

import com.likelion.backend.domain.sensor.dto.SensorConnectRequest;
import com.likelion.backend.domain.sensor.dto.SensorConnectResponse;
import com.likelion.backend.domain.sensor.service.SensorService;
import com.likelion.backend.global.response.ApiResponse;
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

    private final SensorService sensorService;

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

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> disconnect(
            @AuthenticationPrincipal Long currentUserId,
            @PathVariable Long enclosureId
    ) {
        sensorService.disconnect(currentUserId, enclosureId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
