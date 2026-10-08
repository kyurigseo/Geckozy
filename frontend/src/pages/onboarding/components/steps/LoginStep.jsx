import OnboardingInput from '../common/OnboardingInput'
import OnboardingButton from '../common/OnboardingButton'

import logo from '../../../../assets/onboarding/logo.png'
import mailIcon from '../../../../assets/onboarding/icon-mail.png'
import lockIcon from '../../../../assets/onboarding/icon-lock.png'

const LoginStep = ({ form, updateForm, onNext }) => {
  const { login } = form

  const handleChange = (field) => (value) => updateForm('login', { ...login, [field]: value })

  const handleSubmit = (e) => {
    e.preventDefault()
    // TODO: 로그인 API 연결
  }

  return (
    <section className="login-step">
      <img className="login-step-logo" src={logo} alt="Geckozy" />

      <form className="login-step-form" onSubmit={handleSubmit}>
        <OnboardingInput icon={mailIcon} type="email" name="email" placeholder="이메일" value={login.email} onChange={handleChange('email')} />
        <OnboardingInput icon={lockIcon} type="password" name="password" placeholder="비밀번호" value={login.password} onChange={handleChange('password')} />

        <OnboardingButton type="submit">로그인</OnboardingButton>
      </form>

      <OnboardingButton variant="text" onClick={onNext}>회원가입</OnboardingButton>
    </section>
  )
}

export default LoginStep
