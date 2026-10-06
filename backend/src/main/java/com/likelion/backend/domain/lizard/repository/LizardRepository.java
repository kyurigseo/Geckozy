package com.likelion.backend.domain.lizard.repository;

import com.likelion.backend.domain.lizard.entity.Lizard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LizardRepository extends JpaRepository<Lizard, Long> {
    List<Lizard> findAllByUser_UserIdAndDeletedAtIsNull(Long userId);
    Optional<Lizard> findByLizardIdAndDeletedAtIsNull(Long lizardId);
}
