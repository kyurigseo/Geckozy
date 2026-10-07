package com.likelion.backend.domain.enclosure.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EnclosureCreateResponse {
    private Long enclosureId;
    private Long lizardId;
}