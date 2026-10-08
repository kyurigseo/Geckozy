// TODO: 백엔드 API가 나오면 실제 요청으로 교체 (현재는 가짜 응답)
const FAKE_DELAY = 500

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

// 로그인. 항상 성공 응답을 돌려준다.
export const login = async (credentials) => {
  await delay(FAKE_DELAY)
  return { success: true, user: { email: credentials.email } }
}
