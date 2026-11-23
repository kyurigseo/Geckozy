package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideStep;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PreAdoptionGuideStepRepository extends JpaRepository<PreAdoptionGuideStep, Long> {

    Optional<PreAdoptionGuideStep> findByPreAdoptionGuide_PreGuideIdAndStepNumber(Long preGuideId, Integer stepNumber);

    boolean existsByPreAdoptionGuide_PreGuideId(Long preGuideId);
}
