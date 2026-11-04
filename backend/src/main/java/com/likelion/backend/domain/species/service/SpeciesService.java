package com.likelion.backend.domain.species.service;

import com.likelion.backend.domain.species.dto.SpeciesHumidityCycleGuideResponse;
import com.likelion.backend.domain.species.dto.SpeciesTemperatureStandardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpeciesService {


    public SpeciesTemperatureStandardResponse getTemperatureStandards(Long speciesId) {
        boolean isRegistered = isSpeciesRegistered(speciesId);

        if (!isRegistered) {
            log.info("미등록 종 온도 기준 조회 요청 - speciesId: {}", speciesId);
            return SpeciesTemperatureStandardResponse.unregistered(
                    speciesId,
                    "해당 종의 권장 온도 데이터는 현재 준비 중입니다. 일반 기준을 임의로 대체 적용하지 않으니 사육 시 주의해 주세요."
            );
        }

        log.info("등록된 종 온도 기준 조회 요청 - speciesId: {}", speciesId);
        return SpeciesTemperatureStandardResponse.registered(
                speciesId,
                "크레스티드 게코", 
                22.0,          
                26.0,           
                20.0,           
                24.0,           
                "28°C 이상의 고온에 지속적으로 노출될 경우 열사병 위험이 있으니 주의하세요."
        );
    }


    public SpeciesHumidityCycleGuideResponse getHumidityCycleGuides(Long speciesId) {
        boolean hasGuideContent = checkGuideContentAvailability(speciesId);

        if (!hasGuideContent) {
            log.info("습윤 사이클 가이드 미보유 또는 미등록 종 조회 요청 - speciesId: {}", speciesId);
            return SpeciesHumidityCycleGuideResponse.withoutGuide(
                    speciesId,
                    "해당 종의 습윤 사이클 가이드 콘텐츠는 현재 준비 중입니다."
            );
        }

        log.info("습윤 사이클 가이드 조회 요청 - speciesId: {}", speciesId);
        return SpeciesHumidityCycleGuideResponse.withGuide(
                speciesId,
                "크레스티드 게코",
                SpeciesHumidityCycleGuideResponse.HumidityRangeDto.builder()
                        .min(50)
                        .max(80)
                        .build(),
                List.of(
                        SpeciesHumidityCycleGuideResponse.CycleStepDto.builder()
                                .step(1)
                                .title("저녁 분무 (습도 상승)")
                                .description("사육장 벽면과 구조물에 미온수를 분무하여 습도를 80% 이상으로 끌어올립니다.")
                                .build(),
                        SpeciesHumidityCycleGuideResponse.CycleStepDto.builder()
                                .step(2)
                                .title("야간 활동 및 수분 섭취")
                                .description("개체가 벽면에 맺힌 물방울을 핥아 마시며 야간 활동을 합니다.")
                                .build(),
                        SpeciesHumidityCycleGuideResponse.CycleStepDto.builder()
                                .step(3)
                                .title("주간 건조 사이클 (자연 건조)")
                                .description("환기를 통해 낮 동안 습도가 50% 수준까지 서서히 건조되도록 유지하여 곰팡이 및 호흡기 질환을 예방합니다.")
                                .build(),
                        SpeciesHumidityCycleGuideResponse.CycleStepDto.builder()
                                .step(4)
                                .title("다음 분무 준비")
                                .description("사육장 내부가 충분히 건조된 것을 확인한 후 다음 분무 사이클을 준비합니다.")
                                .build()
                )
        );
    }


    private boolean checkGuideContentAvailability(Long speciesId) {
        // Mock: 999L인 경우 미등록(가이드 없음)으로 간주
        return isSpeciesRegistered(speciesId);
    }

    private boolean isSpeciesRegistered(Long speciesId) {
        // Mock 로직: 999L을 미등록 종 ID로 가정
        return speciesId != null && !speciesId.equals(999L);
    }
}
