// 종 이름 → 서버 종 번호 (speciesId). 온보딩·마이 도마뱀 커스텀에서 같이 쓴다
// 서버 DataInitializer가 넣는 순서대로 1~3 (2026-11-17 서버에서 확인). 화면 종 목록 중 나머지는 아직 서버에 없음
// TODO: '종 목록 조회' API가 생기면 서버에서 받아오기
const SPECIES_IDS = {
  '비어디드 드래곤': 1,
  '크레스티드 게코': 2,
  '레오파드 게코': 3,
}

export const UNSUPPORTED_SPECIES_MESSAGE = '아직 지원하지 않는 종이에요.'

// 번호가 없는 종이면 null
export const toSpeciesId = (speciesName) => SPECIES_IDS[speciesName] ?? null
