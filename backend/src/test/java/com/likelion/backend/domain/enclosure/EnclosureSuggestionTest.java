package com.likelion.backend.domain.enclosure;

import com.likelion.backend.domain.enclosure.dto.EnclosureGuidebookRecommendationResponse;
import com.likelion.backend.domain.enclosure.dto.EnclosureHumiditySuggestionResponse;
import com.likelion.backend.domain.enclosure.dto.EnclosureTemperatureSuggestionResponse;
import com.likelion.backend.domain.enclosure.service.EnclosureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnclosureSuggestionTest {

    private EnclosureService enclosureService;

    @BeforeEach
    void setUp() {
        enclosureService = new EnclosureService(null, null, null, null, null, null);
    }

    @Test
    @DisplayName("3. 상황별 온도 관리 제안 - 행동 지침 및 센서 위치 면책 문구 반환 검증")
    void getTemperatureSuggestions_Success() {
        Long enclosureId = 1L;

        EnclosureTemperatureSuggestionResponse response = enclosureService.getTemperatureSuggestions(enclosureId);

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

        EnclosureHumiditySuggestionResponse response = enclosureService.getHumiditySuggestions(enclosureId);

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

        EnclosureGuidebookRecommendationResponse response = enclosureService.getGuidebookRecommendations(enclosureId);

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

        EnclosureGuidebookRecommendationResponse response = enclosureService.getGuidebookRecommendations(enclosureId);

        assertThat(response).isNotNull();
        assertThat(response.getRecommendations()).isNotNull();
        assertThat(response.getRecommendations()).isEmpty();
    }
}
