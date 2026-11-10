package com.likelion.backend.domain.carerecord.repository;

import com.likelion.backend.domain.carerecord.entity.CareRecordStool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareRecordStoolRepository
        extends JpaRepository<CareRecordStool, Long> {

    Optional<CareRecordStool> findByRecordId(Long recordId);
}