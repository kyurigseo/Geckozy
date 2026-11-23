package com.likelion.backend.domain.preadoptionguide.service;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideProgressRepository;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideRepository;
import com.likelion.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 초기 진행 상태 INSERT 전용.
 * 유니크 제약 위반이 나도 호출한 쪽 트랜잭션이 롤백되지 않도록 항상 새 트랜잭션에서 저장하고 바로 flush 한다.
 */
@Component
@RequiredArgsConstructor
public class PreAdoptionGuideProgressCreator {

    private final UserRepository userRepository;
    private final PreAdoptionGuideRepository preAdoptionGuideRepository;
    private final PreAdoptionGuideProgressRepository preAdoptionGuideProgressRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PreAdoptionGuideProgress create(Long userId, Long preGuideId) {
        return preAdoptionGuideProgressRepository.saveAndFlush(
                PreAdoptionGuideProgress.start(
                        userRepository.getReferenceById(userId),
                        preAdoptionGuideRepository.getReferenceById(preGuideId)
                )
        );
    }
}
