package com.likelion.backend.domain.management.repository;

import com.likelion.backend.domain.enclosure.entity.ManagementSprayingTime;
import com.likelion.backend.domain.management.entity.ManagementSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManagementSprayingTimeRepository extends JpaRepository<ManagementSprayingTime, ManagementSprayingTime.ManagementSprayingTimeId> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ManagementSprayingTime t where t.managementSetting = :setting")
    void deleteByManagementSetting(@Param("setting") ManagementSetting setting);
}
