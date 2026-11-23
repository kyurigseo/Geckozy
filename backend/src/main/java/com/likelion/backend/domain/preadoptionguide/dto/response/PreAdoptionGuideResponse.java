package com.likelion.backend.domain.preadoptionguide.dto.response;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PreAdoptionGuideResponse {

    private Long preGuideId;
    private Long speciesId;
    private String title;
    private String description;

    public static PreAdoptionGuideResponse of(PreAdoptionGuide guide, Long speciesId) {
        return PreAdoptionGuideResponse.builder()
                .preGuideId(guide.getPreGuideId())
                .speciesId(speciesId)
                .title(guide.getTitle())
                .description(guide.getDescription())
                .build();
    }
}
