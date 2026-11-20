package com.likelion.backend.domain.preadoptionguide.service;

import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse.BlockDto;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse.BlockItemDto;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse.CheckItemDto;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse.ContentDto;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse.DetailDto;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepResponse.SectionDto;
import com.likelion.backend.domain.preadoptionguide.entity.*;
import com.likelion.backend.domain.preadoptionguide.repository.*;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreAdoptionGuideStepService {

    private final UserRepository userRepository;
    private final PreAdoptionGuideRepository preAdoptionGuideRepository;
    private final PreAdoptionGuideStepRepository stepRepository;
    private final PreAdoptionGuideSectionRepository sectionRepository;
    private final PreAdoptionGuideDetailRepository detailRepository;
    private final PreAdoptionGuideDetailContentRepository contentRepository;
    private final PreAdoptionGuideDetailBlockRepository blockRepository;
    private final PreAdoptionGuideBlockItemRepository blockItemRepository;
    private final PreAdoptionGuideCheckItemRepository checkItemRepository;
    private final PreAdoptionGuideUserCheckRepository userCheckRepository;

    // 단계별 사전 입양 가이드 조회
    public PreAdoptionGuideStepResponse getStep(Long userId, Long preGuideId, Integer stepNumber) {

        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!preAdoptionGuideRepository.existsById(preGuideId)) {
            throw new BusinessException(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);
        }

        PreAdoptionGuideStep step = stepRepository
                .findByPreAdoptionGuide_PreGuideIdAndStepNumber(preGuideId, stepNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.GUIDE_STEP_NOT_FOUND));

        return PreAdoptionGuideStepResponse.builder()
                .preGuideId(preGuideId)
                .stepNumber(step.getStepNumber())
                .title(step.getTitle())
                .description(step.getDescription())
                .sections(buildSections(step.getStepId()))
                .checkItems(buildCheckItems(userId, step.getStepId()))
                .build();
    }

    private List<SectionDto> buildSections(Long stepId) {

        List<PreAdoptionGuideSection> sections =
                sectionRepository.findAllByStep_StepIdOrderBySectionOrderAsc(stepId);

        if (sections.isEmpty()) {
            return List.of();
        }

        List<PreAdoptionGuideDetail> details = detailRepository.findAllBySection_SectionIdInOrderByDetailOrderAsc(
                ids(sections, PreAdoptionGuideSection::getSectionId)
        );

        List<Long> detailIds = ids(details, PreAdoptionGuideDetail::getDetailId);

        List<PreAdoptionGuideDetailContent> contents = detailIds.isEmpty()
                ? List.of()
                : contentRepository.findAllByDetail_DetailIdInOrderByContentOrderAsc(detailIds);

        List<PreAdoptionGuideDetailBlock> blocks = detailIds.isEmpty()
                ? List.of()
                : blockRepository.findAllByDetail_DetailIdInOrderByBlockOrderAsc(detailIds);

        List<Long> blockIds = ids(blocks, PreAdoptionGuideDetailBlock::getBlockId);

        List<PreAdoptionGuideBlockItem> blockItems = blockIds.isEmpty()
                ? List.of()
                : blockItemRepository.findAllByBlock_BlockIdInOrderByItemOrderAsc(blockIds);

        // 조회 결과가 표시 순서로 정렬돼 있으므로 그룹으로 묶어도 순서가 유지된다
        Map<Long, List<PreAdoptionGuideDetail>> detailsBySection =
                groupBy(details, detail -> detail.getSection().getSectionId());
        Map<Long, List<PreAdoptionGuideDetailContent>> contentsByDetail =
                groupBy(contents, content -> content.getDetail().getDetailId());
        Map<Long, List<PreAdoptionGuideDetailBlock>> blocksByDetail =
                groupBy(blocks, block -> block.getDetail().getDetailId());
        Map<Long, List<PreAdoptionGuideBlockItem>> itemsByBlock =
                groupBy(blockItems, item -> item.getBlock().getBlockId());

        return sections.stream()
                .map(section -> SectionDto.builder()
                        .sectionId(section.getSectionId())
                        .title(section.getTitle())
                        .content(section.getContent())
                        .details(detailsBySection.getOrDefault(section.getSectionId(), List.of()).stream()
                                .map(detail -> toDetailDto(detail, contentsByDetail, blocksByDetail, itemsByBlock))
                                .toList())
                        .build())
                .toList();
    }

    private DetailDto toDetailDto(
            PreAdoptionGuideDetail detail,
            Map<Long, List<PreAdoptionGuideDetailContent>> contentsByDetail,
            Map<Long, List<PreAdoptionGuideDetailBlock>> blocksByDetail,
            Map<Long, List<PreAdoptionGuideBlockItem>> itemsByBlock
    ) {
        List<ContentDto> contents = contentsByDetail.getOrDefault(detail.getDetailId(), List.of()).stream()
                .map(content -> ContentDto.builder()
                        .contentId(content.getContentId())
                        .contentTitle(content.getContentTitle())
                        .badge(content.getBadge())
                        .contentText(content.getContentText())
                        .build())
                .toList();

        List<BlockDto> blocks = blocksByDetail.getOrDefault(detail.getDetailId(), List.of()).stream()
                .map(block -> BlockDto.builder()
                        .blockTitle(block.getBlockTitle())
                        .items(itemsByBlock.getOrDefault(block.getBlockId(), List.of()).stream()
                                .map(item -> new BlockItemDto(item.getItemContent()))
                                .toList())
                        .build())
                .toList();

        return DetailDto.builder()
                .question(detail.getQuestion())
                .contents(contents)
                .blocks(blocks)
                .build();
    }

    private List<CheckItemDto> buildCheckItems(Long userId, Long stepId) {

        List<PreAdoptionGuideCheckItem> checkItems =
                checkItemRepository.findAllByStep_StepIdOrderByItemOrderAsc(stepId);

        if (checkItems.isEmpty()) {
            return List.of();
        }

        Set<Long> checkedIds = userCheckRepository
                .findAllByUser_UserIdAndCheckItem_CheckItemIdIn(
                        userId,
                        ids(checkItems, PreAdoptionGuideCheckItem::getCheckItemId)
                )
                .stream()
                .filter(PreAdoptionGuideUserCheck::isChecked)
                .map(userCheck -> userCheck.getCheckItem().getCheckItemId())
                .collect(Collectors.toSet());

        return checkItems.stream()
                .map(item -> CheckItemDto.builder()
                        .checkItemId(item.getCheckItemId())
                        .groupTitle(item.getGroupTitle())
                        .checkTitle(item.getCheckTitle())
                        .checkDescription(item.getCheckDescription())
                        .itemOrder(item.getItemOrder())
                        .checked(checkedIds.contains(item.getCheckItemId()))
                        .build())
                .toList();
    }

    private static <T> List<Long> ids(List<T> entities, Function<T, Long> idGetter) {
        return entities.stream().map(idGetter).toList();
    }

    private static <T> Map<Long, List<T>> groupBy(List<T> entities, Function<T, Long> keyGetter) {
        return entities.stream().collect(Collectors.groupingBy(keyGetter));
    }
}
