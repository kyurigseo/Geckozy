package com.likelion.backend.domain.carerecord.repository;

import com.likelion.backend.domain.carerecord.entity.CareRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CareRecordRepository extends JpaRepository<CareRecord, Long> {

    // 도마뱀의 특정 날짜 범위에 해당하는 기록 조회
    List<CareRecord> findAllByLizard_LizardIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
            Long lizardId,
            LocalDateTime start,
            LocalDateTime end
    );

    // 특정 도마뱀의 기록을 날짜순으로 조회
    List<CareRecord> findAllByLizard_LizardIdOrderByRecordedAtDesc(Long lizardId);

    // 기록 ID와 사용자 ID를 기준으로 본인 소유 기록인지 확인
    Optional<CareRecord> findByRecordIdAndLizard_User_UserId(
            Long recordId,
            Long userId
    );

    // 특정 도마뱀에 해당하는 기록 존재 여부
    boolean existsByRecordIdAndLizard_LizardId(
            Long recordId,
            Long lizardId
    );
}