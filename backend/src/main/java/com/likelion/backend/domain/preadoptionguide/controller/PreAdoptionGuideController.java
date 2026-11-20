package com.likelion.backend.domain.preadoptionguide.controller;

import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideProgressResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideResponse;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideService;
import com.likelion.backend.global.response.ApiResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PreAdoptionGuideController {

    private final PreAdoptionGuideService preAdoptionGuideService;

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
}
