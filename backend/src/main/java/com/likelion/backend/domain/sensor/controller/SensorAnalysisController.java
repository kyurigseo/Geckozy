package com.likelion.backend.domain.sensor.controller;

import com.likelion.backend.domain.sensor.dto.EnvironmentalAlertResponse;
import com.likelion.backend.domain.sensor.dto.HumidityCycleResponse;
import com.likelion.backend.domain.sensor.service.SensorAnalysisService;
import com.likelion.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enclosures/{enclosureId}")
@RequiredArgsConstructor
public class SensorAnalysisController {

    private final SensorAnalysisService sensorAnalysisService;

    @Operation(summary = "습윤 사이클 패턴 감지",
            description = "최근 1시간 습도 시계열에서 급상승-고습 유지-하강 흐름을 감지. 분무 여부는 확정하지 않음")
    @GetMapping("/humidities/cycles")
    public ResponseEntity<ApiResponse<HumidityCycleResponse>> getHumidityCycle(
            @AuthenticationPrincipal Long currentUserId,
            @PathVariable Long enclosureId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sensorAnalysisService.getHumidityCycle(currentUserId, enclosureId)));
    }

    @Operation(summary = "환경 이상상황 감지",
            description = "최근 측정값에서 지속적인 범위 이탈을 감지. 1회성 이상값은 제외하고, 값 튐이 반복되면 센서 점검 안내로 분리")
    @GetMapping("/environmental-alerts")
    public ResponseEntity<ApiResponse<EnvironmentalAlertResponse>> getEnvironmentalAlert(
            @AuthenticationPrincipal Long currentUserId,
            @PathVariable Long enclosureId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sensorAnalysisService.getEnvironmentalAlert(currentUserId, enclosureId)));
    }
}
