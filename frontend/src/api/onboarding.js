import { ApiError, request, setAccessToken } from './client'

// 에러 코드 → 화면에 보여줄 문구. 없는 코드는 각 함수의 기본 문구
const NETWORK_MESSAGE = '서버에 연결할 수 없어요. 잠시 후 다시 시도해주세요.'
const errorMessage = (error, messages, fallback) => {
  if (!(error instanceof ApiError)) throw error
  if (error.code === 'NETWORK_ERROR') return NETWORK_MESSAGE
  return messages[error.code] ?? fallback
}

// 로그인. 토큰을 저장하고, 등록한 도마뱀이 있으면 온보딩을 마친 사용자로 본다
// TODO: 서버에 온보딩 완료 여부가 생기면 그 값으로 교체 ('아직 준비 중이에요'로 둘러보기한 사용자는 지금은 매번 온보딩으로 감)
export const login = async ({ email, password }) => {
  try {
    const { accessToken } = await request('/auth/login', { method: 'POST', body: { email, password } })
    setAccessToken(accessToken)
    const lizards = await request('/lizards')
    return { success: true, user: { email, isOnboardingCompleted: lizards.length > 0 } }
  } catch (error) {
    return {
      success: false,
      message: errorMessage(
        error,
        {
          INVALID_CREDENTIALS: '이메일 또는 비밀번호가 맞지 않아요.',
          INVALID_REQUEST: '이메일 형식을 확인해주세요.',
        },
        '로그인에 실패했어요. 다시 시도해주세요.',
      ),
    }
  }
}

// 아이디 중복확인. available: 사용 가능 여부 (요청 실패면 null + message)
export const checkIdDuplicate = async (id) => {
  try {
    const { available } = await request('/auth/check-username', { params: { username: id } })
    return { available, id }
  } catch (error) {
    return {
      available: null,
      id,
      message: errorMessage(error, { INVALID_REQUEST: '아이디는 50자 이하로 입력해주세요.' }, '중복확인에 실패했어요. 다시 시도해주세요.'),
    }
  }
}

// 회원가입. 서버 아이디 이름은 username
export const signup = async ({ id, password, passwordConfirm, email }) => {
  try {
    const { userId } = await request('/auth/signup', {
      method: 'POST',
      body: { username: id, password, passwordConfirm, email },
    })
    return { success: true, user: { id: userId } }
  } catch (error) {
    return {
      success: false,
      message: errorMessage(
        error,
        {
          DUPLICATE_USERNAME: '이미 사용 중인 아이디예요.',
          // 서버 검사: 비밀번호 8자 이상, 비밀번호 확인 일치, 이메일 형식
          INVALID_REQUEST: '입력한 정보를 확인해주세요. (비밀번호 8자 이상, 비밀번호 확인 일치)',
        },
        '회원가입에 실패했어요. 다시 시도해주세요.',
      ),
    }
  }
}

// 종 이름 → 서버 종 번호 (speciesId)
// TODO: 백엔드에 번호 확인 후 채우기. '종 목록 조회' API가 생기면 서버에서 받아오기
const SPECIES_IDS = {
  '비어디드 드래곤': null,
  '크레스티드 게코': null,
  '레오파드 게코': null,
}

const toNumber = (value) => (value === '' || value === null || value === undefined ? null : Number(value))

// 온보딩 form.gecko → POST /lizards
const toLizardRequest = (gecko) => {
  const birthDateUnknown = gecko.birthKnown !== 'known'
  return {
    speciesId: SPECIES_IDS[gecko.species] ?? null,
    name: gecko.name,
    birthDate: birthDateUnknown ? null : gecko.birthDate,
    birthDateUnknown,
    gender: gecko.gender.toUpperCase(),
    characterColor: gecko.color,
    sizeCm: gecko.sizeUnknown ? null : toNumber(gecko.size),
    weightG: gecko.sizeUnknown ? null : toNumber(gecko.weight),
    sizeWeightUnknown: gecko.sizeUnknown,
  }
}

// 온보딩 form.tank + 고민 → POST /enclosures
const toEnclosureRequest = ({ tank, lizardId, concernIds, concernText }) => {
  const isCustomMaterial = tank.material === 'custom'
  return {
    lizardId,
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
    concernIds,
    concernDetail: concernText.trim() || null,
  }
}

const SPRAYING_METHODS = { manual: 'MANUAL', auto: 'AUTOMATIC', both: 'BOTH' }
// TODO: '정해진 주기 없음'(irregular)은 서버 값이 없어서 빈 값으로 보냄. 백엔드에 값 추가 요청 필요
const SPRAYING_FREQUENCIES = { once: 'ONCE_A_DAY', twice: 'TWICE_A_DAY', over3: 'THREE_OR_MORE_A_DAY', irregular: null }
const HEATING_MODES = { scheduled: 'SCHEDULED', auto: 'AUTO_BY_TEMPERATURE', asNeeded: 'WHEN_NEEDED', none: 'NOT_USED' }

// 온보딩 form.care → PUT /enclosures/{id}/management
const toManagementRequest = (care) => {
  const hasLight = care.lightTime && care.lightTime !== 'none'
  const hasHeatingTime = care.heatingMethod === 'scheduled' && care.heatingTime
  return {
    sprayingMethod: SPRAYING_METHODS[care.mistMethod] ?? null,
    sprayingFrequency: SPRAYING_FREQUENCIES[care.mistFrequency] ?? null,
    sprayingTimes: care.mistTimes.map((time) => time.toUpperCase()),
    lightingEnabled: Boolean(hasLight),
    lightingStartTime: hasLight ? care.lightTime.start : null,
    lightingEndTime: hasLight ? care.lightTime.end : null,
    heatingUsageMode: HEATING_MODES[care.heatingMethod] ?? null,
    heatingStartTime: hasHeatingTime ? care.heatingTime.start : null,
    heatingEndTime: hasHeatingTime ? care.heatingTime.end : null,
  }
}

// 고른 고민 이름 → 서버 고민 번호 (서버 고민 목록과 이름으로 맞춤)
const toConcernIds = async (concerns) => {
  if (concerns.length === 0) return []
  const { concerns: serverConcerns } = await request('/concerns')
  return concerns.map((name) => serverConcerns.find((concern) => concern.name === name)?.concernId).filter(Boolean)
}

// 온보딩 입력값 저장: 도마뱀 등록 → 사육장 등록(고민 포함) → 관리 방식 등록 (계정 정보는 보내지 않음)
// TODO: 중간에 실패하면 다시 시도할 때 도마뱀이 두 번 등록되지 않게 처리
export const saveOnboarding = async ({ gecko, tank, care, concerns, concernText }) => {
  try {
    const { lizardId } = await request('/lizards', { method: 'POST', body: toLizardRequest(gecko) })
    const concernIds = await toConcernIds(concerns)
    const { enclosureId } = await request('/enclosures', {
      method: 'POST',
      body: toEnclosureRequest({ tank, lizardId, concernIds, concernText }),
    })
    await request(`/enclosures/${enclosureId}/management`, { method: 'PUT', body: toManagementRequest(care) })
    return { success: true, lizardId, enclosureId }
  } catch (error) {
    return { success: false, message: errorMessage(error, {}, '정보 저장에 실패했어요. 다시 시도해주세요.') }
  }
}
