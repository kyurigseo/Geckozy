package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideSection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PreAdoptionGuideSectionRepository extends JpaRepository<PreAdoptionGuideSection, Long> {

    List<PreAdoptionGuideSection> findAllByStep_StepIdOrderBySectionOrderAsc(Long stepId);
}
