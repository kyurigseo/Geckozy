package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuideUserCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PreAdoptionGuideUserCheckRepository extends JpaRepository<PreAdoptionGuideUserCheck, Long> {

    List<PreAdoptionGuideUserCheck> findAllByUser_UserIdAndCheckItem_CheckItemIdIn(Long userId, Collection<Long> checkItemIds);

    /*
     * 체크 상태 저장 (없으면 INSERT, 있으면 UPDATE).
     * 체크박스를 빠르게 여러 번 누르면 같은 항목 저장 요청이 동시에 올 수 있어서,
     * "조회 후 INSERT" 대신 한 문장으로 처리해 (user_id, check_item_id) 유니크 제약 충돌이 나지 않게 한다.
     * MySQL / H2(MODE=MySQL) 모두 지원하는 문법.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO pre_adoption_guide_user_checks (user_id, check_item_id, checked, updated_at)
            VALUES (:userId, :checkItemId, :checked, :now)
            ON DUPLICATE KEY UPDATE checked = :checked, updated_at = :now
            """, nativeQuery = true)
    void upsert(
            @Param("userId") Long userId,
            @Param("checkItemId") Long checkItemId,
            @Param("checked") boolean checked,
            @Param("now") LocalDateTime now
    );
}
