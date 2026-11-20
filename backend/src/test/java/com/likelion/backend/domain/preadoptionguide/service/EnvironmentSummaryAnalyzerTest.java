package com.likelion.backend.domain.preadoptionguide.service;

import com.likelion.backend.domain.preadoptionguide.service.EnvironmentSummaryAnalyzer.Input;
import com.likelion.backend.domain.preadoptionguide.service.EnvironmentSummaryAnalyzer.Point;
import com.likelion.backend.domain.preadoptionguide.service.EnvironmentSummaryAnalyzer.Range;
import com.likelion.backend.domain.preadoptionguide.service.EnvironmentSummaryAnalyzer.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentSummaryAnalyzerTest {

    // 크레스티드 게코 기준 (DataInitializer 시드와 동일)
    private static final Range DAY_TEMP = new Range(22, 26);
    private static final Range NIGHT_TEMP = new Range(20, 24);
    private static final double HUMIDITY_MIN = 50;

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 16, 8, 0);

    // ---------------------------------------------------------------- 데이터 생성

    /** 낮 24~25℃ / 밤 23℃ */
    private static double normalTemp(LocalDateTime t) {
        int hour = t.getHour();
        if (hour >= 8 && hour < 20) {
            return 24.0 + (hour % 2) * 0.8;
        }
        return 23.0;
    }

    /**
     * 매일 20시 분무로 80%까지 상승 → 2시간 유지 → 시간당 7%p씩 하강해 다음날 2시에 52% → 이후 52% 유지.
     * 70% 이상 구간은 하루 약 3.5시간 (4시간 기준 이하).
     */
    private static double normalHumidity(LocalDateTime t) {
        double hoursSinceMist = Duration.between(t.toLocalDate().atTime(20, 0), t).toMinutes() / 60.0;
        if (hoursSinceMist < 0) {
            hoursSinceMist += 24;
        }
        if (hoursSinceMist < 2) {
            return 80;
        }
        if (hoursSinceMist < 6) {
            return 80 - (hoursSinceMist - 2) * 7;
        }
        return 52;
    }

    private static List<Point> generate(
            LocalDateTime from, LocalDateTime to,
            Function<LocalDateTime, Double> temp, Function<LocalDateTime, Double> humidity
    ) {
        List<Point> points = new ArrayList<>();
        for (LocalDateTime t = from; !t.isAfter(to); t = t.plusMinutes(10)) {
            points.add(new Point(t, temp.apply(t), humidity.apply(t)));
        }
        return points;
    }

    private static Input input(LocalDateTime end, List<Point> points) {
        return new Input(START, end, points, null, null, DAY_TEMP, NIGHT_TEMP, HUMIDITY_MIN);
    }

    private static Result analyzeNormal(int hours) {
        LocalDateTime end = START.plusHours(hours);
        return EnvironmentSummaryAnalyzer.analyze(input(end,
                generate(START, end, EnvironmentSummaryAnalyzerTest::normalTemp, EnvironmentSummaryAnalyzerTest::normalHumidity)));
    }

    // ---------------------------------------------------------------- 데이터 충분 여부

    @Test
    @DisplayName("1. 시작 후 24시간 전이면 판단하지 않고 추가 관찰 안내")
    void insufficient_Before24Hours() {
        Result result = analyzeNormal(23);

        assertThat(result.sufficientData()).isFalse();
        assertThat(result.temperatureSummary()).isNull();
        assertThat(result.dayNightSummary()).isNull();
        assertThat(result.humiditySummary()).isNull();
        assertThat(result.durationSummary()).isNull();
        assertThat(result.patternSummary()).isNull();
        assertThat(result.checkPoints()).isEmpty();
        assertThat(result.guidance()).isEqualTo("데이터가 더 쌓이면 환경을 판단할 수 있어요.");
    }

    @Test
    @DisplayName("2. 24시간이 지나도 측정이 있는 시간대가 70% 미만이면 판단하지 않음")
    void insufficient_LowCoverage() {
        LocalDateTime end = START.plusHours(30);
        // 30시간 중 앞 20시간만 측정 (67%)
        List<Point> points = generate(START, START.plusHours(20).minusMinutes(1),
                EnvironmentSummaryAnalyzerTest::normalTemp, EnvironmentSummaryAnalyzerTest::normalHumidity);

        assertThat(EnvironmentSummaryAnalyzer.analyze(input(end, points)).sufficientData()).isFalse();
    }

    @Test
    @DisplayName("3. 24시간이 지나고 측정이 70% 이상이면 판단, guidance는 null")
    void sufficient() {
        Result result = analyzeNormal(30);

        assertThat(result.sufficientData()).isTrue();
        assertThat(result.guidance()).isNull();
    }

    // ---------------------------------------------------------------- 정상 환경 요약

    @Test
    @DisplayName("4. 정상 환경 2일: 온도 범위, 낮/밤, 습윤-건조 흐름, 지속 시간, 반복 패턴, 확인할 점 없음")
    void normalEnvironment() {
        Result result = analyzeNormal(48);

        assertThat(result.temperatureSummary()).isEqualTo("23.0~24.8℃ 사이에서 유지됐어요.");
        assertThat(result.dayNightSummary()).isEqualTo("낮에는 주로 24~25℃, 밤에는 23℃ 안팎으로 낮아졌어요.");
        assertThat(result.humiditySummary()).isEqualTo("분무 후 약 80%까지 상승한 뒤, 시간이 지나면서 50%대까지 낮아졌어요.");
        assertThat(result.durationSummary()).isEqualTo("습도 70% 이상은 한 번에 주로 약 4시간 이어졌어요.");
        assertThat(result.patternSummary()).isEqualTo("하루 평균 1번 습윤-건조 흐름이 반복됐어요.");
        assertThat(result.checkPoints()).isEmpty();
    }

    @Test
    @DisplayName("5. 낮과 밤 평균 차이가 1℃ 미만이면 차이가 거의 없다고 요약")
    void noDayNightDifference() {
        LocalDateTime end = START.plusHours(30);
        Result result = EnvironmentSummaryAnalyzer.analyze(input(end,
                generate(START, end, t -> 24.0, EnvironmentSummaryAnalyzerTest::normalHumidity)));

        assertThat(result.temperatureSummary()).isEqualTo("24.0℃로 유지됐어요.");
        assertThat(result.dayNightSummary()).isEqualTo("낮과 밤의 온도 차이가 거의 없었어요.");
    }

    @Test
    @DisplayName("6. 분무 급상승이 없으면 흐름 미확인 문구, 반복 패턴과 지속 시간은 null")
    void noMisting() {
        LocalDateTime end = START.plusHours(30);
        Result result = EnvironmentSummaryAnalyzer.analyze(input(end,
                generate(START, end, EnvironmentSummaryAnalyzerTest::normalTemp, t -> 60.0)));

        assertThat(result.humiditySummary()).isEqualTo("분무 후 습도가 오르는 흐름이 아직 확인되지 않았어요.");
        assertThat(result.patternSummary()).isNull();
        assertThat(result.durationSummary()).isNull();
    }

    @Test
    @DisplayName("7. 1시간 안에 이어진 급상승은 분무 1번으로 셈")
    void spikesWithinOneHourAreOneCycle() {
        List<Point> hums = new ArrayList<>();
        double[] values = {50, 62, 75, 80, 80, 78, 70, 60, 55, 52};
        for (int i = 0; i < values.length; i++) {
            hums.add(new Point(START.plusMinutes(10L * i), 24.0, values[i]));
        }

        assertThat(EnvironmentSummaryAnalyzer.humidityCycles(hums)).hasSize(1);
    }

    // ---------------------------------------------------------------- 확인할 점

    @Test
    @DisplayName("8. 70% 이상이 4시간 넘게 이어진 날이 있으면 일차와 시간을 안내")
    void longHighHumidity() {
        LocalDateTime end = START.plusHours(48);
        // 2일차(10/17) 20시 분무 후 6시간 동안 80% 유지
        Function<LocalDateTime, Double> humidity = t -> {
            LocalDateTime mist = LocalDateTime.of(2026, 10, 17, 20, 0);
            if (!t.isBefore(mist) && t.isBefore(mist.plusHours(6))) {
                return 80.0;
            }
            return t.isAfter(mist) ? 55.0 : normalHumidity(t);
        };

        Result result = EnvironmentSummaryAnalyzer.analyze(input(end,
                generate(START, end, EnvironmentSummaryAnalyzerTest::normalTemp, humidity)));

        assertThat(result.checkPoints()).containsExactly(
                "습도가 높은 상태로 오래 유지된 날이 있었어요.",
                "2일차에는 70% 이상인 상태가 약 6시간 지속됐어요."
        );
    }

    @Test
    @DisplayName("9. 온도가 권장 범위를 1시간 넘게 벗어나면 안내 (1시간 이하는 안내하지 않음)")
    void temperatureOutOfRange() {
        LocalDateTime end = START.plusHours(30);
        LocalDateTime hotFrom = START.plusHours(4);

        Result twoHours = EnvironmentSummaryAnalyzer.analyze(input(end, generate(START, end,
                t -> !t.isBefore(hotFrom) && t.isBefore(hotFrom.plusHours(2)) ? 28.0 : normalTemp(t),
                EnvironmentSummaryAnalyzerTest::normalHumidity)));
        Result oneHour = EnvironmentSummaryAnalyzer.analyze(input(end, generate(START, end,
                t -> !t.isBefore(hotFrom) && t.isBefore(hotFrom.plusHours(1)) ? 28.0 : normalTemp(t),
                EnvironmentSummaryAnalyzerTest::normalHumidity)));

        assertThat(twoHours.checkPoints()).containsExactly("온도가 권장 범위를 벗어난 시간이 있었어요.");
        assertThat(oneHour.checkPoints()).isEmpty();
    }

    @Test
    @DisplayName("10. 밤에는 밤 기준으로 판단: 밤 21℃는 정상, 낮 21℃는 범위 이탈")
    void temperatureRangeByDayNight() {
        LocalDateTime end = START.plusHours(30);
        Result nightCool = EnvironmentSummaryAnalyzer.analyze(input(end, generate(START, end,
                t -> t.getHour() >= 20 || t.getHour() < 8 ? 21.0 : 24.0,
                EnvironmentSummaryAnalyzerTest::normalHumidity)));
        Result dayCool = EnvironmentSummaryAnalyzer.analyze(input(end, generate(START, end,
                t -> t.getHour() >= 8 && t.getHour() < 20 ? 21.0 : 23.0,
                EnvironmentSummaryAnalyzerTest::normalHumidity)));

        assertThat(nightCool.checkPoints()).isEmpty();
        assertThat(dayCool.checkPoints()).containsExactly("온도가 권장 범위를 벗어난 시간이 있었어요.");
    }

    @Test
    @DisplayName("11. 습도가 권장 최저보다 1시간 넘게 낮으면 안내")
    void humidityTooLow() {
        LocalDateTime end = START.plusHours(30);
        Result result = EnvironmentSummaryAnalyzer.analyze(input(end, generate(START, end,
                EnvironmentSummaryAnalyzerTest::normalTemp,
                t -> t.isBefore(START.plusHours(3)) ? 40.0 : normalHumidity(t))));

        assertThat(result.checkPoints()).containsExactly("습도가 권장 범위보다 낮게 유지된 시간이 있었어요.");
    }

    @Test
    @DisplayName("12. 데이터가 30분 넘게 끊긴 구간이 있으면 안내")
    void dataGap() {
        LocalDateTime end = START.plusHours(30);
        List<Point> points = generate(START, end,
                EnvironmentSummaryAnalyzerTest::normalTemp, EnvironmentSummaryAnalyzerTest::normalHumidity)
                .stream()
                .filter(p -> p.at().isBefore(START.plusHours(10)) || !p.at().isBefore(START.plusHours(11)))
                .toList();

        assertThat(EnvironmentSummaryAnalyzer.analyze(input(end, points)).checkPoints())
                .containsExactly("데이터가 끊긴 구간이 있었어요.");
    }

    @Test
    @DisplayName("13. 권장 기준이 없는 종이면 범위 관련 확인할 점은 판단하지 않음")
    void noStandard() {
        LocalDateTime end = START.plusHours(30);
        Result result = EnvironmentSummaryAnalyzer.analyze(new Input(START, end,
                generate(START, end, t -> 30.0, t -> 30.0), null, null, null, null, null));

        assertThat(result.sufficientData()).isTrue();
        assertThat(result.checkPoints()).isEmpty();
    }

    // ---------------------------------------------------------------- 낮/밤 구분

    @Test
    @DisplayName("14. 조명 시간이 자정을 넘겨도 낮/밤을 구분")
    void isDay_OvernightLighting() {
        LocalTime start = LocalTime.of(20, 0);
        LocalTime end = LocalTime.of(8, 0);

        assertThat(EnvironmentSummaryAnalyzer.isDay(LocalTime.of(23, 0), start, end)).isTrue();
        assertThat(EnvironmentSummaryAnalyzer.isDay(LocalTime.of(7, 59), start, end)).isTrue();
        assertThat(EnvironmentSummaryAnalyzer.isDay(LocalTime.of(12, 0), start, end)).isFalse();
    }
}
