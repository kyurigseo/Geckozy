package com.likelion.backend.domain.species.controller;

import com.likelion.backend.domain.species.dto.SpeciesHumidityCycleGuideResponse;
import com.likelion.backend.domain.species.dto.SpeciesTemperatureStandardResponse;
import com.likelion.backend.domain.species.service.SpeciesService;
import com.likelion.backend.global.response.ApiResponse;
import com.likelion.backend.domain.species.dto.SpeciesListResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/species")
@RequiredArgsConstructor
public class SpeciesController {

    private final SpeciesService speciesService;

    // 도마뱀 종 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<SpeciesListResponse>>> getSpeciesList() {

        List<SpeciesListResponse> response =
                speciesService.getSpeciesList();

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @GetMapping("/{speciesId}/temperature-standards")
    public ResponseEntity<ApiResponse<SpeciesTemperatureStandardResponse>> getTemperatureStandards(
            @PathVariable("speciesId") Long speciesId
    ) {
        SpeciesTemperatureStandardResponse response = speciesService.getTemperatureStandards(speciesId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{speciesId}/humidity-cycle-guides")
    public ResponseEntity<ApiResponse<SpeciesHumidityCycleGuideResponse>> getHumidityCycleGuides(
            @PathVariable("speciesId") Long speciesId
    ) {
        SpeciesHumidityCycleGuideResponse response = speciesService.getHumidityCycleGuides(speciesId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
