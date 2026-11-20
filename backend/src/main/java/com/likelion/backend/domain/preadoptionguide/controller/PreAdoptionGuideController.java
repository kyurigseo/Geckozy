package com.likelion.backend.domain.preadoptionguide.controller;

import com.likelion.backend.domain.preadoptionguide.dto.request.PreAdoptionGuideCheckSaveRequest;
import com.likelion.backend.domain.preadoptionguide.dto.request.PreAdoptionGuideStepCompleteRequest;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepCompleteResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStartResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStatusResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentSummaryResponse;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionEnvironmentCheckService;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideCheckSaveResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideCompleteResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideProgressResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideService;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideStepService;
import com.likelion.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PreAdoptionGuideController {

    private final PreAdoptionGuideService preAdoptionGuideService;
    private final PreAdoptionGuideStepService preAdoptionGuideStepService;
    private final PreAdoptionEnvironmentCheckService preAdoptionEnvironmentCheckService;

    // 해당 종 사전 입양 가이드 조회
    @GetMapping("/species/{speciesId}/pre-adoption-guide")
    public ResponseEntity<ApiResponse<PreAdoptionGuideResponse>> getGuideBySpecies(
            @PathVariable("speciesId") @Positive Long speciesId
    ) {
        PreAdoptionGuideResponse response = preAdoptionGuideService.getGuideBySpecies(speciesId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 사전 입양 가이드 진행 상태 조회
    @GetMapping("/pre-adoption-guides/{preGuideId}/progress")
    public ResponseEntity<ApiResponse<PreAdoptionGuideProgressResponse>> getProgress(
            @AuthenticationPrincipal Long userId,
            @PathVariable("preGuideId") @Positive Long preGuideId
    ) {
        PreAdoptionGuideProgressResponse response = preAdoptionGuideService.getProgress(userId, preGuideId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 단계 완료 및 다음 단계 이동
    @PatchMapping("/pre-adoption-guides/{preGuideId}/progress")
    public ResponseEntity<ApiResponse<PreAdoptionGuideStepCompleteResponse>> completeStep(
            @AuthenticationPrincipal Long userId,
            @PathVariable("preGuideId") @Positive Long preGuideId,
            @Valid @RequestBody PreAdoptionGuideStepCompleteRequest request
    ) {
        PreAdoptionGuideStepCompleteResponse response =
                preAdoptionGuideService.completeStep(userId, preGuideId, request.completedStep());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 7일 환경 점검 시작
    @PostMapping("/pre-adoption-guides/{preGuideId}/environment-check")
    public ResponseEntity<ApiResponse<EnvironmentCheckStartResponse>> startEnvironmentCheck(
            @AuthenticationPrincipal Long userId,
            @PathVariable("preGuideId") @Positive Long preGuideId
    ) {
        EnvironmentCheckStartResponse response = preAdoptionGuideService.startEnvironmentCheck(userId, preGuideId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    // 환경 점검 진행 상태 조회
    @GetMapping("/pre-adoption-guides/{preGuideId}/environment-check")
    public ResponseEntity<ApiResponse<EnvironmentCheckStatusResponse>> getEnvironmentCheckStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable("preGuideId") @Positive Long preGuideId
    ) {
        EnvironmentCheckStatusResponse response = preAdoptionEnvironmentCheckService.getStatus(userId, preGuideId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 환경 데이터 중간 요약 조회
    @GetMapping("/pre-adoption-guides/{preGuideId}/environment-check/summary")
    public ResponseEntity<ApiResponse<EnvironmentSummaryResponse>> getEnvironmentSummary(
            @AuthenticationPrincipal Long userId,
            @PathVariable("preGuideId") @Positive Long preGuideId
    ) {
        EnvironmentSummaryResponse response = preAdoptionEnvironmentCheckService.getSummary(userId, preGuideId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 입양 전 가이드 완료
    @PostMapping("/pre-adoption-guides/{preGuideId}/complete")
    public ResponseEntity<ApiResponse<PreAdoptionGuideCompleteResponse>> completeGuide(
            @AuthenticationPrincipal Long userId,
            @PathVariable("preGuideId") @Positive Long preGuideId
    ) {
        PreAdoptionGuideCompleteResponse response = preAdoptionGuideService.completeGuide(userId, preGuideId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 단계별 사전 입양 가이드 조회
    @GetMapping("/pre-adoption-guides/{preGuideId}/steps/{stepNumber}")
    public ResponseEntity<ApiResponse<PreAdoptionGuideStepResponse>> getStep(
            @AuthenticationPrincipal Long userId,
            @PathVariable("preGuideId") @Positive Long preGuideId,
            @PathVariable("stepNumber") @Min(1) @Max(5) Integer stepNumber
    ) {
        PreAdoptionGuideStepResponse response = preAdoptionGuideStepService.getStep(userId, preGuideId, stepNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 단계별 체크리스트 상태 저장
    @PutMapping("/pre-adoption-guides/{preGuideId}/checks/{stepNumber}")
    public ResponseEntity<ApiResponse<PreAdoptionGuideCheckSaveResponse>> saveChecks(
            @AuthenticationPrincipal Long userId,
            @PathVariable("preGuideId") @Positive Long preGuideId,
            @PathVariable("stepNumber") @Min(1) @Max(5) Integer stepNumber,
            @Valid @RequestBody PreAdoptionGuideCheckSaveRequest request
    ) {
        PreAdoptionGuideCheckSaveResponse response =
                preAdoptionGuideStepService.saveChecks(userId, preGuideId, stepNumber, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
