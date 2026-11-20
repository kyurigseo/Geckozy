package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreAdoptionGuideProgressRepository extends JpaRepository<PreAdoptionGuideProgress, Long> {

    Optional<PreAdoptionGuideProgress> findByUser_UserIdAndPreAdoptionGuide_PreGuideId(Long userId, Long preGuideId);
}
