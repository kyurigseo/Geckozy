// TODO: 백엔드 API가 나오면 실제 요청으로 교체 (현재는 가짜 응답)
const FAKE_DELAY = 500

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

// 로그인. 항상 성공 + 온보딩 미완료(처음 로그인) 응답을 돌려준다.
export const login = async (credentials) => {
  await delay(FAKE_DELAY)
  return { success: true, user: { email: credentials.email, isOnboardingCompleted: false } }
}

// 아이디 중복확인. 항상 사용 가능 응답을 돌려준다.
export const checkIdDuplicate = async (id) => {
  await delay(FAKE_DELAY)
  return { available: true, id }
}

// 회원가입. 항상 성공 응답을 돌려준다.
export const signup = async (account) => {
  await delay(FAKE_DELAY)
  return { success: true, user: { id: account.id } }
}

// 온보딩 입력값 저장 (도마뱀·사육장·관리 방식·고민). 항상 성공 응답을 돌려준다.
// TODO: 실제 API 연결 시 비밀번호 등 계정 정보는 빼고 보내기
export const saveOnboarding = async (data) => {
  await delay(FAKE_DELAY)
  return { success: true, data }
}
