import {
  configureGuideSession,
  getGuideContext,
  saveGuideContext,
} from './guide.js'

export const MY_API_BASE = (
  import.meta.env?.VITE_API_BASE_URL ||
  import.meta.env?.VITE_GUIDE_API_BASE_URL ||
  'http://34.64.99.93:8080'
).replace(/\/$/, '')

const getToken = () =>
  getGuideContext().accessToken ||
  sessionStorage.getItem('accessToken') ||
  localStorage.getItem('accessToken')

export async function myRequest(
  path,
  { method = 'GET', body, signal } = {},
) {
  const accessToken = getToken()

  if (!accessToken) {
    throw new Error('로그인이 필요합니다. 다시 로그인해주세요.')
  }

  let response

  try {
    response = await fetch(`${MY_API_BASE}${path}`, {
      method,
      signal: signal || AbortSignal.timeout(10000),
      headers: {
        Authorization: `Bearer ${accessToken}`,
        ...(body === undefined
          ? {}
          : { 'Content-Type': 'application/json' }),
      },
      ...(body === undefined
        ? {}
        : { body: JSON.stringify(body) }),
    })
  } catch (error) {
    if (signal?.aborted) throw error

    throw new Error(
      '서버에 연결하지 못했습니다. 잠시 후 다시 시도해주세요.',
      { cause: error },
    )
  }

  if (response.status === 204 && response.ok) {
    return null
  }

  const result = await response.json().catch(() => null)

  if (
    !response.ok ||
    (result?.code != null &&
      (result.code < 200 || result.code >= 300))
  ) {
    const error = new Error(
      response.status === 401
        ? '로그인이 만료되었습니다. 다시 로그인해주세요.'
        : `${result?.message || '요청에 실패했습니다.'} (${response.status})`,
    )

    error.status = response.status
    error.code = result?.message

    throw error
  }

  if (!result || !Object.hasOwn(result, 'data')) {
    throw new Error('서버 응답을 확인할 수 없습니다.')
  }

  return result.data
}

export function mediaUrl(value) {
  if (!value) return ''

  try {
    const url = new URL(value, `${MY_API_BASE}/`)

    return ['http:', 'https:'].includes(url.protocol)
      ? url.href
      : ''
  } catch {
    return ''
  }
}

export function formatMyDate(value) {
  if (!value) return '—'

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) return '—'

  return new Intl.DateTimeFormat('ko-KR', {
    timeZone: 'Asia/Seoul',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(date)
}

// 도마뱀 관리
export async function getManagedLizards(signal) {
  const list = await myRequest('/lizards', { signal })

  if (!Array.isArray(list)) {
    throw new Error('도마뱀 목록 응답을 확인해주세요.')
  }

  const context = getGuideContext()
  const colors = ['beige', 'orange', 'yellow', 'green', 'blue']

  const items = await Promise.all(
    list.map(async (item) => {
      const detail = await myRequest(
        `/lizards/${encodeURIComponent(item.lizardId)}`,
        { signal },
      )

      const color = String(
        detail.characterColor || '',
      ).toLowerCase()

      return {
        id: item.lizardId,
        name: detail.name ?? item.name,
        species: detail.species ?? item.species,
        color: colors.includes(color) ? color : 'beige',
        tankName:
          context.enclosures?.find(
            (tank) =>
              String(tank.lizardId) ===
              String(item.lizardId),
          )?.name || '—',
        selected:
          String(item.lizardId) ===
          String(context.lizardId),
      }
    }),
  )

  if (signal?.aborted) {
    throw new DOMException('Aborted', 'AbortError')
  }

  if (
    items.length &&
    !items.some((item) => item.selected)
  ) {
    items[0].selected = true

    saveGuideContext({
      lizardId: items[0].id,
      enclosureId: null,
      enclosureInfo: null,
    })
  }

  return items
}

export async function deleteManagedLizard(id) {
  await myRequest(
    `/lizards/${encodeURIComponent(id)}`,
    { method: 'DELETE' },
  )

  const context = getGuideContext()

  const enclosures = context.enclosures?.filter(
    (tank) => String(tank.lizardId) !== String(id),
  )

  const selected =
    String(context.lizardId) === String(id)

  saveGuideContext({
    ...(enclosures ? { enclosures } : {}),
    ...(selected
      ? {
          lizardId: null,
          enclosureId: null,
          enclosureInfo: null,
          preGuideId: null,
        }
      : {}),
  })
}

export async function getManagedProfile(signal) {
  const lizards = await getManagedLizards(signal)
  const selected = lizards.find((item) => item.selected)

  return selected
    ? { ...selected, geckoColor: selected.color }
    : null
}

// 스크랩
export async function getMyScraps(signal) {
  const data = await myRequest(
    '/guidebooks/scraps',
    { signal },
  )

  if (!Array.isArray(data?.guidebooks)) {
    throw new Error('스크랩 목록 응답을 확인해주세요.')
  }

  return data.guidebooks
}

export const getScrapPage = (id, page, signal) =>
  myRequest(
    `/guidebooks/${encodeURIComponent(id)}/pages/${page}`,
    { signal },
  )

export const changeScrap = (id, value) =>
  myRequest(
    `/guidebooks/${encodeURIComponent(id)}/scrap`,
    { method: value ? 'POST' : 'DELETE' },
  )

// 센서
export function getMyEnclosures() {
  const context = getGuideContext()

  if (
    Array.isArray(context.enclosures) &&
    context.enclosures.length
  ) {
    return context.enclosures
  }

  return context.enclosureId
    ? [
        {
          enclosureId: context.enclosureId,
          name: '선택한 사육장',
        },
      ]
    : []
}

export async function getMySensors(signal) {
  const enclosures = getMyEnclosures()

  if (!enclosures.length) {
    throw new Error('사육장 정보를 먼저 연결해주세요.')
  }

  const records = await Promise.all(
    enclosures.map(async (tank) => {
      let data

      try {
        data = await myRequest(
          `/enclosures/${encodeURIComponent(tank.enclosureId)}/sensor/status`,
          { signal },
        )
      } catch (error) {
        if (error.code === 'SENSOR_NOT_FOUND') {
          return null
        }

        throw error
      }

      if (!data?.sensorId) {
        throw new Error('센서 상태 응답을 확인해주세요.')
      }

      return {
        ...data,
        enclosureId: tank.enclosureId,
        tank: tank.name || '—',
      }
    }),
  )

  return records.filter(Boolean)
}

export async function connectMySensor(deviceIdentifier) {
  const { enclosureId } = getGuideContext()

  if (!enclosureId) {
    throw new Error('연결할 사육장을 먼저 선택해주세요.')
  }

  const data = await myRequest(
    `/enclosures/${encodeURIComponent(enclosureId)}/sensor`,
    {
      method: 'POST',
      body: {
        deviceIdentifier: deviceIdentifier.trim(),
      },
    },
  )

  if (!data?.sensorId) {
    throw new Error('센서 연결 응답을 확인해주세요.')
  }

  const tank = getMyEnclosures().find(
    (item) =>
      String(item.enclosureId) === String(enclosureId),
  )

  return {
    ...data,
    enclosureId,
    tank: tank?.name || '선택한 사육장',
  }
}

// 로그아웃·회원탈퇴 후 개인정보 및 온보딩 진행값 정리
export function clearMySession() {
  configureGuideSession(null)

  const exact = [
    'accessToken',
    'refreshToken',
    'selectedLizardId',
    'selectedEnclosureId',
    'geckozy-onboarding',
  ]

  for (const storage of [sessionStorage, localStorage]) {
    const keys = Array.from(
      { length: storage.length },
      (_, index) => storage.key(index),
    )

    for (const key of keys) {
      if (
        key &&
        (
          exact.includes(key) ||
          key.startsWith('geckozy.guide.') ||
          key.startsWith('geckozy.my.')
        )
      ) {
        storage.removeItem(key)
      }
    }
  }
}

export async function logoutAccount() {
  let serverLoggedOut = false

  try {
    if (getToken()) {
      await myRequest('/auth/logout', {
        method: 'POST',
      })

      serverLoggedOut = true
    }
  } catch {
    // 서버 요청이 실패해도 기기의 로그인 정보는 제거합니다.
  } finally {
    clearMySession()
  }

  return { serverLoggedOut }
}

export async function deleteMyAccount() {
  // 실패하면 아래 정리 코드는 실행되지 않습니다.
  await myRequest('/users/me', {
    method: 'DELETE',
  })

  clearMySession()
}