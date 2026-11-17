import {
  ApiError,
  errorMessage,
  request,
} from './client'

import {
  UNSUPPORTED_SPECIES_MESSAGE,
  toSpeciesId,
} from './species'

import {
  getGuideLizards,
  getSelectedLizard,
  loadGuideAccountContext,
} from './guide'

// 선택한 도마뱀의 실제 프로필
export const getMyProfile = async () => {
  const {
    lizard,
    tankName,
  } = await loadGuideAccountContext()

  if (!lizard) return null

  return {
    name: lizard.name,
    species: lizard.species,
    tankName,
    geckoColor: String(
      lizard.characterColor || 'beige',
    ).toLowerCase(),
  }
}

// 항상 첫 번째가 아니라 현재 선택한 도마뱀을 사용합니다.
const getMainLizardId = async () => {
  const lizards = await getGuideLizards()

  if (lizards.length === 0) {
    throw new ApiError(404, 'LIZARD_NOT_FOUND')
  }

  return getSelectedLizard(lizards).lizardId
}

const toNumber = (value) =>
  value === '' ||
  value === null ||
  value === undefined
    ? null
    : Number(value)

const toInputText = (value) =>
  value === null || value === undefined
    ? ''
    : String(Number(value))

const LOAD_FAIL_MESSAGES = {
  LIZARD_NOT_FOUND:
    '등록한 도마뱀을 찾을 수 없어요.',
  ENCLOSURE_NOT_FOUND:
    '등록한 사육장을 찾을 수 없어요.',
}

export const getMyGecko = async () => {
  const lizard = await request(
    `/lizards/${await getMainLizardId()}`,
  )

  return {
    // 저장할 때 조회했던 개체를 정확히 수정하기 위한 ID
    lizardId: lizard.lizardId,
    color: lizard.characterColor,
    name: lizard.name,
    species: lizard.species,
    birthKnown: lizard.birthDateUnknown
      ? 'unknown'
      : 'known',
    birthDate: lizard.birthDateUnknown
      ? ''
      : lizard.birthDate ?? '',
    gender: lizard.gender.toLowerCase(),
    size: toInputText(lizard.sizeCm),
    weight: toInputText(lizard.weightG),
    sizeUnknown: lizard.sizeWeightUnknown,
  }
}

// 색 변경도 기존 개체 PATCH만 실행합니다.
// 신규 도마뱀 POST는 호출하지 않습니다.
export const updateMyGecko = async (gecko) => {
  const speciesId = toSpeciesId(gecko.species)

  if (speciesId === null) {
    return {
      success: false,
      message: UNSUPPORTED_SPECIES_MESSAGE,
    }
  }

  const birthDateUnknown =
    gecko.birthKnown !== 'known'

  try {
    const id =
      gecko.lizardId ?? await getMainLizardId()

    await request(`/lizards/${id}`, {
      method: 'PATCH',
      body: {
        speciesId,
        name: gecko.name,
        birthDate: birthDateUnknown
          ? null
          : gecko.birthDate,
        birthDateUnknown,
        gender: gecko.gender.toUpperCase(),
        characterColor: gecko.color,
        sizeCm: gecko.sizeUnknown
          ? null
          : toNumber(gecko.size),
        weightG: gecko.sizeUnknown
          ? null
          : toNumber(gecko.weight),
      },
    })

    return {
      success: true,
      gecko,
    }
  } catch (error) {
    return {
      success: false,
      message: errorMessage(
        error,
        {
          ...LOAD_FAIL_MESSAGES,
          SPECIES_NOT_FOUND:
            UNSUPPORTED_SPECIES_MESSAGE,
        },
        '저장에 실패했어요. 다시 시도해주세요.',
      ),
    }
  }
}

const getMyEnclosure = async () =>
  request('/enclosures', {
    params: {
      lizardId: await getMainLizardId(),
    },
  })

export const getMyTank = async () => {
  const enclosure = await getMyEnclosure()
  const isCustomMaterial =
    enclosure.material === 'OTHER'

  return {
    wall: enclosure.wallDesign ?? '',
    floor: enclosure.floorDesign ?? '',
    vine: enclosure.vineDesign ?? '',
    structure: enclosure.decorationDesign ?? '',
    width: toInputText(enclosure.width),
    depth: toInputText(enclosure.depth),
    height: toInputText(enclosure.height),
    material: isCustomMaterial
      ? 'custom'
      : enclosure.material.toLowerCase(),
    materialCustom: enclosure.materialOther ?? '',
    ventilation:
      enclosure.ventilation.toLowerCase(),
  }
}

// 기존 커스텀 저장 방식 유지.
// concernIds 보존은 서버 조회 응답 보완이 필요한 상태입니다.
export const updateMyTank = async (tank) => {
  const isCustomMaterial =
    tank.material === 'custom'

  try {
    const {
      enclosureId,
      concernDetail,
    } = await getMyEnclosure()

    await request(`/enclosures/${enclosureId}`, {
      method: 'PATCH',
      body: {
        width: toNumber(tank.width),
        height: toNumber(tank.height),
        depth: toNumber(tank.depth),
        material: isCustomMaterial
          ? 'OTHER'
          : tank.material.toUpperCase(),
        materialOther: isCustomMaterial
          ? tank.materialCustom
          : null,
        ventilation:
          tank.ventilation.toUpperCase(),
        wallDesign: tank.wall || null,
        floorDesign: tank.floor || null,
        vineDesign: tank.vine || null,
        decorationDesign: tank.structure || null,
        concernDetail,
      },
    })

    return {
      success: true,
      tank,
    }
  } catch (error) {
    return {
      success: false,
      message: errorMessage(
        error,
        LOAD_FAIL_MESSAGES,
        '저장에 실패했어요. 다시 시도해주세요.',
      ),
    }
  }
}