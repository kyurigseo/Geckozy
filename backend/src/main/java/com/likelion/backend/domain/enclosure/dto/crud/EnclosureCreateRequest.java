package com.likelion.backend.domain.enclosure.dto.crud;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.entity.EnclosureComponent;
import com.likelion.backend.domain.lizard.entity.Lizard;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class EnclosureCreateRequest {

    @NotNull(message = "도마뱀 ID는 필수입니다.")
    private Long lizardId;
    @NotNull(message = "사육장 가로 크기는 필수입니다.")
    private BigDecimal width;
    @NotNull(message = "사육장 높이 크기는 필수입니다.")
    private BigDecimal height;
    @NotNull(message = "사육장 세로 크기는 필수입니다.")
    private BigDecimal depth;
    @NotNull(message = "사육장 재질은 필수입니다.")
    private Enclosure.Material material;
    @NotNull(message = "환기 구조는 필수입니다.")
    private Enclosure.Ventilation ventilation;
    private String materialOther;

    // 커스텀 디자인 요소
    private String wallDesign;
    private String vineDesign;
    private String floorDesign;
    private String decorationDesign;

    // 고민 관련 정보
    private List<Long> concernIds;
    private String concernDetail;

    // 사육장 구성품 목록
    private List<ComponentDto> components;

    public Enclosure toEntity(Lizard lizard) {
        return Enclosure.builder()
                .lizard(lizard)
                .width(width)
                .height(height)
                .depth(depth)
                .material(material)
                .ventilation(ventilation)
                .materialOther(materialOther)
                .wallDesign(wallDesign)
                .vineDesign(vineDesign)
                .floorDesign(floorDesign)
                .decorationDesign(decorationDesign)
                .concernDetail(concernDetail)
                .build();
    }

    @Getter
    @NoArgsConstructor
    public static class ComponentDto {
        private String componentCategory;
        private String componentName;

        public EnclosureComponent toEntity(Enclosure enclosure) {
            return EnclosureComponent.builder()
                    .enclosure(enclosure)
                    .componentCategory(componentCategory)
                    .componentName(componentName)
                    .build();
        }
    }
}