package com.likelion.backend.domain.carerecord.repository;

import com.likelion.backend.domain.carerecord.entity.CareRecordAbnormalBehavior;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareRecordAbnormalBehaviorRepository
        extends JpaRepository<CareRecordAbnormalBehavior, Long> {

    Optional<CareRecordAbnormalBehavior> findByRecordId(Long recordId);
}