package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideCheckItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface PreAdoptionGuideCheckItemRepository extends JpaRepository<PreAdoptionGuideCheckItem, Long> {

    List<PreAdoptionGuideCheckItem> findAllByStep_StepIdOrderByItemOrderAsc(Long stepId);

    // 해당 가이드의 해당 단계 필수 체크 항목
    List<PreAdoptionGuideCheckItem> findAllByStep_PreAdoptionGuide_PreGuideIdAndStep_StepNumberAndRequiredTrue(
            Long preGuideId,
            Integer stepNumber
    );

    // 요청한 체크 항목 중 해당 가이드의 해당 단계에 속한 것만 조회
    List<PreAdoptionGuideCheckItem> findAllByCheckItemIdInAndStep_PreAdoptionGuide_PreGuideIdAndStep_StepNumber(
            Collection<Long> checkItemIds,
            Long preGuideId,
            Integer stepNumber
    );
}
