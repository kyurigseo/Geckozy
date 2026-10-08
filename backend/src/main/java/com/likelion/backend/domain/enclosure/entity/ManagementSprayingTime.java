package com.likelion.backend.domain.enclosure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "management_spraying_times")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ManagementSprayingTime {

    @EmbeddedId
    private ManagementSprayingTimeId id;

    @MapsId("managementSettingId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "management_setting_id")
    private ManagementSetting managementSetting;

    public ManagementSprayingTime(ManagementSetting managementSetting, SprayingTime sprayingTime) {
        this.managementSetting = managementSetting;
        this.id = new ManagementSprayingTimeId(managementSetting.getManagementSettingId(), sprayingTime);
    }

    public enum SprayingTime {
        MORNING, DAY, EVENING, NIGHT
    }

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ManagementSprayingTimeId implements Serializable {
        private Long managementSettingId;

        @Enumerated(EnumType.STRING)
        @Column(name = "spraying_time")
        private SprayingTime sprayingTime;
    }
}