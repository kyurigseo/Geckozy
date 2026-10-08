package com.likelion.backend.domain.lizard.controller;

import com.likelion.backend.domain.lizard.dto.request.LizardCreateRequest;
import com.likelion.backend.domain.lizard.dto.request.LizardUpdateRequest;
import com.likelion.backend.domain.lizard.dto.response.LizardCreateResponse;
import com.likelion.backend.domain.lizard.dto.response.LizardListResponse;
import com.likelion.backend.domain.lizard.dto.response.LizardResponse;
import com.likelion.backend.global.response.ApiResponse;
import com.likelion.backend.domain.lizard.service.LizardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lizards")
@RequiredArgsConstructor
public class LizardController {

    private final LizardService lizardService;

    @PostMapping
    public ResponseEntity<ApiResponse<LizardCreateResponse>> createLizard(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody LizardCreateRequest request
    ) {
        Long lizardId = lizardService.createLizard(userId, request);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.created(
                        new LizardCreateResponse(lizardId)
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LizardListResponse>>> getLizards(
            @AuthenticationPrincipal Long userId
    ) {
        List<LizardListResponse> response =
                lizardService.getLizards(userId);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @GetMapping("/{lizardId}")
    public ResponseEntity<ApiResponse<LizardResponse>> getLizard(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId
    ) {
        LizardResponse response =
                lizardService.getLizard(userId, lizardId);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @PatchMapping("/{lizardId}")
    public ResponseEntity<ApiResponse<Void>> updateLizard(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId,
            @Valid @RequestBody LizardUpdateRequest request
    ) {
        lizardService.updateLizard(userId, lizardId, request);

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }

    @DeleteMapping("/{lizardId}")
    public ResponseEntity<ApiResponse<Void>> deleteLizard(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId
    ) {
        lizardService.deleteLizard(userId, lizardId);

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }
}