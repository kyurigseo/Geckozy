package com.likelion.backend.domain.preadoptionguide;

import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse.CheckItemDto;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse.DetailDto;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse.SectionDto;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideCheckItem;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideUserCheck;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideCheckItemRepository;
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
 * 서버 시작 시 들어가는 크레스티드 게코 가이드 콘텐츠(PreAdoptionGuideSeedData)를 기준으로
 * 단계별 사전 입양 가이드 조회를 검증한다.
 */
@SpringBootTest
@Transactional
class PreAdoptionGuideStepServiceTest {

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

    @Autowired
    private PreAdoptionGuideCheckItemRepository checkItemRepository;

    private User user;
    private Long preGuideId;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("guide-tester", "guide-tester@example.com", "password"));

        Long crestedGeckoId = speciesRepository.findByName("크레스티드 게코").orElseThrow().getSpeciesId();
        PreAdoptionGuideResponse guide = guideService.getGuideBySpecies(crestedGeckoId);
        preGuideId = guide.getPreGuideId();
    }

    @Test
    @DisplayName("1. 1단계: 섹션 4개와 세부 안내(콘텐츠, 강조 블록)를 표시 순서대로 반환")
    void getStep_Step1_ReturnsSectionsInOrder() {
        PreAdoptionGuideStepResponse response = stepService.getStep(user.getUserId(), preGuideId, 1);

        assertThat(response.getPreGuideId()).isEqualTo(preGuideId);
        assertThat(response.getStepNumber()).isEqualTo(1);
        assertThat(response.getTitle()).isEqualTo("사육장 준비");
        assertThat(response.getDescription()).isEqualTo("도마뱀에게 맞는 사육장의 크기와 소재를 확인해요.");
        assertThat(response.getSections())
                .extracting(SectionDto::getTitle)
                .containsExactly("사육장 형태", "사육장 크기", "소재와 환기", "설치 위치");

        DetailDto verticalDetail = response.getSections().get(0).getDetails().get(0);
        assertThat(verticalDetail.getQuestion()).isEqualTo("왜 세로형이 필요한가요?");
        assertThat(verticalDetail.getContents()).hasSize(2);
        assertThat(verticalDetail.getBlocks()).isEmpty();

        assertThat(response.getSections().get(1).getDetails()).isEmpty();

        DetailDto materialDetail = response.getSections().get(2).getDetails().get(0);
        assertThat(materialDetail.getBlocks()).hasSize(1);
        assertThat(materialDetail.getBlocks().get(0).getBlockTitle()).isEqualTo("기관 권고 사항");
        assertThat(materialDetail.getBlocks().get(0).getItems()).hasSize(2);

        assertThat(response.getCheckItems()).isEmpty();
    }

    @Test
    @DisplayName("2. 2단계: 콘텐츠 제목과 배지, 체크 항목을 반환")
    void getStep_Step2_ReturnsBadgesAndCheckItems() {
        PreAdoptionGuideStepResponse response = stepService.getStep(user.getUserId(), preGuideId, 2);

        DetailDto equipmentDetail = response.getSections().get(3).getDetails().get(0);
        assertThat(equipmentDetail.getQuestion()).isEqualTo("어떤 장비가 필요한가요?");
        assertThat(equipmentDetail.getContents())
                .extracting("contentTitle", "badge")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("주간 조명", null),
                        org.assertj.core.groups.Tuple.tuple("히팅 장비", "필요한 경우"),
                        org.assertj.core.groups.Tuple.tuple("온도조절기", "열원 사용 시 필수")
                );

        assertThat(response.getCheckItems()).hasSize(5);
        assertThat(response.getCheckItems()).extracting(CheckItemDto::getItemOrder).containsExactly(1, 2, 3, 4, 5);
        assertThat(response.getCheckItems()).allMatch(item -> !item.getChecked());
        assertThat(response.getCheckItems()).allMatch(item -> item.getGroupTitle() == null);
        assertThat(response.getCheckItems().get(0).getCheckTitle()).isEqualTo("가지와 덩굴을 여러 높이에 연결해 설치했어요");
        assertThat(response.getCheckItems().get(0).getCheckDescription()).isEqualTo("위아래로 이동할 수 있는 경로가 있어요.");
    }

    @Test
    @DisplayName("2-1. 5단계: 체크 항목이 그룹(사육장 / 생활 준비 / 환경 및 센서)별로 순서대로 반환")
    void getStep_Step5_ReturnsCheckItemGroups() {
        PreAdoptionGuideStepResponse response = stepService.getStep(user.getUserId(), preGuideId, 5);

        assertThat(response.getTitle()).isEqualTo("준비 상태 확인");
        assertThat(response.getCheckItems()).extracting(CheckItemDto::getGroupTitle)
                .containsExactly("사육장", "사육장", "생활 준비", "생활 준비", "생활 준비",
                        "환경 및 센서", "환경 및 센서", "환경 및 센서");
        assertThat(response.getCheckItems()).allMatch(item -> item.getCheckDescription() == null);
    }

    @Test
    @DisplayName("3. 사용자가 체크한 항목만 checked=true로 반환")
    void getStep_ReflectsUserChecks() {
        PreAdoptionGuideStepResponse before = stepService.getStep(user.getUserId(), preGuideId, 2);
        Long firstId = before.getCheckItems().get(0).getCheckItemId();
        Long secondId = before.getCheckItems().get(1).getCheckItemId();

        List<PreAdoptionGuideCheckItem> items = checkItemRepository.findAllById(List.of(firstId, secondId));
        PreAdoptionGuideCheckItem first = items.stream().filter(i -> i.getCheckItemId().equals(firstId)).findFirst().orElseThrow();
        PreAdoptionGuideCheckItem second = items.stream().filter(i -> i.getCheckItemId().equals(secondId)).findFirst().orElseThrow();
        userCheckRepository.save(new PreAdoptionGuideUserCheck(user, first, true));
        userCheckRepository.save(new PreAdoptionGuideUserCheck(user, second, false));

        PreAdoptionGuideStepResponse after = stepService.getStep(user.getUserId(), preGuideId, 2);

        assertThat(after.getCheckItems()).extracting(CheckItemDto::getChecked)
                .containsExactly(true, false, false, false, false);
    }

    @Test
    @DisplayName("4. 다른 사용자의 체크 상태는 반영하지 않음")
    void getStep_IgnoresOtherUsersChecks() {
        User other = userRepository.save(new User("other-tester", "other-tester@example.com", "password"));
        PreAdoptionGuideStepResponse before = stepService.getStep(user.getUserId(), preGuideId, 2);
        PreAdoptionGuideCheckItem first = checkItemRepository
                .findById(before.getCheckItems().get(0).getCheckItemId()).orElseThrow();
        userCheckRepository.save(new PreAdoptionGuideUserCheck(other, first, true));

        PreAdoptionGuideStepResponse after = stepService.getStep(user.getUserId(), preGuideId, 2);

        assertThat(after.getCheckItems()).allMatch(item -> !item.getChecked());
    }

    @Test
    @DisplayName("5. 1~5단계가 모두 조회됨")
    void getStep_AllFiveStepsExist() {
        for (int stepNumber = 1; stepNumber <= 5; stepNumber++) {
            assertThat(stepService.getStep(user.getUserId(), preGuideId, stepNumber).getStepNumber())
                    .isEqualTo(stepNumber);
        }
    }

    @Test
    @DisplayName("6. 존재하지 않는 가이드면 PRE_ADOPTION_GUIDE_NOT_FOUND")
    void getStep_GuideNotFound() {
        assertThatThrownBy(() -> stepService.getStep(user.getUserId(), 999_999L, 1))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);
    }

    @Test
    @DisplayName("7. 가이드에 없는 단계면 GUIDE_STEP_NOT_FOUND")
    void getStep_StepNotFound() {
        assertThatThrownBy(() -> stepService.getStep(user.getUserId(), preGuideId, 6))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GUIDE_STEP_NOT_FOUND);
    }

    @Test
    @DisplayName("8. 토큰의 사용자가 없으면 UNAUTHORIZED")
    void getStep_UserNotFound() {
        assertThatThrownBy(() -> stepService.getStep(999_999L, preGuideId, 1))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("9. 콘텐츠가 준비되지 않은 종은 가이드가 없음")
    void guideOnlyForCrestedGecko() {
        Long beardedDragonId = speciesRepository.findByName("비어디드 드래곤").orElseThrow().getSpeciesId();

        assertThatThrownBy(() -> guideService.getGuideBySpecies(beardedDragonId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);
    }
}
