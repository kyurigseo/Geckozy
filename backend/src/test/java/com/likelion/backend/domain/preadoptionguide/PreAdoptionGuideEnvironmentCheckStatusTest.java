package com.likelion.backend.domain.preadoptionguide;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.lizard.entity.Lizard;
import com.likelion.backend.domain.lizard.repository.LizardRepository;
import com.likelion.backend.domain.preadoptionguide.dto.request.PreAdoptionGuideCheckSaveRequest;
import com.likelion.backend.domain.preadoptionguide.dto.request.PreAdoptionGuideCheckSaveRequest.CheckDto;
import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStartResponse.EnvironmentCheckStatus;
import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStatusResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.EnvironmentCheckStatusResponse.SensorStatus;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideProgressRepository;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideUserCheckRepository;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionEnvironmentCheckService;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideService;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideStepService;
import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import com.likelion.backend.domain.sensor.repository.SensorMeasurementRepository;
import com.likelion.backend.domain.sensor.repository.SensorRepository;
import com.likelion.backend.domain.species.entity.Species;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import com.likelion.backend.global.util.KstTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 환경 점검 진행 상태 조회 검증.
 * 진행 상태가 별도 트랜잭션에서 생성되므로 @Transactional 없이 실제로 커밋하고, 테스트마다 데이터를 지운다.
 */
@SpringBootTest
class PreAdoptionGuideEnvironmentCheckStatusTest {

    @Autowired private PreAdoptionEnvironmentCheckService environmentCheckService;
    @Autowired private PreAdoptionGuideService guideService;
    @Autowired private PreAdoptionGuideStepService stepService;
    @Autowired private SpeciesRepository speciesRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private LizardRepository lizardRepository;
    @Autowired private EnclosureRepository enclosureRepository;
    @Autowired private SensorRepository sensorRepository;
    @Autowired private SensorMeasurementRepository measurementRepository;
    @Autowired private PreAdoptionGuideProgressRepository progressRepository;
    @Autowired private PreAdoptionGuideUserCheckRepository userCheckRepository;

    private final List<Lizard> lizards = new ArrayList<>();
    private final List<Enclosure> enclosures = new ArrayList<>();
    private final List<Sensor> sensors = new ArrayList<>();

    private User user;
    private Species crestedGecko;
    private Long preGuideId;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("env-status-tester", "env-status-tester@example.com", "password"));
        crestedGecko = speciesRepository.findByName("크레스티드 게코").orElseThrow();
        preGuideId = guideService.getGuideBySpecies(crestedGecko.getSpeciesId()).getPreGuideId();
    }

    @AfterEach
    void cleanUp() {
        Long userId = user.getUserId();
        sensors.forEach(sensor -> measurementRepository.findAll().stream()
                .filter(m -> m.getSensor().getSensorId().equals(sensor.getSensorId()))
                .forEach(measurementRepository::delete));
        sensorRepository.deleteAll(sensors);
        userCheckRepository.findAll().stream()
                .filter(check -> check.getUser().getUserId().equals(userId))
                .forEach(userCheckRepository::delete);
        progressRepository.findAll().stream()
                .filter(progress -> progress.getUser().getUserId().equals(userId))
                .forEach(progressRepository::delete);
        enclosureRepository.deleteAll(enclosures);
        lizardRepository.deleteAll(lizards);
        userRepository.delete(user);
    }

    private Enclosure registerEnclosure(Species species) {
        Lizard lizard = lizardRepository.save(Lizard.builder()
                .user(user)
                .species(species)
                .name("게코")
                .birthDateUnknown(true)
                .characterColor("yellow")
                .gender(Lizard.Gender.FEMALE)
                .sizeWeightUnknown(true)
                .build());
        lizards.add(lizard);
        Enclosure enclosure = enclosureRepository.save(Enclosure.builder()
                .lizard(lizard)
                .width(new BigDecimal("45"))
                .depth(new BigDecimal("45"))
                .height(new BigDecimal("60"))
                .material(Enclosure.Material.GLASS)
                .ventilation(Enclosure.Ventilation.TOP)
                .build());
        enclosures.add(enclosure);
        return enclosure;
    }

    private Sensor connectSensor(Enclosure enclosure, LocalDateTime connectedAt) {
        Sensor sensor = Sensor.builder()
                .enclosure(enclosure)
                .deviceIdentifier("env-status-" + enclosure.getId())
                .build();
        ReflectionTestUtils.setField(sensor, "connectedAt", connectedAt);
        sensor = sensorRepository.save(sensor);
        sensors.add(sensor);
        return sensor;
    }

    // from ~ to 사이 everyMinutes 간격으로 측정값 저장
    private void measure(Sensor sensor, LocalDateTime from, LocalDateTime to, int everyMinutes) {
        List<SensorMeasurement> measurements = new ArrayList<>();
        for (LocalDateTime t = from; !t.isAfter(to); t = t.plusMinutes(everyMinutes)) {
            measurements.add(SensorMeasurement.builder()
                    .sensor(sensor)
                    .temperature(new BigDecimal("24.0"))
                    .humidity(new BigDecimal("60.0"))
                    .measuredAt(t)
                    .build());
        }
        measurementRepository.saveAll(measurements);
    }

    // 1~3단계 완료 후 점검 시작, 시작 시각을 hoursAgo 시간 전으로 옮김
    private LocalDateTime startCheck(long hoursAgo) {
        guideService.completeStep(user.getUserId(), preGuideId, 1);
        List<CheckDto> checks = stepService.getStep(user.getUserId(), preGuideId, 2).getCheckItems().stream()
                .map(item -> new CheckDto(item.getCheckItemId(), true))
                .toList();
        stepService.saveChecks(user.getUserId(), preGuideId, 2, new PreAdoptionGuideCheckSaveRequest(checks));
        guideService.completeStep(user.getUserId(), preGuideId, 2);
        guideService.completeStep(user.getUserId(), preGuideId, 3);
        guideService.startEnvironmentCheck(user.getUserId(), preGuideId);

        PreAdoptionGuideProgress progress = progressRepository
                .findByUser_UserIdAndPreAdoptionGuide_PreGuideId(user.getUserId(), preGuideId).orElseThrow();
        LocalDateTime startedAt = KstTime.now().minusHours(hoursAgo).truncatedTo(ChronoUnit.SECONDS);
        progress.startEnvironmentCheck(startedAt);
        progressRepository.save(progress);
        return startedAt;
    }

    private EnvironmentCheckStatusResponse status() {
        return environmentCheckService.getStatus(user.getUserId(), preGuideId);
    }

    @Test
    @DisplayName("1. 점검을 시작하지 않았으면 ENVIRONMENT_CHECK_NOT_FOUND")
    void notStarted() {
        assertThatThrownBy(this::status)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENVIRONMENT_CHECK_NOT_FOUND);

        registerEnclosure(crestedGecko);
        guideService.completeStep(user.getUserId(), preGuideId, 1);

        assertThatThrownBy(this::status)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENVIRONMENT_CHECK_NOT_FOUND);
    }

    @Test
    @DisplayName("2. 센서가 없으면 NOT_REGISTERED, 최근 측정 시각 null")
    void noSensor() {
        registerEnclosure(crestedGecko);
        startCheck(0);

        EnvironmentCheckStatusResponse response = status();

        assertThat(response.getStartDate()).isEqualTo(KstTime.now().toLocalDate());
        assertThat(response.getElapsedDays()).isEqualTo(1);
        assertThat(response.getTotalDays()).isEqualTo(7);
        assertThat(response.getStatus()).isEqualTo(EnvironmentCheckStatus.IN_PROGRESS);
        assertThat(response.getSensorStatus()).isEqualTo(SensorStatus.NOT_REGISTERED);
        assertThat(response.getLatestMeasuredAt()).isNull();
        assertThat(response.getMissingData()).isFalse();
    }

    @Test
    @DisplayName("3. 센서 없이 30분을 넘기면 데이터 공백")
    void noSensor_GapAfterThreshold() {
        registerEnclosure(crestedGecko);
        startCheck(2);

        assertThat(status().getMissingData()).isTrue();
    }

    @Test
    @DisplayName("4. 10분 간격으로 계속 들어오면 CONNECTED, 공백 없음, 최근 측정 시각 반환")
    void regularData() {
        Enclosure enclosure = registerEnclosure(crestedGecko);
        LocalDateTime startedAt = startCheck(3);
        Sensor sensor = connectSensor(enclosure, startedAt.minusHours(1));
        LocalDateTime last = KstTime.now().minusMinutes(5).truncatedTo(ChronoUnit.SECONDS);
        measure(sensor, startedAt.minusHours(1), last, 10);

        EnvironmentCheckStatusResponse response = status();

        assertThat(response.getSensorStatus()).isEqualTo(SensorStatus.CONNECTED);
        assertThat(response.getMissingData()).isFalse();
        assertThat(response.getLatestMeasuredAt()).isNotNull();
        assertThat(response.getLatestMeasuredAt()).isBeforeOrEqualTo(last);
        assertThat(response.getLatestMeasuredAt()).isAfter(last.minusMinutes(10));
    }

    @Test
    @DisplayName("5. 점검 기간 중간에 30분 넘게 끊기면 공백 (지금은 연결돼 있어도)")
    void gapInMiddle() {
        Enclosure enclosure = registerEnclosure(crestedGecko);
        LocalDateTime startedAt = startCheck(3);
        Sensor sensor = connectSensor(enclosure, startedAt);
        measure(sensor, startedAt, startedAt.plusMinutes(60), 10);
        measure(sensor, startedAt.plusMinutes(100), KstTime.now().minusMinutes(1), 10);

        EnvironmentCheckStatusResponse response = status();

        assertThat(response.getSensorStatus()).isEqualTo(SensorStatus.CONNECTED);
        assertThat(response.getMissingData()).isTrue();
    }

    @Test
    @DisplayName("6. 마지막 측정이 30분 넘게 지났으면 DISCONNECTED, 공백")
    void disconnected() {
        Enclosure enclosure = registerEnclosure(crestedGecko);
        LocalDateTime startedAt = startCheck(3);
        Sensor sensor = connectSensor(enclosure, startedAt);
        measure(sensor, startedAt, KstTime.now().minusMinutes(40), 10);

        EnvironmentCheckStatusResponse response = status();

        assertThat(response.getSensorStatus()).isEqualTo(SensorStatus.DISCONNECTED);
        assertThat(response.getMissingData()).isTrue();
    }

    @Test
    @DisplayName("7. 측정값 없이 센서 연결 후 30분이 지났으면 DISCONNECTED")
    void sensorWithoutData() {
        Enclosure enclosure = registerEnclosure(crestedGecko);
        LocalDateTime startedAt = startCheck(3);
        connectSensor(enclosure, startedAt);

        EnvironmentCheckStatusResponse response = status();

        assertThat(response.getSensorStatus()).isEqualTo(SensorStatus.DISCONNECTED);
        assertThat(response.getLatestMeasuredAt()).isNull();
        assertThat(response.getMissingData()).isTrue();
    }

    @Test
    @DisplayName("8. 가장 최근에 등록한 사육장의 센서를 봄 (예전 사육장 센서는 무시)")
    void usesLatestEnclosure() {
        Enclosure older = registerEnclosure(crestedGecko);
        LocalDateTime startedAt = startCheck(3);
        Sensor olderSensor = connectSensor(older, startedAt);
        measure(olderSensor, startedAt, KstTime.now().minusMinutes(1), 10);

        registerEnclosure(crestedGecko);

        assertThat(status().getSensorStatus()).isEqualTo(SensorStatus.NOT_REGISTERED);
    }

    @Test
    @DisplayName("9. 다른 종 도마뱀 사육장의 센서는 보지 않음")
    void ignoresOtherSpecies() {
        registerEnclosure(crestedGecko);
        LocalDateTime startedAt = startCheck(3);
        Enclosure otherSpecies = registerEnclosure(speciesRepository.findByName("레오파드 게코").orElseThrow());
        Sensor sensor = connectSensor(otherSpecies, startedAt);
        measure(sensor, startedAt, KstTime.now().minusMinutes(1), 10);

        assertThat(status().getSensorStatus()).isEqualTo(SensorStatus.NOT_REGISTERED);
    }

    @Test
    @DisplayName("10. 7일이 지나면 COMPLETED, 7일차 이후 끊긴 건 공백으로 보지 않음")
    void completed_WindowCappedAt7Days() {
        Enclosure enclosure = registerEnclosure(crestedGecko);
        LocalDateTime startedAt = startCheck(24 * 8);
        Sensor sensor = connectSensor(enclosure, startedAt);
        measure(sensor, startedAt, startedAt.plusDays(7), 10);

        EnvironmentCheckStatusResponse response = status();

        assertThat(response.getStatus()).isEqualTo(EnvironmentCheckStatus.COMPLETED);
        assertThat(response.getElapsedDays()).isEqualTo(7);
        assertThat(response.getSensorStatus()).isEqualTo(SensorStatus.DISCONNECTED);
        assertThat(response.getMissingData()).isFalse();
    }

    // ---------------------------------------------------------------- 환경 데이터 중간 요약

    @Test
    @DisplayName("12. 요약: 점검을 시작하지 않았으면 ENVIRONMENT_CHECK_NOT_FOUND")
    void summary_NotStarted() {
        assertThatThrownBy(() -> environmentCheckService.getSummary(user.getUserId(), preGuideId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENVIRONMENT_CHECK_NOT_FOUND);
    }

    @Test
    @DisplayName("13. 요약: 시작 직후에는 데이터 부족, 추가 관찰 안내")
    void summary_Insufficient() {
        registerEnclosure(crestedGecko);
        startCheck(2);

        var summary = environmentCheckService.getSummary(user.getUserId(), preGuideId);

        assertThat(summary.getSufficientData()).isFalse();
        assertThat(summary.getTemperatureSummary()).isNull();
        assertThat(summary.getCheckPoints()).isEmpty();
        assertThat(summary.getGuidance()).isEqualTo("데이터가 더 쌓이면 환경을 판단할 수 있어요.");
    }

    @Test
    @DisplayName("14. 요약: 30시간 데이터면 요약하고, 크레스티드 게코 권장 범위(낮 22~26℃)로 확인할 점 판단")
    void summary_UsesSpeciesStandard() {
        Enclosure enclosure = registerEnclosure(crestedGecko);
        LocalDateTime startedAt = startCheck(30);
        Sensor sensor = connectSensor(enclosure, startedAt);

        // 낮(08~20시) 28℃로 권장 범위 초과, 밤 23℃, 습도 60%
        List<SensorMeasurement> measurements = new ArrayList<>();
        for (LocalDateTime t = startedAt; t.isBefore(KstTime.now()); t = t.plusMinutes(10)) {
            boolean day = t.getHour() >= 8 && t.getHour() < 20;
            measurements.add(SensorMeasurement.builder()
                    .sensor(sensor)
                    .temperature(new BigDecimal(day ? "28.0" : "23.0"))
                    .humidity(new BigDecimal("60.0"))
                    .measuredAt(t)
                    .build());
        }
        measurementRepository.saveAll(measurements);

        var summary = environmentCheckService.getSummary(user.getUserId(), preGuideId);

        assertThat(summary.getSufficientData()).isTrue();
        assertThat(summary.getTemperatureSummary()).isEqualTo("23.0~28.0℃ 사이에서 유지됐어요.");
        assertThat(summary.getDayNightSummary()).isEqualTo("낮에는 주로 28℃, 밤에는 23℃ 안팎으로 낮아졌어요.");
        assertThat(summary.getHumiditySummary()).isEqualTo("분무 후 습도가 오르는 흐름이 아직 확인되지 않았어요.");
        assertThat(summary.getCheckPoints()).contains("온도가 권장 범위를 벗어난 시간이 있었어요.");
        assertThat(summary.getGuidance()).isNull();
    }

    @Test
    @DisplayName("11. 존재하지 않는 가이드면 PRE_ADOPTION_GUIDE_NOT_FOUND, 사용자가 없으면 UNAUTHORIZED")
    void notFoundAndUnauthorized() {
        assertThatThrownBy(() -> environmentCheckService.getStatus(user.getUserId(), 999_999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);

        assertThatThrownBy(() -> environmentCheckService.getStatus(999_999L, preGuideId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }
}
