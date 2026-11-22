// 온도·습도 상세 화면용. 반환 모양은 Temperature.jsx / Humidity.jsx 의 임시 데이터와 같아요
// → 두 파일의 fetchTemperature / fetchHumidity 만 이 함수로 바꿔 끼우면 돼요
import { request } from './client'
import { getMainEnclosureId } from './home'

const hoursOf = (iso) => {
  const d = new Date(iso)
  return d.getHours() + d.getMinutes() / 60
}

// { current, measuredAt, hourly:[{h,min,max}] }  (h = 그 시간대의 가운데, 예: 14시대 → 14.5)
export const fetchTemperature = async () => {
  const id = await getMainEnclosureId()
  const [status, history] = await Promise.all([
    request(`/enclosures/${id}/temperatures/status`),
    request(`/enclosures/${id}/temperatures/history`), // date 를 안 보내면 오늘
  ])
  const actual = history?.actual ?? []

  const buckets = new Map()
  actual.forEach(({ measuredAt, value }) => {
    const hour = new Date(measuredAt).getHours()
    const v = Number(value)
    const b = buckets.get(hour)
    buckets.set(hour, b ? { min: Math.min(b.min, v), max: Math.max(b.max, v) } : { min: v, max: v })
  })

  return {
    current: status.currentTemperature == null ? null : Number(status.currentTemperature),
    measuredAt: actual.length ? actual[actual.length - 1].measuredAt : null,
    hourly: [...buckets.entries()].sort((a, b) => a[0] - b[0]).map(([hour, { min, max }]) => ({ h: hour + 0.5, min, max })),
    // 아래는 화면의 하드코딩 값(OK_MIN 등)을 서버 값으로 바꾸고 싶을 때 쓰세요
    status: status.status, // OPTIMAL | HIGH | LOW | UNKNOWN | SENSOR_ERROR
    speciesName: status.speciesName,
    reference: history?.reference ?? null, // { dayMin, dayMax, nightMin, nightMax }
    displayMessage: status.displayMessage,
    cautionText: status.cautionText,
  }
}

// 서버는 시간대 이름만 주고 몇 시~몇 시인지는 안 줘요 → 임시 가정. 백엔드에 확인 필요
const CYCLE_HOURS = { MORNING: [6, 12], DAY: [12, 18], NIGHT: [18, 24] }

// { current, measuredAt, measured:[{h,v}], ideal:[{h,v}], sprayZones:[[a,b]] }
export const fetchHumidity = async () => {
  const id = await getMainEnclosureId()
  const [status, history] = await Promise.all([
    request(`/enclosures/${id}/humidities/status`),
    request(`/enclosures/${id}/humidities/history`),
  ])
  const actual = history?.actual ?? []

  // 권장 사이클은 구간별 범위(min~max)라서, 구간 시작·끝에 가운데 값을 찍은 계단 모양으로 바꿈
  const ideal = (history?.recommendedCycle ?? []).flatMap(({ time, min, max }) => {
    const range = CYCLE_HOURS[time]
    if (!range) return []
    const v = (Number(min) + Number(max)) / 2
    return [{ h: range[0], v }, { h: range[1], v }]
  })

  return {
    current: status.currentHumidity == null ? null : Number(status.currentHumidity),
    measuredAt: actual.length ? actual[actual.length - 1].measuredAt : null,
    measured: actual.map(({ measuredAt, value }) => ({ h: hoursOf(measuredAt), v: Number(value) })),
    ideal,
    sprayZones: [], // 서버에 분무 시간 조회가 없음 (humidities/cycles 가 생기면 채우기)
    cycleStatus: status.cycleStatus,
    trend: status.humidityTrend,
    displayMessage: status.displayMessage,
    cautionText: status.cautionText,
  }
}