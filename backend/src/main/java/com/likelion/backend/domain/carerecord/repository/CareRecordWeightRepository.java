package com.likelion.backend.domain.carerecord.repository;

import com.likelion.backend.domain.carerecord.entity.CareRecordWeight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareRecordWeightRepository
        extends JpaRepository<CareRecordWeight, Long> {

    Optional<CareRecordWeight> findByRecordId(Long recordId);

    List<CareRecordWeight> findAllByRecordIdIn(List<Long> recordIds);
}