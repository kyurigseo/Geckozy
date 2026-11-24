package com.likelion.backend.domain.sensor.mock;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import com.likelion.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * [테스트 전용] 프론트가 직접 가상 측정값을 채울 수 있는 API.
 * mock-sensor.enabled=true 일 때만 등록되며, 꺼져 있으면 404.
 */
@Tag(name = "[TEST] 가상 센서", description = "실제 센서 없이 화면 테스트용 측정값 생성 (운영 시 비활성화)")
@RestController
@RequestMapping("/test/enclosures/{enclosureId}/sensor")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mock-sensor.enabled", havingValue = "true")
public class MockSensorController {

    private final MockSensorService mockSensorService;
    private final EnclosureRepository enclosureRepository;

    @Operation(summary = "가상 측정값 생성",
            description = "최근 hours시간(기본 24, 최대 72) 동안 10분 간격 온도·습도 값을 새로 생성. 센서가 없으면 가상 센서를 연결")
    @PostMapping("/mock-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generate(
            @AuthenticationPrincipal Long currentUserId,
            @PathVariable Long enclosureId,
            @RequestParam(defaultValue = "24") int hours
    ) {
        Enclosure enclosure = enclosureRepository.findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));
        if (!enclosure.getLizard().getUser().getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (hours < 1 || hours > 72) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        int count = mockSensorService.regenerate(enclosureId, hours);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "enclosureId", enclosureId,
                "generated", count,
                "intervalMinutes", MockSensorService.INTERVAL.toMinutes()
        )));
    }
}
