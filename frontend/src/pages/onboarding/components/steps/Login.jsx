import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import OnboardingInput from '../common/OnboardingInput'
import OnboardingButton from '../common/OnboardingButton'
import { login as requestLogin } from '../../../../api/onboarding'

import logo from '../../../../assets/onboarding/logo.png'
import mailIcon from '../../../../assets/onboarding/icon-mail.png'
import lockIcon from '../../../../assets/onboarding/icon-lock.png'
import soundOnIcon from '../../../../assets/onboarding/icon-sound-on.png'

import './Login.scss'

const Login = ({ form, updateForm, goTo }) => {
  const navigate = useNavigate()
  const { login } = form
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleChange = (field) => (value) => updateForm('login', { ...login, [field]: value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (isSubmitting) return

    if (!login.email.trim() || !login.password.trim()) {
      setError('이메일과 비밀번호를 입력해주세요.')
      return
    }

    setError('')
    setIsSubmitting(true)
    const result = await requestLogin({ email: login.email, password: login.password })
    setIsSubmitting(false)

    if (result.success) {
      // TODO: API 응답에 온보딩 완료 여부가 생기면, 미완료 사용자는 goTo('start-method')로 보내기
      navigate('/home')
    } else {
      setError('로그인에 실패했어요. 다시 시도해주세요.')
    }
  }

  return (
    <section className="login">
      {/* TODO: 배경음악 재생/정지 연결, '끄기' 상태 아이콘 받으면 토글 처리 */}
      <button type="button" className="login-sound" aria-label="배경음악 끄기">
        <img src={soundOnIcon} alt="" />
      </button>

      <img className="login-logo" src={logo} alt="Geckozy" />

      <form className="login-form" onSubmit={handleSubmit} noValidate>
        <div className="login-inputs">
          <OnboardingInput
            icon={mailIcon}
            type="email"
            name="email"
            placeholder="이메일을 입력해주세요."
            autoComplete="email"
            value={login.email}
            onChange={handleChange('email')}
          />
          <OnboardingInput
            icon={lockIcon}
            type="password"
            name="password"
            placeholder="비밀번호를 입력해주세요."
            autoComplete="current-password"
            value={login.password}
            onChange={handleChange('password')}
          />
        </div>

        <OnboardingButton type="submit" disabled={isSubmitting}>로그인</OnboardingButton>
      </form>

      <nav className="login-links">
        {/* TODO: 이메일 찾기·비밀번호 찾기 화면 나오면 연결 */}
        <OnboardingButton variant="text">이메일 찾기</OnboardingButton>
        <span className="login-links-divider" aria-hidden="true">|</span>
        <OnboardingButton variant="text">비밀번호 찾기</OnboardingButton>
        <span className="login-links-divider" aria-hidden="true">|</span>
        <OnboardingButton variant="text" onClick={() => goTo('signup')}>회원가입</OnboardingButton>
      </nav>

      {error && <p className="login-error" role="alert">{error}</p>}
    </section>
  )
}

export default Login
