package com.likelion.backend.domain.carerecord.repository;

import com.likelion.backend.domain.carerecord.entity.CareRecordPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareRecordPhotoRepository
        extends JpaRepository<CareRecordPhoto, Long> {

    // 기록에 첨부된 사진을 순서대로 조회
    List<CareRecordPhoto> findAllByCareRecord_RecordIdOrderByPhotoOrderAsc(
            Long recordId
    );

    // 기록에 첨부된 사진 개수
    long countByCareRecord_RecordId(Long recordId);

    // 사진 ID와 기록 ID로 사진 조회
    Optional<CareRecordPhoto> findByPhotoIdAndCareRecord_RecordId(
            Long photoId,
            Long recordId
    );

    // 기록에 연결된 사진 전체 삭제
    void deleteAllByCareRecord_RecordId(Long recordId);
}