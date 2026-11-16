// 도마뱀 커스텀 입력값 규칙 (온보딩·마이 공통)

// 진행바에 반영하는 항목 (입력을 마친 개수 / 전체)
export const GECKO_PROGRESS_CHECKS = [
  (g) => g.color,
  (g) => g.name.trim(),
  (g) => g.species,
  (g) => g.birthKnown === 'unknown' || g.birthDate,
  (g) => g.gender,
  (g) => g.sizeUnknown || g.size.trim() || g.weight.trim(),
]

// 필수 항목 (색상·이름·종·성별)
export const GECKO_REQUIRED_CHECKS = [(g) => g.color, (g) => g.name.trim(), (g) => g.species, (g) => g.gender]
export const GECKO_REQUIRED_MESSAGE = '색상, 이름, 도마뱀 종, 성별을 입력해주세요.'

export const isGeckoComplete = (gecko) => GECKO_REQUIRED_CHECKS.every((check) => check(gecko))
export const getGeckoProgress = (gecko) =>
  GECKO_PROGRESS_CHECKS.filter((check) => check(gecko)).length / GECKO_PROGRESS_CHECKS.length
