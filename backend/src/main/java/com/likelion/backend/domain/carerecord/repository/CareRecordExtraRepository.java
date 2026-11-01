package com.likelion.backend.domain.carerecord.repository;

import com.likelion.backend.domain.carerecord.entity.CareRecordExtra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareRecordExtraRepository
        extends JpaRepository<CareRecordExtra, Long> {

    Optional<CareRecordExtra> findByRecordId(Long recordId);
}