import { request } from './client'
import { getMainEnclosureId } from './home'

const hoursOf = (iso) => {
  const d = new Date(iso)
  return d.getHours() + d.getMinutes() / 60
}

export const fetchTemperature = async () => {
  const id = await getMainEnclosureId()
  const [status, history] = await Promise.all([
    request(`/enclosures/${id}/temperatures/status`),
    request(`/enclosures/${id}/temperatures/history`),
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
    status: status.status,
    speciesName: status.speciesName,
    reference: history?.reference ?? null,
    displayMessage: status.displayMessage,
    cautionText: status.cautionText,
  }
}

const CYCLE_HOURS = { MORNING: [6, 12], DAY: [12, 18], NIGHT: [18, 24] }

export const fetchHumidity = async () => {
  const id = await getMainEnclosureId()
  const [status, history] = await Promise.all([
    request(`/enclosures/${id}/humidities/status`),
    request(`/enclosures/${id}/humidities/history`),
  ])
  const actual = history?.actual ?? []

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
    sprayZones: [],
    cycleStatus: status.cycleStatus,
    trend: status.humidityTrend,
    displayMessage: status.displayMessage,
    cautionText: status.cautionText,
  }
}