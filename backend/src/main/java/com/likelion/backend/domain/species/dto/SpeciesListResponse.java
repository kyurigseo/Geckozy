package com.likelion.backend.domain.species.dto;

import com.likelion.backend.domain.species.entity.Species;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpeciesListResponse {

    private final Long speciesId;
    private final String name;

    public static SpeciesListResponse from(Species species) {
        return new SpeciesListResponse(
                species.getSpeciesId(),
                species.getName()
        );
    }
}