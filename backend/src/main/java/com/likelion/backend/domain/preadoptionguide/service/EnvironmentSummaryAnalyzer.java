package com.likelion.backend.domain.preadoptionguide.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 7일 환경 점검 데이터 중간 요약 (센서 측정값 → 요약 문장).
 * DB와 무관한 순수 계산이라 단위 테스트로 기준을 검증한다. 기준값과 문구는 이 클래스에 모아 둔다.
 *
 * 기존 코드와 맞춘 기준
 * - 낮/밤: 사육장 관리 설정의 조명 시간, 없으면 08~20시 (EnclosureEnvironmentService와 동일)
 * - 분무(습도 급상승): 10분 안에 +10%p 이상 (SensorAnalysisService와 동일)
 * - 데이터 공백: 30분 넘게 측정이 없음 (SensorService.DISCONNECT_THRESHOLD와 동일)
 * - 권장 범위: 종별 환경 기준 (species_environment_standards)
 */
final class EnvironmentSummaryAnalyzer {

    // 분석 가능 판단: 시작 후 24시간 이상, 측정이 있는 시간대(1시간 단위) 70% 이상
    static final Duration MIN_ELAPSED = Duration.ofHours(24);
    static final double MIN_HOURLY_COVERAGE = 0.7;

    static final LocalTime DEFAULT_DAY_START = LocalTime.of(8, 0);
    static final LocalTime DEFAULT_DAY_END = LocalTime.of(20, 0);
    static final double DAY_NIGHT_MIN_DIFF = 1.0;

    static final Duration SPIKE_WINDOW = Duration.ofMinutes(10);
    static final double SPIKE_RISE = 10.0;
    // 이 시간 안에 이어진 급상승은 한 번의 분무로 본다
    static final Duration SPIKE_MERGE = Duration.ofMinutes(60);

    static final double HIGH_HUMIDITY = 70.0;
    static final Duration LONG_HIGH_HUMIDITY = Duration.ofHours(4);
    static final Duration OUT_OF_RANGE_LIMIT = Duration.ofHours(1);
    static final Duration MAX_DATA_GAP = Duration.ofMinutes(30);

    static final String GUIDANCE_INSUFFICIENT = "데이터가 더 쌓이면 환경을 판단할 수 있어요.";

    private EnvironmentSummaryAnalyzer() {
    }

    record Point(LocalDateTime at, Double temperature, Double humidity) {
    }

    record Range(double min, double max) {
        boolean contains(double value) {
            return value >= min && value <= max;
        }
    }

    /**
     * @param points        windowStart ~ windowEnd 사이 측정값 (시간 오름차순)
     * @param dayStart      조명 시작 시각 (null이면 08시)
     * @param dayEnd        조명 종료 시각 (null이면 20시)
     * @param dayTemp       낮 권장 온도 (없으면 null)
     * @param nightTemp     밤 권장 온도 (없으면 null)
     * @param humidityMin   권장 최저 습도 (없으면 null)
     */
    record Input(
            LocalDateTime windowStart,
            LocalDateTime windowEnd,
            List<Point> points,
            LocalTime dayStart,
            LocalTime dayEnd,
            Range dayTemp,
            Range nightTemp,
            Double humidityMin
    ) {
    }

    record Result(
            boolean sufficientData,
            String temperatureSummary,
            String dayNightSummary,
            String humiditySummary,
            String durationSummary,
            String patternSummary,
            List<String> checkPoints,
            String guidance
    ) {
        static Result insufficient() {
            return new Result(false, null, null, null, null, null, List.of(), GUIDANCE_INSUFFICIENT);
        }
    }

    static Result analyze(Input input) {
        if (!isSufficient(input)) {
            return Result.insufficient();
        }

        List<Point> temps = input.points().stream().filter(p -> p.temperature() != null).toList();
        List<Point> hums = input.points().stream().filter(p -> p.humidity() != null).toList();
        LocalTime dayStart = Objects.requireNonNullElse(input.dayStart(), DEFAULT_DAY_START);
        LocalTime dayEnd = Objects.requireNonNullElse(input.dayEnd(), DEFAULT_DAY_END);

        List<Cycle> cycles = humidityCycles(hums);
        List<Run> highRuns = highHumidityRuns(hums);

        List<String> checkPoints = new ArrayList<>();
        addLongHighHumidity(checkPoints, highRuns, input.windowStart());
        addTemperatureOutOfRange(checkPoints, temps, input, dayStart, dayEnd);
        addHumidityTooLow(checkPoints, hums, input.humidityMin());
        if (EnvironmentDataGap.exists(input.windowStart(), input.windowEnd(),
                input.points().stream().map(Point::at).toList(), MAX_DATA_GAP)) {
            checkPoints.add("데이터가 끊긴 구간이 있었어요.");
        }

        return new Result(
                true,
                temperatureSummary(temps),
                dayNightSummary(temps, dayStart, dayEnd),
                humiditySummary(cycles),
                durationSummary(highRuns),
                patternSummary(cycles, input),
                List.copyOf(checkPoints),
                null
        );
    }

    // ---------------------------------------------------------------- 데이터 충분 여부

    static boolean isSufficient(Input input) {
        Duration elapsed = Duration.between(input.windowStart(), input.windowEnd());
        if (elapsed.compareTo(MIN_ELAPSED) < 0) {
            return false;
        }

        long hourSlots = elapsed.toHours();
        Set<Long> covered = new HashSet<>();
        for (Point point : input.points()) {
            long slot = Duration.between(input.windowStart(), point.at()).toHours();
            if (slot >= 0 && slot < hourSlots) {
                covered.add(slot);
            }
        }
        return covered.size() >= hourSlots * MIN_HOURLY_COVERAGE;
    }

    // ---------------------------------------------------------------- 온도

    private static String temperatureSummary(List<Point> temps) {
        if (temps.isEmpty()) {
            return null;
        }
        double min = temps.stream().mapToDouble(Point::temperature).min().orElseThrow();
        double max = temps.stream().mapToDouble(Point::temperature).max().orElseThrow();
        if (oneDecimal(min).equals(oneDecimal(max))) {
            return oneDecimal(min) + "℃로 유지됐어요.";
        }
        return oneDecimal(min) + "~" + oneDecimal(max) + "℃ 사이에서 유지됐어요.";
    }

    private static String dayNightSummary(List<Point> temps, LocalTime dayStart, LocalTime dayEnd) {
        List<Double> day = new ArrayList<>();
        List<Double> night = new ArrayList<>();
        for (Point point : temps) {
            (isDay(point.at().toLocalTime(), dayStart, dayEnd) ? day : night).add(point.temperature());
        }
        if (day.isEmpty() || night.isEmpty()) {
            return null;
        }

        double dayAvg = day.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        double nightAvg = night.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        if (dayAvg - nightAvg < DAY_NIGHT_MIN_DIFF) {
            return "낮과 밤의 온도 차이가 거의 없었어요.";
        }

        List<Double> sortedDay = day.stream().sorted().toList();
        String dayRange = intRange(percentile(sortedDay, 0.25), percentile(sortedDay, 0.75));
        return "낮에는 주로 " + dayRange + "℃, 밤에는 " + Math.round(nightAvg) + "℃ 안팎으로 낮아졌어요.";
    }

    static boolean isDay(LocalTime time, LocalTime dayStart, LocalTime dayEnd) {
        if (dayStart.isBefore(dayEnd)) {
            return !time.isBefore(dayStart) && time.isBefore(dayEnd);
        }
        // 조명이 자정을 넘기는 경우 (예: 20시 ~ 다음날 8시)
        return !time.isBefore(dayStart) || time.isBefore(dayEnd);
    }

    private static void addTemperatureOutOfRange(
            List<String> checkPoints, List<Point> temps, Input input, LocalTime dayStart, LocalTime dayEnd) {
        if (input.dayTemp() == null || input.nightTemp() == null) {
            return;
        }
        Duration outside = accumulate(temps, point -> {
            Range range = isDay(point.at().toLocalTime(), dayStart, dayEnd) ? input.dayTemp() : input.nightTemp();
            return !range.contains(point.temperature());
        });
        if (outside.compareTo(OUT_OF_RANGE_LIMIT) > 0) {
            checkPoints.add("온도가 권장 범위를 벗어난 시간이 있었어요.");
        }
    }

    // ---------------------------------------------------------------- 습도: 분무 후 습윤-건조 흐름

    /** 분무 1회: 급상승 이후 최고점과, 다음 분무 전까지의 최저점(없으면 null) */
    record Cycle(LocalDateTime startedAt, double peak, Double trough) {
    }

    static List<Cycle> humidityCycles(List<Point> hums) {
        List<Integer> spikeStarts = new ArrayList<>();
        LocalDateTime lastSpikeAt = null;

        for (int i = 1; i < hums.size(); i++) {
            double rise = hums.get(i).humidity() - hums.get(referenceIndex(hums, i)).humidity();
            if (rise < SPIKE_RISE) {
                continue;
            }
            LocalDateTime at = hums.get(i).at();
            if (lastSpikeAt == null || Duration.between(lastSpikeAt, at).compareTo(SPIKE_MERGE) >= 0) {
                spikeStarts.add(i);
            }
            lastSpikeAt = at;
        }

        List<Cycle> cycles = new ArrayList<>();
        for (int k = 0; k < spikeStarts.size(); k++) {
            int from = spikeStarts.get(k);
            int to = k + 1 < spikeStarts.size() ? spikeStarts.get(k + 1) : hums.size();

            int peakIndex = from;
            for (int i = from; i < to; i++) {
                if (hums.get(i).humidity() > hums.get(peakIndex).humidity()) {
                    peakIndex = i;
                }
            }

            Double trough = null;
            for (int i = peakIndex + 1; i < to; i++) {
                double value = hums.get(i).humidity();
                if (trough == null || value < trough) {
                    trough = value;
                }
            }
            cycles.add(new Cycle(hums.get(from).at(), hums.get(peakIndex).humidity(), trough));
        }
        return cycles;
    }

    /** i번째 측정 시점에서 10분 이전과 가장 가까운(그 이전) 측정 인덱스. 없으면 0 (SensorAnalysisService와 동일) */
    private static int referenceIndex(List<Point> hums, int i) {
        LocalDateTime target = hums.get(i).at().minus(SPIKE_WINDOW);
        int ref = 0;
        for (int j = 0; j < i; j++) {
            if (!hums.get(j).at().isAfter(target)) {
                ref = j;
            } else {
                break;
            }
        }
        return ref;
    }

    private static String humiditySummary(List<Cycle> cycles) {
        if (cycles.isEmpty()) {
            return "분무 후 습도가 오르는 흐름이 아직 확인되지 않았어요.";
        }
        double peakMin = cycles.stream().mapToDouble(Cycle::peak).min().orElseThrow();
        double peakMax = cycles.stream().mapToDouble(Cycle::peak).max().orElseThrow();
        String rise = "분무 후 약 " + intRange(peakMin, peakMax) + "%까지 상승";

        List<Double> troughs = cycles.stream().map(Cycle::trough).filter(Objects::nonNull).toList();
        if (troughs.isEmpty()) {
            return rise + "했어요.";
        }
        long troughMin = decade(troughs.stream().mapToDouble(Double::doubleValue).min().orElseThrow());
        long troughMax = decade(troughs.stream().mapToDouble(Double::doubleValue).max().orElseThrow());
        String fall = troughMin == troughMax ? troughMin + "%대" : troughMin + "~" + troughMax + "%대";
        return rise + "한 뒤, 시간이 지나면서 " + fall + "까지 낮아졌어요.";
    }

    private static String patternSummary(List<Cycle> cycles, Input input) {
        if (cycles.isEmpty()) {
            return null;
        }
        double days = Math.max(1.0,
                Duration.between(input.windowStart(), input.windowEnd()).toMinutes() / (24.0 * 60));
        double perDay = Math.round(cycles.size() / days * 10) / 10.0;
        String count = perDay == Math.rint(perDay) ? String.valueOf((long) perDay) : String.valueOf(perDay);
        return "하루 평균 " + count + "번 습윤-건조 흐름이 반복됐어요.";
    }

    // ---------------------------------------------------------------- 습도: 70% 이상 지속 시간

    /** 습도 70% 이상이 이어진 구간 */
    record Run(LocalDateTime startedAt, Duration duration) {
    }

    /**
     * 70% 이상인 첫 측정부터, 70% 미만인 첫 측정까지를 한 구간으로 본다.
     * 측정이 30분 넘게 끊기면 끊기기 전 마지막 측정에서 구간을 끝낸다. 데이터가 끝날 때까지 이어지면 마지막 측정까지.
     */
    static List<Run> highHumidityRuns(List<Point> hums) {
        List<Run> runs = new ArrayList<>();
        LocalDateTime runStart = null;
        LocalDateTime previousAt = null;

        for (Point point : hums) {
            boolean gap = previousAt != null && Duration.between(previousAt, point.at()).compareTo(MAX_DATA_GAP) > 0;
            if (runStart != null && gap) {
                runs.add(new Run(runStart, Duration.between(runStart, previousAt)));
                runStart = null;
            }

            boolean high = point.humidity() >= HIGH_HUMIDITY;
            if (runStart == null && high) {
                runStart = point.at();
            } else if (runStart != null && !high) {
                runs.add(new Run(runStart, Duration.between(runStart, point.at())));
                runStart = null;
            }
            previousAt = point.at();
        }
        if (runStart != null) {
            runs.add(new Run(runStart, Duration.between(runStart, previousAt)));
        }
        return runs;
    }

    private static String durationSummary(List<Run> runs) {
        if (runs.isEmpty()) {
            return null;
        }
        List<Duration> sorted = runs.stream().map(Run::duration).sorted().toList();
        Duration median = sorted.get(sorted.size() / 2);
        if (sorted.size() % 2 == 0) {
            median = sorted.get(sorted.size() / 2 - 1).plus(median).dividedBy(2);
        }
        if (median.compareTo(Duration.ofHours(1)) < 0) {
            return "습도 70% 이상은 한 번에 주로 1시간 미만으로 이어졌어요.";
        }
        return "습도 70% 이상은 한 번에 주로 약 " + hours(median) + "시간 이어졌어요.";
    }

    private static void addLongHighHumidity(List<String> checkPoints, List<Run> runs, LocalDateTime windowStart) {
        runs.stream()
                .filter(run -> run.duration().compareTo(LONG_HIGH_HUMIDITY) > 0)
                .max((a, b) -> a.duration().compareTo(b.duration()))
                .ifPresent(longest -> {
                    long day = ChronoUnit.DAYS.between(windowStart.toLocalDate(), longest.startedAt().toLocalDate()) + 1;
                    checkPoints.add("습도가 높은 상태로 오래 유지된 날이 있었어요.");
                    checkPoints.add(day + "일차에는 70% 이상인 상태가 약 " + hours(longest.duration()) + "시간 지속됐어요.");
                });
    }

    private static void addHumidityTooLow(List<String> checkPoints, List<Point> hums, Double humidityMin) {
        if (humidityMin == null) {
            return;
        }
        Duration below = accumulate(hums, point -> point.humidity() < humidityMin);
        if (below.compareTo(OUT_OF_RANGE_LIMIT) > 0) {
            checkPoints.add("습도가 권장 범위보다 낮게 유지된 시간이 있었어요.");
        }
    }

    // ---------------------------------------------------------------- 공통

    /** 조건에 맞는 측정부터 다음 측정까지의 시간을 더한다. 30분 넘게 끊긴 구간은 더하지 않는다. */
    private static Duration accumulate(List<Point> points, java.util.function.Predicate<Point> condition) {
        Duration total = Duration.ZERO;
        for (int i = 0; i + 1 < points.size(); i++) {
            Duration step = Duration.between(points.get(i).at(), points.get(i + 1).at());
            if (step.compareTo(MAX_DATA_GAP) <= 0 && condition.test(points.get(i))) {
                total = total.plus(step);
            }
        }
        return total;
    }

    private static double percentile(List<Double> sorted, double p) {
        return sorted.get((int) Math.min(sorted.size() - 1, Math.floor(p * sorted.size())));
    }

    private static String intRange(double min, double max) {
        long a = Math.round(min);
        long b = Math.round(max);
        return a == b ? String.valueOf(a) : a + "~" + b;
    }

    private static long decade(double value) {
        return (long) Math.floor(value / 10) * 10;
    }

    private static String oneDecimal(double value) {
        return String.format("%.1f", value);
    }

    private static long hours(Duration duration) {
        return Math.max(1, Math.round(duration.toMinutes() / 60.0));
    }
}
