package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideProgress;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PreAdoptionGuideProgressRepository extends JpaRepository<PreAdoptionGuideProgress, Long> {

    Optional<PreAdoptionGuideProgress> findByUser_UserIdAndPreAdoptionGuide_PreGuideId(Long userId, Long preGuideId);

    /*
     * 진행 상태를 수정하기 전에 행 잠금(SELECT ... FOR UPDATE)으로 조회한다.
     * - 완료 버튼 연타 등 같은 사용자의 수정 요청이 동시에 와도 하나씩 순서대로 처리된다.
     * - 잠금 조회는 항상 최신 커밋 값을 읽으므로, 다른 트랜잭션에서 방금 생성한 진행 상태도 보인다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p from PreAdoptionGuideProgress p
            where p.user.userId = :userId and p.preAdoptionGuide.preGuideId = :preGuideId
            """)
    Optional<PreAdoptionGuideProgress> findForUpdate(@Param("userId") Long userId, @Param("preGuideId") Long preGuideId);
}
