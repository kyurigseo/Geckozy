package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface PreAdoptionGuideDetailRepository extends JpaRepository<PreAdoptionGuideDetail, Long> {

    List<PreAdoptionGuideDetail> findAllBySection_SectionIdInOrderByDetailOrderAsc(Collection<Long> sectionIds);
}
