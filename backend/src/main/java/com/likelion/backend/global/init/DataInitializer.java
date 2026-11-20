package com.likelion.backend.global.init;

import com.likelion.backend.domain.concern.entity.Concern;
import com.likelion.backend.domain.concern.repository.ConcernRepository;
import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuide;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideRepository;
import com.likelion.backend.domain.species.entity.Species;
import com.likelion.backend.domain.species.entity.SpeciesEnvironmentStandard;
import com.likelion.backend.domain.species.repository.SpeciesEnvironmentStandardRepository;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 서버 시작 시 기준 데이터(종, 환경 기준, 사전 입양 가이드, 고민 항목)를 넣는다.
 * 이름 기준으로 없을 때만 INSERT 하므로 여러 번 재시작해도 중복되지 않는다.
 *
 * 빈 DB 기준 ID
 *  - species : 1 비어디드 드래곤 / 2 크레스티드 게코 / 3 레오파드 게코
 *  - concerns: 1 온도 관리 ~ 8 사육 환경 (CONCERNS 순서)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SpeciesRepository speciesRepository;
    private final SpeciesEnvironmentStandardRepository standardRepository;
    private final ConcernRepository concernRepository;
    private final PreAdoptionGuideRepository preAdoptionGuideRepository;

    private static final List<String> CONCERNS = List.of(
            "온도 관리",
            "습도 조절",
            "분무 시점",
            "환기",
            "탈피",
            "먹이 급여",
            "센서 / 장비 사용",
            "사육 환경"
    );

    private record SpeciesSeed(
            String name,
            double dayMin, double dayMax,
            double nightMin, double nightMax,
            String temperatureCaution,
            double humidityMin, double humidityMax,
            String humidityCaution
    ) {
    }

    private static final List<SpeciesSeed> SPECIES = List.of(
            new SpeciesSeed(
                    "비어디드 드래곤",
                    26, 32,
                    20, 24,
                    "주간에는 바스킹 스팟(38~42°C)을 따로 마련해 주세요. 사육장 전체가 35°C 이상으로 올라가면 과열 위험이 있어요.",
                    30, 40,
                    "건조한 환경을 좋아해요. 습도가 50% 이상으로 오래 유지되면 호흡기 질환이 생길 수 있어요."
            ),
            new SpeciesSeed(
                    "크레스티드 게코",
                    22, 26,
                    20, 24,
                    "28°C 이상의 고온에 지속적으로 노출될 경우 열사병 위험이 있으니 주의하세요.",
                    50, 80,
                    "저녁 분무로 80%까지 올린 뒤 낮에는 50% 정도로 마르게 해주세요. 계속 습하면 곰팡이가 생길 수 있어요."
            ),
            new SpeciesSeed(
                    "레오파드 게코",
                    26, 32,
                    20, 24,
                    "바닥 히팅으로 따뜻한 구역(31~33°C)과 서늘한 구역을 함께 만들어 주세요. 35°C 이상은 위험해요.",
                    30, 40,
                    "건조한 편이 좋지만, 탈피 시기에는 습식 은신처(웻박스)를 넣어 주세요."
            )
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedSpecies();
        seedPreAdoptionGuides();
        seedConcerns();
    }

    private void seedSpecies() {
        for (SpeciesSeed seed : SPECIES) {
            Species species = speciesRepository.findByName(seed.name())
                    .orElseGet(() -> {
                        log.info("[DataInitializer] species 추가: {}", seed.name());
                        return speciesRepository.save(new Species(seed.name()));
                    });

            if (standardRepository.findBySpecies_SpeciesId(species.getSpeciesId()).isEmpty()) {
                log.info("[DataInitializer] 환경 기준 추가: {}", seed.name());
                standardRepository.save(
                        SpeciesEnvironmentStandard.builder()
                                .species(species)
                                .dayTemperatureMin(BigDecimal.valueOf(seed.dayMin()))
                                .dayTemperatureMax(BigDecimal.valueOf(seed.dayMax()))
                                .nightTemperatureMin(BigDecimal.valueOf(seed.nightMin()))
                                .nightTemperatureMax(BigDecimal.valueOf(seed.nightMax()))
                                .temperatureCaution(seed.temperatureCaution())
                                .humidityMin(BigDecimal.valueOf(seed.humidityMin()))
                                .humidityMax(BigDecimal.valueOf(seed.humidityMax()))
                                .humidityCaution(seed.humidityCaution())
                                .build()
                );
            }
        }
    }

    private void seedPreAdoptionGuides() {
        for (SpeciesSeed seed : SPECIES) {
            speciesRepository.findByName(seed.name()).ifPresent(species -> {
                if (preAdoptionGuideRepository.findBySpecies_SpeciesId(species.getSpeciesId()).isEmpty()) {
                    log.info("[DataInitializer] 사전 입양 가이드 추가: {}", seed.name());
                    preAdoptionGuideRepository.save(
                            PreAdoptionGuide.builder()
                                    .species(species)
                                    .title(seed.name() + " 입양 준비")
                                    .description("입양 전 사육 환경을 준비해요.")
                                    .build()
                    );
                }
            });
        }
    }

    private void seedConcerns() {
        for (String name : CONCERNS) {
            if (!concernRepository.existsByName(name)) {
                log.info("[DataInitializer] concern 추가: {}", name);
                concernRepository.save(Concern.builder().name(name).build());
            }
        }
    }
}
