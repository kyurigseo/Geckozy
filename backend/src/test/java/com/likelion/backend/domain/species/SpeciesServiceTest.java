package com.likelion.backend.domain.species;

import com.likelion.backend.domain.species.dto.SpeciesHumidityCycleGuideResponse;
import com.likelion.backend.domain.species.dto.SpeciesTemperatureStandardResponse;
import com.likelion.backend.domain.species.service.SpeciesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpeciesServiceTest {

    private SpeciesService speciesService;

    @BeforeEach
    void setUp() {
        speciesService = new SpeciesService();
    }

    @Test
    @DisplayName("1. 종별 온도 기준 조회 - 등록된 종인 경우 정상 온도 및 주의사항 반환")
    void getTemperatureStandards_RegisteredSpecies() {
        Long registeredSpeciesId = 1L;

        SpeciesTemperatureStandardResponse response = speciesService.getTemperatureStandards(registeredSpeciesId);

        assertThat(response).isNotNull();
        assertThat(response.getSpeciesId()).isEqualTo(registeredSpeciesId);
        assertThat(response.getSpeciesName()).isEqualTo("크레스티드 게코");
        assertThat(response.getIsRegistered()).isTrue();
        assertThat(response.getDayTemperatureMin()).isEqualTo(22.0);
        assertThat(response.getDayTemperatureMax()).isEqualTo(26.0);
        assertThat(response.getNightTemperatureMin()).isEqualTo(20.0);
        assertThat(response.getNightTemperatureMax()).isEqualTo(24.0);
        assertThat(response.getCautionText()).isNotBlank();
        assertThat(response.getDisplayMessage()).isNull();
    }

    @Test
    @DisplayName("1. 종별 온도 기준 조회 - 미등록 종인 경우 온도 null 및 안내 문구 반환")
    void getTemperatureStandards_UnregisteredSpecies() {
        Long unregisteredSpeciesId = 999L;

        SpeciesTemperatureStandardResponse response = speciesService.getTemperatureStandards(unregisteredSpeciesId);

        assertThat(response).isNotNull();
        assertThat(response.getSpeciesId()).isEqualTo(unregisteredSpeciesId);
        assertThat(response.getSpeciesName()).isNull();
        assertThat(response.getIsRegistered()).isFalse();
        assertThat(response.getDayTemperatureMin()).isNull();
        assertThat(response.getDayTemperatureMax()).isNull();
        assertThat(response.getNightTemperatureMin()).isNull();
        assertThat(response.getNightTemperatureMax()).isNull();
        assertThat(response.getCautionText()).isNull();
        assertThat(response.getDisplayMessage()).isNotBlank();
    }

    @Test
    @DisplayName("2. 습윤 사이클 정보 조회 - 등록된 종인 경우 습도 범위 및 사이클 단계 반환")
    void getHumidityCycleGuides_RegisteredSpecies() {
        Long registeredSpeciesId = 1L;

        SpeciesHumidityCycleGuideResponse response = speciesService.getHumidityCycleGuides(registeredSpeciesId);

        assertThat(response).isNotNull();
        assertThat(response.getSpeciesId()).isEqualTo(registeredSpeciesId);
        assertThat(response.getSpeciesName()).isEqualTo("크레스티드 게코");
        assertThat(response.getHasGuideContent()).isTrue();
        assertThat(response.getHumidityRange()).isNotNull();
        assertThat(response.getHumidityRange().getMin()).isEqualTo(50);
        assertThat(response.getHumidityRange().getMax()).isEqualTo(80);
        assertThat(response.getCycleSteps()).isNotEmpty();
        assertThat(response.getCycleSteps()).hasSize(4);
        assertThat(response.getDisplayMessage()).isNull();
    }

    @Test
    @DisplayName("2. 습윤 사이클 정보 조회 - 미등록 종인 경우 빈 배열 및 안내 문구 반환")
    void getHumidityCycleGuides_UnregisteredSpecies() {
        Long unregisteredSpeciesId = 999L;

        SpeciesHumidityCycleGuideResponse response = speciesService.getHumidityCycleGuides(unregisteredSpeciesId);

        assertThat(response).isNotNull();
        assertThat(response.getSpeciesId()).isEqualTo(unregisteredSpeciesId);
        assertThat(response.getSpeciesName()).isNull();
        assertThat(response.getHasGuideContent()).isFalse();
        assertThat(response.getHumidityRange()).isNull();
        assertThat(response.getCycleSteps()).isNotNull();
        assertThat(response.getCycleSteps()).isEmpty();
        assertThat(response.getDisplayMessage()).isNotBlank();
    }
}
