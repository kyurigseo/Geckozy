import { ApiError, errorMessage, request } from './client'
import { UNSUPPORTED_SPECIES_MESSAGE, toSpeciesId } from './species'

// TODO: 마이 탭 프로필은 아직 가짜 응답 (서버에 사육장 이름이 없음)
const FAKE_DELAY = 300

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

// 마이 탭 프로필 (대표 도마뱀). geckoColor: 온보딩 도마뱀 색 값 ('orange' | 'yellow' | 'blue' | 'green' | 'beige')
export const getMyProfile = async () => {
  await delay(FAKE_DELAY)
  return { name: '레오', species: '크레스티드 게코', tankName: '1번 사육장', geckoColor: 'yellow' }
}

// 대표 도마뱀 = 처음 등록한 도마뱀 (온보딩에서 한 마리만 등록)
// TODO: 도마뱀 여러 마리를 고를 수 있게 되면 선택한 도마뱀 번호로 교체
const getMainLizardId = async () => {
  const lizards = await request('/lizards')
  if (lizards.length === 0) throw new ApiError(404, 'LIZARD_NOT_FOUND')
  return lizards[0].lizardId
}

const toNumber = (value) => (value === '' || value === null || value === undefined ? null : Number(value))
// 서버 숫자(30.00) → 입력칸 문자열('30'). 없으면 ''
const toInputText = (value) => (value === null || value === undefined ? '' : String(Number(value)))

const LOAD_FAIL_MESSAGES = { LIZARD_NOT_FOUND: '등록한 도마뱀을 찾을 수 없어요.', ENCLOSURE_NOT_FOUND: '등록한 사육장을 찾을 수 없어요.' }

// 마이 > 도마뱀 커스텀: GET /lizards/{id} → 온보딩 form.gecko와 같은 모양
export const getMyGecko = async () => {
  const lizard = await request(`/lizards/${await getMainLizardId()}`)
  return {
    color: lizard.characterColor,
    name: lizard.name,
    species: lizard.species,
    birthKnown: lizard.birthDateUnknown ? 'unknown' : 'known',
    // '모름'으로 바꿔도 서버는 예전 날짜를 남겨두므로 화면에는 비워서 보여준다
    birthDate: lizard.birthDateUnknown ? '' : (lizard.birthDate ?? ''),
    gender: lizard.gender.toLowerCase(),
    size: toInputText(lizard.sizeCm),
    weight: toInputText(lizard.weightG),
    sizeUnknown: lizard.sizeWeightUnknown,
  }
}

// PATCH /lizards/{id}. 서버는 빈 값(null)을 '안 바꿈'으로 처리함
// TODO: 크기·몸무게를 '모름'으로 되돌리는 값이 서버에 없음 (sizeWeightUnknown을 받지 않음) → 백엔드 요청
export const updateMyGecko = async (gecko) => {
  const speciesId = toSpeciesId(gecko.species)
  if (speciesId === null) return { success: false, message: UNSUPPORTED_SPECIES_MESSAGE }

  const birthDateUnknown = gecko.birthKnown !== 'known'
  try {
    await request(`/lizards/${await getMainLizardId()}`, {
      method: 'PATCH',
      body: {
        speciesId,
        name: gecko.name,
        birthDate: birthDateUnknown ? null : gecko.birthDate,
        birthDateUnknown,
        gender: gecko.gender.toUpperCase(),
        characterColor: gecko.color,
        sizeCm: gecko.sizeUnknown ? null : toNumber(gecko.size),
        weightG: gecko.sizeUnknown ? null : toNumber(gecko.weight),
      },
    })
    return { success: true, gecko }
  } catch (error) {
    return {
      success: false,
      message: errorMessage(
        error,
        { ...LOAD_FAIL_MESSAGES, SPECIES_NOT_FOUND: UNSUPPORTED_SPECIES_MESSAGE },
        '저장에 실패했어요. 다시 시도해주세요.',
      ),
    }
  }
}

const getMyEnclosure = async () => request('/enclosures', { params: { lizardId: await getMainLizardId() } })

// 마이 > 사육장 커스텀: GET /enclosures?lizardId= → 온보딩 form.tank와 같은 모양
export const getMyTank = async () => {
  const enclosure = await getMyEnclosure()
  const isCustomMaterial = enclosure.material === 'OTHER'
  return {
    wall: enclosure.wallDesign ?? '',
    floor: enclosure.floorDesign ?? '',
    vine: enclosure.vineDesign ?? '',
    structure: enclosure.decorationDesign ?? '',
    width: toInputText(enclosure.width),
    depth: toInputText(enclosure.depth),
    height: toInputText(enclosure.height),
    material: isCustomMaterial ? 'custom' : enclosure.material.toLowerCase(),
    materialCustom: enclosure.materialOther ?? '',
    ventilation: enclosure.ventilation.toLowerCase(),
  }
}

// PATCH /enclosures/{id}. 서버가 보낸 값으로 전부 덮어써서, 화면에 없는 고민 메모는 서버 값을 다시 보낸다
// TODO: 고른 고민(concernIds)은 조회 응답에 없어서 보낼 수 없음 → 저장하면 서버에서 지워짐. 백엔드에 조회 응답 추가 요청
export const updateMyTank = async (tank) => {
  const isCustomMaterial = tank.material === 'custom'
  try {
    const { enclosureId, concernDetail } = await getMyEnclosure()
    await request(`/enclosures/${enclosureId}`, {
      method: 'PATCH',
      body: {
        width: toNumber(tank.width),
        height: toNumber(tank.height),
        depth: toNumber(tank.depth),
        material: isCustomMaterial ? 'OTHER' : tank.material.toUpperCase(),
        materialOther: isCustomMaterial ? tank.materialCustom : null,
        ventilation: tank.ventilation.toUpperCase(),
        wallDesign: tank.wall || null,
        floorDesign: tank.floor || null,
        vineDesign: tank.vine || null,
        decorationDesign: tank.structure || null,
        concernDetail,
      },
    })
    return { success: true, tank }
  } catch (error) {
    return { success: false, message: errorMessage(error, LOAD_FAIL_MESSAGES, '저장에 실패했어요. 다시 시도해주세요.') }
  }
}
