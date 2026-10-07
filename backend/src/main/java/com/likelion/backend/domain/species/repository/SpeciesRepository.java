package com.likelion.backend.domain.species.repository;

import com.likelion.backend.domain.species.entity.Species;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeciesRepository extends JpaRepository<Species, Long> {
}