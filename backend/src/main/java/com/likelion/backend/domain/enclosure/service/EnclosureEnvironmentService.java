package com.likelion.backend.domain.enclosure.service;

import com.likelion.backend.domain.enclosure.dto.environment.*;
import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.enums.PatternStatus;
import com.likelion.backend.domain.enclosure.enums.TemperatureTrend;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.lizard.entity.Lizard;
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
import java.math.RoundingMode;
import java.time.Duration;
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
@Transactional(readOnly = true)
public class EnclosureEnvironmentService {

    private final EnclosureRepository enclosureRepository;
    private final SensorRepository sensorRepository;
    private final SensorMeasurementRepository sensorMeasurementRepository;
    private final ManagementSettingRepository managementSettingRepository;
    private final SpeciesEnvironmentStandardRepository speciesEnvironmentStandardRepository;

    public TemperatureStatusResponse getTemperatureStatus(Long enclosureId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        Lizard lizard = enclosure.getLizard();
        Species species = lizard.getSpecies();

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

        TemperatureStatusResponse.StandardTemperature standardTemp = TemperatureStatusResponse.StandardTemperature.builder()
                .min(minTemp)
                .max(maxTemp)
                .build();

        return TemperatureStatusResponse.of(
                enclosureId,
                lizard.getName(),
                species.getName(),
                currentTemp,
                timeOfDay,
                standardTemp,
                determineStatus(currentTemp, minTemp, maxTemp),
                standard.getTemperatureCaution(),
                generateDisplayMessage(currentTemp, minTemp, maxTemp, isDaytime)
        );
    }

    public TemperatureStatusResponse getTemperatureStatus(Long enclosureId, Long currentUserId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Lizard lizard = enclosure.getLizard();
        Species species = lizard.getSpecies();

        Optional<Sensor> sensorOpt = sensorRepository.findByEnclosure_Id(enclosureId);
        Optional<SensorMeasurement> measurementOpt = sensorOpt.flatMap(
                sensor -> sensorMeasurementRepository.findTopBySensor_SensorIdOrderByMeasuredAtDesc(sensor.getSensorId())
        );

        if (sensorOpt.isEmpty() || measurementOpt.isEmpty() || measurementOpt.get().getTemperature() == null) {
            return TemperatureStatusResponse.ofSensorError(
                    enclosureId,
                    lizard.getName(),
                    species.getName()
            );
        }

        BigDecimal currentTemp = measurementOpt.get().getTemperature();
        Optional<SpeciesEnvironmentStandard> standardOpt = speciesEnvironmentStandardRepository.findBySpecies_SpeciesId(species.getSpeciesId());

        Optional<ManagementSetting> settingOpt = managementSettingRepository.findById(enclosureId);
        String timeOfDayStr = determineTimeOfDay(settingOpt.orElse(null));
        TemperatureStatusResponse.TimeOfDay timeOfDay = "DAYTIME".equals(timeOfDayStr)
                ? TemperatureStatusResponse.TimeOfDay.DAYTIME
                : TemperatureStatusResponse.TimeOfDay.NIGHTTIME;

        if (standardOpt.isEmpty()) {
            return TemperatureStatusResponse.ofUnknown(
                    enclosureId,
                    lizard.getName(),
                    species.getName(),
                    currentTemp,
                    timeOfDay,
                    null
            );
        }

        SpeciesEnvironmentStandard standard = standardOpt.get();
        BigDecimal minTemp = (timeOfDay == TemperatureStatusResponse.TimeOfDay.DAYTIME)
                ? standard.getDayTemperatureMin()
                : standard.getNightTemperatureMin();
        BigDecimal maxTemp = (timeOfDay == TemperatureStatusResponse.TimeOfDay.DAYTIME)
                ? standard.getDayTemperatureMax()
                : standard.getNightTemperatureMax();

        if (minTemp == null || maxTemp == null) {
            return TemperatureStatusResponse.ofUnknown(
                    enclosureId,
                    lizard.getName(),
                    species.getName(),
                    currentTemp,
                    timeOfDay,
                    standard.getTemperatureCaution()
            );
        }

        TemperatureStatusResponse.Status status;
        String displayMessage;
        String timeLabel = (timeOfDay == TemperatureStatusResponse.TimeOfDay.DAYTIME) ? "주간" : "야간";

        if (currentTemp.compareTo(minTemp) < 0) {
            status = TemperatureStatusResponse.Status.LOW;
            displayMessage = String.format("현재 온도가 %s 적정 범위보다 낮습니다.", timeLabel);
        } else if (currentTemp.compareTo(maxTemp) > 0) {
            status = TemperatureStatusResponse.Status.HIGH;
            displayMessage = String.format("현재 온도가 %s 적정 범위보다 높습니다.", timeLabel);
        } else {
            status = TemperatureStatusResponse.Status.OPTIMAL;
            displayMessage = String.format("현재 온도가 %s 적정 범위를 잘 유지하고 있습니다.", timeLabel);
        }

        TemperatureStatusResponse.StandardTemperature standardTemp = TemperatureStatusResponse.StandardTemperature.builder()
                .min(minTemp)
                .max(maxTemp)
                .build();

        return TemperatureStatusResponse.of(
                enclosureId,
                lizard.getName(),
                species.getName(),
                currentTemp,
                timeOfDay,
                standardTemp,
                status,
                standard.getTemperatureCaution(),
                displayMessage
        );
    }

    public HumidityStatusResponse getHumidityStatus(Long enclosureId, Long currentUserId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Lizard lizard = enclosure.getLizard();
        Species species = lizard.getSpecies();

        Optional<Sensor> sensorOpt = sensorRepository.findById(enclosureId);
        List<SensorMeasurement> measurements = sensorOpt
                .map(sensor -> sensorMeasurementRepository.findTop10BySensor_SensorIdOrderByMeasuredAtDesc(sensor.getSensorId()))
                .orElse(Collections.emptyList());

        if (sensorOpt.isEmpty() || measurements.isEmpty() || measurements.get(0).getHumidity() == null) {
            return HumidityStatusResponse.builder()
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

        HumidityStatusResponse.StandardHumidity standardHumidity = null;
        String cautionText = null;
        if (standardOpt.isPresent()) {
            SpeciesEnvironmentStandard std = standardOpt.get();
            if (std.getHumidityMin() != null && std.getHumidityMax() != null) {
                standardHumidity = HumidityStatusResponse.StandardHumidity.builder()
                        .min(std.getHumidityMin())
                        .max(std.getHumidityMax())
                        .build();
                cautionText = std.getHumidityCaution();
            }
        }

        if (measurements.size() < 2) {
            return HumidityStatusResponse.builder()
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

        long minutesBetween = java.time.Duration.between(previous.getMeasuredAt(), latest.getMeasuredAt()).toMinutes();
        int trendDurationMinutes = (int) Math.max(minutesBetween, 1);

        if (standardHumidity == null) {
            return HumidityStatusResponse.builder()
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

        String displayMessage = String.format("최근 분무 이후 습도가 %s 흐름이며, 적정 습윤 사이클 내에 있습니다.", trendText);

        return HumidityStatusResponse.builder()
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

    public AlertResponse getEnclosureAlerts(Long enclosureId, Long currentUserId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<AlertResponse.AlertDetail> alerts = new ArrayList<>();

        Optional<Sensor> sensorOpt = sensorRepository.findById(enclosureId);
        if (sensorOpt.isPresent()) {
            Sensor sensor = sensorOpt.get();
            if ("DISCONNECTED".equals(sensor.getStatus().name())) {
                alerts.add(AlertResponse.AlertDetail.builder()
                        .code("COACH-003")
                        .type("SENSOR_DISCONNECTED")
                        .severity("MEDIUM")
                        .message("센서 연결을 확인해주세요.")
                        .detailType("SENSOR")
                        .build());
            }
        }

        try {
            TemperatureStatusResponse tempStatus = getTemperatureStatus(enclosureId, currentUserId);
            if ("HIGH".equals(tempStatus.getStatus())) {
                alerts.add(AlertResponse.AlertDetail.builder()
                        .code("COACH-012")
                        .type("HIGH_TEMPERATURE")
                        .severity("HIGH")
                        .message("현재 온도가 권장 범위보다 높아요.")
                        .detailType("TEMPERATURE")
                        .build());
            } else if ("LOW".equals(tempStatus.getStatus())) {
                alerts.add(AlertResponse.AlertDetail.builder()
                        .code("COACH-011")
                        .type("LOW_TEMPERATURE")
                        .severity("HIGH")
                        .message("현재 온도가 권장 범위보다 낮아요.")
                        .detailType("TEMPERATURE")
                        .build());
            }
        } catch (Exception ignored) {
        }

        try {
            HumidityStatusResponse humidStatus = getHumidityStatus(enclosureId, currentUserId);
            if (humidStatus.getStandardHumidity() != null && humidStatus.getCurrentHumidity() != null) {
                BigDecimal current = humidStatus.getCurrentHumidity();
                BigDecimal max = humidStatus.getStandardHumidity().getMax();
                BigDecimal min = humidStatus.getStandardHumidity().getMin();

                if (current.compareTo(max) > 0) {
                    alerts.add(AlertResponse.AlertDetail.builder()
                            .code("COACH-013")
                            .type("HIGH_HUMIDITY")
                            .severity("HIGH")
                            .message("현재 습도가 권장 범위보다 높아요.")
                            .detailType("HUMIDITY")
                            .build());
                } else if (current.compareTo(min) < 0) {
                    alerts.add(AlertResponse.AlertDetail.builder()
                            .code("COACH-014")
                            .type("LOW_HUMIDITY")
                            .severity("HIGH")
                            .message("현재 습도가 권장 범위보다 낮아요.")
                            .detailType("HUMIDITY")
                            .build());
                }
            }
        } catch (Exception ignored) {
        }

        if (alerts.isEmpty()) {
            return AlertResponse.builder()
                    .primaryAlert(null)
                    .additionalAlerts(Collections.emptyList())
                    .build();
        }

        alerts.sort((a, b) -> getSeverityRank(a.getSeverity()) - getSeverityRank(b.getSeverity()));

        AlertResponse.AlertDetail primaryAlert = alerts.get(0);
        List<AlertResponse.AlertDetail> additionalAlerts = alerts.subList(1, alerts.size());

        return AlertResponse.builder()
                .primaryAlert(primaryAlert)
                .additionalAlerts(additionalAlerts)
                .build();
    }

    public TemperatureHistoryResponse getTemperatureHistory(Long enclosureId, String dateStr, Long currentUserId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        LocalDate targetDate = (dateStr != null && !dateStr.isBlank())
                ? LocalDate.parse(dateStr)
                : LocalDate.now();

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime queryEndTime = targetDate.equals(LocalDate.now()) && now.isBefore(endOfDay)
                ? now
                : endOfDay;

        Optional<Sensor> sensorOpt = sensorRepository.findById(enclosureId);
        List<TemperatureHistoryResponse.ActualTemperatureDto> actualList = new ArrayList<>();

        if (sensorOpt.isPresent()) {
            List<SensorMeasurement> measurements = sensorMeasurementRepository
                    .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                            sensorOpt.get().getSensorId(), startOfDay, queryEndTime);

            actualList = measurements.stream()
                    .filter(m -> m.getTemperature() != null)
                    .map(m -> TemperatureHistoryResponse.ActualTemperatureDto.builder()
                            .measuredAt(m.getMeasuredAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .value(m.getTemperature())
                            .build())
                    .toList();
        }

        Species species = enclosure.getLizard().getSpecies();
        Optional<SpeciesEnvironmentStandard> standardOpt = speciesEnvironmentStandardRepository.findBySpecies_SpeciesId(species.getSpeciesId());

        TemperatureHistoryResponse.ReferenceTemperatureDto reference = null;
        if (standardOpt.isPresent()) {
            SpeciesEnvironmentStandard std = standardOpt.get();
            reference = TemperatureHistoryResponse.ReferenceTemperatureDto.builder()
                    .dayMin(std.getDayTemperatureMin())
                    .dayMax(std.getDayTemperatureMax())
                    .nightMin(std.getNightTemperatureMin())
                    .nightMax(std.getNightTemperatureMax())
                    .build();
        }

        return TemperatureHistoryResponse.builder()
                .date(targetDate.toString())
                .actual(actualList)
                .reference(reference)
                .build();
    }

    public HumidityHistoryResponse getHumidityHistory(Long enclosureId, String dateStr, Long currentUserId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        LocalDate targetDate = (dateStr != null && !dateStr.isBlank())
                ? LocalDate.parse(dateStr)
                : LocalDate.now();

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime queryEndTime = targetDate.equals(LocalDate.now()) && now.isBefore(endOfDay)
                ? now
                : endOfDay;

        Optional<Sensor> sensorOpt = sensorRepository.findById(enclosureId);
        List<HumidityHistoryResponse.ActualHumidityDto> actualList = new ArrayList<>();

        if (sensorOpt.isPresent()) {
            List<SensorMeasurement> measurements = sensorMeasurementRepository
                    .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                            sensorOpt.get().getSensorId(), startOfDay, queryEndTime);

            actualList = measurements.stream()
                    .filter(m -> m.getHumidity() != null)
                    .map(m -> HumidityHistoryResponse.ActualHumidityDto.builder()
                            .measuredAt(m.getMeasuredAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .value(m.getHumidity())
                            .build())
                    .toList();
        }

        Species species = enclosure.getLizard().getSpecies();
        Optional<SpeciesEnvironmentStandard> standardOpt = speciesEnvironmentStandardRepository.findBySpecies_SpeciesId(species.getSpeciesId());

        List<HumidityHistoryResponse.RecommendedCycleDto> recommendedCycle = new ArrayList<>();

        if (standardOpt.isPresent()) {
            SpeciesEnvironmentStandard std = standardOpt.get();
            BigDecimal min = std.getHumidityMin();
            BigDecimal max = std.getHumidityMax();

            if (min != null && max != null) {
                recommendedCycle.add(HumidityHistoryResponse.RecommendedCycleDto.builder()
                        .time("MORNING")
                        .min(min.add(BigDecimal.valueOf(10)))
                        .max(max)
                        .build());

                recommendedCycle.add(HumidityHistoryResponse.RecommendedCycleDto.builder()
                        .time("DAY")
                        .min(min.subtract(BigDecimal.valueOf(10)))
                        .max(max.subtract(BigDecimal.valueOf(15)))
                        .build());

                recommendedCycle.add(HumidityHistoryResponse.RecommendedCycleDto.builder()
                        .time("NIGHT")
                        .min(min)
                        .max(max.subtract(BigDecimal.valueOf(5)))
                        .build());
            }
        }

        return HumidityHistoryResponse.builder()
                .date(targetDate.toString())
                .actual(actualList)
                .recommendedCycle(recommendedCycle)
                .build();
    }

    public EnvironmentSummaryResponse getEnvironmentSummary(Long enclosureId, String dateStr, Long currentUserId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        LocalDate targetDate = (dateStr != null && !dateStr.isBlank())
                ? LocalDate.parse(dateStr)
                : LocalDate.now();

        LocalDateTime startAt = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime endAt = targetDate.equals(LocalDate.now()) && now.isBefore(endOfDay)
                ? now
                : endOfDay;

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

        if (dataCount < 3) {
            return EnvironmentSummaryResponse.builder()
                    .date(targetDate.toString())
                    .dataCount(dataCount)
                    .startAt(startAtIso)
                    .endAt(endAtIso)
                    .summary(null)
                    .build();
        }

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

        return EnvironmentSummaryResponse.builder()
                .date(targetDate.toString())
                .dataCount(dataCount)
                .startAt(startAtIso)
                .endAt(endAtIso)
                .summary(fullSummary)
                .build();
    }

    public TemperaturePatternResponse getTemperaturePattern(Long enclosureId) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));

        Sensor sensor = sensorRepository.findByEnclosure_Id(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SENSOR_NOT_FOUND));

        String lizardName = enclosure.getLizard() != null ? enclosure.getLizard().getName() : "도마뱀";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fifteenMinutesAgo = now.minusMinutes(15);

        List<SensorMeasurement> measurements = sensorMeasurementRepository
                .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                        sensor.getSensorId(), fifteenMinutesAgo, now
                );

        if (measurements.size() < 2) {
            BigDecimal currentTemp = measurements.isEmpty() ? null : measurements.get(measurements.size() - 1).getTemperature();
            return TemperaturePatternResponse.builder()
                    .enclosureId(enclosureId)
                    .lizardName(lizardName)
                    .currentTemperature(currentTemp)
                    .temperatureTrend(TemperatureTrend.UNKNOWN)
                    .trendSpeed(null)
                    .patternStatus(PatternStatus.INSUFFICIENT_DATA)
                    .hasNoiseWarning(false)
                    .displayMessage("데이터 수신 공백 구간이 존재하여 온도 변화 패턴 분석이 일시 보류되었습니다.")
                    .build();
        }

        SensorMeasurement latest = measurements.get(measurements.size() - 1);
        SensorMeasurement previous = measurements.get(measurements.size() - 2);
        BigDecimal currentTemp = latest.getTemperature();

        double diff = Math.abs(latest.getTemperature().doubleValue() - previous.getTemperature().doubleValue());
        long secondsBetween = Duration.between(previous.getMeasuredAt(), latest.getMeasuredAt()).getSeconds();

        if (diff >= 5.0 && secondsBetween < 60) {
            return TemperaturePatternResponse.builder()
                    .enclosureId(enclosureId)
                    .lizardName(lizardName)
                    .currentTemperature(currentTemp)
                    .temperatureTrend(TemperatureTrend.PENDING_VERIFICATION)
                    .trendSpeed(null)
                    .patternStatus(PatternStatus.NOISE_SUSPECTED)
                    .hasNoiseWarning(true)
                    .displayMessage("일시적인 온도 튐(이상값)이 감지되어 재측정 데이터를 확인 중입니다.")
                    .build();
        }

        SensorMeasurement oldest = measurements.get(0);
        double totalMinutes = Duration.between(oldest.getMeasuredAt(), latest.getMeasuredAt()).toSeconds() / 60.0;
        double tempDiff = latest.getTemperature().doubleValue() - oldest.getTemperature().doubleValue();

        double rawTrendSpeed = totalMinutes > 0 ? (tempDiff / totalMinutes) : 0.0;
        double trendSpeed = BigDecimal.valueOf(rawTrendSpeed)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();

        TemperatureTrend trend;
        PatternStatus patternStatus;
        String displayMessage;

        if (trendSpeed >= 1.0) {
            trend = TemperatureTrend.RAPID_RISING;
            patternStatus = PatternStatus.SUDDEN_HEAT_SPIKE;
            displayMessage = "최근 15분간 온도가 급격히 상승하고 있습니다. 열원 및 조명을 확인해 주세요.";
        } else if (trendSpeed >= 0.3) {
            trend = TemperatureTrend.GRADUAL_RISING;
            patternStatus = PatternStatus.NORMAL;
            displayMessage = "온도가 완만하게 상승 중입니다.";
        } else if (trendSpeed <= -1.0) {
            trend = TemperatureTrend.RAPID_FALLING;
            patternStatus = PatternStatus.NORMAL;
            displayMessage = "온도가 급격히 하락하고 있습니다. 사육장 보온 상태를 점검해 주세요.";
        } else if (trendSpeed <= -0.3) {
            trend = TemperatureTrend.GRADUAL_FALLING;
            patternStatus = PatternStatus.NORMAL;
            displayMessage = "온도가 완만하게 하락 중입니다.";
        } else {
            trend = TemperatureTrend.STABLE;
            patternStatus = PatternStatus.NORMAL;
            displayMessage = "현재 온도 흐름이 안정적입니다.";
        }

        return TemperaturePatternResponse.builder()
                .enclosureId(enclosureId)
                .lizardName(lizardName)
                .currentTemperature(currentTemp)
                .temperatureTrend(trend)
                .trendSpeed(trendSpeed)
                .patternStatus(patternStatus)
                .hasNoiseWarning(false)
                .displayMessage(displayMessage)
                .build();
    }

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

    private String determineTimeOfDay(ManagementSetting setting) {
        if (setting == null || setting.getLightingStartTime() == null || setting.getLightingEndTime() == null) {
            LocalTime now = LocalTime.now();
            return (now.isAfter(LocalTime.of(8, 0)) && now.isBefore(LocalTime.of(20, 0))) ? "DAYTIME" : "NIGHTTIME";
        }

        LocalTime now = LocalTime.now();
        LocalTime start = setting.getLightingStartTime();
        LocalTime end = setting.getLightingEndTime();

        if (start.isBefore(end)) {
            return (now.isAfter(start) && now.isBefore(end)) ? "DAYTIME" : "NIGHTTIME";
        } else {
            return (now.isAfter(start) || now.isBefore(end)) ? "DAYTIME" : "NIGHTTIME";
        }
    }

    private int getSeverityRank(String severity) {
        return switch (severity) {
            case "HIGH" -> 1;
            case "MEDIUM" -> 2;
            case "LOW" -> 3;
            default -> 4;
        };
    }
}
