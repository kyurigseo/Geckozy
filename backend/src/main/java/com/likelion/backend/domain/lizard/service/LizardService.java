package com.likelion.backend.domain.lizard.service;

import com.likelion.backend.domain.lizard.dto.response.LizardListResponse;
import com.likelion.backend.domain.lizard.dto.response.LizardResponse;
import com.likelion.backend.domain.lizard.dto.request.LizardUpdateRequest;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import com.likelion.backend.domain.lizard.dto.request.LizardCreateRequest;
import com.likelion.backend.domain.lizard.entity.Lizard;
import com.likelion.backend.domain.lizard.repository.LizardRepository;
import com.likelion.backend.domain.species.entity.Species;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.domain.lizard.dto.response.LizardHomeResponse;
import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.repository.SensorRepository;
import com.likelion.backend.domain.species.entity.SpeciesEnvironmentStandard;
import com.likelion.backend.domain.species.repository.SpeciesEnvironmentStandardRepository;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LizardService {

    private final LizardRepository lizardRepository;
    private final SpeciesRepository speciesRepository;
    private final UserRepository userRepository;
    private final EnclosureRepository enclosureRepository;
    private final SensorRepository sensorRepository;
    private final SpeciesEnvironmentStandardRepository speciesEnvironmentStandardRepository;

    @Transactional
    public Long createLizard(
            Long userId,
            LizardCreateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        Species species = speciesRepository.findById(request.speciesId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SPECIES_NOT_FOUND));

        Lizard lizard = new Lizard(
                user,
                species,
                request.name(),
                request.birthDate(),
                request.birthDateUnknown(),
                request.characterColor(),
                request.gender(),
                request.sizeCm(),
                request.weightG(),
                request.sizeWeightUnknown()
        );

        Lizard savedLizard = lizardRepository.save(lizard);

        return savedLizard.getLizardId();
    }

    public List<LizardListResponse> getLizards(Long userId) {

        return lizardRepository.findAllByUser_UserIdAndDeletedAtIsNull(userId)
                .stream()
                .map(lizard -> new LizardListResponse(
                        lizard.getLizardId(),
                        lizard.getName(),
                        lizard.getSpecies().getName()
                ))
                .toList();
    }

    // 도마뱀 단건 조회
    public LizardResponse getLizard(Long userId, Long lizardId) {

        Lizard lizard = findMyLizard(userId, lizardId);

        return new LizardResponse(
                lizard.getLizardId(),
                lizard.getSpecies().getSpeciesId(),
                lizard.getSpecies().getName(),
                lizard.getName(),
                lizard.getBirthDate(),
                lizard.isBirthDateUnknown(),
                lizard.getCharacterColor(),
                lizard.getGender(),
                lizard.getSizeCm(),
                lizard.getWeightG(),
                lizard.isSizeWeightUnknown()
        );
    }

    // 사육개체 선택 및 홈 정보 조회
    public LizardHomeResponse getLizardHome(
            Long userId,
            Long lizardId
    ) {

        // 1. 도마뱀 존재 여부 및 소유권 검증
        Lizard lizard = findMyLizard(userId, lizardId);

        Species species = lizard.getSpecies();

        // 2. 사육장 조회
        Enclosure enclosure = enclosureRepository
                .findByLizard_LizardId(lizardId)
                .orElse(null);

        LizardHomeResponse.EnclosureInfo enclosureInfo = null;
        LizardHomeResponse.SensorInfo sensorInfo = null;

        // 3. 사육장이 등록된 경우 센서 조회
        if (enclosure != null) {

            enclosureInfo = new LizardHomeResponse.EnclosureInfo(
                    enclosure.getId()
            );

            Sensor sensor = sensorRepository
                    .findByEnclosure_Id(enclosure.getId())
                    .orElse(null);

            if (sensor != null) {

                sensorInfo = new LizardHomeResponse.SensorInfo(
                        sensor.getSensorId(),
                        sensor.getStatus()
                );
            }
        }

        // 4. 종별 환경 기준 조회
        SpeciesEnvironmentStandard standard =
                speciesEnvironmentStandardRepository
                        .findBySpecies_SpeciesId(species.getSpeciesId())
                        .orElse(null);

        LizardHomeResponse.TemperatureStandard temperatureStandard = null;
        LizardHomeResponse.Range humidityStandard = null;

        if (standard != null) {

            LizardHomeResponse.Range day = createRange(
                    standard.getDayTemperatureMin(),
                    standard.getDayTemperatureMax()
            );

            LizardHomeResponse.Range night = createRange(
                    standard.getNightTemperatureMin(),
                    standard.getNightTemperatureMax()
            );

            if (day != null && night != null) {

                temperatureStandard =
                        new LizardHomeResponse.TemperatureStandard(
                                day,
                                night
                        );
            }

            humidityStandard = createRange(
                    standard.getHumidityMin(),
                    standard.getHumidityMax()
            );
        }

        // 5. 홈 정보 반환
        return new LizardHomeResponse(
                lizard.getLizardId(),
                lizard.getName(),
                species.getName(),
                enclosureInfo,
                sensorInfo,
                temperatureStandard,
                humidityStandard
        );
    }

    // 도마뱀 수정
    @Transactional
    public void updateLizard(
            Long userId,
            Long lizardId,
            LizardUpdateRequest request
    ) {
        Lizard lizard = findMyLizard(userId, lizardId);

        Species species = null;

        if (request.speciesId() != null) {
            species = speciesRepository.findById(request.speciesId())
                    .orElseThrow(() ->
                            new BusinessException(ErrorCode.SPECIES_NOT_FOUND)
                    );
        }

        lizard.update(
                species,
                request.name(),
                request.birthDate(),
                request.birthDateUnknown(),
                request.characterColor(),
                request.gender(),
                request.sizeCm(),
                request.weightG()
        );
    }

    // 도마뱀 삭제
    @Transactional
    public void deleteLizard(Long userId, Long lizardId) {

        Lizard lizard = findMyLizard(userId, lizardId);

        lizard.delete();
    }

    // 종별 환경 기준 범위 변환
    private LizardHomeResponse.Range createRange(
            BigDecimal min,
            BigDecimal max
    ) {

        if (min == null || max == null) {
            return null;
        }

        return new LizardHomeResponse.Range(
                min.doubleValue(),
                max.doubleValue()
        );
    }

    // 내 도마뱀인지 확인
    private Lizard findMyLizard(Long userId, Long lizardId) {

        Lizard lizard = lizardRepository.findByLizardIdAndDeletedAtIsNull(lizardId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.LIZARD_NOT_FOUND)
                );

        if (!lizard.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.LIZARD_NOT_FOUND);
        }

        return lizard;
    }

}
