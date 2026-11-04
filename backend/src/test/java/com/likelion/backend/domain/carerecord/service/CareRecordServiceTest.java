package com.likelion.backend.domain.carerecord.service;

import com.likelion.backend.domain.carerecord.dto.response.*;
import com.likelion.backend.domain.carerecord.entity.*;
import com.likelion.backend.domain.carerecord.entity.enums.*;
import com.likelion.backend.domain.carerecord.repository.*;
import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.lizard.repository.LizardRepository;
import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import com.likelion.backend.domain.sensor.repository.SensorMeasurementRepository;
import com.likelion.backend.domain.sensor.repository.SensorRepository;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

import com.likelion.backend.global.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.likelion.backend.domain.carerecord.dto.request.CareRecordCreateRequest;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.likelion.backend.domain.lizard.entity.Lizard;
import com.likelion.backend.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class CareRecordServiceTest {

    @InjectMocks
    private CareRecordService careRecordService;

    @Mock
    private CareRecordRepository careRecordRepository;

    @Mock
    private CareRecordPhotoRepository careRecordPhotoRepository;

    @Mock
    private CareRecordFeedingRepository feedingRepository;

    @Mock
    private CareRecordWeightRepository weightRepository;

    @Mock
    private CareRecordSheddingRepository sheddingRepository;

    @Mock
    private CareRecordStoolRepository stoolRepository;

    @Mock
    private CareRecordCleaningRepository cleaningRepository;

    @Mock
    private CareRecordAbnormalBehaviorRepository abnormalBehaviorRepository;

    @Mock
    private CareRecordExtraRepository extraRepository;

    @Mock
    private LizardRepository lizardRepository;

    @Mock
    private EnclosureRepository enclosureRepository;

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private SensorMeasurementRepository sensorMeasurementRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private void setField(Object target, String fieldName, Object value)
            throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("REC-01. 월별 기록 캘린더 조회 성공")
    void getCalendar() {
        // given
        Long userId = 1L;
        Long lizardId = 1L;

        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));

        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        CareRecord record1 = mock(CareRecord.class);
        CareRecord record2 = mock(CareRecord.class);
        CareRecord record3 = mock(CareRecord.class);

        when(record1.getRecordedAt())
                .thenReturn(LocalDateTime.of(2026, 11, 1, 10, 0));

        when(record2.getRecordedAt())
                .thenReturn(LocalDateTime.of(2026, 11, 1, 15, 0));

        when(record3.getRecordedAt())
                .thenReturn(LocalDateTime.of(2026, 11, 3, 12, 0));

        when(careRecordRepository
                .findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        eq(lizardId),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(record1, record2, record3));

        // when
        CareRecordCalendarResponse response =
                careRecordService.getCalendar(userId, lizardId, 2026, 11);

        // then
        assertEquals(
                List.of(
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 11, 3)
                ),
                response.getDates()
        );

        verify(careRecordRepository).findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                eq(lizardId),
                eq(LocalDateTime.of(2026, 11, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 12, 1, 0, 0))
        );
    }

    @Test
    @DisplayName("REC-02. 날짜별 기록 조회 성공")
    void getRecordsByDate() {
        // given
        Long userId = 1L;
        Long lizardId = 1L;
        LocalDate date = LocalDate.of(2026, 11, 3);

        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));

        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        CareRecord record1 = mock(CareRecord.class);
        CareRecord record2 = mock(CareRecord.class);

        when(record1.getRecordedAt())
                .thenReturn(LocalDateTime.of(2026, 11, 3, 10, 0));

        when(record2.getRecordedAt())
                .thenReturn(LocalDateTime.of(2026, 11, 3, 15, 30));

        when(careRecordRepository
                .findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        eq(lizardId),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(record1, record2));

        // when
        List<CareRecordListResponse> response =
                careRecordService.getRecordsByDate(userId, lizardId, date);

        // then
        assertEquals(2, response.size());

        verify(careRecordRepository)
                .findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        eq(lizardId),
                        eq(LocalDateTime.of(2026, 11, 3, 0, 0)),
                        eq(LocalDateTime.of(2026, 11, 4, 0, 0))
                );
    }

    @Test
    @DisplayName("REC-03. 먹이 급여 기록 생성 성공")
    void createFeedingRecord() throws Exception {
        // given
        Long userId = 1L;
        Long lizardId = 1L;
        Long recordId = 10L;

        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));
        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        CareRecord savedRecord = new CareRecord(
                lizard,
                CareRecordType.FEEDING,
                "귀뚜라미 급여",
                LocalDateTime.of(2026, 11, 3, 10, 0)
        );

        setField(savedRecord, "recordId", recordId);

        when(careRecordRepository.save(any(CareRecord.class)))
                .thenReturn(savedRecord);

        CareRecordCreateRequest request = new CareRecordCreateRequest();

        setField(request, "recordType", CareRecordType.FEEDING);
        setField(request, "recordedAt", LocalDateTime.of(2026, 11, 3, 10, 0));
        setField(request, "memo", "귀뚜라미 급여");
        setField(
                request,
                "typeDetail",
                objectMapper.readTree("""
                {
                    "feedingType": "INSECT",
                    "feedingResponse": "MOSTLY_ATE"
                }
                """)
        );

        assertThat(request.getRecordType()).isEqualTo(CareRecordType.FEEDING);
        assertThat(request.getRecordedAt()).isNotNull();
        assertThat(request.getTypeDetail()).isNotNull();
        // when
        CareRecordCreateResponse response =
                careRecordService.createRecord(userId, lizardId, request);

        // then
        assertThat(response.getRecordId()).isEqualTo(recordId);

        verify(careRecordRepository).save(any(CareRecord.class));
        verify(feedingRepository).save(any(CareRecordFeeding.class));
    }

    @Test
    @DisplayName("REC-04. 체중 기록 생성 성공")
    void createWeightRecord() throws Exception {
        // given
        Long userId = 1L;
        Long lizardId = 1L;
        Long recordId = 11L;

        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));
        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        CareRecord savedRecord = new CareRecord(
                lizard,
                CareRecordType.WEIGHT,
                "체중 측정",
                LocalDateTime.of(2026, 11, 3, 11, 0)
        );

        setField(savedRecord, "recordId", recordId);

        when(careRecordRepository.save(any(CareRecord.class)))
                .thenReturn(savedRecord);

        CareRecordCreateRequest request = new CareRecordCreateRequest();

        setField(request, "recordType", CareRecordType.WEIGHT);
        setField(request, "recordedAt", LocalDateTime.of(2026, 11, 3, 11, 0));
        setField(request, "memo", "체중 측정");

        setField(
                request,
                "typeDetail",
                objectMapper.readTree("""
            {
                "weightG": 45.50
            }
            """)
        );

        // when
        CareRecordCreateResponse response =
                careRecordService.createRecord(userId, lizardId, request);

        // then
        assertThat(response.getRecordId()).isEqualTo(recordId);

        verify(careRecordRepository).save(any(CareRecord.class));
        verify(weightRepository).save(any(CareRecordWeight.class));
    }

    @Test
    @DisplayName("REC-05. 탈피 기록 생성 성공")
    void createSheddingRecord() throws Exception {
        // given
        Long userId = 1L;
        Long lizardId = 1L;
        Long recordId = 12L;

        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));
        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        CareRecord savedRecord = new CareRecord(
                lizard,
                CareRecordType.SHEDDING,
                "탈피 상태 확인",
                LocalDateTime.of(2026, 11, 3, 12, 0)
        );

        setField(savedRecord, "recordId", recordId);

        when(careRecordRepository.save(any(CareRecord.class)))
                .thenReturn(savedRecord);

        CareRecordCreateRequest request = new CareRecordCreateRequest();

        setField(request, "recordType", CareRecordType.SHEDDING);
        setField(request, "recordedAt", LocalDateTime.of(2026, 11, 3, 12, 0));
        setField(request, "memo", "탈피 상태 확인");

        setField(
                request,
                "typeDetail",
                objectMapper.readTree("""
            {
                "sheddingStatus": "PARTIALLY_REMAINED",
                "sheddingRemainingParts": ["TOE", "TAIL"]
            }
            """)
        );

        // when
        CareRecordCreateResponse response =
                careRecordService.createRecord(userId, lizardId, request);

        // then
        assertThat(response.getRecordId()).isEqualTo(recordId);
        ArgumentCaptor<CareRecordShedding> captor =
                ArgumentCaptor.forClass(CareRecordShedding.class);

        verify(sheddingRepository).save(captor.capture());

        CareRecordShedding savedShedding = captor.getValue();

        assertThat(savedShedding.getSheddingStatus())
                .isEqualTo(SheddingStatus.PARTIALLY_REMAINED);

        assertThat(savedShedding.getSheddingRemainingParts())
                .containsExactly("TOE", "TAIL");

        verify(careRecordRepository).save(any(CareRecord.class));
        verify(sheddingRepository).save(any(CareRecordShedding.class));
    }

    @Test
    @DisplayName("REC-06. 배변 기록 생성 성공")
    void createStoolRecord() throws Exception {
        // given
        Long userId = 1L;
        Long lizardId = 1L;
        Long recordId = 13L;

        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));
        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        CareRecord savedRecord = new CareRecord(
                lizard,
                CareRecordType.STOOL,
                "배변 상태 확인",
                LocalDateTime.of(2026, 11, 3, 13, 0)
        );

        setField(savedRecord, "recordId", recordId);

        when(careRecordRepository.save(any(CareRecord.class)))
                .thenReturn(savedRecord);

        CareRecordCreateRequest request = new CareRecordCreateRequest();

        setField(request, "recordType", CareRecordType.STOOL);
        setField(request, "recordedAt", LocalDateTime.of(2026, 11, 3, 13, 0));
        setField(request, "memo", "배변 상태 확인");

        setField(
                request,
                "typeDetail",
                objectMapper.readTree("""
            {
                "stoolStatus": "ABNORMAL",
                "stoolAbnormalities": ["COLOR_CHANGE", "UNUSUAL_SHAPE"]
            }
            """)
        );

        // when
        CareRecordCreateResponse response =
                careRecordService.createRecord(userId, lizardId, request);

        // then
        assertThat(response.getRecordId()).isEqualTo(recordId);
        ArgumentCaptor<CareRecordStool> captor =
                ArgumentCaptor.forClass(CareRecordStool.class);

        verify(stoolRepository).save(captor.capture());

        CareRecordStool savedStool = captor.getValue();

        assertThat(savedStool.getStoolStatus())
                .isEqualTo(StoolStatus.ABNORMAL);

        assertThat(savedStool.getStoolAbnormalities())
                .containsExactly("COLOR_CHANGE", "UNUSUAL_SHAPE");

        verify(careRecordRepository).save(any(CareRecord.class));
        verify(stoolRepository).save(any(CareRecordStool.class));
    }

    @Test
    @DisplayName("REC-07. 청소 기록 생성 성공")
    void createCleaningRecord() throws Exception {
        // given
        Long userId = 1L;
        Long lizardId = 1L;
        Long recordId = 14L;

        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));
        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        CareRecord savedRecord = new CareRecord(
                lizard,
                CareRecordType.CLEANING,
                "사육장 부분 청소",
                LocalDateTime.of(2026, 11, 3, 14, 0)
        );

        setField(savedRecord, "recordId", recordId);

        when(careRecordRepository.save(any(CareRecord.class)))
                .thenReturn(savedRecord);

        CareRecordCreateRequest request = new CareRecordCreateRequest();

        setField(request, "recordType", CareRecordType.CLEANING);
        setField(request, "recordedAt", LocalDateTime.of(2026, 11, 3, 14, 0));
        setField(request, "memo", "사육장 부분 청소");

        setField(
                request,
                "typeDetail",
                objectMapper.readTree("""
            {
                "cleaningScope": "PARTIAL",
                "cleaningTargets": ["WATER_BOWL", "FOOD_BOWL"]
            }
            """)
        );

        // when
        CareRecordCreateResponse response =
                careRecordService.createRecord(userId, lizardId, request);

        // then
        assertThat(response.getRecordId()).isEqualTo(recordId);
        ArgumentCaptor<CareRecordCleaning> captor =
                ArgumentCaptor.forClass(CareRecordCleaning.class);

        verify(cleaningRepository).save(captor.capture());

        CareRecordCleaning savedCleaning = captor.getValue();

        assertThat(savedCleaning.getCleaningScope())
                .isEqualTo(CleaningScope.PARTIAL);

        assertThat(savedCleaning.getCleaningTargets())
                .containsExactly("WATER_BOWL", "FOOD_BOWL");

        verify(careRecordRepository).save(any(CareRecord.class));
        verify(cleaningRepository).save(any(CareRecordCleaning.class));
    }

    @Test
    @DisplayName("REC-08. 이상 행동 기록 생성 성공")
    void createAbnormalBehaviorRecord() throws Exception {
        // given
        Long userId = 1L;
        Long lizardId = 1L;
        Long recordId = 15L;

        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));
        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        CareRecord savedRecord = new CareRecord(
                lizard,
                CareRecordType.ABNORMAL_BEHAVIOR,
                "평소보다 활동량이 감소함",
                LocalDateTime.of(2026, 11, 3, 15, 0)
        );

        setField(savedRecord, "recordId", recordId);

        when(careRecordRepository.save(any(CareRecord.class)))
                .thenReturn(savedRecord);

        CareRecordCreateRequest request = new CareRecordCreateRequest();

        setField(request, "recordType", CareRecordType.ABNORMAL_BEHAVIOR);
        setField(request, "recordedAt", LocalDateTime.of(2026, 11, 3, 15, 0));
        setField(request, "memo", "평소보다 활동량이 감소함");

        setField(
                request,
                "typeDetail",
                objectMapper.readTree("""
            {
                "abnormalBehaviorType": "ACTIVITY_DECREASE",
                "abnormalOnset": "TODAY",
                "abnormalCurrentStatus": "STILL_OBSERVED"
            }
            """)
        );

        // when
        CareRecordCreateResponse response =
                careRecordService.createRecord(userId, lizardId, request);

        // then
        assertThat(response.getRecordId()).isEqualTo(recordId);
        ArgumentCaptor<CareRecordAbnormalBehavior> captor =
                ArgumentCaptor.forClass(CareRecordAbnormalBehavior.class);

        verify(abnormalBehaviorRepository).save(captor.capture());

        CareRecordAbnormalBehavior savedBehavior = captor.getValue();

        assertThat(savedBehavior.getAbnormalBehaviorType())
                .isEqualTo(AbnormalBehaviorType.ACTIVITY_DECREASE);

        assertThat(savedBehavior.getAbnormalOnset())
                .isEqualTo(AbnormalOnset.TODAY);

        assertThat(savedBehavior.getAbnormalCurrentStatus())
                .isEqualTo(AbnormalCurrentStatus.STILL_OBSERVED);

        verify(careRecordRepository).save(any(CareRecord.class));
        verify(abnormalBehaviorRepository).save(any(CareRecordAbnormalBehavior.class));
    }

    @Test
    @DisplayName("REC-09. 기타 기록 생성 성공")
    void createExtraRecord() throws Exception {
        // given
        Long userId = 1L;
        Long lizardId = 1L;
        Long recordId = 16L;

        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));
        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        CareRecord savedRecord = new CareRecord(
                lizard,
                CareRecordType.OTHER,
                "사육장 환경 변경",
                LocalDateTime.of(2026, 11, 3, 16, 0)
        );

        setField(savedRecord, "recordId", recordId);

        when(careRecordRepository.save(any(CareRecord.class)))
                .thenReturn(savedRecord);

        CareRecordCreateRequest request = new CareRecordCreateRequest();

        setField(request, "recordType", CareRecordType.OTHER);
        setField(request, "recordedAt", LocalDateTime.of(2026, 11, 3, 16, 0));
        setField(request, "memo", "사육장 환경 변경");

        setField(
                request,
                "typeDetail",
                objectMapper.readTree("""
            {
                "extraType": "ENCLOSURE_CHANGE"
            }
            """)
        );

        // when
        CareRecordCreateResponse response =
                careRecordService.createRecord(userId, lizardId, request);

        // then
        assertThat(response.getRecordId()).isEqualTo(recordId);
        ArgumentCaptor<CareRecordExtra> captor =
                ArgumentCaptor.forClass(CareRecordExtra.class);

        verify(extraRepository).save(captor.capture());

        CareRecordExtra savedExtra = captor.getValue();

        assertThat(savedExtra.getExtraType())
                .isEqualTo(ExtraType.ENCLOSURE_CHANGE);

        verify(careRecordRepository).save(any(CareRecord.class));
        verify(extraRepository).save(any(CareRecordExtra.class));
    }

    // 소유권 검증을 통과하도록 Mock 설정
    private void givenOwnedLizard(Long userId, Long lizardId) {
        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));
        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        // 저장된 CareRecord를 그대로 반환
        when(careRecordRepository.save(any(CareRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // 테스트용 요청 생성
    private CareRecordCreateRequest createRequest(
            CareRecordType type,
            String detailJson
    ) throws Exception {
        CareRecordCreateRequest request = new CareRecordCreateRequest();

        setField(request, "recordType", type);
        setField(request, "recordedAt", LocalDateTime.of(2026, 11, 3, 10, 0));
        setField(request, "memo", "예외 테스트");

        JsonNode detail = detailJson == null
                ? null
                : objectMapper.readTree(detailJson);

        setField(request, "typeDetail", detail);

        return request;
    }

    // INVALID_REQUEST 예외 검증
    private void assertInvalidCreate(
            CareRecordType type,
            String detailJson
    ) throws Exception {
        Long userId = 1L;
        Long lizardId = 1L;

        givenOwnedLizard(userId, lizardId);

        CareRecordCreateRequest request = createRequest(type, detailJson);

        assertThatThrownBy(() ->
                careRecordService.createRecord(userId, lizardId, request)
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("REC-03. 사용자 지정 먹이 이름 누락 시 예외")
    void createFeedingRecordWithoutCustomName() throws Exception {
        assertInvalidCreate(
                CareRecordType.FEEDING,
                """
                {
                    "feedingType": "CUSTOM",
                    "feedingResponse": "MOSTLY_ATE"
                }
                """
        );

        verify(feedingRepository, never()).save(any(CareRecordFeeding.class));
    }

    @Test
    @DisplayName("REC-03. 잘못된 먹이 유형 입력 시 예외")
    void createFeedingRecordWithInvalidType() throws Exception {
        assertInvalidCreate(
                CareRecordType.FEEDING,
                """
                {
                    "feedingType": "UNKNOWN",
                    "feedingResponse": "MOSTLY_ATE"
                }
                """
        );

        verify(feedingRepository, never()).save(any(CareRecordFeeding.class));
    }

    @Test
    @DisplayName("REC-04. 체중이 0인 경우 예외")
    void createWeightRecordWithZero() throws Exception {
        assertInvalidCreate(
                CareRecordType.WEIGHT,
                """
                {
                    "weightG": 0
                }
                """
        );

        verify(weightRepository, never()).save(any(CareRecordWeight.class));
    }

    @Test
    @DisplayName("REC-04. 체중이 음수인 경우 예외")
    void createWeightRecordWithNegativeValue() throws Exception {
        assertInvalidCreate(
                CareRecordType.WEIGHT,
                """
                {
                    "weightG": -10
                }
                """
        );

        verify(weightRepository, never()).save(any(CareRecordWeight.class));
    }

    @Test
    @DisplayName("REC-04. 체중이 숫자가 아닌 경우 예외")
    void createWeightRecordWithNonNumericValue() throws Exception {
        assertInvalidCreate(
                CareRecordType.WEIGHT,
                """
                {
                    "weightG": "forty"
                }
                """
        );

        verify(weightRepository, never()).save(any(CareRecordWeight.class));
    }

    @Test
    @DisplayName("REC-05. 잘못된 탈피 상태 입력 시 예외")
    void createSheddingRecordWithInvalidStatus() throws Exception {
        assertInvalidCreate(
                CareRecordType.SHEDDING,
                """
                {
                    "sheddingStatus": "UNKNOWN",
                    "sheddingRemainingParts": ["TOE"]
                }
                """
        );

        verify(sheddingRepository, never()).save(any(CareRecordShedding.class));
    }

    @Test
    @DisplayName("REC-06. 잘못된 배변 상태 입력 시 예외")
    void createStoolRecordWithInvalidStatus() throws Exception {
        assertInvalidCreate(
                CareRecordType.STOOL,
                """
                {
                    "stoolStatus": "UNKNOWN",
                    "stoolAbnormalities": []
                }
                """
        );

        verify(stoolRepository, never()).save(any(CareRecordStool.class));
    }

    @Test
    @DisplayName("REC-07. 잘못된 청소 범위 입력 시 예외")
    void createCleaningRecordWithInvalidScope() throws Exception {
        assertInvalidCreate(
                CareRecordType.CLEANING,
                """
                {
                    "cleaningScope": "UNKNOWN",
                    "cleaningTargets": []
                }
                """
        );

        verify(cleaningRepository, never()).save(any(CareRecordCleaning.class));
    }

    @Test
    @DisplayName("REC-08. 잘못된 이상 행동 유형 입력 시 예외")
    void createAbnormalBehaviorRecordWithInvalidType() throws Exception {
        assertInvalidCreate(
                CareRecordType.ABNORMAL_BEHAVIOR,
                """
                {
                    "abnormalBehaviorType": "UNKNOWN",
                    "abnormalOnset": "TODAY",
                    "abnormalCurrentStatus": "STILL_OBSERVED"
                }
                """
        );

        verify(abnormalBehaviorRepository, never())
                .save(any(CareRecordAbnormalBehavior.class));
    }

    @Test
    @DisplayName("REC-09. 잘못된 추가 기록 유형 입력 시 예외")
    void createExtraRecordWithInvalidType() throws Exception {
        assertInvalidCreate(
                CareRecordType.OTHER,
                """
                {
                    "extraType": "UNKNOWN"
                }
                """
        );

        verify(extraRepository, never()).save(any(CareRecordExtra.class));
    }

    @Test
    @DisplayName("공통. 상세 데이터 누락 시 예외")
    void createRecordWithoutTypeDetail() throws Exception {
        assertInvalidCreate(CareRecordType.WEIGHT, null);

        verify(weightRepository, never()).save(any(CareRecordWeight.class));
    }

    @Test
    @DisplayName("공통. 상세 데이터가 JSON 객체가 아닌 경우 예외")
    void createRecordWithNonObjectDetail() throws Exception {
        assertInvalidCreate(CareRecordType.WEIGHT, "\"invalid\"");

        verify(weightRepository, never()).save(any(CareRecordWeight.class));
    }

    @Test
    @DisplayName("공통. 필수 상세 필드 누락 시 예외")
    void createRecordWithoutRequiredDetailField() throws Exception {
        assertInvalidCreate(
                CareRecordType.STOOL,
                """
                {
                    "stoolAbnormalities": []
                }
                """
        );

        verify(stoolRepository, never()).save(any(CareRecordStool.class));
    }

    @Test
    @DisplayName("공통. 상세 문자열 목록에 숫자가 포함된 경우 예외")
    void createRecordWithNonTextListItem() throws Exception {
        assertInvalidCreate(
                CareRecordType.SHEDDING,
                """
                {
                    "sheddingStatus": "COMPLETED",
                    "sheddingRemainingParts": ["TOE", 123]
                }
                """
        );

        verify(sheddingRepository, never()).save(any(CareRecordShedding.class));
    }

    @Test
    @DisplayName("공통. 상세 문자열 목록이 배열이 아닌 경우 예외")
    void createRecordWithNonArrayListField() throws Exception {
        assertInvalidCreate(
                CareRecordType.CLEANING,
                """
                {
                    "cleaningScope": "FULL",
                    "cleaningTargets": "WATER_BOWL"
                }
                """
        );

        verify(cleaningRepository, never()).save(any(CareRecordCleaning.class));
    }

    // 기록 소유권 검증을 통과하도록 Mock 설정
    private CareRecord givenOwnedRecord(
            Long userId, Long recordId
    ) {
        CareRecord record = mock(CareRecord.class);

        when(careRecordRepository.findByRecordIdAndLizard_User_UserId(recordId, userId))
                .thenReturn(Optional.of(record));

        return record;
    }

    // 사육장과 센서가 연결된 상황 설정
    private Sensor givenConnectedSensor(Long lizardId) {
        Enclosure enclosure = mock(Enclosure.class);
        Sensor sensor = mock(Sensor.class);

        when(enclosureRepository.findByLizard_LizardId(lizardId))
                .thenReturn(Optional.of(enclosure));

        when(enclosure.getId()).thenReturn(100L);

        when(sensorRepository.findByEnclosure_Id(100L))
                .thenReturn(Optional.of(sensor));

        when(sensor.getSensorId()).thenReturn(200L);

        return sensor;
    }

    // 테스트용 환경 측정값 생성
    private SensorMeasurement mockMeasurement(
            LocalDateTime measuredAt,
            String temperature,
            String humidity
    ) {
        SensorMeasurement measurement = mock(SensorMeasurement.class);

        when(measurement.getMeasuredAt()).thenReturn(measuredAt);
        when(measurement.getTemperature())
                .thenReturn(new BigDecimal(temperature));
        when(measurement.getHumidity())
                .thenReturn(new BigDecimal(humidity));

        return measurement;
    }

    private SensorMeasurement mockEnvironmentMeasurement(
            LocalDateTime measuredAt,
            String temperature,
            String humidity
    ) {
        SensorMeasurement measurement = mock(SensorMeasurement.class);

        when(measurement.getMeasuredAt()).thenReturn(measuredAt);
        when(measurement.getTemperature())
                .thenReturn(new BigDecimal(temperature));
        when(measurement.getHumidity())
                .thenReturn(new BigDecimal(humidity));

        return measurement;
    }

    @Test
    @DisplayName("REC-12. 사진 첨부 성공")
    void addPhoto() throws Exception {
        // given
        Long userId = 1L;
        Long recordId = 10L;

        CareRecord record = givenOwnedRecord(userId, recordId);

        when(careRecordPhotoRepository.countByCareRecord_RecordId(recordId))
                .thenReturn(0L);

        CareRecordPhoto savedPhoto = mock(CareRecordPhoto.class);

        when(savedPhoto.getPhotoId()).thenReturn(1L);
        when(savedPhoto.getImageUrl()).thenReturn("/uploads/care-records/test.jpg");

        when(careRecordPhotoRepository.saveAndFlush(any(CareRecordPhoto.class)))
                .thenReturn(savedPhoto);

        // 업로드 경로를 테스트용 임시 디렉터리로 변경
        setField(careRecordService, "uploadDir", tempDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "gecko.jpg",
                "image/jpeg",
                "test image".getBytes()
        );

        // when
        CareRecordPhotoResponse response =
                careRecordService.addPhoto(userId, recordId, file);

        // then
        assertThat(response.getPhotoId()).isEqualTo(1L);
        assertThat(response.getImageUrl())
                .startsWith("/uploads/care-records/");

        // 실제 파일이 임시 디렉터리에 저장됐는지 확인
        try (var files = Files.list(tempDir)) {
            assertThat(files.count()).isEqualTo(1);
        }

        verify(careRecordPhotoRepository)
                .saveAndFlush(any(CareRecordPhoto.class));
    }

    @Test
    @DisplayName("REC-13. 기록 메모 수정 성공")
    void updateMemo_success() {
        // given
        Long userId = 1L;
        Long recordId = 10L;
        String updatedMemo = "수정된 메모";

        CareRecord record = givenOwnedRecord(userId, recordId);

        // when
        careRecordService.updateMemo(userId, recordId, updatedMemo);

        // then
        verify(record).updateMemo(updatedMemo);
    }

    @Test
    @DisplayName("REC-14. 기록 시점 환경 정보 조회 성공")
    void getEnvironment() {
        // given
        Long userId = 1L;
        Long recordId = 10L;
        Long lizardId = 1L;

        LocalDateTime recordedAt = LocalDateTime.of(2026, 11, 3, 10, 0);

        CareRecord record = givenOwnedRecord(userId, recordId);

        Lizard lizard = mock(Lizard.class);
        when(record.getLizard()).thenReturn(lizard);
        when(lizard.getLizardId()).thenReturn(lizardId);
        when(record.getRecordedAt()).thenReturn(recordedAt);

        Sensor sensor = givenConnectedSensor(lizardId);

        SensorMeasurement measurement1 = mock(SensorMeasurement.class);
        when(measurement1.getMeasuredAt())
                .thenReturn(recordedAt.minusMinutes(10));

        SensorMeasurement measurement2 = mockEnvironmentMeasurement(
                recordedAt.plusMinutes(5), "26.0", "65.0"
        );

        when(sensorMeasurementRepository
                .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                        eq(sensor.getSensorId()),
                        eq(recordedAt.minusMinutes(30)),
                        eq(recordedAt.plusMinutes(30))
                ))
                .thenReturn(List.of(measurement1, measurement2));

        // when
        EnvironmentResponse response =
                careRecordService.getEnvironment(userId, recordId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTemperature())
                .isEqualByComparingTo("26.0");
        assertThat(response.getHumidity())
                .isEqualByComparingTo("65.0");
        assertThat(response.getMeasuredAt())
                .isEqualTo(recordedAt.plusMinutes(5));
    }

    @Test
    @DisplayName("REC-15. 기록 상세 조회 성공")
    void getRecordDetail() {
        Long userId = 1L;
        Long recordId = 10L;
        Long lizardId = 1L;

        // 1. 소유한 기록 조회
        CareRecord record = givenOwnedRecord(userId, recordId);

        when(record.getRecordId()).thenReturn(recordId);
        when(record.getRecordType()).thenReturn(CareRecordType.WEIGHT);

        // 2. 기록에 연결된 도마뱀 정보
        Lizard lizard = mock(Lizard.class);
        when(record.getLizard()).thenReturn(lizard);
        when(lizard.getLizardId()).thenReturn(lizardId);

        // 3. 체중 상세 정보
        CareRecordWeight weight = mock(CareRecordWeight.class);
        when(weight.getWeightG()).thenReturn(new BigDecimal("45.5"));

        when(weightRepository.findByRecordId(recordId))
                .thenReturn(Optional.of(weight));

        // 4. 첨부 사진 없음
        when(careRecordPhotoRepository
                .findAllByCareRecord_RecordIdOrderByPhotoOrderAsc(recordId))
                .thenReturn(List.of());

        // 5. 연결된 사육장 없음 → 환경 정보 없음
        when(enclosureRepository.findByLizard_LizardId(lizardId))
                .thenReturn(Optional.empty());

        // 실행
        CareRecordDetailResponse response =
                careRecordService.getRecordDetail(userId, recordId);

        // 검증
        assertThat(response).isNotNull();

        verify(weightRepository).findByRecordId(recordId);

        verify(careRecordPhotoRepository)
                .findAllByCareRecord_RecordIdOrderByPhotoOrderAsc(recordId);

        verify(enclosureRepository).findByLizard_LizardId(lizardId);
    }

    @Test
    @DisplayName("REC-16. 환경 그래프 조회 성공")
    void getEnvironmentGraph() {
        Long userId = 1L;
        Long recordId = 10L;
        Long lizardId = 1L;

        LocalDateTime recordedAt =
                LocalDateTime.of(2026, 11, 3, 10, 0);

        // 1. 소유한 기록 조회
        CareRecord record = givenOwnedRecord(userId, recordId);

        when(record.getLizard()).thenReturn(mock(Lizard.class));
        Lizard lizard = record.getLizard();
        when(lizard.getLizardId()).thenReturn(lizardId);
        when(record.getRecordedAt()).thenReturn(recordedAt);

        // 2. 연결된 센서 조회
        Sensor sensor = givenConnectedSensor(lizardId);

        // 3. 측정값 준비
        SensorMeasurement measurement1 = mockEnvironmentMeasurement(
                recordedAt.minusMinutes(10), "25.0", "60.0"
        );

        SensorMeasurement measurement2 = mockEnvironmentMeasurement(
                recordedAt, "26.0", "65.0"
        );

        SensorMeasurement measurement3 = mockEnvironmentMeasurement(
                recordedAt.plusMinutes(10), "27.0", "70.0"
        );

        when(sensorMeasurementRepository
                .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                        eq(sensor.getSensorId()),
                        eq(recordedAt.minusMinutes(30)),
                        eq(recordedAt.plusMinutes(30))
                ))
                .thenReturn(List.of(
                        measurement1,
                        measurement2,
                        measurement3
                ));

        // 실행: 기록 시각 기준 이전 30분, 이후 30분
        List<EnvironmentGraphResponse> response =
                careRecordService.getEnvironmentGraph(
                        userId, recordId, 30, 30
                );

        // 검증
        assertThat(response).hasSize(3);

        verify(sensorMeasurementRepository)
                .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                        sensor.getSensorId(),
                        recordedAt.minusMinutes(30),
                        recordedAt.plusMinutes(30)
                );
    }

    @Test
    @DisplayName("REC-17. 가장 가까운 환경 정보 조회 성공")
    void getNearestEnvironment() {
        Long userId = 1L;
        Long recordId = 10L;
        Long lizardId = 1L;

        LocalDateTime recordedAt =
                LocalDateTime.of(2026, 11, 3, 10, 0);

        // 1. 소유한 기록 조회
        CareRecord record = givenOwnedRecord(userId, recordId);

        Lizard lizard = mock(Lizard.class);
        when(record.getLizard()).thenReturn(lizard);
        when(lizard.getLizardId()).thenReturn(lizardId);
        when(record.getRecordedAt()).thenReturn(recordedAt);

        // 2. 연결된 센서 조회
        givenConnectedSensor(lizardId);

        // 3. 측정값 준비
        SensorMeasurement measurement1 = mock(SensorMeasurement.class);
        when(measurement1.getMeasuredAt())
                .thenReturn(recordedAt.minusMinutes(10));

        SensorMeasurement measurement2 = mockEnvironmentMeasurement(
                recordedAt.plusMinutes(5), "26.0", "65.0"
        );

        when(sensorMeasurementRepository
                .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                        eq(200L),
                        eq(recordedAt.minusMinutes(30)),
                        eq(recordedAt.plusMinutes(30))
                ))
                .thenReturn(List.of(measurement1, measurement2));

        // 실행
        EnvironmentResponse response =
                careRecordService.getNearestEnvironment(userId, recordId);

        // 검증
        assertThat(response).isNotNull();
        assertThat(response.getTemperature())
                .isEqualByComparingTo("26.0");
        assertThat(response.getHumidity())
                .isEqualByComparingTo("65.0");
        assertThat(response.getMeasuredAt())
                .isEqualTo(recordedAt.plusMinutes(5));

        verify(sensorMeasurementRepository)
                .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                        200L,
                        recordedAt.minusMinutes(30),
                        recordedAt.plusMinutes(30)
                );
    }

    // 온도와 습도만 사용하는 측정값 생성
    private SensorMeasurement mockEnvironmentValues(
            String temperature,
            String humidity
    ) {
        SensorMeasurement measurement = mock(SensorMeasurement.class);

        when(measurement.getTemperature())
                .thenReturn(new BigDecimal(temperature));
        when(measurement.getHumidity())
                .thenReturn(new BigDecimal(humidity));

        return measurement;
    }

    @Test
    @DisplayName("REC-18. 환경 정보 요약 조회 성공")
    void getEnvironmentSummary() {
        Long userId = 1L;
        Long recordId = 10L;
        Long lizardId = 1L;

        LocalDateTime recordedAt =
                LocalDateTime.of(2026, 11, 3, 10, 0);

        // 1. 소유한 기록 조회
        CareRecord record = givenOwnedRecord(userId, recordId);

        Lizard lizard = mock(Lizard.class);
        when(record.getLizard()).thenReturn(lizard);
        when(lizard.getLizardId()).thenReturn(lizardId);
        when(record.getRecordedAt()).thenReturn(recordedAt);

        // 2. 연결된 센서 조회
        Sensor sensor = givenConnectedSensor(lizardId);

        // 3. 측정값 준비
        SensorMeasurement measurement1 = mockEnvironmentValues(
                "24.0", "55.0"
        );

        SensorMeasurement measurement2 = mockEnvironmentValues(
                "26.0", "65.0"
        );

        SensorMeasurement measurement3 = mockEnvironmentValues(
                "28.0", "75.0"
        );

        when(sensorMeasurementRepository
                .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                        eq(sensor.getSensorId()),
                        eq(recordedAt.minusMinutes(30)),
                        eq(recordedAt.plusMinutes(30))
                ))
                .thenReturn(List.of(
                        measurement1,
                        measurement2,
                        measurement3
                ));

        // 실행
        EnvironmentSummaryResponse response =
                careRecordService.getEnvironmentSummary(userId, recordId);

        // 검증
        assertThat(response).isNotNull();
        assertThat(response.isDataAvailable()).isTrue();
        assertThat(response.getSummary())
                .contains("24.0", "28.0", "55.0", "75.0");

        verify(sensorMeasurementRepository)
                .findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                        sensor.getSensorId(),
                        recordedAt.minusMinutes(30),
                        recordedAt.plusMinutes(30)
                );
    }

    @Test
    @DisplayName("REC-19. 월별 기록 변화 조회 성공")
    void getMonthlyChanges() {
        // given
        Long userId = 1L;
        Long lizardId = 1L;

        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));

        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        // 이번 달 기록
        List<CareRecord> currentRecords = List.of(
                mockRecordType(CareRecordType.FEEDING),
                mockRecordType(CareRecordType.FEEDING),
                mockRecordType(CareRecordType.FEEDING),
                mockRecordType(CareRecordType.FEEDING),
                mockRecordType(CareRecordType.STOOL),
                mockRecordType(CareRecordType.STOOL)
        );

        // 지난달 기록
        List<CareRecord> previousRecords = List.of(
                mockRecordType(CareRecordType.FEEDING),
                mockRecordType(CareRecordType.WEIGHT),
                mockRecordType(CareRecordType.WEIGHT),
                mockRecordType(CareRecordType.STOOL)
        );

        when(careRecordRepository
                .findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        eq(lizardId),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                ))
                .thenReturn(currentRecords)
                .thenReturn(previousRecords);

        // when
        MonthlyChangesResponse response =
                careRecordService.getMonthlyChanges(
                        userId, lizardId, 2026, 11
                );

        // then
        assertThat(response.getMessage())
                .isEqualTo("지난달과 비교한 기록 변화예요.");

        assertThat(response.getItems()).hasSize(3);

        // FEEDING: 1개 -> 4개, 3개 증가
        assertThat(response.getItems().get(0).getType())
                .isEqualTo("FEEDING");

        assertThat(response.getItems().get(0).getPreviousCount())
                .isEqualTo(1);

        assertThat(response.getItems().get(0).getCurrentCount())
                .isEqualTo(4);

        assertThat(response.getItems().get(0).getChangeValue())
                .isEqualTo(3);

        assertThat(response.getItems().get(0).getChangeStatus())
                .isEqualTo("INCREASED");

        // WEIGHT: 2개 -> 0개, 2개 감소
        assertThat(response.getItems().get(1).getType())
                .isEqualTo("WEIGHT");

        assertThat(response.getItems().get(1).getChangeValue())
                .isEqualTo(-2);

        assertThat(response.getItems().get(1).getChangeStatus())
                .isEqualTo("DECREASED");

        // STOOL: 1개 -> 2개, 1개 증가
        assertThat(response.getItems().get(2).getType())
                .isEqualTo("STOOL");

        assertThat(response.getItems().get(2).getChangeValue())
                .isEqualTo(1);

        assertThat(response.getItems().get(2).getChangeStatus())
                .isEqualTo("INCREASED");

        // 이번 달과 지난달 조회가 각각 한 번씩 실행됐는지 확인
        verify(careRecordRepository, times(2))
                .findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        eq(lizardId),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    // 월별 변화 테스트용 기록 생성
    private CareRecord mockRecordType(CareRecordType type) {
        CareRecord record = mock(CareRecord.class);
        when(record.getRecordType()).thenReturn(type);
        return record;
    }


    @Test
    @DisplayName("공통. 존재하지 않는 도마뱀 접근 시 예외")
    void accessNonexistentLizard() {
        // given
        Long userId = 1L;
        Long lizardId = 999L;

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                careRecordService.getCalendar(userId, lizardId, 2026, 11)
        ).isInstanceOf(BusinessException.class);

        verify(careRecordRepository, never())
                .findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        anyLong(),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    @Test
    @DisplayName("공통. 다른 사용자의 도마뱀 접근 시 예외")
    void accessOtherUsersLizard() {
        // given
        Long userId = 1L;
        Long otherUserId = 2L;
        Long lizardId = 1L;

        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));

        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(otherUserId);

        // when & then
        assertThatThrownBy(() ->
                careRecordService.getCalendar(userId, lizardId, 2026, 11)
        ).isInstanceOf(BusinessException.class);

        verify(careRecordRepository, never())
                .findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        anyLong(),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    @Test
    @DisplayName("공통. 존재하지 않는 기록 조회 시 예외")
    void getNonexistentRecord() {
        // given
        Long userId = 1L;
        Long recordId = 999L;

        when(careRecordRepository
                .findByRecordIdAndLizard_User_UserId(recordId, userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                careRecordService.updateMemo(userId, recordId, "수정 메모")
        ).isInstanceOf(BusinessException.class);

        verify(careRecordRepository, never()).save(any(CareRecord.class));
    }

    @Test
    @DisplayName("REC-12. 빈 사진 파일 업로드 시 예외")
    void addEmptyPhoto() {
        // given
        Long userId = 1L;
        Long recordId = 10L;

        givenOwnedRecord(userId, recordId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // when & then
        assertThatThrownBy(() ->
                careRecordService.addPhoto(userId, recordId, file)
        ).isInstanceOf(BusinessException.class);

        verify(careRecordPhotoRepository, never())
                .saveAndFlush(any(CareRecordPhoto.class));
    }

    @Test
    @DisplayName("REC-12. 지원하지 않는 사진 확장자 업로드 시 예외")
    void addPhotoWithInvalidExtension() {
        // given
        Long userId = 1L;
        Long recordId = 10L;

        givenOwnedRecord(userId, recordId);

        when(careRecordPhotoRepository.countByCareRecord_RecordId(recordId))
                .thenReturn(0L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "gecko.gif",
                "image/gif",
                "test image".getBytes()
        );

        // when & then
        assertThatThrownBy(() ->
                careRecordService.addPhoto(userId, recordId, file)
        ).isInstanceOf(BusinessException.class);

        verify(careRecordPhotoRepository, never())
                .saveAndFlush(any(CareRecordPhoto.class));
    }

    @Test
    @DisplayName("REC-12. 사진 최대 개수 초과 시 예외")
    void addPhotoExceedingMaxCount() {
        // given
        Long userId = 1L;
        Long recordId = 10L;

        givenOwnedRecord(userId, recordId);

        when(careRecordPhotoRepository.countByCareRecord_RecordId(recordId))
                .thenReturn(3L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "gecko.jpg",
                "image/jpeg",
                "test image".getBytes()
        );

        // when & then
        assertThatThrownBy(() ->
                careRecordService.addPhoto(userId, recordId, file)
        ).isInstanceOf(BusinessException.class);

        verify(careRecordPhotoRepository, never())
                .saveAndFlush(any(CareRecordPhoto.class));
    }

    @Test
    @DisplayName("REC-16. 이전 조회 시간이 음수인 경우 예외")
    void getEnvironmentGraphWithNegativeBeforeMinutes() {
        // given
        Long userId = 1L;
        Long recordId = 10L;

        givenOwnedRecord(userId, recordId);

        // when & then
        assertThatThrownBy(() ->
                careRecordService.getEnvironmentGraph(
                        userId, recordId, -1, 30
                )
        ).isInstanceOf(BusinessException.class);

        verifyNoInteractions(enclosureRepository);
        verifyNoInteractions(sensorRepository);
        verifyNoInteractions(sensorMeasurementRepository);
    }

    @Test
    @DisplayName("REC-19. 잘못된 월 입력 시 예외")
    void getMonthlyChangesWithInvalidMonth() {
        // given
        Long userId = 1L;
        Long lizardId = 1L;

        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));

        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        // when & then
        assertThatThrownBy(() ->
                careRecordService.getMonthlyChanges(
                        userId, lizardId, 2026, 13
                )
        ).isInstanceOf(BusinessException.class);

        verify(careRecordRepository, never())
                .findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        anyLong(),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    @Test
    @DisplayName("REC-19. 잘못된 연도 입력 시 예외")
    void getMonthlyChangesWithInvalidYear() {
        // given
        Long userId = 1L;
        Long lizardId = 1L;

        Lizard lizard = mock(Lizard.class);
        User user = mock(User.class);

        when(lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId))
                .thenReturn(Optional.of(lizard));

        when(lizard.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(userId);

        // when & then
        assertThatThrownBy(() ->
                careRecordService.getMonthlyChanges(
                        userId, lizardId, 0, 11
                )
        ).isInstanceOf(BusinessException.class);

        verify(careRecordRepository, never())
                .findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        anyLong(),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }



}