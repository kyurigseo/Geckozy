package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideBlockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface PreAdoptionGuideBlockItemRepository extends JpaRepository<PreAdoptionGuideBlockItem, Long> {

    List<PreAdoptionGuideBlockItem> findAllByBlock_BlockIdInOrderByItemOrderAsc(Collection<Long> blockIds);
}
