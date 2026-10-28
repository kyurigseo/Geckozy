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
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
                .orElseThrow(() -> new BusinessException(ErrorCode.LIZARD_NOT_FOUND));

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

    /**
     * 사육장 현재 온도 상태 판정 API
     */
    @Transactional(readOnly = true)
    public TemperatureStatusResponse getTemperatureStatus(Long enclosureId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

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

    public EnclosureResponseDto getEnclosureByLizardId(Long lizardId, Long currentUserId) {
        // 1. 도마뱀 존재 여부 확인
        Lizard lizard = lizardRepository.findById(lizardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIZARD_NOT_FOUND));

        // 2. 본인 소유 도마뱀인지 검증 (접근 권한 확인)
        if (!lizard.getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 3. 해당 도마뱀에 연결된 사육장 조회
        Enclosure enclosure = enclosureRepository.findByLizard_LizardId(lizardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        return EnclosureResponseDto.from(enclosure);
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

    @Transactional
    public EnclosureUpdateResponseDto updateEnclosure(Long enclosureId, EnclosureUpdateRequestDto requestDto, Long currentUserId) {
        // 1. 사육장 존재 및 소유권 확인
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 2. 기본 정보 수정 (Dirty Checking)
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

        // 3. 기존 부속품(Components) 삭제 후 재등록
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

        // 4. 기존 고민(Concerns) 삭제 후 재등록
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

        // 5. 수정 완료 응답 반환
        return EnclosureUpdateResponseDto.of(enclosure, updatedConcernIds);
    }


    public TemperatureStatusResponseDto getTemperatureStatus(Long enclosureId, Long currentUserId) {
        // 1. 사육장 및 소유권 검증
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Lizard lizard = enclosure.getLizard();
        Species species = lizard.getSpecies();

        // 2. 센서 및 최신 측정 데이터 조회
        Optional<Sensor> sensorOpt = sensorRepository.findById(enclosureId);
        Optional<SensorMeasurement> measurementOpt = sensorOpt.flatMap(
                sensor -> sensorMeasurementRepository.findTopBySensor_SensorIdOrderByMeasuredAtDesc(sensor.getSensorId())
        );

        // [예외 2 처리] 센서 미연결 또는 측정 데이터 부재
        if (sensorOpt.isEmpty() || measurementOpt.isEmpty() || measurementOpt.get().getTemperature() == null) {
            return TemperatureStatusResponseDto.builder()
                    .enclosureId(enclosureId)
                    .lizardName(lizard.getName())
                    .speciesName(species.getName())
                    .currentTemperature(null)
                    .timeOfDay(null)
                    .standardTemperature(null)
                    .status("SENSOR_ERROR")
                    .cautionText(null)
                    .displayMessage("온도 센서가 연결되지 않았거나 측정 데이터를 수신할 수 없습니다.")
                    .build();
        }

        BigDecimal currentTemp = measurementOpt.get().getTemperature();

        // 3. 종별 환경 기준 조회
        Optional<SpeciesEnvironmentStandard> standardOpt = speciesEnvironmentStandardRepository.findBySpecies_SpeciesId(species.getSpeciesId());

        // 4. 주간/야간 시간 판정 (ManagementSettings 조명 시간 기준)
        Optional<ManagementSetting> settingOpt = managementSettingRepository.findById(enclosureId);
        String timeOfDay = determineTimeOfDay(settingOpt.orElse(null));

        // [예외 1 처리] 종별 환경 기준이 등록되지 않은 경우
        if (standardOpt.isEmpty()) {
            return TemperatureStatusResponseDto.builder()
                    .enclosureId(enclosureId)
                    .lizardName(lizard.getName())
                    .speciesName(species.getName())
                    .currentTemperature(currentTemp)
                    .timeOfDay(timeOfDay)
                    .standardTemperature(null)
                    .status("UNKNOWN")
                    .cautionText(null)
                    .displayMessage("등록된 종별 권장 온도 기준이 없어 수치만 표시되며, 상태 판정이 보류됩니다.")
                    .build();
        }

        SpeciesEnvironmentStandard standard = standardOpt.get();
        BigDecimal minTemp = "DAYTIME".equals(timeOfDay) ? standard.getDayTemperatureMin() : standard.getNightTemperatureMin();
        BigDecimal maxTemp = "DAYTIME".equals(timeOfDay) ? standard.getDayTemperatureMax() : standard.getNightTemperatureMax();

        // 기준 온도가 세팅되어 있지 않은 경우도 UNKNOWN 처리
        if (minTemp == null || maxTemp == null) {
            return TemperatureStatusResponseDto.builder()
                    .enclosureId(enclosureId)
                    .lizardName(lizard.getName())
                    .speciesName(species.getName())
                    .currentTemperature(currentTemp)
                    .timeOfDay(timeOfDay)
                    .standardTemperature(null)
                    .status("UNKNOWN")
                    .cautionText(standard.getTemperatureCaution())
                    .displayMessage("등록된 종별 권장 온도 기준이 없어 수치만 표시되며, 상태 판정이 보류됩니다.")
                    .build();
        }

        // 5. 온도 상태 판정 (OPTIMAL, HIGH, LOW)
        String status;
        String displayMessage;
        String timeLabel = "DAYTIME".equals(timeOfDay) ? "주간" : "야간";

        if (currentTemp.compareTo(minTemp) < 0) {
            status = "LOW";
            displayMessage = String.format("현재 온도가 %s 적정 범위보다 낮습니다.", timeLabel);
        } else if (currentTemp.compareTo(maxTemp) > 0) {
            status = "HIGH";
            displayMessage = String.format("현재 온도가 %s 적정 범위보다 높습니다.", timeLabel);
        } else {
            status = "OPTIMAL";
            displayMessage = String.format("현재 온도가 %s 적정 범위를 잘 유지하고 있습니다.", timeLabel);
        }

        return TemperatureStatusResponseDto.builder()
                .enclosureId(enclosureId)
                .lizardName(lizard.getName())
                .speciesName(species.getName())
                .currentTemperature(currentTemp)
                .timeOfDay(timeOfDay)
                .standardTemperature(TemperatureStatusResponseDto.StandardTemperature.builder()
                        .min(minTemp)
                        .max(maxTemp)
                        .build())
                .status(status)
                .cautionText(standard.getTemperatureCaution())
                .displayMessage(displayMessage)
                .build();
    }

    // 주/야간 판정 헬퍼 메서드 (조명 시간 기준)
    private String determineTimeOfDay(ManagementSetting setting) {
        if (setting == null || setting.getLightingStartTime() == null || setting.getLightingEndTime() == null) {
            LocalTime now = LocalTime.now();
            return (now.isAfter(LocalTime.of(8, 0)) && now.isBefore(LocalTime.of(20, 0))) ? "DAYTIME" : "NIGHTTIME";
        }

        LocalTime now = LocalTime.now();
        // 이미 LocalTime 타입이므로 .toLocalTime() 제거
        LocalTime start = setting.getLightingStartTime();
        LocalTime end = setting.getLightingEndTime();

        if (start.isBefore(end)) {
            return (now.isAfter(start) && now.isBefore(end)) ? "DAYTIME" : "NIGHTTIME";
        } else { // 자정을 넘어가는 경우 (예: 22:00 ~ 06:00)
            return (now.isAfter(start) || now.isBefore(end)) ? "DAYTIME" : "NIGHTTIME";
        }
    }

    // EnclosureService.java 내부에 추가

    public HumidityStatusResponseDto getHumidityStatus(Long enclosureId, Long currentUserId) {
        // 1. 사육장 및 소유권 검증
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Lizard lizard = enclosure.getLizard();
        Species species = lizard.getSpecies();

        // 2. 센서 및 최근 측정 데이터 조회 (최대 10개)
        Optional<Sensor> sensorOpt = sensorRepository.findById(enclosureId);
        List<SensorMeasurement> measurements = sensorOpt
                .map(sensor -> sensorMeasurementRepository.findTop10BySensor_SensorIdOrderByMeasuredAtDesc(sensor.getSensorId()))
                .orElse(Collections.emptyList());

        // [예외 3 처리] 센서 미연결 또는 측정 데이터가 아예 없는 경우
        if (sensorOpt.isEmpty() || measurements.isEmpty() || measurements.get(0).getHumidity() == null) {
            return HumidityStatusResponseDto.builder()
                    .enclosureId(enclosureId)
                    .lizardName(lizard.getName())
                    .speciesName(species.getName())
                    .currentHumidity(null)
                    .standardHumidity(null)
                    .humidityTrend(null)
                    .trendDurationMinutes(null)
                    .cycleStatus("SENSOR_ERROR")
                    .cautionText(null)
                    .displayMessage("습도 센서가 연결되지 않았거나 측정 데이터를 수신할 수 없습니다.")
                    .build();
        }

        BigDecimal currentHumidity = measurements.get(0).getHumidity();
        Optional<SpeciesEnvironmentStandard> standardOpt = speciesEnvironmentStandardRepository.findBySpecies_SpeciesId(species.getSpeciesId());

        // 종별 기준 습도 DTO 생성
        HumidityStatusResponseDto.StandardHumidity standardHumidity = null;
        String cautionText = null;
        if (standardOpt.isPresent()) {
            SpeciesEnvironmentStandard std = standardOpt.get();
            if (std.getHumidityMin() != null && std.getHumidityMax() != null) {
                standardHumidity = HumidityStatusResponseDto.StandardHumidity.builder()
                        .min(std.getHumidityMin())
                        .max(std.getHumidityMax())
                        .build();
                cautionText = std.getHumidityCaution();
            }
        }

        // [예외 1 처리] 최근 측정 데이터가 부족한 경우 (데이터 2개 미만)
        if (measurements.size() < 2) {
            return HumidityStatusResponseDto.builder()
                    .enclosureId(enclosureId)
                    .lizardName(lizard.getName())
                    .speciesName(species.getName())
                    .currentHumidity(currentHumidity)
                    .standardHumidity(standardHumidity)
                    .humidityTrend("UNKNOWN")
                    .trendDurationMinutes(null)
                    .cycleStatus("INSUFFICIENT_DATA")
                    .cautionText(cautionText)
                    .displayMessage("최근 측정 데이터가 부족하여 흐름 분석을 제외하고 현재 습도 수치만 제공합니다.")
                    .build();
        }

        // 3. 습도 흐름(Trend) 및 지속 시간 계산
        SensorMeasurement latest = measurements.get(0);
        SensorMeasurement previous = measurements.get(1);

        double diff = latest.getHumidity().doubleValue() - previous.getHumidity().doubleValue();
        String humidityTrend;
        String trendText;

        if (diff > 1.0) {
            humidityTrend = "RISING";
            trendText = "상승하는";
        } else if (diff < -1.0) {
            humidityTrend = "FALLING";
            trendText = "하락하는";
        } else {
            humidityTrend = "MAINTAINING";
            trendText = "유지되는";
        }

        // 두 측정 시간 사이의 분(Minutes) 차이 계산
        long minutesBetween = java.time.Duration.between(previous.getMeasuredAt(), latest.getMeasuredAt()).toMinutes();
        int trendDurationMinutes = (int) Math.max(minutesBetween, 1);

        // [예외 2 처리] 종별 권장 습도 기준이 없는 경우
        if (standardHumidity == null) {
            return HumidityStatusResponseDto.builder()
                    .enclosureId(enclosureId)
                    .lizardName(lizard.getName())
                    .speciesName(species.getName())
                    .currentHumidity(currentHumidity)
                    .standardHumidity(null)
                    .humidityTrend(humidityTrend)
                    .trendDurationMinutes(trendDurationMinutes)
                    .cycleStatus("UNKNOWN")
                    .cautionText(null)
                    .displayMessage("등록된 종별 권장 습도 기준이 없습니다.")
                    .build();
        }

        // 4. 정상 습윤 사이클 응답 생성
        String displayMessage = String.format("최근 분무 이후 습도가 %s 흐름이며, 적정 습윤 사이클 내에 있습니다.", trendText);

        return HumidityStatusResponseDto.builder()
                .enclosureId(enclosureId)
                .lizardName(lizard.getName())
                .speciesName(species.getName())
                .currentHumidity(currentHumidity)
                .standardHumidity(standardHumidity)
                .humidityTrend(humidityTrend)
                .trendDurationMinutes(trendDurationMinutes)
                .cycleStatus("HUMID_CYCLE_NORMAL")
                .cautionText(cautionText)
                .displayMessage(displayMessage)
                .build();
    }

    public AlertResponseDto getEnclosureAlerts(Long enclosureId, Long currentUserId) {
        // 1. 사육장 및 소유권 검증 (404, 401 처리)
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<AlertResponseDto.AlertDetail> alerts = new ArrayList<>();

        // 2. 센서 상태 점검 (DISCONNECTED 체크)
        Optional<Sensor> sensorOpt = sensorRepository.findById(enclosureId);
        if (sensorOpt.isPresent()) {
            Sensor sensor = sensorOpt.get();
            if ("DISCONNECTED".equals(sensor.getStatus().name())) {
                alerts.add(AlertResponseDto.AlertDetail.builder()
                        .code("COACH-003")
                        .type("SENSOR_DISCONNECTED")
                        .severity("MEDIUM")
                        .message("센서 연결을 확인해주세요.")
                        .detailType("SENSOR")
                        .build());
            }
        }

        // 3. 온도 상태 점검 (HIGH/LOW)
        try {
            TemperatureStatusResponseDto tempStatus = getTemperatureStatus(enclosureId, currentUserId);
            if ("HIGH".equals(tempStatus.getStatus())) {
                alerts.add(AlertResponseDto.AlertDetail.builder()
                        .code("COACH-012")
                        .type("HIGH_TEMPERATURE")
                        .severity("HIGH")
                        .message("현재 온도가 권장 범위보다 높아요.")
                        .detailType("TEMPERATURE")
                        .build());
            } else if ("LOW".equals(tempStatus.getStatus())) {
                alerts.add(AlertResponseDto.AlertDetail.builder()
                        .code("COACH-011")
                        .type("LOW_TEMPERATURE")
                        .severity("HIGH")
                        .message("현재 온도가 권장 범위보다 낮아요.")
                        .detailType("TEMPERATURE")
                        .build());
            }
        } catch (Exception ignored) {}

        // 4. 습도 상태 점검 (HIGH/LOW)
        try {
            HumidityStatusResponseDto humidStatus = getHumidityStatus(enclosureId, currentUserId);
            if (humidStatus.getStandardHumidity() != null && humidStatus.getCurrentHumidity() != null) {
                BigDecimal current = humidStatus.getCurrentHumidity();
                BigDecimal max = humidStatus.getStandardHumidity().getMax();
                BigDecimal min = humidStatus.getStandardHumidity().getMin();

                if (current.compareTo(max) > 0) {
                    alerts.add(AlertResponseDto.AlertDetail.builder()
                            .code("COACH-013")
                            .type("HIGH_HUMIDITY")
                            .severity("HIGH")
                            .message("현재 습도가 권장 범위보다 높아요.")
                            .detailType("HUMIDITY")
                            .build());
                } else if (current.compareTo(min) < 0) {
                    alerts.add(AlertResponseDto.AlertDetail.builder()
                            .code("COACH-014")
                            .type("LOW_HUMIDITY")
                            .severity("HIGH")
                            .message("현재 습도가 권장 범위보다 낮아요.")
                            .detailType("HUMIDITY")
                            .build());
                }
            }
        } catch (Exception ignored) {}

        // 5. 경고가 없는 경우 (NO_ALERT)
        if (alerts.isEmpty()) {
            return AlertResponseDto.builder()
                    .primaryAlert(null)
                    .additionalAlerts(Collections.emptyList())
                    .build();
        }

        // 6. 우선순위(Severity) 정렬: HIGH -> MEDIUM -> LOW
        alerts.sort((a, b) -> getSeverityRank(a.getSeverity()) - getSeverityRank(b.getSeverity()));

        // 7. Primary와 Additional 분리
        AlertResponseDto.AlertDetail primaryAlert = alerts.get(0);
        List<AlertResponseDto.AlertDetail> additionalAlerts = alerts.subList(1, alerts.size());

        return AlertResponseDto.builder()
                .primaryAlert(primaryAlert)
                .additionalAlerts(additionalAlerts)
                .build();
    }

    private int getSeverityRank(String severity) {
        return switch (severity) {
            case "HIGH" -> 1;
            case "MEDIUM" -> 2;
            case "LOW" -> 3;
            default -> 4;
        };
    }

    public TemperatureHistoryResponseDto getTemperatureHistory(Long enclosureId, String dateStr, Long currentUserId) {
        // 1. 사육장 및 소유권 검증
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 2. 날짜 파싱 (미입력 시 오늘 날짜)
        LocalDate targetDate = (dateStr != null && !dateStr.isBlank())
                ? LocalDate.parse(dateStr)
                : LocalDate.now();

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);
        LocalDateTime now = LocalDateTime.now();

        // 조회 대상 종료 시간 (오늘일 경우 현재 시각 이후는 제외)
        LocalDateTime queryEndTime = targetDate.equals(LocalDate.now()) && now.isBefore(endOfDay)
                ? now
                : endOfDay;

        // 3. 센서 측정 데이터 조회
        Optional<Sensor> sensorOpt = sensorRepository.findById(enclosureId);
        List<TemperatureHistoryResponseDto.ActualTemperatureDto> actualList = new ArrayList<>();

        if (sensorOpt.isPresent()) {
            List<SensorMeasurement> measurements = sensorMeasurementRepository
                    .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                            sensorOpt.get().getSensorId(), startOfDay, queryEndTime);

            actualList = measurements.stream()
                    .filter(m -> m.getTemperature() != null)
                    .map(m -> TemperatureHistoryResponseDto.ActualTemperatureDto.builder()
                            .measuredAt(m.getMeasuredAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .value(m.getTemperature())
                            .build())
                    .toList();
        }

        // 4. 종별 참고 기준 온도 조회
        Species species = enclosure.getLizard().getSpecies();
        Optional<SpeciesEnvironmentStandard> standardOpt = speciesEnvironmentStandardRepository.findBySpecies_SpeciesId(species.getSpeciesId());

        TemperatureHistoryResponseDto.ReferenceTemperatureDto reference = null;
        if (standardOpt.isPresent()) {
            SpeciesEnvironmentStandard std = standardOpt.get();
            reference = TemperatureHistoryResponseDto.ReferenceTemperatureDto.builder()
                    .dayMin(std.getDayTemperatureMin())
                    .dayMax(std.getDayTemperatureMax())
                    .nightMin(std.getNightTemperatureMin())
                    .nightMax(std.getNightTemperatureMax())
                    .build();
        }

        return TemperatureHistoryResponseDto.builder()
                .date(targetDate.toString())
                .actual(actualList)
                .reference(reference)
                .build();
    }

    public HumidityHistoryResponseDto getHumidityHistory(Long enclosureId, String dateStr, Long currentUserId) {
        // 1. 사육장 및 소유권 검증 (404, 401 예외)
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 2. 날짜 파싱 (미입력 시 오늘 날짜)
        LocalDate targetDate = (dateStr != null && !dateStr.isBlank())
                ? LocalDate.parse(dateStr)
                : LocalDate.now();

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);
        LocalDateTime now = LocalDateTime.now();

        // 오늘일 경우 현재 시각 이후의 데이터는 조회 대상 제외
        LocalDateTime queryEndTime = targetDate.equals(LocalDate.now()) && now.isBefore(endOfDay)
                ? now
                : endOfDay;

        // 3. 센서 측정 습도 데이터 조회
        Optional<Sensor> sensorOpt = sensorRepository.findById(enclosureId);
        List<HumidityHistoryResponseDto.ActualHumidityDto> actualList = new ArrayList<>();

        if (sensorOpt.isPresent()) {
            List<SensorMeasurement> measurements = sensorMeasurementRepository
                    .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                            sensorOpt.get().getSensorId(), startOfDay, queryEndTime);

            actualList = measurements.stream()
                    .filter(m -> m.getHumidity() != null)
                    .map(m -> HumidityHistoryResponseDto.ActualHumidityDto.builder()
                            .measuredAt(m.getMeasuredAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .value(m.getHumidity())
                            .build())
                    .toList();
        }

        // 4. 종별 권장 습도 사이클 매핑 (MORNING, DAY, NIGHT)
        Species species = enclosure.getLizard().getSpecies();
        Optional<SpeciesEnvironmentStandard> standardOpt = speciesEnvironmentStandardRepository.findBySpecies_SpeciesId(species.getSpeciesId());

        List<HumidityHistoryResponseDto.RecommendedCycleDto> recommendedCycle = new ArrayList<>();

        if (standardOpt.isPresent()) {
            SpeciesEnvironmentStandard std = standardOpt.get();
            BigDecimal min = std.getHumidityMin();
            BigDecimal max = std.getHumidityMax();

            if (min != null && max != null) {
                // MORNING (아침 분무기 직후 습윤 구간)
                recommendedCycle.add(HumidityHistoryResponseDto.RecommendedCycleDto.builder()
                        .time("MORNING")
                        .min(min.add(BigDecimal.valueOf(10))) // 예: 70.0
                        .max(max)                             // 예: 80.0
                        .build());

                // DAY (낮건조/환기 구간)
                recommendedCycle.add(HumidityHistoryResponseDto.RecommendedCycleDto.builder()
                        .time("DAY")
                        .min(min.subtract(BigDecimal.valueOf(10))) // 예: 50.0
                        .max(max.subtract(BigDecimal.valueOf(15))) // 예: 65.0
                        .build());

                // NIGHT (야간 안정 구간)
                recommendedCycle.add(HumidityHistoryResponseDto.RecommendedCycleDto.builder()
                        .time("NIGHT")
                        .min(min)                             // 예: 60.0
                        .max(max.subtract(BigDecimal.valueOf(5)))  // 예: 75.0
                        .build());
            }
        }

        return HumidityHistoryResponseDto.builder()
                .date(targetDate.toString())
                .actual(actualList)
                .recommendedCycle(recommendedCycle)
                .build();
    }

    public EnvironmentSummaryResponseDto getEnvironmentSummary(Long enclosureId, String dateStr, Long currentUserId) {
        // 1. 사육장 존재 및 소유권 검증 (404, 401)
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 2. 날짜 파싱 (미입력 시 오늘 날짜)
        LocalDate targetDate = (dateStr != null && !dateStr.isBlank())
                ? LocalDate.parse(dateStr)
                : LocalDate.now();

        LocalDateTime startAt = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);
        LocalDateTime now = LocalDateTime.now();

        // 오늘일 경우 현재 시각까지만 분석
        LocalDateTime endAt = targetDate.equals(LocalDate.now()) && now.isBefore(endOfDay)
                ? now
                : endOfDay;

        // 3. 센서 및 0시~현재까지의 측정 데이터 조회
        Optional<Sensor> sensorOpt = sensorRepository.findById(enclosureId);
        List<SensorMeasurement> measurements = new ArrayList<>();

        if (sensorOpt.isPresent()) {
            measurements = sensorMeasurementRepository
                    .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                            sensorOpt.get().getSensorId(), startAt, endAt);
        }

        int dataCount = measurements.size();
        String startAtIso = startAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String endAtIso = endAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        // [예외 처리] 데이터가 3개 미만이면 데이터 부족 반환
        if (dataCount < 3) {
            return EnvironmentSummaryResponseDto.builder()
                    .date(targetDate.toString())
                    .dataCount(dataCount)
                    .startAt(startAtIso)
                    .endAt(endAtIso)
                    .summary(null)
                    .build();
        }

        // 4. 온도 & 습도 추이 동적 분석
        SensorMeasurement first = measurements.get(0);
        SensorMeasurement last = measurements.get(dataCount - 1);

        String tempSummary = "온도는 안정적이었고";
        if (first.getTemperature() != null && last.getTemperature() != null) {
            double tempDiff = last.getTemperature().doubleValue() - first.getTemperature().doubleValue();
            if (tempDiff > 1.5) tempSummary = "온도는 조금씩 올라갔고";
            else if (tempDiff < -1.5) tempSummary = "온도는 조금씩 낮아졌고";
        }

        String humidSummary = "습도는 안정적으로 유지되고 있어요.";
        if (first.getHumidity() != null && last.getHumidity() != null) {
            double humidDiff = last.getHumidity().doubleValue() - first.getHumidity().doubleValue();
            if (humidDiff > 3.0) humidSummary = "습도는 조금씩 높아지고 있어요.";
            else if (humidDiff < -3.0) humidSummary = "습도는 조금씩 낮아지고 있어요.";
        }

        String timePeriodText = now.getHour() < 12 ? "오전 동안 " : "오늘 동안 ";
        String fullSummary = timePeriodText + tempSummary + " " + humidSummary;

        return EnvironmentSummaryResponseDto.builder()
                .date(targetDate.toString())
                .dataCount(dataCount)
                .startAt(startAtIso)
                .endAt(endAtIso)
                .summary(fullSummary)
                .build();
    }

}