package com.likelion.backend.domain.enclosure.entity;

import com.likelion.backend.domain.lizard.entity.Lizard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "enclosures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Enclosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enclosure_id")
    private Long id;

    // 1:1 관계 (Lizard당 사육장 1개, UK_ENCLOSURES_LIZARD)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lizard_id", nullable = false, unique = true)
    private Lizard lizard;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal width;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal height;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal depth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Material material;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Ventilation ventilation;

    @Column(name = "material_other", length = 100)
    private String materialOther;

    @Column(name = "wall_design", length = 30)
    private String wallDesign;

    @Column(name = "vine_design", length = 30)
    private String vineDesign;

    @Column(name = "floor_design", length = 30)
    private String floorDesign;

    @Column(name = "decoration_design", length = 30)
    private String decorationDesign;

    @Column(name = "concern_detail", length = 300)
    private String concernDetail;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- Enum 정의 ---

    public enum Material {
        GLASS, ACRYLIC, PVC, MESH, OTHER
    }

    public enum Ventilation {
        TOP, SIDE, BOTH, UNKNOWN
    }

    // --- 빌더 패턴 ---

    @Builder
    public Enclosure(Lizard lizard, BigDecimal width, BigDecimal height, BigDecimal depth,
                     Material material, Ventilation ventilation, String materialOther,
                     String wallDesign, String vineDesign, String floorDesign,
                     String decorationDesign, String concernDetail) {
        this.lizard = lizard;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.material = material;
        this.ventilation = ventilation;
        this.materialOther = materialOther;
        this.wallDesign = wallDesign;
        this.vineDesign = vineDesign;
        this.floorDesign = floorDesign;
        this.decorationDesign = decorationDesign;
        this.concernDetail = concernDetail;
    }
}