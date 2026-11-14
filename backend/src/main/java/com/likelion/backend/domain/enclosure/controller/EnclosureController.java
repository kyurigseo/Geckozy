package com.likelion.backend.domain.enclosure.controller;

import com.likelion.backend.domain.enclosure.dto.crud.EnclosureCreateRequest;
import com.likelion.backend.domain.enclosure.dto.crud.EnclosureCreateResponse;
import com.likelion.backend.domain.enclosure.dto.crud.EnclosureUpdateRequest;
import com.likelion.backend.domain.enclosure.dto.crud.EnclosureUpdateResponse;
import com.likelion.backend.domain.enclosure.dto.environment.*;
import com.likelion.backend.domain.enclosure.dto.suggestion.*;
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
    public ResponseEntity<ApiResponse<Map<String, Long>>> createEnclosure(
            @Valid @RequestBody EnclosureCreateRequest request
    ) {
        EnclosureCreateResponse response = enclosureService.createEnclosure(request);
        Map<String, Long> data = Map.of("enclosureId", response.getEnclosureId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(data));
    }

    @PutMapping("/{enclosureId}/management")
    public ResponseEntity<ApiResponse<ManagementSettingResponse>> updateManagementSetting(
            @PathVariable("enclosureId") Long enclosureId,
            @Valid @RequestBody ManagementSettingRequest request) {

        ManagementSettingResponse response = enclosureService.updateManagementSetting(enclosureId, request);

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
    public ResponseEntity<ApiResponse<EnclosureResponse>> getEnclosure(
            @RequestParam("lizardId") Long lizardId,
            @AuthenticationPrincipal Long currentUserId // Security Context에서 현재 유저 ID 추출 (프로젝트 SecurityDetails 설정에 맞게 변경 가능)
    ) {
        EnclosureResponse response = enclosureService.getEnclosureByLizardId(lizardId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", response));
    }


    @PatchMapping("/{enclosureId}")
    public ResponseEntity<ApiResponse<EnclosureUpdateResponse>> updateEnclosure(
            @PathVariable("enclosureId") Long enclosureId,
            @Valid @RequestBody EnclosureUpdateRequest requestDto, // @Valid로 필수값 유효성 검증
            @AuthenticationPrincipal Long currentUserId
    ) {
        EnclosureUpdateResponse response = enclosureService.updateEnclosure(enclosureId, requestDto, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", response));
    }

    @GetMapping("/{enclosureId}/temperatures/status")
    public ResponseEntity<ApiResponse<TemperatureStatusResponse>> getTemperatureStatus(
            @PathVariable("enclosureId") Long enclosureId,
            @AuthenticationPrincipal Long currentUserId
    ) {
        TemperatureStatusResponse response = enclosureService.getTemperatureStatus(enclosureId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", response));
    }

    @GetMapping("/{enclosureId}/humidities/status")
    public ResponseEntity<ApiResponse<HumidityStatusResponse>> getHumidityStatus(
            @PathVariable("enclosureId") Long enclosureId,
            @AuthenticationPrincipal Long currentUserId
    ) {
        HumidityStatusResponse response = enclosureService.getHumidityStatus(enclosureId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", response));
    }

    @GetMapping("/{enclosureId}/alerts")
    public ResponseEntity<ApiResponse<AlertResponse>> getEnclosureAlerts(
            @PathVariable("enclosureId") Long enclosureId,
            @AuthenticationPrincipal Long currentUserId
    ) {
        AlertResponse response = enclosureService.getEnclosureAlerts(enclosureId, currentUserId);

        // 경고가 없으면 "NO_ALERT" 메세지 반환
        String message = (response.getPrimaryAlert() == null) ? "NO_ALERT" : "SUCCESS";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/{enclosureId}/temperatures/history")
    public ResponseEntity<ApiResponse<TemperatureHistoryResponse>> getTemperatureHistory(
            @PathVariable("enclosureId") Long enclosureId,
            @RequestParam(value = "date", required = false) String date,
            @AuthenticationPrincipal Long currentUserId
    ) {
        TemperatureHistoryResponse response = enclosureService.getTemperatureHistory(enclosureId, date, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", response));
    }

    @GetMapping("/{enclosureId}/humidities/history")
    public ResponseEntity<ApiResponse<HumidityHistoryResponse>> getHumidityHistory(
            @PathVariable("enclosureId") Long enclosureId,
            @RequestParam(value = "date", required = false) String date,
            @AuthenticationPrincipal Long currentUserId
    ) {
        HumidityHistoryResponse response = enclosureService.getHumidityHistory(enclosureId, date, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", response));
    }

    @GetMapping("/{enclosureId}/environment/summary")
    public ResponseEntity<ApiResponse<EnvironmentSummaryResponse>> getEnvironmentSummary(
            @PathVariable("enclosureId") Long enclosureId,
            @RequestParam(value = "date", required = false) String date,
            @AuthenticationPrincipal Long currentUserId
    ) {
        EnvironmentSummaryResponse response = enclosureService.getEnvironmentSummary(enclosureId, date, currentUserId);

        // 데이터가 부족하면 INSUFFICIENT_DATA 메시지 전달
        String message = (response.getSummary() == null) ? "INSUFFICIENT_DATA" : "SUCCESS";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/{enclosureId}/temperatures/patterns")
    public ApiResponse<TemperaturePatternResponse> getTemperaturePattern(
            @PathVariable("enclosureId") Long enclosureId
    ) {
        TemperaturePatternResponse response = enclosureService.getTemperaturePattern(enclosureId);
        return ApiResponse.success(response);
    }
}