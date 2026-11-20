package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideDetailContent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface PreAdoptionGuideDetailContentRepository extends JpaRepository<PreAdoptionGuideDetailContent, Long> {

    List<PreAdoptionGuideDetailContent> findAllByDetail_DetailIdInOrderByContentOrderAsc(Collection<Long> detailIds);
}
