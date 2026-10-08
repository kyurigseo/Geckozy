package com.likelion.backend.domain.management.repository;
import com.likelion.backend.domain.enclosure.entity.ManagementSprayingTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagementSprayingTimeRepository extends JpaRepository<ManagementSprayingTime, ManagementSprayingTime.ManagementSprayingTimeId> {
}
