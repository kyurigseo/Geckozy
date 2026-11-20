package com.likelion.backend.domain.preadoptionguide;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.lizard.entity.Lizard;
import com.likelion.backend.domain.lizard.repository.LizardRepository;
import com.likelion.backend.domain.preadoptionguide.dto.request.PreAdoptionGuideCheckSaveRequest;
import com.likelion.backend.domain.preadoptionguide.dto.request.PreAdoptionGuideCheckSaveRequest.CheckDto;
import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStartResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStartResponse.EnvironmentCheckStatus;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideCompleteResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideProgressResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepCompleteResponse;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
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
import com.likelion.backend.global.util.KstTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 7일 환경 점검 시작과, 점검 완료 여부에 따른 4단계 완료를 검증한다.
 * 진행 상태가 별도 트랜잭션에서 생성되므로 @Transactional 없이 실제로 커밋하고, 테스트마다 데이터를 지운다.
 */
@SpringBootTest
class PreAdoptionGuideEnvironmentCheckStartTest {

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

    private User user;
    private Lizard lizard;
    private Enclosure enclosure;
    private Long preGuideId;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("env-tester", "env-tester@example.com", "password"));
        Species crestedGecko = speciesRepository.findByName("크레스티드 게코").orElseThrow();
        preGuideId = guideService.getGuideBySpecies(crestedGecko.getSpeciesId()).getPreGuideId();
        lizard = lizardRepository.save(Lizard.builder()
                .user(user)
                .species(crestedGecko)
                .name("게코")
                .birthDateUnknown(true)
                .characterColor("yellow")
                .gender(Lizard.Gender.FEMALE)
                .sizeWeightUnknown(true)
                .build());
        enclosure = enclosureRepository.save(Enclosure.builder()
                .lizard(lizard)
                .width(new BigDecimal("45"))
                .depth(new BigDecimal("45"))
                .height(new BigDecimal("60"))
                .material(Enclosure.Material.GLASS)
                .ventilation(Enclosure.Ventilation.TOP)
                .build());
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
        enclosureRepository.delete(enclosure);
        lizardRepository.delete(lizard);
        userRepository.delete(user);
    }

    private void completeThrough(int lastStep) {
        for (int step = 1; step <= lastStep; step++) {
            if (step == 2) {
                List<CheckDto> checks = stepService.getStep(user.getUserId(), preGuideId, 2).getCheckItems().stream()
                        .map(item -> new CheckDto(item.getCheckItemId(), true))
                        .toList();
                stepService.saveChecks(user.getUserId(), preGuideId, 2, new PreAdoptionGuideCheckSaveRequest(checks));
            }
            guideService.completeStep(user.getUserId(), preGuideId, step);
        }
    }

    private EnvironmentCheckStartResponse start() {
        return guideService.startEnvironmentCheck(user.getUserId(), preGuideId);
    }

    private void assertStartFails(ErrorCode errorCode) {
        assertThatThrownBy(this::start)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    // 점검 시작 시각을 과거로 옮겨 시간이 지난 상황을 만든다
    private void moveStartBack(long hours) {
        PreAdoptionGuideProgress progress = progressRepository
                .findByUser_UserIdAndPreAdoptionGuide_PreGuideId(user.getUserId(), preGuideId).orElseThrow();
        progress.startEnvironmentCheck(progress.getEnvironmentCheckStartedAt().minusHours(hours));
        progressRepository.save(progress);
    }

    @Test
    @DisplayName("1. 진행 기록이 없으면 PRE_ADOPTION_STEP_NOT_COMPLETED")
    void noProgress() {
        assertStartFails(ErrorCode.PRE_ADOPTION_STEP_NOT_COMPLETED);
    }

    @Test
    @DisplayName("2. 3단계를 완료하지 않았으면 PRE_ADOPTION_STEP_NOT_COMPLETED")
    void step3NotCompleted() {
        completeThrough(2);

        assertStartFails(ErrorCode.PRE_ADOPTION_STEP_NOT_COMPLETED);
    }

    @Test
    @DisplayName("3. 3단계 완료 후 시작하면 오늘 날짜, 1일차, 7일, IN_PROGRESS")
    void start_Success() {
        completeThrough(3);

        EnvironmentCheckStartResponse response = start();

        assertThat(response.getStartDate()).isEqualTo(KstTime.now().toLocalDate());
        assertThat(response.getElapsedDays()).isEqualTo(1);
        assertThat(response.getTotalDays()).isEqualTo(7);
        assertThat(response.getStatus()).isEqualTo(EnvironmentCheckStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("4. 이미 시작했으면 ENVIRONMENT_CHECK_ALREADY_STARTED (완료된 뒤에도)")
    void alreadyStarted() {
        completeThrough(3);
        start();

        assertStartFails(ErrorCode.ENVIRONMENT_CHECK_ALREADY_STARTED);

        moveStartBack(24 * 8);
        assertStartFails(ErrorCode.ENVIRONMENT_CHECK_ALREADY_STARTED);
    }

    @Test
    @DisplayName("5. 4단계: 점검 시작 직후에는 ENVIRONMENT_CHECK_NOT_COMPLETED")
    void step4_RightAfterStart() {
        completeThrough(3);
        start();

        assertThatThrownBy(() -> guideService.completeStep(user.getUserId(), preGuideId, 4))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENVIRONMENT_CHECK_NOT_COMPLETED);
    }

    @Test
    @DisplayName("6. 4단계: 시작 후 167시간이면 아직 ENVIRONMENT_CHECK_NOT_COMPLETED")
    void step4_Before168Hours() {
        completeThrough(3);
        start();
        moveStartBack(167);

        assertThatThrownBy(() -> guideService.completeStep(user.getUserId(), preGuideId, 4))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENVIRONMENT_CHECK_NOT_COMPLETED);
    }

    @Test
    @DisplayName("7. 4단계: 시작 후 만 7일이 지나면 완료되고 5단계로 이동")
    void step4_After168Hours() {
        completeThrough(3);
        start();
        moveStartBack(168);

        PreAdoptionGuideStepCompleteResponse response = guideService.completeStep(user.getUserId(), preGuideId, 4);

        assertThat(response.getCompletedStep()).isEqualTo(4);
        assertThat(response.getCurrentStep()).isEqualTo(5);
    }

    @Test
    @DisplayName("8. 존재하지 않는 가이드면 PRE_ADOPTION_GUIDE_NOT_FOUND")
    void guideNotFound() {
        assertThatThrownBy(() -> guideService.startEnvironmentCheck(user.getUserId(), 999_999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);
    }

    @Test
    @DisplayName("9. 토큰의 사용자가 없으면 UNAUTHORIZED")
    void userNotFound() {
        assertThatThrownBy(() -> guideService.startEnvironmentCheck(999_999L, preGuideId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    // ---------------------------------------------------------------- 입양 전 가이드 완료

    private void checkStep5(int count) {
        List<CheckDto> checks = stepService.getStep(user.getUserId(), preGuideId, 5).getCheckItems().stream()
                .limit(count)
                .map(item -> new CheckDto(item.getCheckItemId(), true))
                .toList();
        stepService.saveChecks(user.getUserId(), preGuideId, 5, new PreAdoptionGuideCheckSaveRequest(checks));
    }

    // 1~3단계 완료 → 점검 시작 → 만 7일 경과
    private void finishEnvironmentCheck() {
        completeThrough(3);
        start();
        moveStartBack(168);
    }

    private void assertCompleteFails(ErrorCode errorCode) {
        assertThatThrownBy(() -> guideService.completeGuide(user.getUserId(), preGuideId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    @DisplayName("11. 완료: 진행 기록이 없으면 ENVIRONMENT_CHECK_NOT_COMPLETED")
    void complete_NoProgress() {
        assertCompleteFails(ErrorCode.ENVIRONMENT_CHECK_NOT_COMPLETED);
    }

    @Test
    @DisplayName("12. 완료: 7일 점검이 끝나지 않았으면 ENVIRONMENT_CHECK_NOT_COMPLETED (5단계 체크를 다 했어도)")
    void complete_EnvironmentCheckNotDone() {
        completeThrough(3);
        start();
        moveStartBack(167);
        checkStep5(8);

        assertCompleteFails(ErrorCode.ENVIRONMENT_CHECK_NOT_COMPLETED);
    }

    @Test
    @DisplayName("13. 완료: 5단계 필수 항목을 다 체크하지 않았으면 PRE_ADOPTION_REQUIRED_ITEMS_INCOMPLETE")
    void complete_RequiredItemsIncomplete() {
        finishEnvironmentCheck();
        checkStep5(7);

        assertCompleteFails(ErrorCode.PRE_ADOPTION_REQUIRED_ITEMS_INCOMPLETE);
    }

    @Test
    @DisplayName("14. 완료: 4단계를 따로 완료하지 않았어도 4·5단계를 함께 완료")
    void complete_WithoutStep4() {
        finishEnvironmentCheck();
        checkStep5(8);

        PreAdoptionGuideCompleteResponse response = guideService.completeGuide(user.getUserId(), preGuideId);

        assertThat(response.getCompleted()).isTrue();
        assertThat(response.getCompletedAt()).isNotNull();
        assertThat(response.getCompletedAt().getNano()).isZero();

        PreAdoptionGuideProgressResponse progress = guideService.getProgress(user.getUserId(), preGuideId);
        assertThat(progress.getCompletedStep()).isEqualTo(5);
        assertThat(progress.getCurrentStep()).isEqualTo(5);

        // 단계 완료 API 응답에도 가이드 전체 완료가 반영됨
        PreAdoptionGuideStepCompleteResponse step = guideService.completeStep(user.getUserId(), preGuideId, 1);
        assertThat(step.getCompleted()).isTrue();
    }

    @Test
    @DisplayName("15. 완료: 4단계를 먼저 완료한 경우에도 완료")
    void complete_AfterStep4() {
        finishEnvironmentCheck();
        guideService.completeStep(user.getUserId(), preGuideId, 4);
        checkStep5(8);

        assertThat(guideService.completeGuide(user.getUserId(), preGuideId).getCompleted()).isTrue();
    }

    @Test
    @DisplayName("16. 완료: 다시 요청하면 처음 완료 시각 그대로 반환 (체크를 해제했어도)")
    void complete_Again_ReturnsFirstCompletedAt() {
        finishEnvironmentCheck();
        checkStep5(8);
        LocalDateTime first = guideService.completeGuide(user.getUserId(), preGuideId).getCompletedAt();

        List<CheckDto> uncheck = stepService.getStep(user.getUserId(), preGuideId, 5).getCheckItems().stream()
                .map(item -> new CheckDto(item.getCheckItemId(), false))
                .toList();
        stepService.saveChecks(user.getUserId(), preGuideId, 5, new PreAdoptionGuideCheckSaveRequest(uncheck));

        PreAdoptionGuideCompleteResponse again = guideService.completeGuide(user.getUserId(), preGuideId);
        assertThat(again.getCompleted()).isTrue();
        assertThat(again.getCompletedAt()).isEqualTo(first);
    }

    @Test
    @DisplayName("17. 완료: 존재하지 않는 가이드면 PRE_ADOPTION_GUIDE_NOT_FOUND, 사용자가 없으면 UNAUTHORIZED")
    void complete_NotFoundAndUnauthorized() {
        assertThatThrownBy(() -> guideService.completeGuide(user.getUserId(), 999_999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);

        assertThatThrownBy(() -> guideService.completeGuide(999_999L, preGuideId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("18. 완료: 동시에 요청해도 모두 같은 완료 시각을 받음")
    void complete_Concurrent_SameCompletedAt() throws Exception {
        finishEnvironmentCheck();
        checkStep5(8);

        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<PreAdoptionGuideCompleteResponse>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    startLatch.await();
                    return guideService.completeGuide(user.getUserId(), preGuideId);
                }));
            }
            startLatch.countDown();

            List<LocalDateTime> completedAts = new ArrayList<>();
            for (Future<PreAdoptionGuideCompleteResponse> future : futures) {
                completedAts.add(future.get(10, TimeUnit.SECONDS).getCompletedAt());
            }
            assertThat(completedAts.stream().distinct()).hasSize(1);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("10. 시작 요청이 동시에 들어와도 하나만 시작되고 나머지는 ENVIRONMENT_CHECK_ALREADY_STARTED")
    void concurrentStart_OnlyOneSucceeds() throws Exception {
        completeThrough(3);

        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<EnvironmentCheckStartResponse>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    startLatch.await();
                    return start();
                }));
            }
            startLatch.countDown();

            int succeeded = 0;
            int alreadyStarted = 0;
            for (Future<EnvironmentCheckStartResponse> future : futures) {
                try {
                    future.get(10, TimeUnit.SECONDS);
                    succeeded++;
                } catch (ExecutionException e) {
                    assertThat(e.getCause()).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) e.getCause()).getErrorCode())
                            .isEqualTo(ErrorCode.ENVIRONMENT_CHECK_ALREADY_STARTED);
                    alreadyStarted++;
                }
            }

            assertThat(succeeded).isEqualTo(1);
            assertThat(alreadyStarted).isEqualTo(threads - 1);
        } finally {
            executor.shutdownNow();
        }
    }
}
