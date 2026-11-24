package com.likelion.backend.domain.sensor.mock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;

/**
 * [테스트 전용] mock-sensor.enclosure-ids 에 지정한 사육장에 10분마다 가상 측정값을 넣는다.
 * 서버 시작 직후 첫 실행에서 최근 24시간 공백을 채운다.
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mock-sensor.enabled", havingValue = "true")
public class MockSensorScheduler {

    private final MockSensorService mockSensorService;

    @Value("${mock-sensor.enclosure-ids:}")
    private String enclosureIds;

    @Scheduled(initialDelay = 30_000, fixedRate = 600_000) // 시작 30초 후, 이후 10분마다
    public void tick() {
        for (Long id : parseIds()) {
            try {
                int count = mockSensorService.fillUntilNow(id);
                if (count > 1) log.info("[MOCK_SENSOR] 사육장 {} 측정값 {}건 생성", id, count);
            } catch (Exception e) {
                log.warn("[MOCK_SENSOR] 사육장 {} 생성 실패: {}", id, e.getMessage());
            }
        }
    }

    private List<Long> parseIds() {
        if (enclosureIds == null || enclosureIds.isBlank()) return List.of();
        return Arrays.stream(enclosureIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
    }
}
