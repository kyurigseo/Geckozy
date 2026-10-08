package com.likelion.backend.domain.species;

import com.likelion.backend.domain.species.controller.SpeciesController;
import com.likelion.backend.domain.species.dto.SpeciesHumidityCycleGuideResponse;
import com.likelion.backend.domain.species.dto.SpeciesTemperatureStandardResponse;
import com.likelion.backend.domain.species.service.SpeciesService;
import com.likelion.backend.global.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class SpeciesControllerTest {

    private SpeciesController speciesController;

    @BeforeEach
    void setUp() {
        SpeciesService speciesService = new SpeciesService();
        speciesController = new SpeciesController(speciesService);
    }

    @Test
    @DisplayName("1. 종별 온도 기준 컨트롤러 응답 포맷 검증")
    void getTemperatureStandards_ReturnsSuccessResponse() {
        ResponseEntity<ApiResponse<SpeciesTemperatureStandardResponse>> response =
                speciesController.getTemperatureStandards(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getIsRegistered()).isTrue();
    }

    @Test
    @DisplayName("2. 습윤 사이클 가이드 컨트롤러 응답 포맷 검증")
    void getHumidityCycleGuides_ReturnsSuccessResponse() {
        ResponseEntity<ApiResponse<SpeciesHumidityCycleGuideResponse>> response =
                speciesController.getHumidityCycleGuides(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getHasGuideContent()).isTrue();
    }
}
