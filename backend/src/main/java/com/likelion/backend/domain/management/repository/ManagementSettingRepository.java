package com.likelion.backend.domain.management.repository;

import com.likelion.backend.domain.management.entity.ManagementSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManagementSettingRepository extends JpaRepository<ManagementSetting, Long> {
    Optional<ManagementSetting> findByEnclosure_Id(Long enclosureId);
}