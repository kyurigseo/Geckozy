// 백엔드 공통 요청 함수
// 서버 주소: .env.development의 VITE_API_URL
// 응답 형식: { code, message, data } → 성공이면 data만 돌려주고, 실패면 ApiError를 던진다
const BASE_URL = import.meta.env.VITE_API_URL

// 로그인 토큰 (JWT). 로그인할 때 받아서 이후 요청 헤더에 붙인다
const TOKEN_KEY = 'geckozy-access-token'

export const getAccessToken = () => {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export const setAccessToken = (token) => {
  try {
    localStorage.setItem(TOKEN_KEY, token)
  } catch {
    // 저장소를 쓸 수 없는 환경(사파리 개인정보 보호 모드 등)
  }
}

export const clearAccessToken = () => {
  try {
    localStorage.removeItem(TOKEN_KEY)
  } catch {
    // 저장소를 쓸 수 없는 환경
  }
}

// status: HTTP 상태 코드 (서버에 닿지 못하면 0)
// code: 서버 에러 코드 (예: 'DUPLICATE_USERNAME', 'INVALID_CREDENTIALS'). 백엔드 ErrorCode.java 참고
export class ApiError extends Error {
  constructor(status, code) {
    super(code)
    this.status = status
    this.code = code
  }
}

// path: '/auth/login' 같은 경로, params: 쿼리스트링, body: JSON으로 보낼 값
export const request = async (path, { method = 'GET', params, body } = {}) => {
  const url = new URL(path, BASE_URL)
  if (params) {
    Object.entries(params).forEach(([key, value]) => url.searchParams.set(key, value))
  }

  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = getAccessToken()
  if (token) headers.Authorization = `Bearer ${token}`

  let response
  try {
    response = await fetch(url, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) })
  } catch {
    throw new ApiError(0, 'NETWORK_ERROR')
  }

  const json = await response.json().catch(() => null)
  if (!response.ok) throw new ApiError(response.status, json?.message ?? 'UNKNOWN_ERROR')
  return json?.data
}
