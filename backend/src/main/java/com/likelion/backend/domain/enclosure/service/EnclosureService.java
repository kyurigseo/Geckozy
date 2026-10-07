package com.likelion.backend.domain.enclosure.service;

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
}
