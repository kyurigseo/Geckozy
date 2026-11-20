package com.likelion.backend.domain.preadoptionguide.service;

import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStartResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideProgressResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepCompleteResponse;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuide;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideCheckItem;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideCheckItemRepository;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideProgressRepository;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideRepository;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideUserCheckRepository;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import com.likelion.backend.global.util.KstTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreAdoptionGuideService {

    private final SpeciesRepository speciesRepository;
    private final PreAdoptionGuideRepository preAdoptionGuideRepository;
    private final PreAdoptionGuideProgressRepository preAdoptionGuideProgressRepository;
    private final UserRepository userRepository;
    private final PreAdoptionGuideProgressCreator preAdoptionGuideProgressCreator;
    private final PreAdoptionGuideCheckItemRepository checkItemRepository;
    private final PreAdoptionGuideUserCheckRepository userCheckRepository;

    private static final int ENCLOSURE_STEP = 1;
    private static final int ENVIRONMENT_CHECK_STEP = 4;

    // 해당 종 사전 입양 가이드 조회
    public PreAdoptionGuideResponse getGuideBySpecies(Long speciesId) {

        if (!speciesRepository.existsById(speciesId)) {
            throw new BusinessException(ErrorCode.SPECIES_NOT_FOUND);
        }

        PreAdoptionGuide guide = preAdoptionGuideRepository
                .findBySpecies_SpeciesId(speciesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND));

        return PreAdoptionGuideResponse.of(guide, speciesId);
    }

    /*
     * 사전 입양 가이드 진행 상태 조회 (기록이 없으면 첫 단계로 초기 진행 상태 생성)
     *
     * 같은 사용자의 첫 조회가 동시에 들어오면 둘 다 "기록 없음"을 보고 INSERT 할 수 있다.
     * (user_id, pre_guide_id) 유니크 제약 때문에 하나는 실패하므로, 실패한 쪽은 먼저 저장된 기록을 다시 읽어 반환한다.
     * 감싸는 트랜잭션이 있으면 실패한 INSERT 때문에 전체가 롤백되고(MySQL REPEATABLE READ에서는 재조회도 안 보임)
     * 409가 나가므로, 이 메서드는 트랜잭션 없이 실행하고 INSERT만 별도 트랜잭션(PreAdoptionGuideProgressCreator)에서 한다.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PreAdoptionGuideProgressResponse getProgress(Long userId, Long preGuideId) {

        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!preAdoptionGuideRepository.existsById(preGuideId)) {
            throw new BusinessException(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);
        }

        PreAdoptionGuideProgress progress = findProgress(userId, preGuideId)
                .orElseGet(() -> createOrFindProgress(userId, preGuideId));

        return PreAdoptionGuideProgressResponse.of(progress, preGuideId);
    }

    /*
     * 단계 완료 및 다음 단계 이동
     * - 순서대로만 완료할 수 있다 (완료한 마지막 단계 + 1). 이미 완료한 단계를 다시 보내면 현재 상태를 그대로 반환한다.
     * - 5단계 완료는 입양 전 가이드 완료 API에서 처리하므로 여기서는 받지 않는다.
     * - 단계별 완료 조건: 해당 단계의 필수 체크 항목 전부 체크
     *     + 1단계: 가이드 종의 도마뱀에 사육장 등록
     *     + 4단계: 7일 환경 점검 완료
     */
    @Transactional
    public PreAdoptionGuideStepCompleteResponse completeStep(Long userId, Long preGuideId, int stepNumber) {

        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!preAdoptionGuideRepository.existsById(preGuideId)) {
            throw new BusinessException(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);
        }

        if (stepNumber >= PreAdoptionGuideProgress.LAST_STEP) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        PreAdoptionGuideProgress progress = findProgressForUpdate(userId, preGuideId);

        // 이미 완료한 단계 (완료 버튼 연타 등): 변경 없이 현재 상태 반환
        if (stepNumber <= progress.getCompletedStep()) {
            return PreAdoptionGuideStepCompleteResponse.of(progress);
        }

        // 단계를 건너뛰는 경우
        if (stepNumber != progress.nextStepToComplete()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        validateStepCompletable(progress, userId, preGuideId, stepNumber);

        progress.completeStep(stepNumber);

        return PreAdoptionGuideStepCompleteResponse.of(progress);
    }

    /*
     * 7일 환경 점검 시작
     * - 3단계(센서 설치)를 완료해야 시작할 수 있다.
     * - 한 번 시작하면 다시 시작할 수 없다 (진행 중이든 완료됐든 ENVIRONMENT_CHECK_ALREADY_STARTED).
     * - 진행 상태 행을 잠근 채 처리하므로 시작 요청이 동시에 와도 하나만 시작된다.
     */
    @Transactional
    public EnvironmentCheckStartResponse startEnvironmentCheck(Long userId, Long preGuideId) {

        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!preAdoptionGuideRepository.existsById(preGuideId)) {
            throw new BusinessException(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);
        }

        // 진행 상태가 없으면 아직 아무 단계도 완료하지 않은 것
        PreAdoptionGuideProgress progress = preAdoptionGuideProgressRepository.findForUpdate(userId, preGuideId)
                .filter(PreAdoptionGuideProgress::canStartEnvironmentCheck)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRE_ADOPTION_STEP_NOT_COMPLETED));

        if (progress.isEnvironmentCheckStarted()) {
            throw new BusinessException(ErrorCode.ENVIRONMENT_CHECK_ALREADY_STARTED);
        }

        LocalDateTime now = KstTime.now();
        progress.startEnvironmentCheck(now);

        return EnvironmentCheckStartResponse.of(progress, now);
    }

    private void validateStepCompletable(
            PreAdoptionGuideProgress progress,
            Long userId,
            Long preGuideId,
            int stepNumber
    ) {

        List<Long> requiredItemIds = checkItemRepository
                .findAllByStep_PreAdoptionGuide_PreGuideIdAndStep_StepNumberAndRequiredTrue(preGuideId, stepNumber)
                .stream()
                .map(PreAdoptionGuideCheckItem::getCheckItemId)
                .toList();

        if (!requiredItemIds.isEmpty()
                && userCheckRepository.countByUser_UserIdAndCheckItem_CheckItemIdInAndCheckedTrue(userId, requiredItemIds)
                < requiredItemIds.size()) {
            throw new BusinessException(ErrorCode.REQUIRED_CHECKS_INCOMPLETE);
        }

        if (stepNumber == ENCLOSURE_STEP
                && !preAdoptionGuideRepository.existsEnclosureForGuideSpecies(userId, preGuideId)) {
            throw new BusinessException(ErrorCode.ENCLOSURE_NOT_REGISTERED);
        }

        if (stepNumber == ENVIRONMENT_CHECK_STEP && !progress.isEnvironmentCheckCompleted(KstTime.now())) {
            throw new BusinessException(ErrorCode.ENVIRONMENT_CHECK_INCOMPLETE);
        }
    }

    // 진행 상태를 잠금 조회. 아직 없으면 (첫 조회 없이 바로 완료 요청한 경우) 생성 후 다시 잠금 조회
    private PreAdoptionGuideProgress findProgressForUpdate(Long userId, Long preGuideId) {
        return preAdoptionGuideProgressRepository.findForUpdate(userId, preGuideId)
                .orElseGet(() -> {
                    try {
                        preAdoptionGuideProgressCreator.create(userId, preGuideId);
                    } catch (DataIntegrityViolationException e) {
                        // 동시에 들어온 다른 요청이 먼저 생성한 경우: 그 기록을 사용.
                        // 그런 기록이 없으면 중복이 아닌 다른 제약 위반이므로 원래 예외를 그대로 던진다.
                        return preAdoptionGuideProgressRepository.findForUpdate(userId, preGuideId)
                                .orElseThrow(() -> e);
                    }
                    return preAdoptionGuideProgressRepository.findForUpdate(userId, preGuideId)
                            .orElseThrow(() -> new IllegalStateException("생성한 진행 상태를 찾을 수 없음"));
                });
    }

    private PreAdoptionGuideProgress createOrFindProgress(Long userId, Long preGuideId) {
        try {
            return preAdoptionGuideProgressCreator.create(userId, preGuideId);
        } catch (DataIntegrityViolationException e) {
            // 동시에 들어온 다른 요청이 먼저 생성한 경우
            return findProgress(userId, preGuideId).orElseThrow(() -> e);
        }
    }

    private Optional<PreAdoptionGuideProgress> findProgress(Long userId, Long preGuideId) {
        return preAdoptionGuideProgressRepository
                .findByUser_UserIdAndPreAdoptionGuide_PreGuideId(userId, preGuideId);
    }
}
