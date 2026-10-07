package com.likelion.backend.domain.management.repository;

import com.likelion.backend.domain.enclosure.entity.ManagementSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagementSettingRepository extends JpaRepository<ManagementSetting, Long> {
    Optional<ManagementSetting> findByEnclosureId(Long enclosureId);
}