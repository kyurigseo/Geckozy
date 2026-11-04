// 사육장 커스텀 입력값 규칙 (온보딩·마이 공통)

// 진행바에 반영하는 항목 (입력을 마친 개수 / 전체)
export const TANK_PROGRESS_CHECKS = [
  (t) => t.wall,
  (t) => t.floor,
  (t) => t.vine,
  (t) => t.structure,
  (t) => t.width && t.depth && t.height,
  (t) => (t.material === 'custom' ? t.materialCustom.trim() : t.material),
  (t) => t.ventilation,
]

// 필수 항목 (벽지·바닥재)
export const TANK_REQUIRED_CHECKS = [(t) => t.wall, (t) => t.floor]
export const TANK_REQUIRED_MESSAGE = '벽지와 바닥재를 선택해주세요.'

export const isTankComplete = (tank) => TANK_REQUIRED_CHECKS.every((check) => check(tank))
export const getTankProgress = (tank) =>
  TANK_PROGRESS_CHECKS.filter((check) => check(tank)).length / TANK_PROGRESS_CHECKS.length
