package com.likelion.backend.domain.enclosure.repository;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnclosureRepository extends JpaRepository<Enclosure, Long> {

    Optional<Enclosure> findByLizard_LizardId(Long lizardId);

    default Enclosure getOrThrow(Long enclosureId) {
        return findById(enclosureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENCLOSURE_NOT_FOUND));
    }
}