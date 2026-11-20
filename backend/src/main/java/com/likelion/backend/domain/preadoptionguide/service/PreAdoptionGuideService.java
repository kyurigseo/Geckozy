package com.likelion.backend.domain.preadoptionguide.service;

import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideResponse;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuide;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideRepository;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
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
}
