package com.likelion.backend.domain.enclosure.controller;

import com.likelion.backend.domain.enclosure.dto.EnclosureCreateRequest;
import com.likelion.backend.domain.enclosure.dto.EnclosureCreateResponse;
import com.likelion.backend.domain.enclosure.dto.EnclosureGuidebookRecommendationResponse;
import com.likelion.backend.domain.enclosure.dto.EnclosureHumiditySuggestionResponse;
import com.likelion.backend.domain.enclosure.dto.EnclosureTemperatureSuggestionResponse;
import com.likelion.backend.domain.enclosure.dto.ManagementSettingRequest;
import com.likelion.backend.domain.enclosure.dto.ManagementSettingResponse;
import com.likelion.backend.domain.enclosure.service.EnclosureService;
import com.likelion.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
