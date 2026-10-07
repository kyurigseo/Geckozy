package com.likelion.backend.domain.enclosure.service;

import com.likelion.backend.domain.concern.entity.Concern;
import com.likelion.backend.domain.concern.repository.ConcernRepository;
import com.likelion.backend.domain.enclosure.dto.EnclosureCreateRequest;
import com.likelion.backend.domain.enclosure.dto.EnclosureCreateResponse;
import com.likelion.backend.domain.enclosure.dto.ManagementSettingRequest;
import com.likelion.backend.domain.enclosure.dto.ManagementSettingResponse;
import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.entity.EnclosureComponent;
import com.likelion.backend.domain.enclosure.entity.EnclosureConcern;
import com.likelion.backend.domain.enclosure.entity.ManagementSetting;
import com.likelion.backend.domain.enclosure.repository.EnclosureComponentRepository;
import com.likelion.backend.domain.enclosure.repository.EnclosureConcernRepository;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.management.repository.ManagementSettingRepository;
import com.likelion.backend.domain.lizard.entity.Lizard;
import com.likelion.backend.domain.lizard.repository.LizardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EnclosureService {

    private final EnclosureRepository enclosureRepository;
    private final LizardRepository lizardRepository;
    private final ConcernRepository concernRepository;
    private final EnclosureComponentRepository enclosureComponentRepository;
    private final EnclosureConcernRepository enclosureConcernRepository;
    private final ManagementSettingRepository managementSettingRepository; // 1. 리포지토리 필드 추가

    public EnclosureCreateResponse createEnclosure(EnclosureCreateRequest request) {
        // 1. 도마뱀 존재 여부 확인
        Lizard lizard = lizardRepository.findById(request.getLizardId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도마뱀입니다. id=" + request.getLizardId()));

        // 2. Enclosure 엔티티 생성 및 저장
        Enclosure enclosure = Enclosure.builder()
                .lizard(lizard)
                .width(request.getWidth())
                .height(request.getHeight())
                .depth(request.getDepth())
                .material(request.getMaterial())
                .ventilation(request.getVentilation())
                .materialOther(request.getMaterialOther())
                .wallDesign(request.getWallDesign())
                .vineDesign(request.getVineDesign())
                .floorDesign(request.getFloorDesign())
                .decorationDesign(request.getDecorationDesign())
                .concernDetail(request.getConcernDetail())
                .build();

        Enclosure savedEnclosure = enclosureRepository.save(enclosure);

        // 3. 사육장 구성품 저장 (존재할 경우)
        if (request.getComponents() != null && !request.getComponents().isEmpty()) {
            for (EnclosureCreateRequest.ComponentDto compDto : request.getComponents()) {
                EnclosureComponent component = EnclosureComponent.builder()
                        .enclosure(savedEnclosure)
                        .componentCategory(compDto.getComponentCategory())
                        .componentName(compDto.getComponentName())
                        .build();
                enclosureComponentRepository.save(component);
            }
        }

        // 4. 사육장 고민 매핑 저장 (존재할 경우)
        if (request.getConcernIds() != null && !request.getConcernIds().isEmpty()) {
            for (Long concernId : request.getConcernIds()) {
                Concern concern = concernRepository.findById(concernId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고민 항목입니다. id=" + concernId));
                EnclosureConcern enclosureConcern = new EnclosureConcern(savedEnclosure, concern);
                enclosureConcernRepository.save(enclosureConcern);
            }
        }

        return EnclosureCreateResponse.builder()
                .enclosureId(savedEnclosure.getId())
                .lizardId(lizard.getId())
                .build();
    }

    public ManagementSettingResponse updateManagementSetting(Long enclosureId, ManagementSettingRequest request) {
        // 1. 사육장 존재 여부 확인 (없으면 예외)
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사육장입니다. id=" + enclosureId));

        // 2. Enum 타입 매핑 및 엔티티 생성
        ManagementSetting managementSetting = ManagementSetting.builder()
                .enclosure(enclosure)
                .sprayingMethod(request.getSprayingMethod() != null ? ManagementSetting.SprayingMethod.valueOf(request.getSprayingMethod().name()) : null)
                .sprayingFrequency(request.getSprayingFrequency() != null ? ManagementSetting.SprayingFrequency.valueOf(request.getSprayingFrequency().name()) : null)
                .lightingEnabled(request.getLightingEnabled())
                .lightingStartTime(request.getLightingStartTime())
                .lightingEndTime(request.getLightingEndTime())
                .heatingUsageMode(request.getHeatingUsageMode() != null ? ManagementSetting.HeatingUsageMode.valueOf(request.getHeatingUsageMode().name()) : null)
                .heatingStartTime(request.getHeatingStartTime())
                .heatingEndTime(request.getHeatingEndTime())
                .build();

        ManagementSetting savedSetting = managementSettingRepository.save(managementSetting);

        return new ManagementSettingResponse(savedSetting.getId());
    }
}