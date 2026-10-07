package com.likelion.backend.domain.enclosure.dto;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class EnclosureCreateRequest {

    private Long lizardId;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal depth;
    private Enclosure.Material material;
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

    @Getter
    @NoArgsConstructor
    public static class ComponentDto {
        private String componentCategory;
        private String componentName;
    }
}