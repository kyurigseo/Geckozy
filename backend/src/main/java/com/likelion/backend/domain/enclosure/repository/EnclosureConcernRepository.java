package com.likelion.backend.domain.enclosure.repository;

import com.likelion.backend.domain.enclosure.entity.EnclosureConcern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnclosureConcernRepository extends JpaRepository<EnclosureConcern, EnclosureConcern.EnclosureConcernId> {
}