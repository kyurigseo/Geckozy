package com.likelion.backend.domain.lizard.service;

import com.likelion.backend.domain.lizard.dto.response.LizardListResponse;
import com.likelion.backend.domain.lizard.dto.response.LizardResponse;
import com.likelion.backend.domain.lizard.dto.request.LizardUpdateRequest;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import com.likelion.backend.domain.lizard.dto.request.LizardCreateRequest;
import com.likelion.backend.domain.lizard.entity.Lizard;
import com.likelion.backend.domain.lizard.repository.LizardRepository;
import com.likelion.backend.domain.species.entity.Species;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LizardService {

    private final LizardRepository lizardRepository;
    private final SpeciesRepository speciesRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createLizard(
            Long userId,
            LizardCreateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        Species species = speciesRepository.findById(request.speciesId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SPECIES_NOT_FOUND));

        Lizard lizard = new Lizard(
                user,
                species,
                request.name(),
                request.birthDate(),
                request.birthDateUnknown(),
                request.characterColor(),
                request.gender(),
                request.sizeCm(),
                request.weightG(),
                request.sizeWeightUnknown()
        );

        Lizard savedLizard = lizardRepository.save(lizard);

        return savedLizard.getLizardId();
    }

    public List<LizardListResponse> getLizards(Long userId) {

        return lizardRepository.findAllByUser_UserIdAndDeletedAtIsNull(userId)
                .stream()
                .map(lizard -> new LizardListResponse(
                        lizard.getLizardId(),
                        lizard.getName(),
                        lizard.getSpecies().getName()
                ))
                .toList();
    }

    // 도마뱀 단건 조회
    public LizardResponse getLizard(Long userId, Long lizardId) {

        Lizard lizard = findMyLizard(userId, lizardId);

        return new LizardResponse(
                lizard.getLizardId(),
                lizard.getSpecies().getSpeciesId(),
                lizard.getSpecies().getName(),
                lizard.getName(),
                lizard.getBirthDate(),
                lizard.isBirthDateUnknown(),
                lizard.getCharacterColor(),
                lizard.getGender(),
                lizard.getSizeCm(),
                lizard.getWeightG(),
                lizard.isSizeWeightUnknown()
        );
    }

    // 도마뱀 수정
    @Transactional
    public void updateLizard(
            Long userId,
            Long lizardId,
            LizardUpdateRequest request
    ) {
        Lizard lizard = findMyLizard(userId, lizardId);

        Species species = null;

        if (request.speciesId() != null) {
            species = speciesRepository.findById(request.speciesId())
                    .orElseThrow(() ->
                            new BusinessException(ErrorCode.SPECIES_NOT_FOUND)
                    );
        }

        lizard.update(
                species,
                request.name(),
                request.birthDate(),
                request.birthDateUnknown(),
                request.characterColor(),
                request.gender(),
                request.sizeCm(),
                request.weightG()
        );
    }

    // 도마뱀 삭제
    @Transactional
    public void deleteLizard(Long userId, Long lizardId) {

        Lizard lizard = findMyLizard(userId, lizardId);

        lizard.delete();
    }

    // 내 도마뱀인지 확인
    private Lizard findMyLizard(Long userId, Long lizardId) {

        Lizard lizard = lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.LIZARD_NOT_FOUND)
                );

        if (!lizard.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.LIZARD_NOT_FOUND);
        }

        return lizard;
    }
}
