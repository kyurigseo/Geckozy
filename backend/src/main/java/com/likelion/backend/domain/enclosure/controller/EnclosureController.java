package com.likelion.backend.domain.enclosure.controller;

import com.likelion.backend.domain.enclosure.dto.*;
import com.likelion.backend.domain.enclosure.service.EnclosureService;
import com.likelion.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/enclosures")
@RequiredArgsConstructor
public class EnclosureController {

    private final EnclosureService enclosureService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> createEnclosure(@RequestBody EnclosureCreateRequest request) {
        EnclosureCreateResponse response = enclosureService.createEnclosure(request);
        Map<String, Long> data = Map.of("enclosureId", response.getEnclosureId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(data));
    }

    @PutMapping("/{enclosureId}/management")
    public ResponseEntity<ApiResponse<ManagementSettingResponse>> updateManagementSetting(
            @PathVariable("enclosureId") Long enclosureId,
            @RequestBody ManagementSettingRequest request) {

        ManagementSettingResponse response = enclosureService.updateManagementSetting(enclosureId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{enclosureId}/temperatures/status")
    public ResponseEntity<ApiResponse<TemperatureStatusResponse>> getTemperatureStatus(
            @PathVariable("enclosureId") Long enclosureId) {

        TemperatureStatusResponse response = enclosureService.getTemperatureStatus(enclosureId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{enclosureId}/temperatures/suggestions")
    public ResponseEntity<ApiResponse<EnclosureTemperatureSuggestionResponse>> getTemperatureSuggestions(
            @PathVariable("enclosureId") Long enclosureId
    ) {
        EnclosureTemperatureSuggestionResponse response = enclosureService.getTemperatureSuggestions(enclosureId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{enclosureId}/humidities/suggestions")
    public ResponseEntity<ApiResponse<EnclosureHumiditySuggestionResponse>> getHumiditySuggestions(
            @PathVariable("enclosureId") Long enclosureId
    ) {
        EnclosureHumiditySuggestionResponse response = enclosureService.getHumiditySuggestions(enclosureId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{enclosureId}/guidebooks/recommendations")
    public ResponseEntity<ApiResponse<EnclosureGuidebookRecommendationResponse>> getGuidebookRecommendations(
            @PathVariable("enclosureId") Long enclosureId
    ) {
        EnclosureGuidebookRecommendationResponse response = enclosureService.getGuidebookRecommendations(enclosureId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<EnclosureResponseDto>> getEnclosure(
            @RequestParam("lizardId") Long lizardId,
            @AuthenticationPrincipal Long currentUserId // Security Context에서 현재 유저 ID 추출 (프로젝트 SecurityDetails 설정에 맞게 변경 가능)
    ) {
        EnclosureResponseDto response = enclosureService.getEnclosureByLizardId(lizardId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", response));
    }


    @PatchMapping("/{enclosureId}")
    public ResponseEntity<ApiResponse<EnclosureUpdateResponseDto>> updateEnclosure(
            @PathVariable("enclosureId") Long enclosureId,
            @Valid @RequestBody EnclosureUpdateRequestDto requestDto, // @Valid로 필수값 유효성 검증
            @AuthenticationPrincipal Long currentUserId
    ) {
        EnclosureUpdateResponseDto response = enclosureService.updateEnclosure(enclosureId, requestDto, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", response));
    }

    @GetMapping("/{enclosureId}/temperatures/status")
    public ResponseEntity<ApiResponse<TemperatureStatusResponseDto>> getTemperatureStatus(
            @PathVariable("enclosureId") Long enclosureId,
            @AuthenticationPrincipal Long currentUserId
    ) {
        TemperatureStatusResponseDto response = enclosureService.getTemperatureStatus(enclosureId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", response));
    }

    @GetMapping("/{enclosureId}/humidities/status")
    public ResponseEntity<ApiResponse<HumidityStatusResponseDto>> getHumidityStatus(
            @PathVariable("enclosureId") Long enclosureId,
            @AuthenticationPrincipal Long currentUserId
    ) {
        HumidityStatusResponseDto response = enclosureService.getHumidityStatus(enclosureId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", response));
    }

    @GetMapping("/{enclosureId}/alerts")
    public ResponseEntity<ApiResponse<AlertResponseDto>> getEnclosureAlerts(
            @PathVariable("enclosureId") Long enclosureId,
            @AuthenticationPrincipal Long currentUserId
    ) {
        AlertResponseDto response = enclosureService.getEnclosureAlerts(enclosureId, currentUserId);

        // 경고가 없으면 "NO_ALERT" 메세지 반환
        String message = (response.getPrimaryAlert() == null) ? "NO_ALERT" : "SUCCESS";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/{enclosureId}/temperatures/history")
    public ResponseEntity<ApiResponse<TemperatureHistoryResponseDto>> getTemperatureHistory(
            @PathVariable("enclosureId") Long enclosureId,
            @RequestParam(value = "date", required = false) String date,
            @AuthenticationPrincipal Long currentUserId
    ) {
        TemperatureHistoryResponseDto response = enclosureService.getTemperatureHistory(enclosureId, date, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", response));
    }

    @GetMapping("/{enclosureId}/humidities/history")
    public ResponseEntity<ApiResponse<HumidityHistoryResponseDto>> getHumidityHistory(
            @PathVariable("enclosureId") Long enclosureId,
            @RequestParam(value = "date", required = false) String date,
            @AuthenticationPrincipal Long currentUserId
    ) {
        HumidityHistoryResponseDto response = enclosureService.getHumidityHistory(enclosureId, date, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", response));
    }

    @GetMapping("/{enclosureId}/environment/summary")
    public ResponseEntity<ApiResponse<EnvironmentSummaryResponseDto>> getEnvironmentSummary(
            @PathVariable("enclosureId") Long enclosureId,
            @RequestParam(value = "date", required = false) String date,
            @AuthenticationPrincipal Long currentUserId
    ) {
        EnvironmentSummaryResponseDto response = enclosureService.getEnvironmentSummary(enclosureId, date, currentUserId);

        // 데이터가 부족하면 INSUFFICIENT_DATA 메시지 전달
        String message = (response.getSummary() == null) ? "INSUFFICIENT_DATA" : "SUCCESS";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}