package com.likelion.backend.domain.lizard.repository;

import com.likelion.backend.domain.lizard.entity.Lizard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LizardRepository extends JpaRepository<Lizard, Long> {
}