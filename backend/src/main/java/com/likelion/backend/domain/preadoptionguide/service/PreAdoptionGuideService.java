package com.likelion.backend.domain.preadoptionguide.service;

import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideProgressResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideResponse;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuide;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideProgressRepository;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideRepository;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreAdoptionGuideService {

    private final SpeciesRepository speciesRepository;
    private final PreAdoptionGuideRepository preAdoptionGuideRepository;
    private final PreAdoptionGuideProgressRepository preAdoptionGuideProgressRepository;
    private final UserRepository userRepository;

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

    // 사전 입양 가이드 진행 상태 조회 (기록이 없으면 첫 단계로 초기 진행 상태 생성)
    @Transactional
    public PreAdoptionGuideProgressResponse getProgress(Long userId, Long preGuideId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        PreAdoptionGuide guide = preAdoptionGuideRepository.findById(preGuideId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND));

        PreAdoptionGuideProgress progress = preAdoptionGuideProgressRepository
                .findByUser_UserIdAndPreAdoptionGuide_PreGuideId(userId, preGuideId)
                .orElseGet(() -> preAdoptionGuideProgressRepository.save(
                        PreAdoptionGuideProgress.start(user, guide)
                ));

        return PreAdoptionGuideProgressResponse.of(progress, guide.getPreGuideId());
    }
}
