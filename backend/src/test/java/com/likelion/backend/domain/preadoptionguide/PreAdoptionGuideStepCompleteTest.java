package com.likelion.backend.domain.preadoptionguide;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.lizard.entity.Lizard;
import com.likelion.backend.domain.lizard.repository.LizardRepository;
import com.likelion.backend.domain.preadoptionguide.dto.request.PreAdoptionGuideCheckSaveRequest;
import com.likelion.backend.domain.preadoptionguide.dto.request.PreAdoptionGuideCheckSaveRequest.CheckDto;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideProgressResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepCompleteResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse.CheckItemDto;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideProgressRepository;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideUserCheckRepository;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideService;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideStepService;
import com.likelion.backend.domain.species.entity.Species;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 서버 시작 시 들어가는 크레스티드 게코 가이드 콘텐츠를 기준으로 단계 완료 및 다음 단계 이동을 검증한다.
 *
 * 진행 상태는 별도 트랜잭션에서 생성되므로(동시 요청 대비) 테스트 데이터도 실제로 커밋돼 있어야 한다.
 * 그래서 @Transactional 을 붙이지 않고, 테스트마다 만든 데이터를 직접 지운다.
 */
@SpringBootTest
class PreAdoptionGuideStepCompleteTest {

    @Autowired
    private PreAdoptionGuideService guideService;

    @Autowired
    private PreAdoptionGuideStepService stepService;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LizardRepository lizardRepository;

    @Autowired
    private EnclosureRepository enclosureRepository;

    @Autowired
    private PreAdoptionGuideProgressRepository progressRepository;

    @Autowired
    private PreAdoptionGuideUserCheckRepository userCheckRepository;

    private final List<Lizard> lizards = new ArrayList<>();
    private final List<Enclosure> enclosures = new ArrayList<>();

    private User user;
    private Species crestedGecko;
    private Long preGuideId;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("complete-tester", "complete-tester@example.com", "password"));
        crestedGecko = speciesRepository.findByName("크레스티드 게코").orElseThrow();
        preGuideId = guideService.getGuideBySpecies(crestedGecko.getSpeciesId()).getPreGuideId();
    }

    @AfterEach
    void cleanUp() {
        Long userId = user.getUserId();
        userCheckRepository.findAll().stream()
                .filter(check -> check.getUser().getUserId().equals(userId))
                .forEach(userCheckRepository::delete);
        progressRepository.findAll().stream()
                .filter(progress -> progress.getUser().getUserId().equals(userId))
                .forEach(progressRepository::delete);
        enclosureRepository.deleteAll(enclosures);
        lizardRepository.deleteAll(lizards);
        userRepository.delete(user);
        enclosures.clear();
        lizards.clear();
    }

    private Lizard registerEnclosure(Species species) {
        Lizard lizard = lizardRepository.save(Lizard.builder()
                .user(user)
                .species(species)
                .name("게코")
                .birthDateUnknown(true)
                .characterColor("yellow")
                .gender(Lizard.Gender.FEMALE)
                .sizeWeightUnknown(true)
                .build());
        lizards.add(lizard);
        Enclosure enclosure = enclosureRepository.save(Enclosure.builder()
                .lizard(lizard)
                .width(new BigDecimal("45"))
                .depth(new BigDecimal("45"))
                .height(new BigDecimal("60"))
                .material(Enclosure.Material.GLASS)
                .ventilation(Enclosure.Ventilation.TOP)
                .build());
        enclosures.add(enclosure);
        return lizard;
    }

    private void checkAll(int stepNumber, boolean checked) {
        List<CheckDto> checks = stepService.getStep(user.getUserId(), preGuideId, stepNumber).getCheckItems().stream()
                .map(item -> new CheckDto(item.getCheckItemId(), checked))
                .toList();
        stepService.saveChecks(user.getUserId(), preGuideId, stepNumber, new PreAdoptionGuideCheckSaveRequest(checks));
    }

    private PreAdoptionGuideStepCompleteResponse complete(int stepNumber) {
        return guideService.completeStep(user.getUserId(), preGuideId, stepNumber);
    }

    private void assertFails(int stepNumber, ErrorCode errorCode) {
        assertThatThrownBy(() -> complete(stepNumber))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    // 1~3단계 완료 조건을 모두 갖추고 순서대로 완료
    private void completeThroughStep3() {
        registerEnclosure(crestedGecko);
        complete(1);
        checkAll(2, true);
        complete(2);
        complete(3);
    }

    @Test
    @DisplayName("1. 1단계: 사육장을 등록하지 않으면 ENCLOSURE_NOT_REGISTERED")
    void step1_WithoutEnclosure() {
        assertFails(1, ErrorCode.ENCLOSURE_NOT_REGISTERED);
    }

    @Test
    @DisplayName("2. 1단계: 가이드와 다른 종 도마뱀의 사육장은 인정하지 않음")
    void step1_EnclosureOfOtherSpecies() {
        registerEnclosure(speciesRepository.findByName("레오파드 게코").orElseThrow());

        assertFails(1, ErrorCode.ENCLOSURE_NOT_REGISTERED);
    }

    @Test
    @DisplayName("3. 1단계: 삭제된 도마뱀의 사육장은 인정하지 않음")
    void step1_EnclosureOfDeletedLizard() {
        Lizard lizard = registerEnclosure(crestedGecko);
        lizard.delete();
        lizardRepository.save(lizard);

        assertFails(1, ErrorCode.ENCLOSURE_NOT_REGISTERED);
    }

    @Test
    @DisplayName("4. 1단계: 사육장이 있으면 완료하고 2단계로 이동")
    void step1_Success() {
        registerEnclosure(crestedGecko);

        PreAdoptionGuideStepCompleteResponse response = complete(1);

        assertThat(response.getCompletedStep()).isEqualTo(1);
        assertThat(response.getCurrentStep()).isEqualTo(2);
        assertThat(response.getCompleted()).isFalse();
    }

    @Test
    @DisplayName("5. 진행 상태 조회 없이 바로 완료 요청해도 진행 상태를 만들어 처리하고, 조회에 반영됨")
    void complete_WithoutPriorProgress_ReflectsInGetProgress() {
        registerEnclosure(crestedGecko);
        complete(1);

        PreAdoptionGuideProgressResponse progress = guideService.getProgress(user.getUserId(), preGuideId);
        assertThat(progress.getCompletedStep()).isEqualTo(1);
        assertThat(progress.getCurrentStep()).isEqualTo(2);
    }

    @Test
    @DisplayName("6. 단계를 건너뛰면 INVALID_REQUEST")
    void skipStep_InvalidRequest() {
        assertFails(2, ErrorCode.INVALID_REQUEST);
        assertFails(3, ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("7. 2단계: 필수 체크 항목을 일부만 체크하면 REQUIRED_CHECKS_INCOMPLETE")
    void step2_PartialChecks() {
        registerEnclosure(crestedGecko);
        complete(1);

        List<Long> itemIds = stepService.getStep(user.getUserId(), preGuideId, 2).getCheckItems().stream()
                .map(CheckItemDto::getCheckItemId)
                .toList();
        stepService.saveChecks(user.getUserId(), preGuideId, 2, new PreAdoptionGuideCheckSaveRequest(
                itemIds.subList(0, 4).stream().map(id -> new CheckDto(id, true)).toList()
        ));

        assertFails(2, ErrorCode.REQUIRED_CHECKS_INCOMPLETE);
    }

    @Test
    @DisplayName("8. 2단계: 체크했다가 해제한 항목은 체크하지 않은 것으로 봄")
    void step2_UncheckedCountsAsIncomplete() {
        registerEnclosure(crestedGecko);
        complete(1);
        checkAll(2, true);
        checkAll(2, false);

        assertFails(2, ErrorCode.REQUIRED_CHECKS_INCOMPLETE);
    }

    @Test
    @DisplayName("9. 2~3단계: 필수 체크를 모두 하면 순서대로 완료 (3단계는 조건 없음)")
    void step2And3_Success() {
        registerEnclosure(crestedGecko);
        complete(1);
        checkAll(2, true);

        PreAdoptionGuideStepCompleteResponse step2 = complete(2);
        assertThat(step2.getCompletedStep()).isEqualTo(2);
        assertThat(step2.getCurrentStep()).isEqualTo(3);

        PreAdoptionGuideStepCompleteResponse step3 = complete(3);
        assertThat(step3.getCompletedStep()).isEqualTo(3);
        assertThat(step3.getCurrentStep()).isEqualTo(4);
    }

    @Test
    @DisplayName("10. 4단계: 7일 환경 점검이 완료되지 않으면 ENVIRONMENT_CHECK_NOT_COMPLETED")
    void step4_EnvironmentCheckIncomplete() {
        completeThroughStep3();

        assertFails(4, ErrorCode.ENVIRONMENT_CHECK_NOT_COMPLETED);
    }

    @Test
    @DisplayName("11. 5단계는 입양 전 가이드 완료 API에서 처리하므로 INVALID_REQUEST")
    void step5_InvalidRequest() {
        assertFails(5, ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("12. 이미 완료한 단계를 다시 보내면 변경 없이 현재 상태 반환")
    void completedStepAgain_ReturnsCurrentState() {
        completeThroughStep3();

        PreAdoptionGuideStepCompleteResponse response = complete(1);

        assertThat(response.getCompletedStep()).isEqualTo(3);
        assertThat(response.getCurrentStep()).isEqualTo(4);
    }

    @Test
    @DisplayName("13. 존재하지 않는 가이드면 PRE_ADOPTION_GUIDE_NOT_FOUND")
    void guideNotFound() {
        assertThatThrownBy(() -> guideService.completeStep(user.getUserId(), 999_999L, 1))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);
    }

    @Test
    @DisplayName("14. 토큰의 사용자가 없으면 UNAUTHORIZED")
    void userNotFound() {
        assertThatThrownBy(() -> guideService.completeStep(999_999L, preGuideId, 1))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }
}
