package com.likelion.backend.domain.species;

import com.likelion.backend.domain.species.dto.SpeciesListResponse;
import com.likelion.backend.domain.species.dto.SpeciesHumidityCycleGuideResponse;
import com.likelion.backend.domain.species.dto.SpeciesTemperatureStandardResponse;
import com.likelion.backend.domain.species.entity.Species;
import com.likelion.backend.domain.species.entity.SpeciesEnvironmentStandard;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.domain.species.repository.SpeciesEnvironmentStandardRepository;
import com.likelion.backend.domain.species.service.SpeciesService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpeciesServiceTest {

    @Mock
    private SpeciesRepository speciesRepository;

    @Mock
    private SpeciesEnvironmentStandardRepository
            speciesEnvironmentStandardRepository;

    @InjectMocks
    private SpeciesService speciesService;

    // 테스트용 종별 환경 기준 생성
    private SpeciesEnvironmentStandard createEnvironmentStandard() {

        Species species = new Species("크레스티드 게코");

        return SpeciesEnvironmentStandard.builder()
                .species(species)
                .dayTemperatureMin(new BigDecimal("22.0"))
                .dayTemperatureMax(new BigDecimal("26.0"))
                .nightTemperatureMin(new BigDecimal("20.0"))
                .nightTemperatureMax(new BigDecimal("24.0"))
                .temperatureCaution("28°C 이상의 고온에 주의하세요.")
                .humidityMin(new BigDecimal("50.0"))
                .humidityMax(new BigDecimal("80.0"))
                .humidityCaution("과도한 습도에 주의하세요.")
                .build();
    }

    @Test
    @DisplayName("1. 도마뱀 종 목록 조회 성공")
    void getSpeciesList_Success() {

        Species species1 = mock(Species.class);
        Species species2 = mock(Species.class);

        when(species1.getSpeciesId()).thenReturn(1L);
        when(species1.getName()).thenReturn("크레스티드 게코");

        when(species2.getSpeciesId()).thenReturn(2L);
        when(species2.getName()).thenReturn("레오파드 게코");

        when(speciesRepository.findAll(
                Sort.by(Sort.Direction.ASC, "speciesId")
        )).thenReturn(List.of(species1, species2));

        List<SpeciesListResponse> response =
                speciesService.getSpeciesList();

        assertThat(response).hasSize(2);

        assertThat(response.get(0).getSpeciesId()).isEqualTo(1L);
        assertThat(response.get(0).getName()).isEqualTo("크레스티드 게코");

        assertThat(response.get(1).getSpeciesId()).isEqualTo(2L);
        assertThat(response.get(1).getName()).isEqualTo("레오파드 게코");

        verify(speciesRepository).findAll(
                Sort.by(Sort.Direction.ASC, "speciesId")
        );
    }

    @Test
    @DisplayName("2. 종별 온도 기준 조회 - 등록된 종")
    void getTemperatureStandards_RegisteredSpecies() {

        Long speciesId = 1L;

        SpeciesEnvironmentStandard standard =
                createEnvironmentStandard();

        when(speciesEnvironmentStandardRepository
                .findBySpecies_SpeciesId(speciesId))
                .thenReturn(Optional.of(standard));

        SpeciesTemperatureStandardResponse response =
                speciesService.getTemperatureStandards(speciesId);

        assertThat(response).isNotNull();
        assertThat(response.getSpeciesId()).isEqualTo(speciesId);
        assertThat(response.getSpeciesName()).isEqualTo("크레스티드 게코");
        assertThat(response.getIsRegistered()).isTrue();

        assertThat(response.getDayTemperatureMin()).isEqualTo(22.0);
        assertThat(response.getDayTemperatureMax()).isEqualTo(26.0);
        assertThat(response.getNightTemperatureMin()).isEqualTo(20.0);
        assertThat(response.getNightTemperatureMax()).isEqualTo(24.0);

        assertThat(response.getCautionText())
                .isEqualTo("28°C 이상의 고온에 주의하세요.");

        assertThat(response.getDisplayMessage()).isNull();

        verify(speciesEnvironmentStandardRepository)
                .findBySpecies_SpeciesId(speciesId);
    }

    @Test
    @DisplayName("3. 종별 온도 기준 조회 - 환경 기준 미등록")
    void getTemperatureStandards_UnregisteredSpecies() {

        Long speciesId = 999L;

        when(speciesEnvironmentStandardRepository
                .findBySpecies_SpeciesId(speciesId))
                .thenReturn(Optional.empty());

        SpeciesTemperatureStandardResponse response =
                speciesService.getTemperatureStandards(speciesId);

        assertThat(response).isNotNull();
        assertThat(response.getSpeciesId()).isEqualTo(speciesId);
        assertThat(response.getSpeciesName()).isNull();
        assertThat(response.getIsRegistered()).isFalse();

        assertThat(response.getDayTemperatureMin()).isNull();
        assertThat(response.getDayTemperatureMax()).isNull();
        assertThat(response.getNightTemperatureMin()).isNull();
        assertThat(response.getNightTemperatureMax()).isNull();

        assertThat(response.getCautionText()).isNull();
        assertThat(response.getDisplayMessage()).isNotBlank();

        verify(speciesEnvironmentStandardRepository)
                .findBySpecies_SpeciesId(speciesId);
    }

    @Test
    @DisplayName("4. 습윤 사이클 정보 조회 - 가이드 보유 종")
    void getHumidityCycleGuides_RegisteredSpecies() {

        Long speciesId = 1L;

        SpeciesEnvironmentStandard standard =
                createEnvironmentStandard();

        when(speciesEnvironmentStandardRepository
                .findBySpecies_SpeciesId(speciesId))
                .thenReturn(Optional.of(standard));

        SpeciesHumidityCycleGuideResponse response =
                speciesService.getHumidityCycleGuides(speciesId);

        assertThat(response).isNotNull();
        assertThat(response.getSpeciesId()).isEqualTo(speciesId);
        assertThat(response.getSpeciesName()).isEqualTo("크레스티드 게코");
        assertThat(response.getHasGuideContent()).isTrue();

        assertThat(response.getHumidityRange()).isNotNull();
        assertThat(response.getHumidityRange().getMin()).isEqualTo(50);
        assertThat(response.getHumidityRange().getMax()).isEqualTo(80);

        assertThat(response.getCycleSteps()).hasSize(4);
        assertThat(response.getDisplayMessage()).isNull();

        verify(speciesEnvironmentStandardRepository)
                .findBySpecies_SpeciesId(speciesId);
    }

    @Test
    @DisplayName("5. 습윤 사이클 정보 조회 - 환경 기준 미등록")
    void getHumidityCycleGuides_UnregisteredSpecies() {

        Long speciesId = 999L;

        when(speciesEnvironmentStandardRepository
                .findBySpecies_SpeciesId(speciesId))
                .thenReturn(Optional.empty());

        SpeciesHumidityCycleGuideResponse response =
                speciesService.getHumidityCycleGuides(speciesId);

        assertThat(response).isNotNull();
        assertThat(response.getSpeciesId()).isEqualTo(speciesId);
        assertThat(response.getSpeciesName()).isNull();
        assertThat(response.getHasGuideContent()).isFalse();

        assertThat(response.getHumidityRange()).isNull();
        assertThat(response.getCycleSteps()).isNotNull();
        assertThat(response.getCycleSteps()).isEmpty();
        assertThat(response.getDisplayMessage()).isNotBlank();

        verify(speciesEnvironmentStandardRepository)
                .findBySpecies_SpeciesId(speciesId);
    }
}