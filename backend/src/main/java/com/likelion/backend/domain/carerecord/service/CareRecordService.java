package com.likelion.backend.domain.carerecord.service;

import tools.jackson.databind.JsonNode;
import com.likelion.backend.domain.carerecord.dto.request.CareRecordCreateRequest;
import com.likelion.backend.domain.carerecord.dto.response.*;
import com.likelion.backend.domain.carerecord.entity.*;
import com.likelion.backend.domain.carerecord.entity.enums.*;
import com.likelion.backend.domain.carerecord.repository.*;
import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.lizard.entity.Lizard;
import com.likelion.backend.domain.lizard.repository.LizardRepository;
import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import com.likelion.backend.domain.sensor.repository.SensorMeasurementRepository;
import com.likelion.backend.domain.sensor.repository.SensorRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CareRecordService {

    private final CareRecordRepository careRecordRepository;
    private final CareRecordPhotoRepository careRecordPhotoRepository;

    private final CareRecordFeedingRepository feedingRepository;
    private final CareRecordWeightRepository weightRepository;
    private final CareRecordSheddingRepository sheddingRepository;
    private final CareRecordStoolRepository stoolRepository;
    private final CareRecordCleaningRepository cleaningRepository;
    private final CareRecordAbnormalBehaviorRepository abnormalBehaviorRepository;
    private final CareRecordExtraRepository extraRepository;

    private final LizardRepository lizardRepository;
    private final EnclosureRepository enclosureRepository;
    private final SensorRepository sensorRepository;
    private final SensorMeasurementRepository sensorMeasurementRepository;

    @Value("${file.upload-dir:uploads/care-records}")
    private String uploadDir;

    // -----------------------------
    // REC-01. 월별 기록 캘린더 조회
    // -----------------------------
    public CareRecordCalendarResponse getCalendar(
            Long userId,
            Long lizardId,
            int year,
            int month
    ) {
        validateLizardOwner(userId, lizardId);

        YearMonth yearMonth;

        try {
            yearMonth = YearMonth.of(year, month);
        } catch (DateTimeException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<CareRecord> records =
                careRecordRepository
                        .findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                                lizardId, start, end
                        );

        List<LocalDate> dates = records.stream()
                .map(record -> record.getRecordedAt().toLocalDate())
                .distinct()
                .toList();

        return new CareRecordCalendarResponse(dates);
    }

    // -----------------------------
    // REC-02. 날짜별 기록 조회
    // -----------------------------
    public List<CareRecordListResponse> getRecordsByDate(
            Long userId,
            Long lizardId,
            LocalDate date
    ) {
        validateLizardOwner(userId, lizardId);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<CareRecord> records =
                careRecordRepository
                        .findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                                lizardId, start, end
                        );

        return records.stream()
                .map(CareRecordListResponse::new)
                .toList();
    }

    // -----------------------------
    // REC-03~11. 기록 생성
    // -----------------------------
    @Transactional
    public CareRecordCreateResponse createRecord(
            Long userId,
            Long lizardId,
            CareRecordCreateRequest request
    ) {
        Lizard lizard = validateLizardOwner(userId, lizardId);

        CareRecord careRecord = new CareRecord(
                lizard,
                request.getRecordType(),
                request.getMemo(),
                request.getRecordedAt()
        );

        CareRecord savedRecord = careRecordRepository.save(careRecord);

        saveTypeDetail(savedRecord, request.getTypeDetail());

        return new CareRecordCreateResponse(savedRecord.getRecordId());
    }

    // 기록 유형에 따라 상세 Entity 저장
    private void saveTypeDetail(
            CareRecord careRecord,
            JsonNode detail
    ) {
        CareRecordType type = careRecord.getRecordType();

        if (detail == null || detail.isNull() || !detail.isObject()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        switch (type) {
            case FEEDING -> saveFeeding(careRecord, detail);
            case WEIGHT -> saveWeight(careRecord, detail);
            case SHEDDING -> saveShedding(careRecord, detail);
            case STOOL -> saveStool(careRecord, detail);
            case CLEANING -> saveCleaning(careRecord, detail);
            case ABNORMAL_BEHAVIOR -> saveAbnormalBehavior(careRecord, detail);
            case OTHER -> saveExtra(careRecord, detail);
        }
    }

    private void saveFeeding(CareRecord record, JsonNode detail) {
        FeedingType feedingType =
                enumValue(detail, "feedingType", FeedingType.class);

        FeedingResponse feedingResponse =
                enumValue(detail, "feedingResponse", FeedingResponse.class);

        String customName = textValue(detail, "feedingCustomName");

        if (feedingType == FeedingType.CUSTOM &&
                (customName == null || customName.isBlank())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        feedingRepository.save(
                new CareRecordFeeding(
                        record,
                        feedingType,
                        customName,
                        feedingResponse
                )
        );
    }

    private void saveWeight(CareRecord record, JsonNode detail) {
        BigDecimal weight = decimalValue(detail, "weightG");

        if (weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        weightRepository.save(new CareRecordWeight(record, weight));
    }

    private void saveShedding(CareRecord record, JsonNode detail) {
        SheddingStatus status =
                enumValue(detail, "sheddingStatus", SheddingStatus.class);

        List<String> remainingParts =
                stringListValue(detail, "sheddingRemainingParts");

        sheddingRepository.save(
                new CareRecordShedding(record, status, remainingParts)
        );
    }

    private void saveStool(CareRecord record, JsonNode detail) {
        StoolStatus status =
                enumValue(detail, "stoolStatus", StoolStatus.class);

        List<String> abnormalities =
                stringListValue(detail, "stoolAbnormalities");

        stoolRepository.save(
                new CareRecordStool(record, status, abnormalities)
        );
    }

    private void saveCleaning(CareRecord record, JsonNode detail) {
        CleaningScope scope =
                enumValue(detail, "cleaningScope", CleaningScope.class);

        List<String> targets =
                stringListValue(detail, "cleaningTargets");

        cleaningRepository.save(
                new CareRecordCleaning(record, scope, targets)
        );
    }

    private void saveAbnormalBehavior(CareRecord record, JsonNode detail) {
        AbnormalBehaviorType type =
                enumValue(detail, "abnormalBehaviorType", AbnormalBehaviorType.class);

        AbnormalOnset onset =
                enumValue(detail, "abnormalOnset", AbnormalOnset.class);

        AbnormalCurrentStatus status =
                enumValue(detail, "abnormalCurrentStatus", AbnormalCurrentStatus.class);

        abnormalBehaviorRepository.save(
                new CareRecordAbnormalBehavior(record, type, onset, status)
        );
    }

    private void saveExtra(CareRecord record, JsonNode detail) {
        ExtraType extraType =
                enumValue(detail, "extraType", ExtraType.class);

        extraRepository.save(new CareRecordExtra(record, extraType));
    }

    // -----------------------------
    // REC-12. 사진 첨부
    // -----------------------------
    @Transactional
    public CareRecordPhotoResponse addPhoto(
            Long userId,
            Long recordId,
            MultipartFile file
    ) {
        CareRecord record = validateRecordOwner(userId, recordId);

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PHOTO);
        }

        long photoCount = careRecordPhotoRepository.countByCareRecord_RecordId(recordId);

        if (photoCount >= 3) {
            throw new BusinessException(ErrorCode.PHOTO_LIMIT_EXCEEDED);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getImageExtension(originalFilename);

        String savedFilename = UUID.randomUUID() + extension;

        Path targetPath = null;

        try {
            Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(directory);

            targetPath = directory.resolve(savedFilename).normalize();

            if (!targetPath.startsWith(directory)) {
                throw new BusinessException(ErrorCode.INVALID_PHOTO);
            }

            file.transferTo(targetPath);

            String imageUrl = "/uploads/care-records/" + savedFilename;

            CareRecordPhoto photo = new CareRecordPhoto(
                    record,
                    imageUrl,
                    (int) photoCount + 1
            );

            return new CareRecordPhotoResponse(
                    careRecordPhotoRepository.saveAndFlush(photo)
            );

        } catch (IOException e) {
            deleteFileIfExists(targetPath);
            throw new BusinessException(ErrorCode.PHOTO_UPLOAD_FAILED);

        } catch (RuntimeException e) {
            deleteFileIfExists(targetPath);
            throw e;
        }
    }

    private void deleteFileIfExists(Path path) {
        if (path == null) {
            return;
        }

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // 파일 삭제 실패는 별도 로그로 남기는 것을 권장
        }
    }

    private String getImageExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_PHOTO);
        }

        String extension = filename.substring(filename.lastIndexOf("."))
                .toLowerCase();

        if (!List.of(".jpg", ".jpeg", ".png", ".webp").contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_PHOTO);
        }

        return extension;
    }

    // -----------------------------
    // REC-13. 메모 수정
    // -----------------------------
    @Transactional
    public void updateMemo(
            Long userId,
            Long recordId,
            String memo
    ) {
        CareRecord record = validateRecordOwner(userId, recordId);
        record.updateMemo(memo);
    }

    // -----------------------------
    // 공통 소유권 검증
    // -----------------------------
    private Lizard validateLizardOwner(Long userId, Long lizardId) {
        Lizard lizard = lizardRepository
                .findByLizardIdAndDeletedAtIsNull(lizardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIZARD_NOT_FOUND));

        if (!lizard.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return lizard;
    }

    private CareRecord validateRecordOwner(Long userId, Long recordId) {
        return careRecordRepository
                .findByRecordIdAndLizard_User_UserId(recordId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARE_RECORD_NOT_FOUND));
    }

    // -----------------------------
    // JSON 필드 변환 도우미
    // -----------------------------
    private String textValue(JsonNode detail, String fieldName) {
        JsonNode value = detail.get(fieldName);

        if (value == null || value.isNull()) {
            return null;
        }

        return value.asText();
    }

    private BigDecimal decimalValue(JsonNode detail, String fieldName) {
        JsonNode value = detail.get(fieldName);

        if (value == null || !value.isNumber()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        return value.decimalValue();
    }

    private <T extends Enum<T>> T enumValue(
            JsonNode detail,
            String fieldName,
            Class<T> enumClass
    ) {
        String value = textValue(detail, fieldName);

        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private List<String> stringListValue(JsonNode detail, String fieldName) {
        JsonNode value = detail.get(fieldName);

        if (value == null || value.isNull()) {
            return new ArrayList<>();
        }

        if (!value.isArray()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        List<String> result = new ArrayList<>();

        value.forEach(item -> {
            if (!item.isTextual()) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST);
            }
            result.add(item.asText());
        });

        return result;
    }

    // -----------------------------
    // REC-14. 기록 시점 환경 정보 조회
    // -----------------------------
    public EnvironmentResponse getEnvironment(
            Long userId,
            Long recordId
    ) {
        CareRecord record = validateRecordOwner(userId, recordId);

        SensorMeasurement measurement = findNearestMeasurement(record);

        if (measurement == null) {
            return null;
        }

        return new EnvironmentResponse(measurement);
    }

    // 기록 시각 기준 ±30분 이내에서 가장 가까운 측정값 조회
    private SensorMeasurement findNearestMeasurement(CareRecord record) {

        // 해당 도마뱀의 사육장 조회
        Enclosure enclosure = enclosureRepository
                .findByLizard_LizardId(record.getLizard().getLizardId())
                .orElse(null);

        if (enclosure == null) {
            return null;
        }

        // 사육장에 연결된 센서 조회
        Sensor sensor = sensorRepository
                .findByEnclosure_Id(enclosure.getId())
                .orElse(null);

        if (sensor == null) {
            return null;
        }

        LocalDateTime recordedAt = record.getRecordedAt();

        LocalDateTime start = recordedAt.minusMinutes(30);
        LocalDateTime end = recordedAt.plusMinutes(30);

        // ±30분 범위의 측정값 조회
        List<SensorMeasurement> measurements =
                sensorMeasurementRepository
                        .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                                sensor.getSensorId(),
                                start,
                                end
                        );

        if (measurements.isEmpty()) {
            return null;
        }

        // 기록 시각과 시간 차이가 가장 작은 측정값 선택
        return measurements.stream()
                .min(Comparator.comparingLong(measurement ->
                        Math.abs(Duration.between(
                                recordedAt,
                                measurement.getMeasuredAt()
                        ).toMillis())
                ))
                .orElse(null);
    }

    // -----------------------------
// REC-15. 기록 상세 조회
// -----------------------------
    public CareRecordDetailResponse getRecordDetail(
            Long userId,
            Long recordId
    ) {
        CareRecord record = validateRecordOwner(userId, recordId);

        // 기록 유형별 상세 정보
        Map<String, Object> typeDetail = getTypeDetail(record);

        // 첨부 사진
        List<CareRecordPhotoResponse> photos =
                careRecordPhotoRepository
                        .findAllByCareRecord_RecordIdOrderByPhotoOrderAsc(recordId)
                        .stream()
                        .map(CareRecordPhotoResponse::new)
                        .toList();

        // 기록 시점 환경 정보
        EnvironmentResponse environment =
                getEnvironment(userId, recordId);

        return new CareRecordDetailResponse(
                record,
                typeDetail,
                photos,
                environment
        );
    }

    // 기록 유형별 상세 정보 조회
    private Map<String, Object> getTypeDetail(CareRecord record) {

        Long recordId = record.getRecordId();

        Map<String, Object> detail = new LinkedHashMap<>();

        switch (record.getRecordType()) {

            case FEEDING -> feedingRepository.findByRecordId(recordId)
                    .ifPresent(feeding -> {
                        detail.put("feedingType", feeding.getFeedingType());
                        detail.put("feedingCustomName", feeding.getFeedingCustomName());
                        detail.put("feedingResponse", feeding.getFeedingResponse());
                    });

            case WEIGHT -> weightRepository.findByRecordId(recordId)
                    .ifPresent(weight ->
                            detail.put("weightG", weight.getWeightG())
                    );

            case SHEDDING -> sheddingRepository.findByRecordId(recordId)
                    .ifPresent(shedding -> {
                        detail.put("sheddingStatus", shedding.getSheddingStatus());
                        detail.put("sheddingRemainingParts",
                                shedding.getSheddingRemainingParts());
                    });

            case STOOL -> stoolRepository.findByRecordId(recordId)
                    .ifPresent(stool -> {
                        detail.put("stoolStatus", stool.getStoolStatus());
                        detail.put("stoolAbnormalities", stool.getStoolAbnormalities());
                    });

            case CLEANING -> cleaningRepository.findByRecordId(recordId)
                    .ifPresent(cleaning -> {
                        detail.put("cleaningScope", cleaning.getCleaningScope());
                        detail.put("cleaningTargets", cleaning.getCleaningTargets());
                    });

            case ABNORMAL_BEHAVIOR ->
                    abnormalBehaviorRepository.findByRecordId(recordId)
                            .ifPresent(behavior -> {
                                detail.put("abnormalBehaviorType",
                                        behavior.getAbnormalBehaviorType());
                                detail.put("abnormalOnset",
                                        behavior.getAbnormalOnset());
                                detail.put("abnormalCurrentStatus",
                                        behavior.getAbnormalCurrentStatus());
                            });

            case OTHER -> extraRepository.findByRecordId(recordId)
                    .ifPresent(extra ->
                            detail.put("extraType", extra.getExtraType())
                    );
        }

        return detail;
    }

    // -----------------------------
    // REC-16. 환경 그래프 조회
    // -----------------------------
    public List<EnvironmentGraphResponse> getEnvironmentGraph(
            Long userId,
            Long recordId,
            int beforeMinutes,
            int afterMinutes
    ) {
        CareRecord record = validateRecordOwner(userId, recordId);

        if (beforeMinutes < 0 || afterMinutes < 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        Enclosure enclosure = enclosureRepository
                .findByLizard_LizardId(record.getLizard().getLizardId())
                .orElse(null);

        if (enclosure == null) {
            return List.of();
        }

        Sensor sensor = sensorRepository
                .findByEnclosure_Id(enclosure.getId())
                .orElse(null);

        if (sensor == null) {
            return List.of();
        }

        LocalDateTime recordedAt = record.getRecordedAt();

        LocalDateTime start = recordedAt.minusMinutes(beforeMinutes);
        LocalDateTime end = recordedAt.plusMinutes(afterMinutes);

        List<SensorMeasurement> measurements =
                sensorMeasurementRepository
                        .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                                sensor.getSensorId(),
                                start,
                                end
                        );

        return measurements.stream()
                .map(EnvironmentGraphResponse::new)
                .toList();
    }

    // -----------------------------
    // REC-17. 가장 가까운 환경 정보 조회
    // -----------------------------
    public EnvironmentResponse getNearestEnvironment(
            Long userId,
            Long recordId
    ) {
        CareRecord record = validateRecordOwner(userId, recordId);

        SensorMeasurement measurement = findNearestMeasurement(record);

        if (measurement == null) {
            return null;
        }

        return new EnvironmentResponse(measurement);
    }

    // -----------------------------
    // REC-18. 환경 정보 요약
    // -----------------------------
    public EnvironmentSummaryResponse getEnvironmentSummary(
            Long userId,
            Long recordId
    ) {
        CareRecord record = validateRecordOwner(userId, recordId);

        Enclosure enclosure = enclosureRepository
                .findByLizard_LizardId(record.getLizard().getLizardId())
                .orElse(null);

        if (enclosure == null) {
            return new EnvironmentSummaryResponse(
                    false,
                    "연결된 사육장이 없어 환경 정보를 확인할 수 없습니다."
            );
        }

        Sensor sensor = sensorRepository
                .findByEnclosure_Id(enclosure.getId())
                .orElse(null);

        if (sensor == null) {
            return new EnvironmentSummaryResponse(
                    false,
                    "연결된 센서가 없어 환경 정보를 확인할 수 없습니다."
            );
        }

        LocalDateTime recordedAt = record.getRecordedAt();

        List<SensorMeasurement> measurements =
                sensorMeasurementRepository
                        .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                                sensor.getSensorId(),
                                recordedAt.minusMinutes(30),
                                recordedAt.plusMinutes(30)
                        );

        if (measurements.isEmpty()) {
            return new EnvironmentSummaryResponse(
                    false,
                    "기록 시점 주변의 환경 측정 데이터가 없습니다."
            );
        }

        // 온도와 습도 최저·최고값 계산
        BigDecimal minTemperature = measurements.stream()
                .map(SensorMeasurement::getTemperature)
                .filter(value -> value != null)
                .min(BigDecimal::compareTo)
                .orElse(null);

        BigDecimal maxTemperature = measurements.stream()
                .map(SensorMeasurement::getTemperature)
                .filter(value -> value != null)
                .max(BigDecimal::compareTo)
                .orElse(null);

        BigDecimal minHumidity = measurements.stream()
                .map(SensorMeasurement::getHumidity)
                .filter(value -> value != null)
                .min(BigDecimal::compareTo)
                .orElse(null);

        BigDecimal maxHumidity = measurements.stream()
                .map(SensorMeasurement::getHumidity)
                .filter(value -> value != null)
                .max(BigDecimal::compareTo)
                .orElse(null);

        StringBuilder summary = new StringBuilder();

        if (minTemperature != null && maxTemperature != null) {
            summary.append("온도는 ")
                    .append(minTemperature)
                    .append("~")
                    .append(maxTemperature)
                    .append("℃ 범위였습니다. ");
        }

        if (minHumidity != null && maxHumidity != null) {
            summary.append("습도는 ")
                    .append(minHumidity)
                    .append("~")
                    .append(maxHumidity)
                    .append("% 범위였습니다.");
        }

        if (summary.isEmpty()) {
            return new EnvironmentSummaryResponse(
                    false,
                    "환경 측정값이 없어 요약할 수 없습니다."
            );
        }

        return new EnvironmentSummaryResponse(
                true,
                summary.toString().trim()
        );
    }

    // -----------------------------
    // REC-19. 월별 기록 변화 조회
    // -----------------------------
    public MonthlyChangesResponse getMonthlyChanges(
            Long userId,
            Long lizardId,
            int year,
            int month
    ) {
        validateLizardOwner(userId, lizardId);

        // 조회할 월 검증
        YearMonth currentMonth;

        try {
            currentMonth = YearMonth.of(year, month);
        } catch (DateTimeException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        YearMonth previousMonth = currentMonth.minusMonths(1);

        if (year < 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 이번 달 조회 범위
        LocalDateTime currentStart =
                currentMonth.atDay(1).atStartOfDay();

        LocalDateTime currentEnd =
                currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        // 지난달 조회 범위
        LocalDateTime previousStart =
                previousMonth.atDay(1).atStartOfDay();

        LocalDateTime previousEnd = currentStart;

        // 이번 달 기록 조회
        List<CareRecord> currentRecords =
                careRecordRepository
                        .findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                                lizardId,
                                currentStart,
                                currentEnd
                        );

        // 지난달 기록 조회
        List<CareRecord> previousRecords =
                careRecordRepository
                        .findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                                lizardId,
                                previousStart,
                                previousEnd
                        );

        // 기록 유형별 개수 집계
        Map<CareRecordType, Integer> currentCounts =
                new EnumMap<>(CareRecordType.class);

        Map<CareRecordType, Integer> previousCounts =
                new EnumMap<>(CareRecordType.class);

        // 모든 기록 유형의 초기값을 0으로 설정
        for (CareRecordType type : CareRecordType.values()) {
            currentCounts.put(type, 0);
            previousCounts.put(type, 0);
        }

        // 이번 달 기록 집계
        for (CareRecord record : currentRecords) {
            CareRecordType type = record.getRecordType();

            currentCounts.put(
                    type,
                    currentCounts.get(type) + 1
            );
        }

        // 지난달 기록 집계
        for (CareRecord record : previousRecords) {
            CareRecordType type = record.getRecordType();

            previousCounts.put(
                    type,
                    previousCounts.get(type) + 1
            );
        }

        // 유형별 변화량 계산
        List<MonthlyChangeItemResponse> items =
                new ArrayList<>();

        for (CareRecordType type : CareRecordType.values()) {

            int currentCount = currentCounts.get(type);
            int previousCount = previousCounts.get(type);

            int changeValue = currentCount - previousCount;

            // 변화가 없는 유형은 제외
            if (changeValue == 0) {
                continue;
            }

            items.add(
                    new MonthlyChangeItemResponse(
                            type.name(),
                            previousCount,
                            currentCount
                    )
            );
        }

        // 변화량 절댓값이 큰 순서로 정렬
        items.sort(
                Comparator.comparingInt(
                        (MonthlyChangeItemResponse item) ->
                                Math.abs(item.getChangeValue())
                ).reversed()
        );

        // 최대 3개만 반환
        List<MonthlyChangeItemResponse> result =
                items.stream()
                        .limit(3)
                        .toList();

        String message = result.isEmpty()
                ? "비교 가능한 기록이 없어요."
                : "지난달과 비교한 기록 변화예요.";

        return new MonthlyChangesResponse(result, message);
    }
}