package com.likelion.backend.domain.species.service;

import com.likelion.backend.domain.species.dto.SpeciesTemperatureStandardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpeciesService {

    public SpeciesTemperatureStandardResponse getTemperatureStandards(Long speciesId) {
        boolean isRegistered = isSpeciesRegistered(speciesId);

        if (!isRegistered) {
            log.info("미등록 종 온도 기준 조회 요청 - speciesId: {}", speciesId);
            return SpeciesTemperatureStandardResponse.unregistered(
                    speciesId,
                    "해당 종의 권장 온도 데이터는 현재 준비 중입니다. 일반 기준을 임의로 대체 적용하지 않으니 사육 시 주의해 주세요."
            );
        }

        log.info("등록된 종 온도 기준 조회 요청 - speciesId: {}", speciesId);
        return SpeciesTemperatureStandardResponse.registered(
                speciesId,
                "크레스티드 게코", 
                22.0,            
                26.0,           
                20.0,           
                24.0,          
                "28°C 이상의 고온에 지속적으로 노출될 경우 열사병 위험이 있으니 주의하세요."
        );
    }


    private boolean isSpeciesRegistered(Long speciesId) {
        return speciesId != null && !speciesId.equals(999L);
    }
}
