package com.likelion.backend.domain.carerecord.repository;

import com.likelion.backend.domain.carerecord.entity.CareRecordCleaning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareRecordCleaningRepository
        extends JpaRepository<CareRecordCleaning, Long> {

    Optional<CareRecordCleaning> findByRecordId(Long recordId);
}