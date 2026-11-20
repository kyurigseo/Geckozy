package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideUserCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface PreAdoptionGuideUserCheckRepository extends JpaRepository<PreAdoptionGuideUserCheck, Long> {

    List<PreAdoptionGuideUserCheck> findAllByUser_UserIdAndCheckItem_CheckItemIdIn(Long userId, Collection<Long> checkItemIds);
}
