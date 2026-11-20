package com.likelion.backend.domain.preadoptionguide.repository;

import com.likelion.backend.domain.preadoptionguide.entity.PreAdoptionGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PreAdoptionGuideRepository extends JpaRepository<PreAdoptionGuide, Long> {

    Optional<PreAdoptionGuide> findBySpecies_SpeciesId(Long speciesId);

    /*
     * 1단계(사육장 준비) 완료 조건:
     * 사용자의 (삭제되지 않은) 도마뱀 중 가이드와 같은 종인 도마뱀에 사육장이 등록돼 있는지
     */
    @Query("""
            select count(e) > 0 from Enclosure e
            where e.lizard.user.userId = :userId
              and e.lizard.deletedAt is null
              and e.lizard.species.speciesId = (
                  select g.species.speciesId from PreAdoptionGuide g where g.preGuideId = :preGuideId
              )
            """)
    boolean existsEnclosureForGuideSpecies(@Param("userId") Long userId, @Param("preGuideId") Long preGuideId);

    /*
     * 7일 환경 점검 대상 사육장: 1단계와 같은 기준(가이드 종의 삭제되지 않은 도마뱀 사육장).
     * 여러 개면 가장 최근에 등록한 사육장이 맨 앞.
     */
    @Query("""
            select e.id from Enclosure e
            where e.lizard.user.userId = :userId
              and e.lizard.deletedAt is null
              and e.lizard.species.speciesId = (
                  select g.species.speciesId from PreAdoptionGuide g where g.preGuideId = :preGuideId
              )
            order by e.createdAt desc, e.id desc
            """)
    List<Long> findEnclosureIdsForGuideSpecies(@Param("userId") Long userId, @Param("preGuideId") Long preGuideId);
}
