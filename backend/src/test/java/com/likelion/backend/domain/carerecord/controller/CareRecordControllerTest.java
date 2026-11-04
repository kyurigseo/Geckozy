package com.likelion.backend.domain.carerecord.controller;

import com.likelion.backend.domain.auth.jwt.JwtProvider;
import com.likelion.backend.domain.carerecord.dto.response.*;
import com.likelion.backend.domain.carerecord.service.CareRecordService;
import com.likelion.backend.domain.lizard.entity.Lizard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.likelion.backend.domain.carerecord.dto.request.CareRecordCreateRequest;
import com.likelion.backend.domain.carerecord.entity.CareRecordPhoto;
import com.likelion.backend.domain.sensor.entity.SensorMeasurement;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Map;
import com.likelion.backend.domain.carerecord.entity.CareRecord;
import com.likelion.backend.domain.carerecord.entity.enums.CareRecordType;

import java.time.LocalDateTime;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(CareRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CareRecordControllerTest.TestSecurityConfig.class)
class CareRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CareRecordService careRecordService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    private final Long userId = 1L;
    private final Long lizardId = 1L;

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
    }

    // 각 테스트 실행 전 인증 정보 설정
    @BeforeEach
    void setUpSecurityContext() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of()
                )
        );

        SecurityContextHolder.setContext(context);
    }

    // 각 테스트 실행 후 인증 정보 제거
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("REC-01. 월별 기록 캘린더 조회")
    void getCalendar() throws Exception {
        CareRecordCalendarResponse response =
                new CareRecordCalendarResponse(
                        List.of(
                                LocalDate.of(2026, 11, 3),
                                LocalDate.of(2026, 11, 5)
                        )
                );

        when(careRecordService.getCalendar(userId, lizardId, 2026, 11))
                .thenReturn(response);

        mockMvc.perform(
                        get("/lizards/{lizardId}/care-records/calendar", lizardId)
                                .param("year", "2026")
                                .param("month", "11")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.dates").isArray())
                .andExpect(jsonPath("$.data.dates.length()").value(2))
                .andExpect(jsonPath("$.data.dates[0]").value("2026-11-03"))
                .andExpect(jsonPath("$.data.dates[1]").value("2026-11-05"));

        verify(careRecordService)
                .getCalendar(userId, lizardId, 2026, 11);
    }

    @Test
    @DisplayName("REC-02. 날짜별 기록 조회")
    void getRecordsByDate() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 3);

        // 테스트용 CareRecord Mock 객체
        CareRecord record = mock(CareRecord.class);

        // Mock 객체의 getter 반환값 설정
        when(record.getRecordId()).thenReturn(1L);
        when(record.getRecordType()).thenReturn(CareRecordType.FEEDING);
        when(record.getMemo()).thenReturn("먹이 급여");
        when(record.getRecordedAt())
                .thenReturn(LocalDateTime.of(2026, 11, 3, 10, 0));

        // DTO를 먼저 생성
        CareRecordListResponse recordResponse =
                new CareRecordListResponse(record);

        List<CareRecordListResponse> response = List.of(recordResponse);

        // 서비스 반환값 설정
        when(careRecordService.getRecordsByDate(userId, lizardId, date))
                .thenReturn(response);

        mockMvc.perform(
                        get("/lizards/{lizardId}/care-records", lizardId)
                                .param("date", "2026-11-03")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].recordId").value(1))
                .andExpect(jsonPath("$.data[0].recordType").value("FEEDING"))
                .andExpect(jsonPath("$.data[0].memo").value("먹이 급여"))
                .andExpect(jsonPath("$.data[0].recordedAt")
                        .value("2026-11-03T10:00:00"));

        verify(careRecordService)
                .getRecordsByDate(userId, lizardId, date);
    }

    @Test
    @DisplayName("REC-03~11. 기록 생성")
    void createRecord() throws Exception {

        CareRecordCreateResponse response =
                new CareRecordCreateResponse(1L);

        when(careRecordService.createRecord(
                eq(userId),
                eq(lizardId),
                any(CareRecordCreateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/lizards/{lizardId}/care-records", lizardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "recordType": "FEEDING",
                                        "recordedAt": "2026-11-03T10:00:00",
                                        "memo": "먹이 급여",
                                        "typeDetail": {}
                                    }
                                    """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.recordId").value(1));

        verify(careRecordService).createRecord(
                eq(userId),
                eq(lizardId),
                any(CareRecordCreateRequest.class)
        );
    }

    @Test
    @DisplayName("REC-12. 기록 사진 첨부")
    void addPhoto() throws Exception {

        Long recordId = 1L;

        CareRecordPhoto photo = mock(CareRecordPhoto.class);

        when(photo.getPhotoId()).thenReturn(10L);
        when(photo.getImageUrl())
                .thenReturn("https://example.com/image.jpg");

        CareRecordPhotoResponse response =
                new CareRecordPhotoResponse(photo);

        when(careRecordService.addPhoto(
                eq(userId),
                eq(recordId),
                any()
        )).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "feeding.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image".getBytes()
        );

        mockMvc.perform(
                        multipart(
                                "/lizards/{lizardId}/care-records/{recordId}/photos",
                                lizardId,
                                recordId
                        )
                                .file(file)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.photoId").value(10))
                .andExpect(jsonPath("$.data.imageUrl")
                        .value("https://example.com/image.jpg"));

        verify(careRecordService).addPhoto(
                eq(userId),
                eq(recordId),
                any()
        );
    }

    @Test
    @DisplayName("REC-13. 기록 메모 수정")
    void updateMemo() throws Exception {

        Long recordId = 1L;

        mockMvc.perform(
                        patch(
                                "/lizards/{lizardId}/care-records/{recordId}/memo",
                                lizardId,
                                recordId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "memo": "먹이 급여 완료"
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(careRecordService)
                .updateMemo(userId, recordId, "먹이 급여 완료");
    }

    @Test
    @DisplayName("REC-14. 기록 시점 환경 정보 조회")
    void getEnvironment() throws Exception {

        Long recordId = 1L;

        EnvironmentResponse response = createEnvironmentResponse();

        when(careRecordService.getEnvironment(userId, recordId))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/lizards/{lizardId}/care-records/{recordId}/environment",
                                lizardId,
                                recordId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.temperature").value(27.5))
                .andExpect(jsonPath("$.data.humidity").value(65.0))
                .andExpect(jsonPath("$.data.measuredAt")
                        .value("2026-11-03T10:00:00"));

        verify(careRecordService).getEnvironment(userId, recordId);
    }

    @Test
    @DisplayName("REC-15. 기록 상세 조회")
    void getRecordDetail() throws Exception {

        Long recordId = 1L;

        Lizard lizard = mock(Lizard.class);
        when(lizard.getLizardId()).thenReturn(lizardId);

        CareRecord record = mock(CareRecord.class);
        when(record.getRecordId()).thenReturn(recordId);
        when(record.getLizard()).thenReturn(lizard);
        when(record.getRecordType()).thenReturn(CareRecordType.FEEDING);
        when(record.getRecordedAt())
                .thenReturn(LocalDateTime.of(2026, 11, 3, 10, 0));
        when(record.getMemo()).thenReturn("먹이 급여");

        CareRecordDetailResponse response =
                new CareRecordDetailResponse(
                        record,
                        Map.of("food", "귀뚜라미"),
                        List.of(),
                        null
                );

        when(careRecordService.getRecordDetail(userId, recordId))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/lizards/{lizardId}/care-records/{recordId}",
                                lizardId,
                                recordId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.recordId").value(1))
                .andExpect(jsonPath("$.data.lizardId").value(1))
                .andExpect(jsonPath("$.data.recordType").value("FEEDING"))
                .andExpect(jsonPath("$.data.recordedAt")
                        .value("2026-11-03T10:00:00"))
                .andExpect(jsonPath("$.data.memo").value("먹이 급여"))
                .andExpect(jsonPath("$.data.typeDetail.food").value("귀뚜라미"))
                .andExpect(jsonPath("$.data.photos").isArray())
                .andExpect(jsonPath("$.data.photos.length()").value(0))
                .andExpect(jsonPath("$.data.environment").doesNotExist());

        verify(careRecordService).getRecordDetail(userId, recordId);
    }

    @Test
    @DisplayName("REC-16. 환경 그래프 조회")
    void getEnvironmentGraph() throws Exception {

        Long recordId = 1L;

        SensorMeasurement measurement = createSensorMeasurement();

        EnvironmentGraphResponse graphResponse =
                new EnvironmentGraphResponse(measurement);

        List<EnvironmentGraphResponse> response =
                List.of(graphResponse);

        when(careRecordService.getEnvironmentGraph(
                userId, recordId, 60, 45
        )).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/lizards/{lizardId}/care-records/{recordId}/environment/graph",
                                lizardId,
                                recordId
                        )
                                .param("beforeMinutes", "60")
                                .param("afterMinutes", "45")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].temperature").value(27.5))
                .andExpect(jsonPath("$.data[0].humidity").value(65.0))
                .andExpect(jsonPath("$.data[0].measuredAt")
                        .value("2026-11-03T10:00:00"));

        verify(careRecordService)
                .getEnvironmentGraph(userId, recordId, 60, 45);
    }

    @Test
    @DisplayName("REC-17. 가장 가까운 환경 정보 조회")
    void getNearestEnvironment() throws Exception {

        Long recordId = 1L;

        EnvironmentResponse response = createEnvironmentResponse();

        when(careRecordService.getNearestEnvironment(userId, recordId))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/lizards/{lizardId}/care-records/{recordId}/environment/nearest",
                                lizardId,
                                recordId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.temperature").value(27.5))
                .andExpect(jsonPath("$.data.humidity").value(65.0))
                .andExpect(jsonPath("$.data.measuredAt")
                        .value("2026-11-03T10:00:00"));

        verify(careRecordService)
                .getNearestEnvironment(userId, recordId);
    }

    @Test
    @DisplayName("REC-18. 환경 정보 요약 조회")
    void getEnvironmentSummary() throws Exception {

        Long recordId = 1L;

        EnvironmentSummaryResponse response =
                new EnvironmentSummaryResponse(
                        true,
                        "기록 시점의 온도와 습도는 적정 범위입니다."
                );

        when(careRecordService.getEnvironmentSummary(userId, recordId))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/lizards/{lizardId}/care-records/{recordId}/environment/summary",
                                lizardId,
                                recordId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.dataAvailable").value(true))
                .andExpect(jsonPath("$.data.summary")
                        .value("기록 시점의 온도와 습도는 적정 범위입니다."));

        verify(careRecordService)
                .getEnvironmentSummary(userId, recordId);
    }

    @Test
    @DisplayName("REC-19. 월별 기록 변화 조회")
    void getMonthlyChanges() throws Exception {
        MonthlyChangeItemResponse item =
                new MonthlyChangeItemResponse("FEEDING", 3, 5);

        MonthlyChangesResponse response =
                new MonthlyChangesResponse(
                        List.of(item),
                        "월별 기록 변화 조회 성공"
                );

        when(careRecordService.getMonthlyChanges(userId, lizardId, 2026, 11))
                .thenReturn(response);

        mockMvc.perform(
                        get("/lizards/{lizardId}/care-records/monthly-changes", lizardId)
                                .param("year", "2026")
                                .param("month", "11")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].type").value("FEEDING"))
                .andExpect(jsonPath("$.data.items[0].previousCount").value(3))
                .andExpect(jsonPath("$.data.items[0].currentCount").value(5))
                .andExpect(jsonPath("$.data.items[0].changeValue").value(2))
                .andExpect(jsonPath("$.data.items[0].changeStatus").value("INCREASED"))
                .andExpect(jsonPath("$.data.message").value("월별 기록 변화 조회 성공"));

        verify(careRecordService)
                .getMonthlyChanges(userId, lizardId, 2026, 11);
    }

    // 테스트용 SensorMeasurement Mock 생성
    private SensorMeasurement createSensorMeasurement() {

        SensorMeasurement measurement = mock(SensorMeasurement.class);

        when(measurement.getTemperature())
                .thenReturn(new BigDecimal("27.5"));

        when(measurement.getHumidity())
                .thenReturn(new BigDecimal("65.0"));

        when(measurement.getMeasuredAt())
                .thenReturn(LocalDateTime.of(2026, 11, 3, 10, 0));

        return measurement;
    }

    // 테스트용 EnvironmentResponse 생성
    private EnvironmentResponse createEnvironmentResponse() {

        SensorMeasurement measurement = createSensorMeasurement();

        return new EnvironmentResponse(measurement);
    }
}
