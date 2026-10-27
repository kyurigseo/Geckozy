package com.likelion.backend.domain.enclosure.repository;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnclosureRepository extends JpaRepository<Enclosure, Long> {
    Optional<Enclosure> findByLizard_LizardId(Long lizardId);
}