package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreAdoptionGuideRepository extends JpaRepository<PreAdoptionGuide, Long> {

    Optional<PreAdoptionGuide> findBySpecies_SpeciesId(Long speciesId);
}
