package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideCheckItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PreAdoptionGuideCheckItemRepository extends JpaRepository<PreAdoptionGuideCheckItem, Long> {

    List<PreAdoptionGuideCheckItem> findAllByStep_StepIdOrderByItemOrderAsc(Long stepId);
}
