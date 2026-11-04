package com.likelion.backend.domain.enclosure.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EnclosureGuidebookRecommendationResponse {

    private List<GuidebookDto> recommendations;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class GuidebookDto {
        private Long guidebookId;
        private String title;
        private String keyword;
        private String reason;
    }

    public static EnclosureGuidebookRecommendationResponse of(List<GuidebookDto> recommendations) {
        return EnclosureGuidebookRecommendationResponse.builder()
                .recommendations(recommendations != null ? recommendations : Collections.emptyList())
                .build();
    }
}
