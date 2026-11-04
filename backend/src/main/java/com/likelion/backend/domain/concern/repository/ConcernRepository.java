package com.likelion.backend.domain.concern.repository;

import com.likelion.backend.domain.concern.entity.Concern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcernRepository extends JpaRepository<Concern, Long> {
}