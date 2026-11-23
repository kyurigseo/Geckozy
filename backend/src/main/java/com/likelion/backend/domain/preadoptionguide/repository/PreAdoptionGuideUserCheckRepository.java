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

    long countByUser_UserIdAndCheckItem_CheckItemIdInAndCheckedTrue(Long userId, Collection<Long> checkItemIds);

    /*
     * 체크 상태 저장 (없으면 INSERT, 있으면 UPDATE) - DB마다 문법이 달라서 둘로 나눔.
     * 직접 호출하지 말고 PreAdoptionGuideUserCheckUpserter를 통해 호출한다 (연결된 DB에 맞는 쪽을 골라 줌).
     *
     * 체크박스를 빠르게 여러 번 누르면 같은 항목 저장 요청이 동시에 올 수 있어서,
     * "조회 후 INSERT" 대신 한 문장으로 처리해 (user_id, check_item_id) 유니크 제약 충돌이 나지 않게 한다.
     */

    // MySQL (운영 서버)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO pre_adoption_guide_user_checks (user_id, check_item_id, checked, updated_at)
            VALUES (:userId, :checkItemId, :checked, :now)
            ON DUPLICATE KEY UPDATE checked = :checked, updated_at = :now
            """, nativeQuery = true)
    void upsertForMySql(
            @Param("userId") Long userId,
            @Param("checkItemId") Long checkItemId,
            @Param("checked") boolean checked,
            @Param("now") LocalDateTime now
    );

    // H2 (로컬 실행 / 테스트). MODE=MySQL 설정 여부와 관계없이 동작
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            MERGE INTO pre_adoption_guide_user_checks (user_id, check_item_id, checked, updated_at)
            KEY (user_id, check_item_id)
            VALUES (:userId, :checkItemId, :checked, :now)
            """, nativeQuery = true)
    void upsertForH2(
            @Param("userId") Long userId,
            @Param("checkItemId") Long checkItemId,
            @Param("checked") boolean checked,
            @Param("now") LocalDateTime now
    );
}
