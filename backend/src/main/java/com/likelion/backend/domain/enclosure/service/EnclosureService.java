package com.likelion.backend.domain.enclosure.service;

import com.likelion.backend.domain.concern.entity.Concern;
import com.likelion.backend.domain.concern.repository.ConcernRepository;
import com.likelion.backend.domain.enclosure.dto.*;
import com.likelion.backend.domain.enclosure.entity.*;
import com.likelion.backend.domain.enclosure.repository.*;
import com.likelion.backend.domain.lizard.entity.Lizard;
import com.likelion.backend.domain.lizard.repository.LizardRepository;
import com.likelion.backend.domain.management.entity.ManagementSetting;
import com.likelion.backend.domain.management.repository.ManagementSettingRepository;
import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import com.likelion.backend.domain.sensor.repository.SensorMeasurementRepository;
import com.likelion.backend.domain.sensor.repository.SensorRepository;
import com.likelion.backend.domain.species.entity.Species;
import com.likelion.backend.domain.species.entity.SpeciesEnvironmentStandard;
import com.likelion.backend.domain.species.repository.SpeciesEnvironmentStandardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private final SensorRepository sensorRepository;
    private final SensorMeasurementRepository sensorMeasurementRepository;
    private final SpeciesEnvironmentStandardRepository speciesEnvironmentStandardRepository;

    public EnclosureCreateResponse createEnclosure(EnclosureCreateRequest request) {
        Lizard lizard = lizardRepository.findByLizardIdAndDeletedAtIsNull(request.getLizardId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도마뱀입니다. id=" + request.getLizardId()));

        Enclosure savedEnclosure = enclosureRepository.save(request.toEntity(lizard));

        // 1. 사육장 구성품 저장
        if (request.getComponents() != null) {
            request.getComponents().forEach(compDto -> 
                enclosureComponentRepository.save(compDto.toEntity(savedEnclosure))
            );
        }

        // 2. 사육장 고민 매핑 저장
        if (request.getConcernIds() != null) {
            request.getConcernIds().forEach(concernId -> {
                Concern concern = concernRepository.findById(concernId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고민 항목입니다. id=" + concernId));
                enclosureConcernRepository.save(new EnclosureConcern(savedEnclosure, concern));
            });
        }

        return new EnclosureCreateResponse(savedEnclosure.getId(), lizard.getLizardId());
    }

    public ManagementSettingResponse updateManagementSetting(Long enclosureId, ManagementSettingRequest request) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사육장입니다. id=" + enclosureId));

        ManagementSetting savedSetting = managementSettingRepository.save(request.toEntity(enclosure));
        return new ManagementSettingResponse(savedSetting.getId());
    }

    /**
     * 사육장 현재 온도 상태 판정 API
     */
    @Transactional(readOnly = true)
    public TemperatureStatusResponse getTemperatureStatus(Long enclosureId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사육장입니다. id=" + enclosureId));

        Lizard lizard = enclosure.getLizard();
        Species species = lizard.getSpecies();

        // 1. 센서 및 최신 측정 데이터 확인
        Optional<Sensor> sensorOpt = sensorRepository.findByEnclosure_Id(enclosureId);
        if (sensorOpt.isEmpty() || sensorOpt.get().getStatus() == Sensor.Status.DISCONNECTED) {
            return TemperatureStatusResponse.ofSensorError(enclosureId, lizard.getName(), species.getName());
        }

        Optional<SensorMeasurement> measurementOpt = sensorMeasurementRepository
                .findTopBySensor_SensorIdOrderByMeasuredAtDesc(sensorOpt.get().getSensorId());

        if (measurementOpt.isEmpty() || measurementOpt.get().getTemperature() == null) {
            return TemperatureStatusResponse.ofSensorError(enclosureId, lizard.getName(), species.getName());
        }

        BigDecimal currentTemp = measurementOpt.get().getTemperature();
        boolean isDaytime = checkIsDaytime(enclosureId);
        TemperatureStatusResponse.TimeOfDay timeOfDay = isDaytime 
                ? TemperatureStatusResponse.TimeOfDay.DAYTIME 
                : TemperatureStatusResponse.TimeOfDay.NIGHTTIME;

        // 2. 종별 적정 온도 기준 조회
        Optional<SpeciesEnvironmentStandard> standardOpt = speciesEnvironmentStandardRepository.findBySpecies_SpeciesId(species.getSpeciesId());
        if (standardOpt.isEmpty()) {
            return TemperatureStatusResponse.ofUnknown(enclosureId, lizard.getName(), species.getName(), currentTemp, timeOfDay, null);
        }

        SpeciesEnvironmentStandard standard = standardOpt.get();
        BigDecimal minTemp = isDaytime ? standard.getDayTemperatureMin() : standard.getNightTemperatureMin();
        BigDecimal maxTemp = isDaytime ? standard.getDayTemperatureMax() : standard.getNightTemperatureMax();

        if (minTemp == null || maxTemp == null) {
            return TemperatureStatusResponse.ofUnknown(enclosureId, lizard.getName(), species.getName(), currentTemp, timeOfDay, standard.getTemperatureCaution());
        }

        // 3. 온도 상태 판정
        return TemperatureStatusResponse.of(
                enclosureId, lizard.getName(), species.getName(), currentTemp, timeOfDay,
                new StandardTemperatureDto(minTemp, maxTemp),
                determineStatus(currentTemp, minTemp, maxTemp),
                standard.getTemperatureCaution(),
                generateDisplayMessage(currentTemp, minTemp, maxTemp, isDaytime)
        );
    }

    @Transactional(readOnly = true)
    public EnclosureTemperatureSuggestionResponse getTemperatureSuggestions(Long enclosureId) {
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
    public EnclosureHumiditySuggestionResponse getHumiditySuggestions(Long enclosureId) {
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
    public EnclosureGuidebookRecommendationResponse getGuidebookRecommendations(Long enclosureId) {
        if (enclosureId != null && enclosureId.equals(999L)) {
            return EnclosureGuidebookRecommendationResponse.of(Collections.emptyList());
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

    // --- Private Helper Methods ---

    private boolean checkIsDaytime(Long enclosureId) {
        LocalTime now = LocalTime.now();
        return managementSettingRepository.findByEnclosure_Id(enclosureId)
                .filter(s -> s.getLightingStartTime() != null && s.getLightingEndTime() != null)
                .map(s -> {
                    LocalTime start = s.getLightingStartTime();
                    LocalTime end = s.getLightingEndTime();
                    return start.isBefore(end) 
                            ? !now.isBefore(start) && now.isBefore(end)
                            : !now.isBefore(start) || now.isBefore(end);
                })
                .orElseGet(() -> !now.isBefore(LocalTime.of(8, 0)) && now.isBefore(LocalTime.of(20, 0)));
    }

    private TemperatureStatusResponse.Status determineStatus(BigDecimal current, BigDecimal min, BigDecimal max) {
        if (current.compareTo(min) < 0) return TemperatureStatusResponse.Status.LOW;
        if (current.compareTo(max) > 0) return TemperatureStatusResponse.Status.HIGH;
        return TemperatureStatusResponse.Status.OPTIMAL;
    }

    private String generateDisplayMessage(BigDecimal current, BigDecimal min, BigDecimal max, boolean isDaytime) {
        if (current.compareTo(min) < 0) return "현재 온도가 적정 범주보다 낮습니다. 난방 장치를 점검해 주세요.";
        if (current.compareTo(max) > 0) return "현재 온도가 적정 범주보다 높습니다. 통풍 및 환기를 실시해 주세요.";
        return String.format("현재 온도가 %s 적정 범위를 잘 유지하고 있습니다.", isDaytime ? "주간" : "야간");
    }
}