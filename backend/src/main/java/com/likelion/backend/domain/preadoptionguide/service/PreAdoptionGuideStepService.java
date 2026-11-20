package com.likelion.backend.domain.preadoptionguide.service;

import com.likelion.backend.domain.preadoptionguide.dto.request.PreAdoptionGuideCheckSaveRequest;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideCheckSaveResponse;
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
import com.likelion.backend.global.util.KstTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final PreAdoptionGuideBlockRepository blockRepository;
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

    /*
     * 단계별 체크리스트 상태 저장
     * - 요청한 체크 항목이 모두 해당 가이드의 해당 단계에 속해야 한다. 하나라도 아니면 CHECK_ITEM_NOT_FOUND
     * - 같은 항목이 요청에 두 번 들어오면 어느 값을 저장할지 모호하므로 INVALID_REQUEST
     * - 저장은 항목별 upsert (없으면 생성, 있으면 덮어쓰기)
     */
    @Transactional
    public PreAdoptionGuideCheckSaveResponse saveChecks(
            Long userId,
            Long preGuideId,
            Integer stepNumber,
            PreAdoptionGuideCheckSaveRequest request
    ) {

        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!preAdoptionGuideRepository.existsById(preGuideId)) {
            throw new BusinessException(ErrorCode.PRE_ADOPTION_GUIDE_NOT_FOUND);
        }

        Set<Long> requestedIds = request.checks().stream()
                .map(PreAdoptionGuideCheckSaveRequest.CheckDto::checkItemId)
                .collect(Collectors.toSet());

        if (requestedIds.size() != request.checks().size()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        List<PreAdoptionGuideCheckItem> checkItems = checkItemRepository
                .findAllByCheckItemIdInAndStep_PreAdoptionGuide_PreGuideIdAndStep_StepNumber(
                        requestedIds, preGuideId, stepNumber
                );

        if (checkItems.size() != requestedIds.size()) {
            throw new BusinessException(ErrorCode.CHECK_ITEM_NOT_FOUND);
        }

        LocalDateTime now = KstTime.now();
        request.checks().forEach(check ->
                userCheckRepository.upsert(userId, check.checkItemId(), check.checked(), now)
        );

        return new PreAdoptionGuideCheckSaveResponse(request.checks().size());
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

        // 강조 블록은 안내 카드에 바로 붙은 것과 세부 안내 팝업 안에 있는 것 두 종류
        List<PreAdoptionGuideBlock> sectionBlocks = blockRepository.findAllBySection_SectionIdInOrderByBlockOrderAsc(
                ids(sections, PreAdoptionGuideSection::getSectionId)
        );

        List<PreAdoptionGuideBlock> detailBlocks = detailIds.isEmpty()
                ? List.of()
                : blockRepository.findAllByDetail_DetailIdInOrderByBlockOrderAsc(detailIds);

        List<Long> blockIds = new ArrayList<>(ids(sectionBlocks, PreAdoptionGuideBlock::getBlockId));
        blockIds.addAll(ids(detailBlocks, PreAdoptionGuideBlock::getBlockId));

        List<PreAdoptionGuideBlockItem> blockItems = blockIds.isEmpty()
                ? List.of()
                : blockItemRepository.findAllByBlock_BlockIdInOrderByItemOrderAsc(blockIds);

        // 조회 결과가 표시 순서로 정렬돼 있으므로 그룹으로 묶어도 순서가 유지된다
        Map<Long, List<PreAdoptionGuideDetail>> detailsBySection =
                groupBy(details, detail -> detail.getSection().getSectionId());
        Map<Long, List<PreAdoptionGuideDetailContent>> contentsByDetail =
                groupBy(contents, content -> content.getDetail().getDetailId());
        Map<Long, List<PreAdoptionGuideBlock>> blocksBySection =
                groupBy(sectionBlocks, block -> block.getSection().getSectionId());
        Map<Long, List<PreAdoptionGuideBlock>> blocksByDetail =
                groupBy(detailBlocks, block -> block.getDetail().getDetailId());
        Map<Long, List<PreAdoptionGuideBlockItem>> itemsByBlock =
                groupBy(blockItems, item -> item.getBlock().getBlockId());

        return sections.stream()
                .map(section -> SectionDto.builder()
                        .sectionId(section.getSectionId())
                        .title(section.getTitle())
                        .content(section.getContent())
                        .blocks(toBlockDtos(blocksBySection.getOrDefault(section.getSectionId(), List.of()), itemsByBlock))
                        .details(detailsBySection.getOrDefault(section.getSectionId(), List.of()).stream()
                                .map(detail -> toDetailDto(detail, contentsByDetail, blocksByDetail, itemsByBlock))
                                .toList())
                        .build())
                .toList();
    }

    private DetailDto toDetailDto(
            PreAdoptionGuideDetail detail,
            Map<Long, List<PreAdoptionGuideDetailContent>> contentsByDetail,
            Map<Long, List<PreAdoptionGuideBlock>> blocksByDetail,
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

        return DetailDto.builder()
                .question(detail.getQuestion())
                .contents(contents)
                .blocks(toBlockDtos(blocksByDetail.getOrDefault(detail.getDetailId(), List.of()), itemsByBlock))
                .build();
    }

    private List<BlockDto> toBlockDtos(
            List<PreAdoptionGuideBlock> blocks,
            Map<Long, List<PreAdoptionGuideBlockItem>> itemsByBlock
    ) {
        return blocks.stream()
                .map(block -> BlockDto.builder()
                        .blockTitle(block.getBlockTitle())
                        .items(itemsByBlock.getOrDefault(block.getBlockId(), List.of()).stream()
                                .map(item -> new BlockItemDto(item.getItemContent()))
                                .toList())
                        .build())
                .toList();
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
