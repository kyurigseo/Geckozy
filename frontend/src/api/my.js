// TODO: 백엔드 API가 나오면 실제 요청으로 교체 (현재는 가짜 응답)
const FAKE_DELAY = 300

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

// 마이 탭 프로필 (대표 도마뱀). geckoColor: 온보딩 도마뱀 색 값 ('orange' | 'yellow' | 'blue' | 'green' | 'beige')
export const getMyProfile = async () => {
  await delay(FAKE_DELAY)
  return { name: '레오', species: '크레스티드 게코', tankName: '1번 사육장', geckoColor: 'yellow' }
}

// 마이 > 도마뱀 커스텀: 등록한 도마뱀 정보 (온보딩 form.gecko와 같은 모양)
export const getMyGecko = async () => {
  await delay(FAKE_DELAY)
  return {
    color: 'green',
    name: '레오',
    species: '크레스티드 게코',
    birthKnown: 'unknown',
    birthDate: '',
    gender: 'female',
    size: '6',
    weight: '26',
    sizeUnknown: false,
  }
}

export const updateMyGecko = async (gecko) => {
  await delay(FAKE_DELAY)
  return { success: true, gecko }
}

// 마이 > 사육장 커스텀: 등록한 사육장 정보 (온보딩 form.tank와 같은 모양)
export const getMyTank = async () => {
  await delay(FAKE_DELAY)
  return {
    wall: 'cream',
    floor: 'bark',
    vine: 'heart',
    structure: 'log',
    width: '45',
    depth: '45',
    height: '60',
    material: 'glass',
    materialCustom: '',
    ventilation: 'top',
  }
}

export const updateMyTank = async (tank) => {
  await delay(FAKE_DELAY)
  return { success: true, tank }
}
