import { useState } from 'react'

import OnboardingHeader from '../common/OnboardingHeader'
import OnboardingField from '../common/OnboardingField'
import OnboardingInput from '../common/OnboardingInput'
import OnboardingSelect from '../common/OnboardingSelect'
import OnboardingButton from '../common/OnboardingButton'
import { checkIdDuplicate, signup as requestSignup } from '../../../../api/onboarding'

import './Signup.scss'

// 피그마 드롭다운 컴포넌트 순서. 피그마의 'hanmail.com'은 실제 도메인인 hanmail.net으로 표기
const EMAIL_DOMAINS = [
  { value: 'naver.com', label: 'naver.com' },
  { value: 'daum.net', label: 'daum.net' },
  { value: 'gmail.com', label: 'gmail.com' },
  { value: 'hanmail.net', label: 'hanmail.net' },
]

const REQUIRED_FIELDS = ['id', 'password', 'passwordConfirm', 'emailId', 'emailDomain']

const Signup = ({ form, updateForm, onPrev, resetTo }) => {
  const { signup } = form
  const [idCheckMessage, setIdCheckMessage] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleChange = (field) => (value) => updateForm('signup', { ...signup, [field]: value })

  const handleIdChange = (value) => {
    handleChange('id')(value)
    setIdCheckMessage('')
  }

  const handleIdCheck = async () => {
    if (!signup.id.trim()) {
      setIdCheckMessage('아이디를 입력해주세요.')
      return
    }

    const result = await checkIdDuplicate(signup.id)
    setIdCheckMessage(result.available ? '사용 가능한 아이디예요.' : '이미 사용 중인 아이디예요.')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (isSubmitting) return

    // TODO: 아이디·비밀번호 형식, 비밀번호 확인 일치, 중복확인 완료 여부 검사
    if (REQUIRED_FIELDS.some((field) => !signup[field].trim())) {
      setError('모든 항목을 입력해주세요.')
      return
    }

    setError('')
    setIsSubmitting(true)
    const result = await requestSignup({
      id: signup.id,
      password: signup.password,
      email: `${signup.emailId}@${signup.emailDomain}`,
    })
    setIsSubmitting(false)

    // 가입 후 로그인 화면으로. 처음 로그인하면 온보딩(현재 상황)이 이어진다
    if (result.success) {
      resetTo('login')
    } else {
      setError('회원가입에 실패했어요. 다시 시도해주세요.')
    }
  }

  return (
    <section className="signup">
      <OnboardingHeader title="회원가입" onBack={onPrev} />

      <form className="signup-form" onSubmit={handleSubmit} noValidate>
        <div className="signup-fields">
          <OnboardingField label="아이디" hint="4~12자/영문 소문자(숫자 조합 가능)" message={idCheckMessage}>
            <div className="signup-id-row">
              <OnboardingInput
                name="id"
                placeholder="아이디를 입력해주세요."
                autoComplete="username"
                value={signup.id}
                onChange={handleIdChange}
              />
              <OnboardingButton variant="filled" onClick={handleIdCheck}>중복확인</OnboardingButton>
            </div>
          </OnboardingField>

          <OnboardingField label="비밀번호" hint="6~20자/영문 대문자, 소문자, 특수문자 중 2가지 이상 포함">
            <OnboardingInput
              type="password"
              name="password"
              placeholder="비밀번호를 입력해주세요."
              autoComplete="new-password"
              value={signup.password}
              onChange={handleChange('password')}
            />
            <OnboardingInput
              type="password"
              name="passwordConfirm"
              placeholder="비밀번호를 다시 한번 입력해주세요."
              autoComplete="new-password"
              value={signup.passwordConfirm}
              onChange={handleChange('passwordConfirm')}
            />
          </OnboardingField>

          <OnboardingField label="이메일">
            <div className="signup-email-row">
              <OnboardingInput
                name="emailId"
                placeholder="이메일"
                autoComplete="off"
                value={signup.emailId}
                onChange={handleChange('emailId')}
              />
              <span className="signup-email-at" aria-hidden="true">@</span>
              <div className="signup-email-domain">
                <OnboardingSelect
                  ariaLabel="이메일 도메인"
                  placeholder="선택"
                  options={EMAIL_DOMAINS}
                  value={signup.emailDomain}
                  onChange={handleChange('emailDomain')}
                />
              </div>
            </div>
          </OnboardingField>
        </div>

        <div className="signup-footer">
          {error && <p className="signup-error" role="alert">{error}</p>}
          <OnboardingButton type="submit" disabled={isSubmitting}>회원가입</OnboardingButton>
        </div>
      </form>
    </section>
  )
}

export default Signup
