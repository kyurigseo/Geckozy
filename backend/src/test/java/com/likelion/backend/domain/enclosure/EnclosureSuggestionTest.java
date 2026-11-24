package com.likelion.backend.domain.enclosure;

import com.likelion.backend.domain.enclosure.dto.suggestion.EnclosureGuidebookRecommendationResponse;
import com.likelion.backend.domain.enclosure.dto.suggestion.EnclosureHumiditySuggestionResponse;
import com.likelion.backend.domain.enclosure.dto.suggestion.EnclosureTemperatureSuggestionResponse;
import com.likelion.backend.domain.enclosure.service.EnclosureRecommendationService;
import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.enclosure.service.EnclosureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnclosureSuggestionTest {

    private static final Long USER_ID = 1L;

    private EnclosureService enclosureService;

    @BeforeEach
    void setUp() {
        // 소유자 확인을 통과하도록 사육장 → 도마뱀 → 유저 ID를 스텁
        Enclosure enclosure = mock(Enclosure.class, RETURNS_DEEP_STUBS);
        when(enclosure.getLizard().getUser().getUserId()).thenReturn(USER_ID);

        EnclosureRepository enclosureRepository = mock(EnclosureRepository.class);
        when(enclosureRepository.findById(anyLong())).thenReturn(Optional.of(enclosure));

        enclosureService = new EnclosureService(
                null,
                new EnclosureRecommendationService(enclosureRepository),
                null,
                null
        );
    }
    @Test
    @DisplayName("3. 상황별 온도 관리 제안 - 행동 지침 및 센서 위치 면책 문구 반환 검증")
    void getTemperatureSuggestions_Success() {
        Long enclosureId = 1L;

        EnclosureTemperatureSuggestionResponse response = enclosureService.getTemperatureSuggestions(enclosureId, USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getEnclosureId()).isEqualTo(enclosureId);
        assertThat(response.getPrimaryAction()).isEqualTo("CHECK_SENSOR_LOCATION_AND_VENTILATE");
        assertThat(response.getActionTitle()).isNotBlank();
        assertThat(response.getActionGuide()).isNotBlank();
        assertThat(response.getDisclaimer()).isNotBlank();
    }

    @Test
    @DisplayName("4. 상황별 습도 관리 제안 - 수동 점검 유도 및 자동 분무 비권장 반환 검증")
    void getHumiditySuggestions_Success() {
        Long enclosureId = 1L;

        EnclosureHumiditySuggestionResponse response = enclosureService.getHumiditySuggestions(enclosureId, USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getEnclosureId()).isEqualTo(enclosureId);
        assertThat(response.getPrimaryAction()).isEqualTo("CHECK_SUBSTRATE_MOISTURE_BEFORE_SPRAYING");
        assertThat(response.getActionTitle()).isNotBlank();
        assertThat(response.getActionGuide()).isNotBlank();
        assertThat(response.getIsAutomatedSprayingRecommended()).isFalse();
    }

    @Test
    @DisplayName("5. 관련 관리 도감 추천 - 환경 상태 매칭 도감 목록 반환 검증")
    void getGuidebookRecommendations_Matched() {
        Long enclosureId = 1L;

        EnclosureGuidebookRecommendationResponse response = enclosureService.getGuidebookRecommendations(enclosureId, USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getRecommendations()).isNotEmpty();
        assertThat(response.getRecommendations()).allSatisfy(guide -> {
            assertThat(guide.getGuidebookId()).isNotNull();
            assertThat(guide.getTitle()).isNotBlank();
            assertThat(guide.getKeyword()).isNotBlank();
            assertThat(guide.getReason()).isNotBlank();
        });
    }

    @Test
    @DisplayName("5. 관련 관리 도감 추천 - 매칭 도감 없을 시 빈 배열 반환 검증")
    void getGuidebookRecommendations_NoMatch_ReturnsEmptyList() {
        Long enclosureId = 999L;

        EnclosureGuidebookRecommendationResponse response = enclosureService.getGuidebookRecommendations(enclosureId, USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getRecommendations()).isNotNull();
        assertThat(response.getRecommendations()).isEmpty();
    }
}
