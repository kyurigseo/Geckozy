package com.likelion.backend.domain.carerecord.repository;

import com.likelion.backend.domain.carerecord.entity.CareRecordFeeding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareRecordFeedingRepository
        extends JpaRepository<CareRecordFeeding, Long> {

    Optional<CareRecordFeeding> findByRecordId(Long recordId);
}