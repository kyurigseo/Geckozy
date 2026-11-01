package com.likelion.backend.domain.carerecord.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "care_record_photos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_care_record_photo_order",
                        columnNames = {"record_id", "photo_order"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long photoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private CareRecord careRecord;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    @Column(name = "photo_order", nullable = false)
    private Integer photoOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CareRecordPhoto(
            CareRecord careRecord,
            String imageUrl,
            Integer photoOrder
    ) {
        this.careRecord = careRecord;
        this.imageUrl = imageUrl;
        this.photoOrder = photoOrder;
    }
}