package com.likelion.backend.domain.preadoptionguide.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PreAdoptionGuideStepResponse {

    private Long preGuideId;
    private Integer stepNumber;
    private String title;
    private String description;
    private List<SectionDto> sections;
    private List<CheckItemDto> checkItems;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SectionDto {
        private Long sectionId;
        private String title;
        private String content;
        // 카드 안에 바로 보이는 강조 블록 (예: 설치할 때 확인해주세요)
        private List<BlockDto> blocks;
        private List<DetailDto> details;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DetailDto {
        private String question;
        private List<ContentDto> contents;
        private List<BlockDto> blocks;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ContentDto {
        private Long contentId;
        private String contentTitle;
        private String badge;
        private String contentText;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class BlockDto {
        private String blockTitle;
        private List<BlockItemDto> items;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class BlockItemDto {
        private String itemContent;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CheckItemDto {
        private Long checkItemId;
        private String groupTitle;
        private String checkTitle;
        private String checkDescription;
        private Integer itemOrder;
        private Boolean checked;
    }
}
