package com.likelion.backend.domain.preadoptionguide.service;

import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideProgressResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideResponse;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuide;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideProgressRepository;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideRepository;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
