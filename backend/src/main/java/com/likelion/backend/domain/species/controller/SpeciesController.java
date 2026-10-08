package com.likelion.backend.domain.species.controller;

import com.likelion.backend.domain.species.dto.SpeciesHumidityCycleGuideResponse;
import com.likelion.backend.domain.species.dto.SpeciesTemperatureStandardResponse;
import com.likelion.backend.domain.species.service.SpeciesService;
import com.likelion.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/species")
@RequiredArgsConstructor
public class SpeciesController {

    private final SpeciesService speciesService;

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
