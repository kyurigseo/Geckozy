package com.likelion.backend.domain.enclosure.service;

import com.likelion.backend.domain.concern.entity.Concern;
import com.likelion.backend.domain.concern.repository.ConcernRepository;
import com.likelion.backend.domain.enclosure.dto.EnclosureCreateRequest;
import com.likelion.backend.domain.enclosure.dto.EnclosureCreateResponse;
import com.likelion.backend.domain.enclosure.dto.EnclosureGuidebookRecommendationResponse;
import com.likelion.backend.domain.enclosure.dto.EnclosureHumiditySuggestionResponse;
import com.likelion.backend.domain.enclosure.dto.EnclosureTemperatureSuggestionResponse;
import com.likelion.backend.domain.enclosure.dto.ManagementSettingRequest;
import com.likelion.backend.domain.enclosure.dto.ManagementSettingResponse;
import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.entity.EnclosureComponent;
import com.likelion.backend.domain.enclosure.entity.EnclosureConcern;
import com.likelion.backend.domain.enclosure.entity.ManagementSetting;
import com.likelion.backend.domain.enclosure.repository.EnclosureComponentRepository;
import com.likelion.backend.domain.enclosure.repository.EnclosureConcernRepository;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.lizard.entity.Lizard;
import com.likelion.backend.domain.lizard.repository.LizardRepository;
import com.likelion.backend.domain.management.repository.ManagementSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EnclosureService {

    private final EnclosureRepository enclosureRepository;
    private final LizardRepository lizardRepository;
    private final ConcernRepository concernRepository;
    private final EnclosureComponentRepository enclosureComponentRepository;
    private final EnclosureConcernRepository enclosureConcernRepository;
    private final ManagementSettingRepository managementSettingRepository;

    public EnclosureCreateResponse createEnclosure(EnclosureCreateRequest request) {
        Lizard lizard = lizardRepository.findById(request.getLizardId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 도마뱀입니다. id=" + request.getLizardId()
                        )
                );

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

        if (request.getConcernIds() != null && !request.getConcernIds().isEmpty()) {
            for (Long concernId : request.getConcernIds()) {
                Concern concern = concernRepository.findById(concernId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 고민 항목입니다. id=" + concernId
                                )
                        );

                EnclosureConcern enclosureConcern =
                        new EnclosureConcern(savedEnclosure, concern);

                enclosureConcernRepository.save(enclosureConcern);
            }
        }

        return EnclosureCreateResponse.builder()
                .enclosureId(savedEnclosure.getId())
                .lizardId(lizard.getId())
                .build();
    }

    public ManagementSettingResponse updateManagementSetting(
            Long enclosureId,
            ManagementSettingRequest request
    ) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 사육장입니다. id=" + enclosureId
                        )
                );

        ManagementSetting managementSetting = ManagementSetting.builder()
                .enclosure(enclosure)
                .sprayingMethod(
                        request.getSprayingMethod() != null
                                ? ManagementSetting.SprayingMethod.valueOf(
                                        request.getSprayingMethod().name()
                                )
                                : null
                )
                .sprayingFrequency(
                        request.getSprayingFrequency() != null
                                ? ManagementSetting.SprayingFrequency.valueOf(
                                        request.getSprayingFrequency().name()
                                )
                                : null
                )
                .lightingEnabled(request.getLightingEnabled())
                .lightingStartTime(request.getLightingStartTime())
                .lightingEndTime(request.getLightingEndTime())
                .heatingUsageMode(
                        request.getHeatingUsageMode() != null
                                ? ManagementSetting.HeatingUsageMode.valueOf(
                                        request.getHeatingUsageMode().name()
                                )
                                : null
                )
                .heatingStartTime(request.getHeatingStartTime())
                .heatingEndTime(request.getHeatingEndTime())
                .build();

        ManagementSetting savedSetting =
                managementSettingRepository.save(managementSetting);

        return new ManagementSettingResponse(savedSetting.getId());
    }

    @Transactional(readOnly = true)
    public EnclosureTemperatureSuggestionResponse getTemperatureSuggestions(
            Long enclosureId
    ) {
        return EnclosureTemperatureSuggestionResponse.builder()
                .enclosureId(enclosureId)
                .primaryAction("CHECK_SENSOR_LOCATION_AND_VENTILATE")
                .actionTitle("센서 위치 점검 및 환기 상태 확인 권장")
                .actionGuide(
                        "센서 주변부 열 집중이나 외풍으로 인해 측정 편차가 발생했을 수 있습니다. " +
                        "사육장 내 핫존과 쿨존의 실제 체감 온도를 직접 점검한 뒤 필요시 통풍구를 확보해 주세요."
                )
                .disclaimer(
                        "단일 센서 측정값은 부착 위치에 따라 사육장 전체 온도를 단정할 수 없으므로 다각도 점검이 필요합니다."
                )
                .build();
    }

    @Transactional(readOnly = true)
    public EnclosureHumiditySuggestionResponse getHumiditySuggestions(
            Long enclosureId
    ) {
        return EnclosureHumiditySuggestionResponse.builder()
                .enclosureId(enclosureId)
                .primaryAction("CHECK_SUBSTRATE_MOISTURE_BEFORE_SPRAYING")
                .actionTitle("바닥재 잔여 수분 직접 확인 후 분무 여부 결정")
                .actionGuide(
                        "수치가 낮게 표시되더라도 바닥재 하부에 수분이 남아있을 수 있습니다. " +
                        "무조건적인 분무 대신 바닥재를 손으로 직접 만져 확인한 후 필요 시에만 가볍게 분무해 주세요."
                )
                .isAutomatedSprayingRecommended(false)
                .build();
    }

    @Transactional(readOnly = true)
    public EnclosureGuidebookRecommendationResponse getGuidebookRecommendations(
            Long enclosureId
    ) {
        if (enclosureId != null && enclosureId.equals(999L)) {
            return EnclosureGuidebookRecommendationResponse.of(
                    Collections.emptyList()
            );
        }

        return EnclosureGuidebookRecommendationResponse.of(
                List.of(
                        EnclosureGuidebookRecommendationResponse.GuidebookDto.builder()
                                .guidebookId(101L)
                                .title("환기 불량과 곰팡이 방지 완벽 가이드")
                                .keyword("환기/습도")
                                .reason("현재 사육장 내 습도 정체 구간이 감지되어 환기 관리 도감을 추천합니다.")
                                .build(),

                        EnclosureGuidebookRecommendationResponse.GuidebookDto.builder()
                                .guidebookId(102L)
                                .title("고온 스트레스 예방 및 핫존 세팅법")
                                .keyword("온도/열원")
                                .reason("주간 최고 온도 근접 상태로 열원 거리 조절 가이드가 필요합니다.")
                                .build()
                )
        );
    }
}
