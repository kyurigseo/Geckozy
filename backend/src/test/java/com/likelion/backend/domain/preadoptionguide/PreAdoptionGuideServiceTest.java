package com.likelion.backend.domain.preadoptionguide;

import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideProgressResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideResponse;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuide;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideProgressRepository;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideRepository;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideProgressCreator;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideService;
import com.likelion.backend.domain.species.entity.Species;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
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

    @Mock
    private PreAdoptionGuideProgressRepository preAdoptionGuideProgressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PreAdoptionGuideProgressCreator preAdoptionGuideProgressCreator;

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

    private PreAdoptionGuideProgress createProgress(Long progressId, int currentStep, int completedStep) {
        PreAdoptionGuideProgress progress = PreAdoptionGuideProgress.start(mock(User.class), mock(PreAdoptionGuide.class));
        ReflectionTestUtils.setField(progress, "progressId", progressId);
        ReflectionTestUtils.setField(progress, "currentStep", currentStep);
        ReflectionTestUtils.setField(progress, "completedStep", completedStep);
        return progress;
    }

    @Test
    @DisplayName("4. 진행 기록이 없으면 첫 단계로 초기 진행 상태를 생성해 반환")
    void getProgress_CreatesInitialProgress() {
        when(userRepository.existsById(10L)).thenReturn(true);
        when(preAdoptionGuideRepository.existsById(1L)).thenReturn(true);
        when(preAdoptionGuideProgressRepository.findByUser_UserIdAndPreAdoptionGuide_PreGuideId(10L, 1L))
                .thenReturn(Optional.empty());
        when(preAdoptionGuideProgressCreator.create(10L, 1L)).thenReturn(createProgress(1L, 1, 0));

        PreAdoptionGuideProgressResponse response = preAdoptionGuideService.getProgress(10L, 1L);

        assertThat(response.getProgressId()).isEqualTo(1L);
        assertThat(response.getPreGuideId()).isEqualTo(1L);
        assertThat(response.getCurrentStep()).isEqualTo(1);
        assertThat(response.getCompletedStep()).isEqualTo(0);
    }

    @Test
    @DisplayName("5. 진행 기록이 있으면 새로 만들지 않고 기존 진행 상태를 반환")
    void getProgress_ReturnsExistingProgress() {
        when(userRepository.existsById(10L)).thenReturn(true);
        when(preAdoptionGuideRepository.existsById(1L)).thenReturn(true);
        when(preAdoptionGuideProgressRepository.findByUser_UserIdAndPreAdoptionGuide_PreGuideId(10L, 1L))
                .thenReturn(Optional.of(createProgress(3L, 3, 2)));

        PreAdoptionGuideProgressResponse response = preAdoptionGuideService.getProgress(10L, 1L);

        assertThat(response.getProgressId()).isEqualTo(3L);
        assertThat(response.getCurrentStep()).isEqualTo(3);
        assertThat(response.getCompletedStep()).isEqualTo(2);
        verify(preAdoptionGuideProgressCreator, never()).create(any(), any());
    }

    @Test
    @DisplayName("6. 동시에 다른 요청이 먼저 생성했으면(유니크 제약 위반) 그 기록을 다시 읽어 반환")
    void getProgress_ConcurrentCreate_ReturnsExistingProgress() {
        when(userRepository.existsById(10L)).thenReturn(true);
        when(preAdoptionGuideRepository.existsById(1L)).thenReturn(true);
        when(preAdoptionGuideProgressRepository.findByUser_UserIdAndPreAdoptionGuide_PreGuideId(10L, 1L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(createProgress(7L, 1, 0)));
        when(preAdoptionGuideProgressCreator.create(10L, 1L))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        PreAdoptionGuideProgressResponse response = preAdoptionGuideService.getProgress(10L, 1L);

        assertThat(response.getProgressId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("7. 존재하지 않는 가이드면 PRE_ADOPTION_GUIDE_NOT_FOUND")
    void getProgress_GuideNotFound() {
        when(userRepository.existsById(10L)).thenReturn(true);
        when(preAdoptionGuideRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> preAdoptionGuideService.getProgress(10L, 99L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);

        verify(preAdoptionGuideProgressCreator, never()).create(any(), any());
    }

    @Test
    @DisplayName("8. 토큰의 사용자가 없으면 UNAUTHORIZED")
    void getProgress_UserNotFound() {
        when(userRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> preAdoptionGuideService.getProgress(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }
}
