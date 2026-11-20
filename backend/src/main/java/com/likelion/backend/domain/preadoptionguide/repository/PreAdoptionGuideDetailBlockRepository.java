package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideDetailBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface PreAdoptionGuideDetailBlockRepository extends JpaRepository<PreAdoptionGuideDetailBlock, Long> {

    List<PreAdoptionGuideDetailBlock> findAllByDetail_DetailIdInOrderByBlockOrderAsc(Collection<Long> detailIds);
}
