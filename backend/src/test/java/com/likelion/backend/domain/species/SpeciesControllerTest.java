package com.likelion.backend.domain.species;

import com.likelion.backend.domain.species.controller.SpeciesController;
import com.likelion.backend.domain.species.dto.SpeciesListResponse;
import com.likelion.backend.domain.species.dto.SpeciesHumidityCycleGuideResponse;
import com.likelion.backend.domain.species.dto.SpeciesTemperatureStandardResponse;
import com.likelion.backend.domain.species.service.SpeciesService;
import com.likelion.backend.global.response.ApiResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpeciesControllerTest {

    @Mock
    private SpeciesService speciesService;

    @InjectMocks
    private SpeciesController speciesController;

    @Test
    @DisplayName("1. 도마뱀 종 목록 조회 응답 검증")
    void getSpeciesList_ReturnsSuccessResponse() {

        List<SpeciesListResponse> speciesList = List.of(
                new SpeciesListResponse(1L, "크레스티드 게코"),
                new SpeciesListResponse(2L, "레오파드 게코")
        );

        when(speciesService.getSpeciesList())
                .thenReturn(speciesList);

        ResponseEntity<ApiResponse<List<SpeciesListResponse>>> response =
                speciesController.getSpeciesList();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData()).hasSize(2);

        assertThat(response.getBody().getData().get(0).getSpeciesId())
                .isEqualTo(1L);

        assertThat(response.getBody().getData().get(0).getName())
                .isEqualTo("크레스티드 게코");

        verify(speciesService).getSpeciesList();
    }

    @Test
    @DisplayName("2. 종별 온도 기준 컨트롤러 응답 포맷 검증")
    void getTemperatureStandards_ReturnsSuccessResponse() {

        SpeciesTemperatureStandardResponse mockResponse =
                SpeciesTemperatureStandardResponse.registered(
                        1L,
                        "크레스티드 게코",
                        22.0,
                        26.0,
                        20.0,
                        24.0,
                        "고온에 주의하세요."
                );

        when(speciesService.getTemperatureStandards(1L))
                .thenReturn(mockResponse);

        ResponseEntity<ApiResponse<SpeciesTemperatureStandardResponse>> response =
                speciesController.getTemperatureStandards(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getIsRegistered()).isTrue();

        verify(speciesService).getTemperatureStandards(1L);
    }

    @Test
    @DisplayName("3. 습윤 사이클 가이드 컨트롤러 응답 포맷 검증")
    void getHumidityCycleGuides_ReturnsSuccessResponse() {

        SpeciesHumidityCycleGuideResponse mockResponse =
                SpeciesHumidityCycleGuideResponse.withGuide(
                        1L,
                        "크레스티드 게코",

                        SpeciesHumidityCycleGuideResponse.HumidityRangeDto.builder()
                                .min(50)
                                .max(80)
                                .build(),

                        List.of(
                                SpeciesHumidityCycleGuideResponse.CycleStepDto.builder()
                                        .step(1)
                                        .title("저녁 분무")
                                        .description("습도를 높입니다.")
                                        .build()
                        )
                );

        when(speciesService.getHumidityCycleGuides(1L))
                .thenReturn(mockResponse);

        ResponseEntity<ApiResponse<SpeciesHumidityCycleGuideResponse>> response =
                speciesController.getHumidityCycleGuides(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getHasGuideContent()).isTrue();

        verify(speciesService).getHumidityCycleGuides(1L);
    }
}