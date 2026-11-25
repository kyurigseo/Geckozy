// 홈 화면용 API. 팀 client.js(request, ApiError)를 그대로 쓰고, my.js 는 건드리지 않아요.
import { ApiError, request } from './client'

// 대표 도마뱀 = 처음 등록한 도마뱀 (my.js 와 같은 규칙)
export const getMainLizard = async () => {
  const lizards = await request('/lizards')
  if (!lizards || lizards.length === 0) throw new ApiError(404, 'LIZARD_NOT_FOUND')
  return lizards[0] // { lizardId, name, species }
}

// 사육장이 아직 없으면 null
export const getEnclosureOf = async (lizardId) => {
  try {
    return await request('/enclosures', { params: { lizardId } })
  } catch (error) {
    if (error instanceof ApiError && error.code === 'ENCLOSURE_NOT_FOUND') return null
    throw error
  }
}

// 온도·습도 화면처럼 사육장 번호만 필요할 때
export const getMainEnclosureId = async () => {
  const lizard = await getMainLizard()
  const enclosure = await getEnclosureOf(lizard.lizardId)
  if (!enclosure) throw new ApiError(404, 'ENCLOSURE_NOT_FOUND')
  return enclosure.enclosureId
}

// 한 요청이 실패해도 홈 전체가 비지 않게, 실패하면 null
const soft = (promise) => promise.catch(() => null)

// 알림 종류 → 이동할 화면, 일러스트 위 핀 위치(%)
const ALERT_VIEW = {
  HUMIDITY: { link: '/humidity', x: 75.8, y: 24.3, bubbleSide: 'left' },
  SENSOR: { link: '/my/sensor', x: 31.4, y: 52, bubbleSide: 'right' },
  TEMPERATURE: { link: '/temperature', x: 56, y: 40, bubbleSide: 'left' },
}

const toAlerts = (alertData) => {
  if (!alertData) return []
  const all = [alertData.primaryAlert, ...(alertData.additionalAlerts ?? [])].filter(Boolean)
  const seen = new Set()
  return all.flatMap((a, i) => {
    const view = ALERT_VIEW[a.detailType]
    if (!view || seen.has(a.detailType)) return [] // 같은 종류는 핀 하나만
    seen.add(a.detailType)
    return [{ id: `${a.detailType}-${i}`, text: a.message, ...view }]
  })
}

const toTemperatureCard = (s) => {
  if (!s) return null
  if (s.status === 'SENSOR_ERROR' || s.currentTemperature == null) {
    return { value: '-', unit: '°C', label: '센서 확인', tone: 'warn' }
  }
  const ok = s.status === 'OPTIMAL'
  return { value: Number(s.currentTemperature).toFixed(1), unit: '°C', label: ok ? '안정' : '확인 필요', tone: ok ? 'ok' : 'warn' }
}

const toHumidityCard = (s) => {
  if (!s) return null
  if (s.cycleStatus === 'SENSOR_ERROR' || s.currentHumidity == null) {
    return { value: '-', unit: '%', label: '센서 확인', tone: 'warn' }
  }
  const ok = s.cycleStatus === 'HUMID_CYCLE_NORMAL'
  return { value: String(Math.round(Number(s.currentHumidity))), unit: '%', label: ok ? '안정' : '확인 필요', tone: ok ? 'ok' : 'warn' }
}

// 홈 화면에 필요한 걸 한 번에. 사육장이 없으면 enclosure: null
export const fetchHome = async () => {
  const lizard = await getMainLizard()
  const enclosure = await getEnclosureOf(lizard.lizardId)
  const base = { petName: lizard.name, speciesName: lizard.species, lizardId: lizard.lizardId, characterColor: lizard.characterColor ?? null }
  if (!enclosure) return { ...base, enclosure: null, tank: null, alerts: [], temperature: null, humidity: null, summary: null, coach: null }

  const id = enclosure.enclosureId
  const [temp, humid, alertData, summary, tempSuggest, humidSuggest] = await Promise.all([
    soft(request(`/enclosures/${id}/temperatures/status`)),
    soft(request(`/enclosures/${id}/humidities/status`)),
    soft(request(`/enclosures/${id}/alerts`)),
    soft(request(`/enclosures/${id}/environment/summary`)),
    soft(request(`/enclosures/${id}/temperatures/suggestions`)),
    soft(request(`/enclosures/${id}/humidities/suggestions`)),
  ])

  // 환경 코치: 온도 알림이 우선이면 온도 제안, 아니면 습도 제안
  const primary = alertData?.primaryAlert?.detailType
  const suggestion = primary === 'TEMPERATURE' ? tempSuggest : humidSuggest ?? tempSuggest

  return {
    ...base,
    enclosure,
    // TankPreview 가 쓰는 모양 (my.js getMyTank 와 같은 키)
    tank: {
      wall: enclosure.wallDesign ?? '',
      floor: enclosure.floorDesign ?? '',
      vine: enclosure.vineDesign ?? '',
      structure: enclosure.decorationDesign ?? '',
    },
    alerts: toAlerts(alertData),
    temperature: toTemperatureCard(temp),
    humidity: toHumidityCard(humid),
    summary: summary?.summary ?? null, // 데이터 부족이면 null
    coach: suggestion ? { title: suggestion.actionTitle, text: suggestion.actionGuide } : null,
  }
}