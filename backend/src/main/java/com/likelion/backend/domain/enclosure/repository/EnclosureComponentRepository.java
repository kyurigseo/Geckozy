package com.likelion.backend.domain.enclosure.repository;

import com.likelion.backend.domain.enclosure.entity.EnclosureComponent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnclosureComponentRepository extends JpaRepository<EnclosureComponent, Long> {
}