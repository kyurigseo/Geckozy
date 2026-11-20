package com.likelion.backend.global.init;

import com.likelion.backend.domain.preadoptionguide.entity.*;
import com.likelion.backend.domain.preadoptionguide.repository.*;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 사전 입양 가이드와 단계별 콘텐츠(섹션, 세부 안내, 체크 항목)를 넣는다.
 * 가이드가 없을 때만 가이드를, 단계가 하나도 없을 때만 단계 콘텐츠를 INSERT 한다.
 * DataInitializer 트랜잭션 안에서 호출된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class PreAdoptionGuideSeeder {

    private final SpeciesRepository speciesRepository;
    private final PreAdoptionGuideRepository guideRepository;
    private final PreAdoptionGuideStepRepository stepRepository;
    private final PreAdoptionGuideSectionRepository sectionRepository;
    private final PreAdoptionGuideDetailRepository detailRepository;
    private final PreAdoptionGuideDetailContentRepository contentRepository;
    private final PreAdoptionGuideDetailBlockRepository blockRepository;
    private final PreAdoptionGuideBlockItemRepository blockItemRepository;
    private final PreAdoptionGuideCheckItemRepository checkItemRepository;

    void seed() {
        speciesRepository.findByName(PreAdoptionGuideSeedData.SPECIES_NAME).ifPresent(species -> {
            PreAdoptionGuide guide = guideRepository.findBySpecies_SpeciesId(species.getSpeciesId())
                    .orElseGet(() -> {
                        log.info("[DataInitializer] 사전 입양 가이드 추가: {}", species.getName());
                        return guideRepository.save(
                                PreAdoptionGuide.builder()
                                        .species(species)
                                        .title(PreAdoptionGuideSeedData.GUIDE_TITLE)
                                        .description(PreAdoptionGuideSeedData.GUIDE_DESCRIPTION)
                                        .build()
                        );
                    });

            if (!stepRepository.existsByPreAdoptionGuide_PreGuideId(guide.getPreGuideId())) {
                log.info("[DataInitializer] 사전 입양 가이드 단계 콘텐츠 추가: {}", species.getName());
                PreAdoptionGuideSeedData.STEPS.forEach(step -> seedStep(guide, step));
            }
        });
    }

    private void seedStep(PreAdoptionGuide guide, PreAdoptionGuideSeedData.Step seed) {
        PreAdoptionGuideStep step = stepRepository.save(
                PreAdoptionGuideStep.builder()
                        .preAdoptionGuide(guide)
                        .stepNumber(seed.number())
                        .title(seed.title())
                        .description(seed.description())
                        .build()
        );

        forEachIndexed(seed.sections(), (sectionSeed, sectionOrder) -> {
            PreAdoptionGuideSection section = sectionRepository.save(
                    PreAdoptionGuideSection.builder()
                            .step(step)
                            .title(sectionSeed.title())
                            .content(sectionSeed.content())
                            .sectionOrder(sectionOrder)
                            .build()
            );

            forEachIndexed(sectionSeed.details(), (detailSeed, detailOrder) -> {
                PreAdoptionGuideDetail detail = detailRepository.save(
                        PreAdoptionGuideDetail.builder()
                                .section(section)
                                .question(detailSeed.question())
                                .detailOrder(detailOrder)
                                .build()
                );

                forEachIndexed(detailSeed.contents(), (contentSeed, contentOrder) ->
                        contentRepository.save(
                                PreAdoptionGuideDetailContent.builder()
                                        .detail(detail)
                                        .contentTitle(contentSeed.title())
                                        .badge(contentSeed.badge())
                                        .contentText(contentSeed.text())
                                        .contentOrder(contentOrder)
                                        .build()
                        ));

                forEachIndexed(detailSeed.blocks(), (blockSeed, blockOrder) -> {
                    PreAdoptionGuideDetailBlock block = blockRepository.save(
                            PreAdoptionGuideDetailBlock.builder()
                                    .detail(detail)
                                    .blockTitle(blockSeed.title())
                                    .blockOrder(blockOrder)
                                    .build()
                    );

                    forEachIndexed(blockSeed.items(), (itemContent, itemOrder) ->
                            blockItemRepository.save(
                                    PreAdoptionGuideBlockItem.builder()
                                            .block(block)
                                            .itemContent(itemContent)
                                            .itemOrder(itemOrder)
                                            .build()
                            ));
                });
            });
        });

        forEachIndexed(seed.checkItems(), (checkSeed, itemOrder) ->
                checkItemRepository.save(
                        PreAdoptionGuideCheckItem.builder()
                                .step(step)
                                .groupTitle(checkSeed.group())
                                .checkTitle(checkSeed.title())
                                .checkDescription(checkSeed.description())
                                .itemOrder(itemOrder)
                                .required(true)
                                .build()
                ));
    }

    // 1부터 시작하는 표시 순서와 함께 순회
    private static <T> void forEachIndexed(List<T> list, java.util.function.BiConsumer<T, Integer> action) {
        for (int i = 0; i < list.size(); i++) {
            action.accept(list.get(i), i + 1);
        }
    }
}
