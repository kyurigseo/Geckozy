package com.likelion.backend.domain.enclosure.dto.crud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnclosureCreateResponse {
    private Long enclosureId;
    private Long lizardId;
}