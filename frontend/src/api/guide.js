import { getAccessToken } from './client'

const BASE_URL = (
  import.meta.env.VITE_API_URL ||
  import.meta.env.VITE_GUIDE_API_BASE_URL ||
  'http://34.64.99.93:8080'
).replace(/\/$/, '')

let session = null

export function configureGuideSession(value) {
  session = value
}

function token() {
  return getAccessToken()
}

function storageKey() {
  const value = token() || ''
  let hash = 0

  for (const char of value) {
    hash = (Math.imul(31, hash) + char.charCodeAt(0)) | 0
  }

  return `geckozy.guide.${hash}`
}

export function getGuideContext() {
  try {
    return {
      ...JSON.parse(
        sessionStorage.getItem(storageKey()) || '{}',
      ),
      ...session,
    }
  } catch {
    return { ...session }
  }
}

export function saveGuideContext(value) {
  const context = {
    ...getGuideContext(),
    ...value,
  }

  delete context.accessToken

  sessionStorage.setItem(
    storageKey(),
    JSON.stringify(context),
  )

  return context
}

export async function guideRequest(
  path,
  { method = 'GET', body, signal } = {},
) {
  const accessToken = token()

  if (!accessToken) {
    throw new Error(
      '로그인이 필요합니다. 다시 로그인해주세요.',
    )
  }

  let response

  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      signal,
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
    if (error.name === 'AbortError') throw error

    throw new Error(
      'API 서버에 연결할 수 없습니다. 서버 주소와 CORS 설정을 확인해주세요.',
      { cause: error },
    )
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
        : `${result?.message || 'API 요청 실패'} (${response.status})`,
    )

    error.status = response.status
    error.code = result?.message

    throw error
  }

  if (!result || !Object.hasOwn(result, 'data')) {
    throw new Error('API 응답 형식을 확인해주세요.')
  }

  return result.data
}

// 생성 ID 순으로 정렬하여 화면의 사육장 번호를 맞춥니다.
export async function getGuideLizards(signal) {
  const list = await guideRequest('/lizards', { signal })

  if (!Array.isArray(list)) {
    throw new Error('도마뱀 목록을 확인해주세요.')
  }

  return [...list].sort((a, b) => {
    const left = BigInt(a.lizardId)
    const right = BigInt(b.lizardId)

    return left < right ? -1 : left > right ? 1 : 0
  })
}

// 선택한 도마뱀이 없거나 삭제됐다면 첫 번째 개체를 사용합니다.
export function getSelectedLizard(list) {
  const id = getGuideContext().lizardId

  return (
    list.find(
      (item) => String(item.lizardId) === String(id),
    ) ||
    list[0] ||
    null
  )
}

// 마이·가이드에서 공통으로 사용할 선택 개체 정보를 불러옵니다.
// requestedId가 있으면 해당 도마뱀으로 선택을 변경합니다.
export async function loadGuideAccountContext(
  signal,
  requestedId,
) {
  const list = await getGuideLizards(signal)

  const selected =
    requestedId == null
      ? getSelectedLizard(list)
      : list.find(
          (item) =>
            String(item.lizardId) === String(requestedId),
        )

  if (requestedId != null && !selected) {
    throw new Error('선택한 도마뱀을 찾을 수 없어요.')
  }

  if (!selected) {
    saveGuideContext({
      lizardId: null,
      enclosureId: null,
      enclosureInfo: null,
      enclosures: [],
      lizardDraft: null,
      speciesId: null,
      preGuideId: null,
      guideMode: null,
      localChecks: {},
      tankName: null,
    })

    return {
      lizard: null,
      enclosure: null,
      list,
    }
  }

  const lizard = await guideRequest(
    `/lizards/${encodeURIComponent(selected.lizardId)}`,
    { signal },
  )

  let enclosure = null

  try {
    enclosure = await guideRequest(
      `/enclosures?lizardId=${encodeURIComponent(selected.lizardId)}`,
      { signal },
    )
  } catch (error) {
    if (error.code !== 'ENCLOSURE_NOT_FOUND') {
      throw error
    }
  }

  if (signal?.aborted) {
    throw new DOMException('Aborted', 'AbortError')
  }

  const index = list.findIndex(
    (item) =>
      String(item.lizardId) === String(selected.lizardId),
  )

  const tankName = `${index + 1}번 사육장`
  const previous = getGuideContext()

  const changed =
    String(previous.lizardId) !== String(selected.lizardId)

  const materials = {
    GLASS: '유리',
    ACRYLIC: '아크릴',
    PVC: 'PVC',
    MESH: '메쉬',
    OTHER: '직접 입력',
  }

  const ventilations = {
    TOP: '상단',
    SIDE: '측면',
    BOTH: '둘 다',
    UNKNOWN: '잘 모르겠어요',
  }

  saveGuideContext({
    ...(changed
      ? {
          preGuideId: null,
          guideMode: null,
          localChecks: {},
        }
      : {}),
    lizardId: selected.lizardId,
    speciesId: lizard.speciesId,
    tankName,
    lizardDraft: {
      ...lizard,
      speciesName: lizard.species,
      adoptionDate: changed
        ? null
        : previous.lizardDraft?.adoptionDate ?? null,
    },
    enclosureId: enclosure?.enclosureId ?? null,
    enclosureInfo: enclosure
      ? {
          width: String(enclosure.width ?? ''),
          depth: String(enclosure.depth ?? ''),
          height: String(enclosure.height ?? ''),
          material:
            materials[enclosure.material] ?? '유리',
          customMaterial: enclosure.materialOther ?? '',
          ventilation:
            ventilations[enclosure.ventilation] ?? '상단',
        }
      : null,
    enclosures: enclosure
      ? [
          {
            lizardId: selected.lizardId,
            enclosureId: enclosure.enclosureId,
            name: tankName,
          },
        ]
      : [],
  })

  return {
    lizard,
    enclosure,
    list,
    tankName,
  }
}

function guidePath(suffix = '') {
  const { preGuideId } = getGuideContext()

  if (!preGuideId) {
    throw new Error(
      '먼저 가이드 화면에서 종을 선택해주세요.',
    )
  }

  return `/pre-adoption-guides/${encodeURIComponent(preGuideId)}${suffix}`
}

export const getSpecies = (signal) =>
  guideRequest('/species', { signal })

export const getSpeciesGuide = (id) =>
  guideRequest(
    `/species/${encodeURIComponent(id)}/pre-adoption-guide`,
  )

export const getGuideProgress = (signal) =>
  guideRequest(guidePath('/progress'), { signal })

export const getGuideStep = (step, signal) =>
  guideRequest(
    guidePath(`/steps/${step}`),
    { signal },
  )

export const saveGuideChecks = (step, checks) =>
  guideRequest(guidePath(`/checks/${step}`), {
    method: 'PUT',
    body: { checks },
  })

export const completeGuideStep = (completedStep) =>
  guideRequest(guidePath('/progress'), {
    method: 'PATCH',
    body: { completedStep },
  })

export const completeGuide = () =>
  guideRequest(guidePath('/complete'), {
    method: 'POST',
  })

export const getEnvironmentCheck = (signal) =>
  guideRequest(
    guidePath('/environment-check'),
    { signal },
  )

export const getEnvironmentSummary = (signal) =>
  guideRequest(
    guidePath('/environment-check/summary'),
    { signal },
  )

export async function startEnvironmentCheck() {
  try {
    return await guideRequest(
      guidePath('/environment-check'),
      { method: 'POST' },
    )
  } catch (error) {
    if (
      error.code === 'ENVIRONMENT_CHECK_ALREADY_STARTED'
    ) {
      return getEnvironmentCheck()
    }

    throw error
  }
}

export function getSensorStatus(signal) {
  const { enclosureId } = getGuideContext()

  if (!enclosureId) {
    throw new Error(
      '등록된 사육장 정보가 필요합니다.',
    )
  }

  return guideRequest(
    `/enclosures/${encodeURIComponent(enclosureId)}/sensor/status`,
    { signal },
  )
}

export async function saveGuideEnclosure(info) {
  const {
    lizardId,
    enclosureId,
    guideMode,
  } = getGuideContext()

  const material = {
    유리: 'GLASS',
    아크릴: 'ACRYLIC',
    PVC: 'PVC',
    메쉬: 'MESH',
    '직접 입력': 'OTHER',
  }[info.material]

  const body = {
    width: Number(info.width),
    depth: Number(info.depth),
    height: Number(info.height),
    material,
    materialOther:
      material === 'OTHER'
        ? info.customMaterial.trim()
        : null,
    ventilation: {
      상단: 'TOP',
      측면: 'SIDE',
      '둘 다': 'BOTH',
      '잘 모르겠어요': 'UNKNOWN',
    }[info.ventilation],
  }

  if (
    ![body.width, body.depth, body.height].every(
      (value) =>
        Number.isFinite(value) && value > 0,
    )
  ) {
    throw new Error(
      '사육장 크기를 양수로 입력해주세요.',
    )
  }

  if (material === 'OTHER' && !body.materialOther) {
    throw new Error('사육장 소재를 입력해주세요.')
  }

  // 기존 기본 가이드의 임시 저장 방식은 유지합니다.
  if (guideMode === 'static') {
    saveGuideContext({
      enclosureInfo: info,
    })

    return { localOnly: true }
  }

  if (!lizardId && !enclosureId) {
    throw new Error(
      '먼저 도마뱀 정보를 등록해주세요.',
    )
  }

  const data = await guideRequest(
    enclosureId
      ? `/enclosures/${encodeURIComponent(enclosureId)}`
      : '/enclosures',
    {
      method: enclosureId ? 'PATCH' : 'POST',
      body: enclosureId
        ? body
        : { ...body, lizardId },
    },
  )

  const id = enclosureId || data?.enclosureId

  if (!id) {
    throw new Error('사육장 ID가 응답에 없습니다.')
  }

  saveGuideContext({
    enclosureId: id,
    enclosureInfo: info,
  })

  return data
}