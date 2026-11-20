package com.likelion.backend.domain.preadoptionguide;

import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideResponse;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuide;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideRepository;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideService;
import com.likelion.backend.domain.species.entity.Species;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreAdoptionGuideServiceTest {

    @Mock
    private SpeciesRepository speciesRepository;

    @Mock
    private PreAdoptionGuideRepository preAdoptionGuideRepository;

    @InjectMocks
    private PreAdoptionGuideService preAdoptionGuideService;

    @Test
    @DisplayName("1. 해당 종 사전 입양 가이드 조회 성공")
    void getGuideBySpecies_Success() {
        PreAdoptionGuide guide = PreAdoptionGuide.builder()
                .species(new Species("크레스티드 게코"))
                .title("크레스티드 게코 입양 준비")
                .description("입양 전 사육 환경을 준비해요.")
                .build();
        ReflectionTestUtils.setField(guide, "preGuideId", 1L);

        when(speciesRepository.existsById(1L)).thenReturn(true);
        when(preAdoptionGuideRepository.findBySpecies_SpeciesId(1L)).thenReturn(Optional.of(guide));

        PreAdoptionGuideResponse response = preAdoptionGuideService.getGuideBySpecies(1L);

        assertThat(response.getPreGuideId()).isEqualTo(1L);
        assertThat(response.getSpeciesId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("크레스티드 게코 입양 준비");
        assertThat(response.getDescription()).isEqualTo("입양 전 사육 환경을 준비해요.");
    }

    @Test
    @DisplayName("2. 존재하지 않는 종이면 SPECIES_NOT_FOUND")
    void getGuideBySpecies_SpeciesNotFound() {
        when(speciesRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> preAdoptionGuideService.getGuideBySpecies(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SPECIES_NOT_FOUND);

        verify(preAdoptionGuideRepository, never()).findBySpecies_SpeciesId(any());
    }

    @Test
    @DisplayName("3. 종은 있지만 가이드가 없으면 PRE_ADOPTION_GUIDE_NOT_FOUND")
    void getGuideBySpecies_GuideNotFound() {
        when(speciesRepository.existsById(2L)).thenReturn(true);
        when(preAdoptionGuideRepository.findBySpecies_SpeciesId(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> preAdoptionGuideService.getGuideBySpecies(2L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);
    }
}
