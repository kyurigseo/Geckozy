package com.likelion.backend.domain.enclosure.service;

import com.likelion.backend.domain.concern.entity.Concern;
import com.likelion.backend.domain.concern.repository.ConcernRepository;
import com.likelion.backend.domain.enclosure.dto.crud.EnclosureCreateRequest;
import com.likelion.backend.domain.enclosure.dto.crud.EnclosureCreateResponse;
import com.likelion.backend.domain.enclosure.dto.crud.EnclosureUpdateRequest;
import com.likelion.backend.domain.enclosure.dto.crud.EnclosureUpdateResponse;
import com.likelion.backend.domain.enclosure.dto.environment.EnclosureResponse;
import com.likelion.backend.domain.enclosure.dto.suggestion.ManagementSettingRequest;
import com.likelion.backend.domain.enclosure.dto.suggestion.ManagementSettingResponse;
import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.entity.EnclosureComponent;
import com.likelion.backend.domain.enclosure.entity.EnclosureConcern;
import com.likelion.backend.domain.enclosure.repository.EnclosureComponentRepository;
import com.likelion.backend.domain.enclosure.repository.EnclosureConcernRepository;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.lizard.entity.Lizard;
import com.likelion.backend.domain.lizard.repository.LizardRepository;
import com.likelion.backend.domain.management.entity.ManagementSetting;
import com.likelion.backend.domain.management.repository.ManagementSettingRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EnclosureCommandService {

    private final EnclosureRepository enclosureRepository;
    private final LizardRepository lizardRepository;
    private final ConcernRepository concernRepository;
    private final EnclosureComponentRepository enclosureComponentRepository;
    private final EnclosureConcernRepository enclosureConcernRepository;
    private final ManagementSettingRepository managementSettingRepository;

    public EnclosureCreateResponse createEnclosure(EnclosureCreateRequest request) {
        Lizard lizard = lizardRepository.findByLizardIdAndDeletedAtIsNull(request.getLizardId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LIZARD_NOT_FOUND));

        Enclosure savedEnclosure = enclosureRepository.save(request.toEntity(lizard));

        if (request.getComponents() != null) {
            request.getComponents().forEach(compDto ->
                    enclosureComponentRepository.save(compDto.toEntity(savedEnclosure))
            );
        }

        if (request.getConcernIds() != null) {
            request.getConcernIds().forEach(concernId -> {
                Concern concern = concernRepository.findById(concernId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
                enclosureConcernRepository.save(new EnclosureConcern(savedEnclosure, concern));
            });
        }

        return new EnclosureCreateResponse(savedEnclosure.getId(), lizard.getLizardId());
    }

    public ManagementSettingResponse updateManagementSetting(Long enclosureId, ManagementSettingRequest request) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        ManagementSetting savedSetting = managementSettingRepository.save(request.toEntity(enclosure));
        return new ManagementSettingResponse(savedSetting.getId());
    }

    @Transactional(readOnly = true)
    public EnclosureResponse getEnclosureByLizardId(Long lizardId, Long currentUserId) {
        Lizard lizard = lizardRepository.findById(lizardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIZARD_NOT_FOUND));

        if (!lizard.getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Enclosure enclosure = enclosureRepository.findByLizard_LizardId(lizardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        return EnclosureResponse.from(enclosure);
    }

    public EnclosureUpdateResponse updateEnclosure(Long enclosureId, EnclosureUpdateRequest requestDto, Long currentUserId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        enclosure.update(
                requestDto.getWidth(),
                requestDto.getHeight(),
                requestDto.getDepth(),
                requestDto.getMaterial(),
                requestDto.getVentilation(),
                requestDto.getMaterialOther(),
                requestDto.getWallDesign(),
                requestDto.getVineDesign(),
                requestDto.getFloorDesign(),
                requestDto.getDecorationDesign(),
                requestDto.getConcernDetail()
        );

        enclosureComponentRepository.deleteByEnclosureId(enclosureId);
        if (requestDto.getComponents() != null && !requestDto.getComponents().isEmpty()) {
            List<EnclosureComponent> newComponents = requestDto.getComponents().stream()
                    .map(c -> EnclosureComponent.builder()
                            .enclosure(enclosure)
                            .componentCategory(c.getComponentCategory())
                            .componentName(c.getComponentName())
                            .build())
                    .toList();
            enclosureComponentRepository.saveAll(newComponents);
        }

        enclosureConcernRepository.deleteByEnclosureId(enclosureId);
        List<Long> updatedConcernIds = new ArrayList<>();
        if (requestDto.getConcernIds() != null && !requestDto.getConcernIds().isEmpty()) {
            for (Long concernId : requestDto.getConcernIds()) {
                Concern concern = concernRepository.findById(concernId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

                EnclosureConcern enclosureConcern = new EnclosureConcern(enclosure, concern);
                enclosureConcernRepository.save(enclosureConcern);
                updatedConcernIds.add(concernId);
            }
        }

        return EnclosureUpdateResponse.of(enclosure, updatedConcernIds);
    }
}
