package com.likelion.backend.domain.enclosure.service;

import com.likelion.backend.domain.enclosure.dto.EnclosureHumiditySuggestionResponse;
import com.likelion.backend.domain.enclosure.dto.EnclosureTemperatureSuggestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnclosureService {

    public EnclosureTemperatureSuggestionResponse getTemperatureSuggestions(Long enclosureId) {
        return EnclosureTemperatureSuggestionResponse.builder()
                .enclosureId(enclosureId)
                .primaryAction("CHECK_SENSOR_LOCATION_AND_VENTILATE")
                .actionTitle("센서 위치 점검 및 환기 상태 확인 권장")
                .actionGuide("센서 주변부 열 집중이나 외풍으로 인해 측정 편차가 발생했을 수 있습니다. 사육장 내 핫존과 쿨존의 실제 체감 온도를 직접 점검한 뒤 필요시 통풍구를 확보해 주세요.")
                .disclaimer("단일 센서 측정값은 부착 위치에 따라 사육장 전체 온도를 단정할 수 없으므로 다각도 점검이 필요합니다.")
                .build();
    }

    public EnclosureHumiditySuggestionResponse getHumiditySuggestions(Long enclosureId) {
        return EnclosureHumiditySuggestionResponse.builder()
                .enclosureId(enclosureId)
                .primaryAction("CHECK_SUBSTRATE_MOISTURE_BEFORE_SPRAYING")
                .actionTitle("바닥재 잔여 수분 직접 확인 후 분무 여부 결정")
                .actionGuide("수치가 낮게 표시되더라도 바닥재 하부에 수분이 남아있을 수 있습니다. 무조건적인 분무 대신 바닥재를 손으로 직접 만져 확인한 후 필요 시에만 가볍게 분무해 주세요.")
                .isAutomatedSprayingRecommended(false)
                .build();
    }
}
