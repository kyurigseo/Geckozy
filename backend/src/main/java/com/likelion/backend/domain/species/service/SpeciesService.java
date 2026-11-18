package com.likelion.backend.domain.species.service;

import com.likelion.backend.domain.species.dto.SpeciesListResponse;
import com.likelion.backend.domain.species.dto.SpeciesHumidityCycleGuideResponse;
import com.likelion.backend.domain.species.dto.SpeciesTemperatureStandardResponse;
import com.likelion.backend.domain.species.entity.SpeciesEnvironmentStandard;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.domain.species.repository.SpeciesEnvironmentStandardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpeciesService {

    private final SpeciesRepository speciesRepository;
    private final SpeciesEnvironmentStandardRepository speciesEnvironmentStandardRepository;

    // 도마뱀 종 목록 조회
    public List<SpeciesListResponse> getSpeciesList() {

        return speciesRepository.findAll(
                        Sort.by(Sort.Direction.ASC, "speciesId")
                )
                .stream()
                .map(SpeciesListResponse::from)
                .toList();
    }

    // 종별 온도 기준 조회
    public SpeciesTemperatureStandardResponse getTemperatureStandards(
            Long speciesId
    ) {

        SpeciesEnvironmentStandard standard =
                speciesEnvironmentStandardRepository
                        .findBySpecies_SpeciesId(speciesId)
                        .orElse(null);

        if (standard == null
                || standard.getDayTemperatureMin() == null
                || standard.getDayTemperatureMax() == null
                || standard.getNightTemperatureMin() == null
                || standard.getNightTemperatureMax() == null) {

            log.info("온도 기준 미등록 종 조회 - speciesId: {}", speciesId);

            return SpeciesTemperatureStandardResponse.unregistered(
                    speciesId,
                    "해당 종의 권장 온도 데이터는 현재 준비 중입니다. 일반 기준을 임의로 대체 적용하지 않으니 사육 시 주의해 주세요."
            );
        }

        log.info("종별 온도 기준 조회 - speciesId: {}", speciesId);

        return SpeciesTemperatureStandardResponse.registered(
                speciesId,
                standard.getSpecies().getName(),
                standard.getDayTemperatureMin().doubleValue(),
                standard.getDayTemperatureMax().doubleValue(),
                standard.getNightTemperatureMin().doubleValue(),
                standard.getNightTemperatureMax().doubleValue(),
                standard.getTemperatureCaution()
        );
    }

    // 습윤 사이클 가이드 조회
    public SpeciesHumidityCycleGuideResponse getHumidityCycleGuides(
            Long speciesId
    ) {

        SpeciesEnvironmentStandard standard =
                speciesEnvironmentStandardRepository
                        .findBySpecies_SpeciesId(speciesId)
                        .orElse(null);

        if (standard == null
                || standard.getHumidityMin() == null
                || standard.getHumidityMax() == null
                || standard.getHumidityMin().stripTrailingZeros().scale() > 0
                || standard.getHumidityMax().stripTrailingZeros().scale() > 0
                || !"크레스티드 게코".equals(
                standard.getSpecies().getName()
        )) {

            log.info("습윤 사이클 가이드 미등록 - speciesId: {}", speciesId);

            return SpeciesHumidityCycleGuideResponse.withoutGuide(
                    speciesId,
                    "해당 종의 습윤 사이클 가이드 콘텐츠는 현재 준비 중입니다."
            );
        }

        int humidityMin = standard.getHumidityMin().intValueExact();
        int humidityMax = standard.getHumidityMax().intValueExact();

        log.info("습윤 사이클 가이드 조회 - speciesId: {}", speciesId);

        return SpeciesHumidityCycleGuideResponse.withGuide(
                speciesId,
                standard.getSpecies().getName(),

                SpeciesHumidityCycleGuideResponse.HumidityRangeDto.builder()
                        .min(humidityMin)
                        .max(humidityMax)
                        .build(),

                List.of(
                        SpeciesHumidityCycleGuideResponse.CycleStepDto.builder()
                                .step(1)
                                .title("저녁 분무 (습도 상승)")
                                .description(
                                        "사육장 벽면과 구조물에 미온수를 분무하여 습도를 "
                                                + humidityMax + "% 수준으로 끌어올립니다."
                                )
                                .build(),

                        SpeciesHumidityCycleGuideResponse.CycleStepDto.builder()
                                .step(2)
                                .title("야간 활동 및 수분 섭취")
                                .description(
                                        "개체가 벽면에 맺힌 물방울을 핥아 마시며 야간 활동을 합니다."
                                )
                                .build(),

                        SpeciesHumidityCycleGuideResponse.CycleStepDto.builder()
                                .step(3)
                                .title("주간 건조 사이클 (자연 건조)")
                                .description(
                                        "환기를 통해 낮 동안 습도가 "
                                                + humidityMin
                                                + "% 수준까지 서서히 건조되도록 유지합니다."
                                )
                                .build(),

                        SpeciesHumidityCycleGuideResponse.CycleStepDto.builder()
                                .step(4)
                                .title("다음 분무 준비")
                                .description(
                                        "사육장 내부가 충분히 건조된 것을 확인한 후 다음 분무 사이클을 준비합니다."
                                )
                                .build()
                )
        );
    }
}