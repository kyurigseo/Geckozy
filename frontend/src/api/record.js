// 기록(Record) 화면용 API. 백엔드 CareRecordController 기준이에요.
// 화면이 쓰던 임시 데이터 모양으로 바꿔서 돌려주니까, 화면에서는 import 만 바꾸면 돼요.
import { ApiError, getAccessToken, request } from './client'
import { getMainLizard } from './home'

const BASE_URL = import.meta.env.VITE_API_URL
const pad = (n) => String(n).padStart(2, '0')

// ───────── 화면 type ↔ 서버 recordType ─────────
const TYPE_TO_SERVER = {
  feeding: 'FEEDING',
  shedding: 'SHEDDING',
  weight: 'WEIGHT',
  cleaning: 'CLEANING',
  poop: 'STOOL',
  symptom: 'ABNORMAL_BEHAVIOR',
  etc: 'OTHER',
}
const SERVER_TO_TYPE = Object.fromEntries(Object.entries(TYPE_TO_SERVER).map(([ui, server]) => [server, ui]))
export const toUiType = (serverType) => SERVER_TO_TYPE[serverType] ?? 'etc'

// ───────── 폼에 보이는 글자 ↔ 서버 enum ─────────
const FEEDING_TYPE = [['곤충', 'INSECT'], ['전용 사료', 'PELLET'], ['과일/퓨레', 'FRUIT_PUREE'], ['직접 입력', 'CUSTOM']]
const FEEDING_RESPONSE = [['대부분 먹음', 'MOSTLY_ATE'], ['평소보다 적게 먹음', 'ATE_LESS_THAN_USUAL'], ['거의 먹지 않음', 'BARELY_ATE']]
const SHEDDING_STATUS = [['완료', 'COMPLETED'], ['일부 남음', 'PARTIALLY_REMAINED'], ['진행 중', 'IN_PROGRESS']]
const CLEANING_SCOPE = [['전체 청소', 'FULL'], ['부분 청소', 'PARTIAL']]
const STOOL_STATUS = [['평소와 비슷함', 'NORMAL'], ['평소와 다름', 'ABNORMAL']]
const ABNORMAL_TYPE = [
  ['활동 감소', 'ACTIVITY_DECREASE'], ['먹이 반응 저하', 'FEEDING_RESPONSE_DECREASE'], ['호흡', 'BREATHING'],
  ['외형 변화', 'APPEARANCE_CHANGE'], ['움직임 이상', 'MOVEMENT_ABNORMAL'], ['기타', 'OTHER'],
]
const ABNORMAL_ONSET = [['오늘 처음', 'TODAY'], ['2~3일 전부터', 'TWO_TO_THREE_DAYS_AGO'], ['그보다 오래됨', 'LONGER_AGO'], ['잘 모르겠음', 'UNKNOWN']]
const ABNORMAL_STATUS = [['현재도 관찰됨', 'STILL_OBSERVED'], ['지금은 보이지 않음', 'NOT_CURRENTLY_OBSERVED']]
const EXTRA_TYPE = [['행동 변화', 'BEHAVIOR_CHANGE'], ['사육장 변화', 'ENCLOSURE_CHANGE'], ['장비 변경', 'EQUIPMENT_CHANGE'], ['기타', 'OTHER']]

const toEnum = (table, label) => table.find(([l]) => l === label)?.[1]
const toLabel = (table, value) => table.find(([, v]) => v === value)?.[0] ?? value

const invalid = () => new ApiError(400, 'INVALID_REQUEST')
const need = (value) => {
  if (value === undefined || value === null || value === '') throw invalid()
  return value
}

// 폼 값 → 서버 typeDetail (증세는 여러 개를 고를 수 있어서 배열로 돌려줘요)
const buildDetails = (type, f) => {
  switch (type) {
    case 'feeding': {
      const feedingType = need(toEnum(FEEDING_TYPE, f.feedType))
      const custom = (f.customInput ?? '').trim()
      if (feedingType === 'CUSTOM' && !custom) throw invalid()
      return [{
        feedingType,
        feedingCustomName: feedingType === 'CUSTOM' ? custom : null,
        feedingResponse: need(toEnum(FEEDING_RESPONSE, f.response)),
      }]
    }
    case 'weight': {
      const weightG = Number(f.weight)
      if (!(weightG > 0)) throw invalid()
      return [{ weightG }]
    }
    case 'shedding': {
      const sheddingStatus = need(toEnum(SHEDDING_STATUS, f.status))
      return [{ sheddingStatus, sheddingRemainingParts: sheddingStatus === 'COMPLETED' ? [] : f.parts ?? [] }]
    }
    case 'cleaning':
      return [{ cleaningScope: need(toEnum(CLEANING_SCOPE, f.scope)), cleaningTargets: f.items ?? [] }]
    case 'poop': {
      const stoolStatus = need(toEnum(STOOL_STATUS, f.status))
      return [{ stoolStatus, stoolAbnormalities: stoolStatus === 'NORMAL' ? [] : f.diffs ?? [] }]
    }
    case 'symptom': {
      if (!f.changes || f.changes.length === 0) throw invalid()
      // 서버는 기록 하나에 증세 하나만 받아서, 고른 개수만큼 기록을 만들어요
      return f.changes.map((label) => ({
        abnormalBehaviorType: need(toEnum(ABNORMAL_TYPE, label)),
        abnormalOnset: need(toEnum(ABNORMAL_ONSET, f.startTime)),
        abnormalCurrentStatus: need(toEnum(ABNORMAL_STATUS, f.observed)),
      }))
    }
    case 'etc':
      return [{ extraType: need(toEnum(EXTRA_TYPE, f.etcType)) }]
    default:
      throw invalid()
  }
}

// "2026-11-22T15:30:00" (서버 LocalDateTime 형식. 시간대 표시 없음)
const toLocalDateTime = (date) =>
  `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`

// 사진은 따로 올려야 해서 multipart 로 직접 보내요 (request() 는 JSON 전용)
const uploadPhoto = async (lizardId, recordId, file) => {
  const body = new FormData()
  body.append('file', file)
  const headers = {}
  const token = getAccessToken()
  if (token) headers.Authorization = `Bearer ${token}`

  let response
  try {
    response = await fetch(new URL(`/lizards/${lizardId}/care-records/${recordId}/photos`, BASE_URL), { method: 'POST', headers, body })
  } catch {
    throw new ApiError(0, 'NETWORK_ERROR')
  }
  const json = await response.json().catch(() => null)
  if (!response.ok) throw new ApiError(response.status, json?.message ?? 'UNKNOWN_ERROR')
  return json?.data
}

// 기록 저장. form 은 각 입력 폼이 onComplete 로 넘기는 값
// { ...폼 값, memo, photos: File[], recordedAt?: Date }
// 돌려주는 값: { recordIds, date: 'YYYY-MM-DD', photoFailed: 올리지 못한 사진 수 }
export const saveRecord = async (type, form) => {
  const details = buildDetails(type, form) // 잘못된 입력은 여기서 먼저 막아요
  const lizard = await getMainLizard()
  const recordedAt = form.recordedAt ?? new Date()
  const memo = (form.memo ?? '').trim() || null

  const recordIds = []
  for (const typeDetail of details) {
    const { recordId } = await request(`/lizards/${lizard.lizardId}/care-records`, {
      method: 'POST',
      body: { recordType: TYPE_TO_SERVER[type], recordedAt: toLocalDateTime(recordedAt), memo, typeDetail },
    })
    recordIds.push(recordId)
  }

  // 사진은 첫 번째 기록에만 붙여요 (서버 제한: 기록당 3장)
  let photoFailed = 0
  for (const file of (form.photos ?? []).slice(0, 3)) {
    try {
      await uploadPhoto(lizard.lizardId, recordIds[0], file)
    } catch {
      photoFailed += 1
    }
  }
  return { recordIds, date: toLocalDateTime(recordedAt).slice(0, 10), photoFailed }
}

// ───────── 조회 ─────────
const getCalendar = (lizardId, year, month) => request(`/lizards/${lizardId}/care-records/calendar`, { params: { year, month } })
const getRecordsByDate = (lizardId, date) => request(`/lizards/${lizardId}/care-records`, { params: { date } })
const getRecordDetail = (lizardId, recordId) => request(`/lizards/${lizardId}/care-records/${recordId}`)

// 사진 주소는 서버 기준 상대 경로('/uploads/...')라서 서버 주소를 붙여요
const photoUrl = (imageUrl) => (/^https?:/.test(imageUrl) ? imageUrl : new URL(imageUrl, BASE_URL).href)

// 한 달치 기록 목록 (상세 포함). 서버에 월 전체 조회가 없어서 날짜별로 받아요
const loadMonthDetails = async (lizardId, year, month) => {
  const calendar = await getCalendar(lizardId, year, month)
  const perDay = await Promise.all((calendar?.dates ?? []).map((date) => getRecordsByDate(lizardId, date)))
  const list = perDay.flat()
  return Promise.all(list.map((item) => getRecordDetail(lizardId, item.recordId)))
}

// ───────── Record.jsx: 달력 + 지난달 대비 변화 ─────────
const TYPE_LABEL = { feeding: '먹이', shedding: '탈피', weight: '체중', cleaning: '청소', poop: '배변', symptom: '이상 증세', etc: '추가 기록' }

const toSummary = (item) => {
  const type = toUiType(item.type)
  const percent = item.previousCount > 0 ? Math.round((item.changeValue / item.previousCount) * 100) : item.currentCount > 0 ? 100 : 0
  const n = Math.abs(item.changeValue)
  const message = `지난달보다 ${TYPE_LABEL[type]} 기록이 ${n}번 ${item.changeValue > 0 ? '늘었' : '줄었'}어요.`
  return { type, percent, message }
}

// { records:[{date,type,alert}], summary:[{type,percent,message}] }
export const fetchRecords = async (year, month) => {
  const lizard = await getMainLizard()
  const [calendar, changes] = await Promise.all([
    getCalendar(lizard.lizardId, year, month),
    request(`/lizards/${lizard.lizardId}/care-records/monthly-changes`, { params: { year, month } }).catch(() => null),
  ])
  const dates = calendar?.dates ?? []
  const perDay = await Promise.all(dates.map((date) => getRecordsByDate(lizard.lizardId, date)))

  const seen = new Set()
  const records = []
  for (let i = 0; i < dates.length; i += 1) {
    for (const item of perDay[i]) {
      const type = toUiType(item.recordType)
      const key = `${dates[i]}-${type}`
      if (seen.has(key)) continue // 같은 날 같은 종류는 한 번만 표시
      seen.add(key)
      records.push({ date: dates[i], type, recordId: item.recordId, alert: false })
    }
  }

  // 배변 이상은 상세를 봐야 알 수 있어서 배변 기록만 확인해요
  await Promise.all(
    records
      .filter((r) => r.type === 'poop')
      .map(async (r) => {
        const detail = await getRecordDetail(lizard.lizardId, r.recordId).catch(() => null)
        r.alert = detail?.typeDetail?.stoolStatus === 'ABNORMAL'
      }),
  )

  return { records, summary: (changes?.items ?? []).map(toSummary) }
}

// ───────── Recorddetail / RecordList: 한 달 기록 ─────────
const tag = (text, tone = 'orange') => ({ text, tone })

const toTags = (type, d) => {
  switch (type) {
    case 'feeding':
      return [
        tag(d.feedingType === 'CUSTOM' ? d.feedingCustomName : toLabel(FEEDING_TYPE, d.feedingType)),
        ...(d.feedingResponse && d.feedingResponse !== 'MOSTLY_ATE' ? [tag(toLabel(FEEDING_RESPONSE, d.feedingResponse), 'pink')] : []),
      ]
    case 'shedding':
      return [
        tag(toLabel(SHEDDING_STATUS, d.sheddingStatus), d.sheddingStatus === 'COMPLETED' ? 'orange' : 'pink'),
        ...(d.sheddingRemainingParts ?? []).map((p) => tag(p)),
      ]
    case 'weight':
      return [tag(`${Number(d.weightG)}g`)]
    case 'cleaning':
      return [tag(toLabel(CLEANING_SCOPE, d.cleaningScope)), ...(d.cleaningTargets ?? []).map((t) => tag(t))]
    case 'poop':
      return [
        tag(d.stoolStatus === 'ABNORMAL' ? '이상 있음' : toLabel(STOOL_STATUS, d.stoolStatus), d.stoolStatus === 'ABNORMAL' ? 'pink' : 'orange'),
        ...(d.stoolAbnormalities ?? []).map((a) => tag(a)),
      ]
    case 'symptom':
      return [tag(toLabel(ABNORMAL_TYPE, d.abnormalBehaviorType), 'pink'), tag(toLabel(ABNORMAL_STATUS, d.abnormalCurrentStatus))]
    default:
      return d.extraType ? [tag(toLabel(EXTRA_TYPE, d.extraType))] : []
  }
}

// [{ id, date, time:'HH:mm', recordedAt, type, alert, tags, memo, photos:[url] }]
export const fetchMonthRecords = async (year, month) => {
  const lizard = await getMainLizard()
  const details = await loadMonthDetails(lizard.lizardId, year, month)
  return details.map((r) => {
    const type = toUiType(r.recordType)
    const at = r.recordedAt // 'YYYY-MM-DDTHH:mm:ss'
    return {
      id: r.recordId,
      date: at.slice(0, 10),
      time: at.slice(11, 16),
      recordedAt: at,
      type,
      alert: type === 'poop' && r.typeDetail?.stoolStatus === 'ABNORMAL',
      tags: toTags(type, r.typeDetail ?? {}),
      memo: r.memo ?? '',
      photos: (r.photos ?? []).map((p) => photoUrl(p.imageUrl)),
    }
  })
}

// ───────── 기록 전후 24시간 환경 ─────────
export const POINTS = 25 // 2시간 간격 25칸, 가운데(12번)가 기록 시점
export const CENTER = 12
const SLOT_MS = 2 * 60 * 60 * 1000

// record: fetchMonthRecords 가 준 기록 하나. 측정값이 없는 칸은 null, 하나도 없으면 []
export const fetchRecordEnv = async (record) => {
  const lizard = await getMainLizard()
  const graph = await request(`/lizards/${lizard.lizardId}/care-records/${record.id}/environment/graph`, {
    params: { beforeMinutes: 1440, afterMinutes: 1440 },
  })
  if (!graph || graph.length === 0) return []

  const base = new Date(record.recordedAt).getTime()
  const slots = Array.from({ length: POINTS }, () => [])
  graph.forEach((m) => {
    const i = Math.round((new Date(m.measuredAt).getTime() - base) / SLOT_MS) + CENTER
    if (i >= 0 && i < POINTS && m.temperature != null && m.humidity != null) {
      slots[i].push({ t: Number(m.temperature), h: Number(m.humidity) })
    }
  })
  return slots.map((list) =>
    list.length === 0
      ? null
      : {
          tmin: Math.min(...list.map((x) => x.t)),
          tmax: Math.max(...list.map((x) => x.t)),
          h: list.reduce((sum, x) => sum + x.h, 0) / list.length,
        },
  )
}