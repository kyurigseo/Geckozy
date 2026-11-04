package com.likelion.backend.domain.guidebook.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GuidebookPageResponse {

    private Long guidebookId;
    private String title;
    private int currentPage;
    private int totalPages;
    private PageContent content;
    private boolean hasPrevious;
    private boolean hasNext;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class PageContent {
        private String title;
        private String description;
        private String imageUrl;
    }
}
