import { ApiError, request } from './client'

export const getMainLizard = async () => {
  const lizards = await request('/lizards')
  if (!lizards || lizards.length === 0) throw new ApiError(404, 'LIZARD_NOT_FOUND')
  return lizards[0]
}

export const getEnclosureOf = async (lizardId) => {
  try {
    return await request('/enclosures', { params: { lizardId } })
  } catch (error) {
    if (error instanceof ApiError && error.code === 'ENCLOSURE_NOT_FOUND') return null
    throw error
  }
}

export const getMainEnclosureId = async () => {
  const lizard = await getMainLizard()
  const enclosure = await getEnclosureOf(lizard.lizardId)
  if (!enclosure) throw new ApiError(404, 'ENCLOSURE_NOT_FOUND')
  return enclosure.enclosureId
}

const soft = (promise) => promise.catch(() => null)

const ALERT_VIEW = {
  HUMIDITY: { link: '/humidity', x: 76, y: 20, bubbleSide: 'left' },
  SENSOR: { link: '/my/sensor', x: 33, y: 55, bubbleSide: 'right' },
  TEMPERATURE: { link: '/temperature', x: 50, y: 12, bubbleSide: 'left' },
}

const toAlerts = (alertData) => {
  if (!alertData) return []
  const all = [alertData.primaryAlert, ...(alertData.additionalAlerts ?? [])].filter(Boolean)
  const seen = new Set()
  return all.flatMap((a, i) => {
    const view = ALERT_VIEW[a.detailType]
    if (!view || seen.has(a.detailType)) return []
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

export const fetchHome = async () => {
  const lizard = await getMainLizard()
  const enclosure = await getEnclosureOf(lizard.lizardId)
  const base = { petName: lizard.name, speciesName: lizard.species, lizardId: lizard.lizardId }
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

  const primary = alertData?.primaryAlert?.detailType
  const suggestion = primary === 'TEMPERATURE' ? tempSuggest : humidSuggest ?? tempSuggest

  return {
    ...base,
    enclosure,
    tank: {
      wall: enclosure.wallDesign ?? '',
      floor: enclosure.floorDesign ?? '',
      vine: enclosure.vineDesign ?? '',
      structure: enclosure.decorationDesign ?? '',
    },
    alerts: toAlerts(alertData),
    temperature: toTemperatureCard(temp),
    humidity: toHumidityCard(humid),
    summary: summary?.summary ?? null,
    coach: suggestion ? { title: suggestion.actionTitle, text: suggestion.actionGuide } : null,
  }
}