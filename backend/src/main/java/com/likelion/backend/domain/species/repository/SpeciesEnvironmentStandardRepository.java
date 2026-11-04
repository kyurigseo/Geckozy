package com.likelion.backend.domain.species.repository;

import com.likelion.backend.domain.species.entity.SpeciesEnvironmentStandard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpeciesEnvironmentStandardRepository extends JpaRepository<SpeciesEnvironmentStandard, Long> {
    Optional<SpeciesEnvironmentStandard> findBySpecies_SpeciesId(Long speciesId);
}