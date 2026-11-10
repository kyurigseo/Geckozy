package com.likelion.backend.domain.carerecord.repository;

import com.likelion.backend.domain.carerecord.entity.CareRecordShedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareRecordSheddingRepository
        extends JpaRepository<CareRecordShedding, Long> {

    Optional<CareRecordShedding> findByRecordId(Long recordId);
}