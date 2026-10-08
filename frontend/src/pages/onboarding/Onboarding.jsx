import { useState } from 'react'

import LoginStep from './components/steps/LoginStep'
import SignupStep from './components/steps/SignupStep'
import StartMethodStep from './components/steps/StartMethodStep'
import GeckoCustomStep from './components/steps/GeckoCustomStep'
import TankCustomStep from './components/steps/TankCustomStep'
import CareQuestionStep from './components/steps/CareQuestionStep'
import ConcernStep from './components/steps/ConcernStep'
import LoadingStep from './components/steps/LoadingStep'

import './Onboarding.scss'

// 로그인 → 회원가입 → 시작 방식 선택 → 도마뱀 커스텀 → 사육장 커스텀 → 관리 방식 질문 → 고민 선택 → 로딩
const STEPS = [
  { key: 'login', Component: LoginStep },
  { key: 'signup', Component: SignupStep },
  { key: 'start-method', Component: StartMethodStep },
  { key: 'gecko-custom', Component: GeckoCustomStep },
  { key: 'tank-custom', Component: TankCustomStep },
  { key: 'care-question', Component: CareQuestionStep },
  { key: 'concern', Component: ConcernStep },
  { key: 'loading', Component: LoadingStep },
]

const INITIAL_FORM = {
  login: { email: '', password: '' },
  signup: { email: '', password: '', passwordConfirm: '', nickname: '' },
  startMethod: '',
  gecko: { color: '', name: '' },
  tank: { wall: '', floor: '', structure: '', vine: '' },
  care: {},
  concerns: [],
}

const Onboarding = () => {
  const [stepIndex, setStepIndex] = useState(0)
  const [form, setForm] = useState(INITIAL_FORM)

  const goNext = () => setStepIndex((prev) => Math.min(prev + 1, STEPS.length - 1))
  const goPrev = () => setStepIndex((prev) => Math.max(prev - 1, 0))

  // key: INITIAL_FORM의 최상위 키 (예: 'gecko'), value: 해당 키의 새 값
  const updateForm = (key, value) => setForm((prev) => ({ ...prev, [key]: value }))

  const { key, Component: CurrentStep } = STEPS[stepIndex]

  return (
    <div className={`onboarding onboarding-${key}`}>
      <CurrentStep form={form} updateForm={updateForm} onNext={goNext} onPrev={goPrev} />
    </div>
  )
}

export default Onboarding
