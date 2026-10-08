import OnboardingInput from '../common/OnboardingInput'
import OnboardingButton from '../common/OnboardingButton'

import mailIcon from '../../../../assets/onboarding/icon-mail.png'
import lockIcon from '../../../../assets/onboarding/icon-lock.png'

const SignupStep = ({ form, updateForm, onNext, onPrev }) => {
  const { signup } = form

  const handleChange = (field) => (value) => updateForm('signup', { ...signup, [field]: value })

  const handleSubmit = (e) => {
    e.preventDefault()
    // TODO: 회원가입 API 연결 + 입력값 검증
    onNext()
  }

  return (
    <section className="signup-step">
      <form className="signup-step-form" onSubmit={handleSubmit}>
        <OnboardingInput label="닉네임" name="nickname" value={signup.nickname} onChange={handleChange('nickname')} />
        <OnboardingInput label="이메일" icon={mailIcon} type="email" name="email" value={signup.email} onChange={handleChange('email')} />
        <OnboardingInput label="비밀번호" icon={lockIcon} type="password" name="password" value={signup.password} onChange={handleChange('password')} />
        <OnboardingInput label="비밀번호 확인" icon={lockIcon} type="password" name="passwordConfirm" value={signup.passwordConfirm} onChange={handleChange('passwordConfirm')} />

        <OnboardingButton type="submit">가입하기</OnboardingButton>
      </form>

      <OnboardingButton variant="text" onClick={onPrev}>로그인으로 돌아가기</OnboardingButton>
    </section>
  )
}

export default SignupStep
