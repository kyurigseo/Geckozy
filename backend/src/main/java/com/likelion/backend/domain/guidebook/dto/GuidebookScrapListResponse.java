package com.likelion.backend.domain.guidebook.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GuidebookScrapListResponse {

    private List<GuidebookScrapItemDto> guidebooks;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class GuidebookScrapItemDto {
        private Long guidebookId;
        private String title;
        private String thumbnailUrl;
        private String scrappedAt;
    }
}
