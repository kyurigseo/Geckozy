package com.likelion.backend.domain.concern.dto;

import com.likelion.backend.domain.concern.entity.Concern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConcernResponse {
    private Long concernId;
    private String name;

    public static ConcernResponse from(Concern concern) {
        return ConcernResponse.builder()
                .concernId(concern.getId())
                .name(concern.getName())
                .build();
    }
}