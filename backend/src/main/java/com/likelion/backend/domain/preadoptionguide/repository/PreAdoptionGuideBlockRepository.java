package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface PreAdoptionGuideBlockRepository extends JpaRepository<PreAdoptionGuideBlock, Long> {

    List<PreAdoptionGuideBlock> findAllBySection_SectionIdInOrderByBlockOrderAsc(Collection<Long> sectionIds);

    List<PreAdoptionGuideBlock> findAllByDetail_DetailIdInOrderByBlockOrderAsc(Collection<Long> detailIds);
}
