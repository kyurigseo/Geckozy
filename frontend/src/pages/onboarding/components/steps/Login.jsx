import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import OnboardingInput from '../common/OnboardingInput'
import OnboardingButton from '../common/OnboardingButton'
import SoundToggle from '../common/SoundToggle'
import { login as requestLogin } from '../../../../api/onboarding'

import logo from '../../../../assets/onboarding/logo.png'
import mailIcon from '../../../../assets/onboarding/icon-mail.png'
import lockIcon from '../../../../assets/onboarding/icon-lock.png'

import './Login.scss'

const Login = ({ form, updateForm, goTo, isSoundOn, onToggleSound }) => {
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
      // 온보딩은 처음 로그인했을 때만 진행. 이미 마친 사용자는 홈으로
      if (result.user.isOnboardingCompleted) {
        navigate('/home')
      } else {
        goTo('start-method')
      }
    } else {
      setError('로그인에 실패했어요. 다시 시도해주세요.')
    }
  }

  return (
    <section className="login">
      <SoundToggle isOn={isSoundOn} onToggle={onToggleSound} />

      <img className="login-logo" src={logo} alt="Geckozy" />

      <form className="login-form" onSubmit={handleSubmit} noValidate>
        <div className="login-inputs">
          <OnboardingInput
            variant="pixel"
            icon={mailIcon}
            type="email"
            name="email"
            placeholder="이메일을 입력해주세요."
            autoComplete="email"
            value={login.email}
            onChange={handleChange('email')}
          />
          <OnboardingInput
            variant="pixel"
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
