package com.likelion.backend.domain.enclosure.repository;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnclosureRepository extends JpaRepository<Enclosure, Long> {
}