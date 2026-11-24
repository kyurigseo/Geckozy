package com.likelion.backend.domain.preadoptionguide;

import com.likelion.backend.domain.preadoptionguide.dto.request.PreAdoptionGuideCheckSaveRequest;
import com.likelion.backend.domain.preadoptionguide.dto.request.PreAdoptionGuideCheckSaveRequest.CheckDto;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideCheckSaveResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse.CheckItemDto;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideUserCheckRepository;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideService;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideStepService;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 서버 시작 시 들어가는 크레스티드 게코 가이드 콘텐츠를 기준으로 단계별 체크리스트 상태 저장을 검증한다.
 */
@SpringBootTest
@Transactional
class PreAdoptionGuideCheckSaveTest {

    @Autowired
    private PreAdoptionGuideStepService stepService;

    @Autowired
    private PreAdoptionGuideService guideService;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PreAdoptionGuideUserCheckRepository userCheckRepository;

    private User user;
    private Long preGuideId;
    private List<Long> step2ItemIds;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("check-tester", "check-tester@example.com", "password"));
        Long crestedGeckoId = speciesRepository.findByName("크레스티드 게코").orElseThrow().getSpeciesId();
        preGuideId = guideService.getGuideBySpecies(crestedGeckoId).getPreGuideId();
        step2ItemIds = stepService.getStep(user.getUserId(), preGuideId, 2).getCheckItems().stream()
                .map(CheckItemDto::getCheckItemId)
                .toList();
    }

    private PreAdoptionGuideCheckSaveRequest request(CheckDto... checks) {
        return new PreAdoptionGuideCheckSaveRequest(List.of(checks));
    }

    private List<Boolean> step2Checked() {
        return stepService.getStep(user.getUserId(), preGuideId, 2).getCheckItems().stream()
                .map(CheckItemDto::getChecked)
                .toList();
    }

    @Test
    @DisplayName("1. 체크 상태를 저장하면 savedCount를 반환하고 단계 조회에 반영됨")
    void saveChecks_SavesAndReflectsInStep() {
        PreAdoptionGuideCheckSaveResponse response = stepService.saveChecks(user.getUserId(), preGuideId, 2, request(
                new CheckDto(step2ItemIds.get(0), true),
                new CheckDto(step2ItemIds.get(2), true)
        ));

        assertThat(response.getSavedCount()).isEqualTo(2);
        assertThat(step2Checked()).containsExactly(true, false, true, false, false);
    }

    @Test
    @DisplayName("2. 이미 저장된 항목을 다시 저장하면 새로 만들지 않고 값을 덮어씀 (체크 해제 포함)")
    void saveChecks_UpdatesExisting() {
        stepService.saveChecks(user.getUserId(), preGuideId, 2, request(new CheckDto(step2ItemIds.get(0), true)));
        stepService.saveChecks(user.getUserId(), preGuideId, 2, request(new CheckDto(step2ItemIds.get(0), false)));

        assertThat(step2Checked()).containsExactly(false, false, false, false, false);
        assertThat(userCheckRepository
                .findAllByUser_UserIdAndCheckItem_CheckItemIdIn(user.getUserId(), List.of(step2ItemIds.get(0))))
                .hasSize(1);
    }

    @Test
    @DisplayName("3. 요청에 없는 항목의 저장 상태는 바뀌지 않음")
    void saveChecks_KeepsOtherItems() {
        stepService.saveChecks(user.getUserId(), preGuideId, 2, request(new CheckDto(step2ItemIds.get(0), true)));
        stepService.saveChecks(user.getUserId(), preGuideId, 2, request(new CheckDto(step2ItemIds.get(1), true)));

        assertThat(step2Checked()).containsExactly(true, true, false, false, false);
    }

    @Test
    @DisplayName("4. 다른 단계의 체크 항목이면 CHECK_ITEM_NOT_FOUND, 아무것도 저장하지 않음")
    void saveChecks_ItemFromOtherStep() {
        Long step5ItemId = stepService.getStep(user.getUserId(), preGuideId, 5).getCheckItems().get(0).getCheckItemId();

        assertThatThrownBy(() -> stepService.saveChecks(user.getUserId(), preGuideId, 2, request(
                new CheckDto(step2ItemIds.get(0), true),
                new CheckDto(step5ItemId, true)
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CHECK_ITEM_NOT_FOUND);

        assertThat(step2Checked()).containsExactly(false, false, false, false, false);
    }

    @Test
    @DisplayName("5. 존재하지 않는 체크 항목이면 CHECK_ITEM_NOT_FOUND")
    void saveChecks_ItemNotFound() {
        assertThatThrownBy(() -> stepService.saveChecks(user.getUserId(), preGuideId, 2,
                request(new CheckDto(999_999L, true))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CHECK_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("6. 체크 항목이 없는 단계(1단계)에 저장하면 CHECK_ITEM_NOT_FOUND")
    void saveChecks_StepWithoutItems() {
        assertThatThrownBy(() -> stepService.saveChecks(user.getUserId(), preGuideId, 1,
                request(new CheckDto(step2ItemIds.get(0), true))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CHECK_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("7. 같은 항목이 요청에 두 번 있으면 INVALID_REQUEST")
    void saveChecks_DuplicateItem() {
        assertThatThrownBy(() -> stepService.saveChecks(user.getUserId(), preGuideId, 2, request(
                new CheckDto(step2ItemIds.get(0), true),
                new CheckDto(step2ItemIds.get(0), false)
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("8. 존재하지 않는 가이드면 PRE_ADOPTION_GUIDE_NOT_FOUND")
    void saveChecks_GuideNotFound() {
        assertThatThrownBy(() -> stepService.saveChecks(user.getUserId(), 999_999L, 2,
                request(new CheckDto(step2ItemIds.get(0), true))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);
    }

    @Test
    @DisplayName("9. 토큰의 사용자가 없으면 UNAUTHORIZED")
    void saveChecks_UserNotFound() {
        assertThatThrownBy(() -> stepService.saveChecks(999_999L, preGuideId, 2,
                request(new CheckDto(step2ItemIds.get(0), true))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("10. 다른 사용자의 체크 상태에는 영향을 주지 않음")
    void saveChecks_IsolatedPerUser() {
        User other = userRepository.save(new User("check-other", "check-other@example.com", "password"));
        stepService.saveChecks(other.getUserId(), preGuideId, 2, request(new CheckDto(step2ItemIds.get(0), true)));

        assertThat(step2Checked()).containsExactly(false, false, false, false, false);
    }
}
