import { useState } from 'react'

import Login from './components/steps/Login'
import Signup from './components/steps/Signup'
import StartMethod from './components/steps/StartMethod'
import GeckoCustom from './components/steps/GeckoCustom'
import TankCustom from './components/steps/TankCustom'
import CareQuestion from './components/steps/CareQuestion'
import Concern from './components/steps/Concern'
import Loading from './components/steps/Loading'

import './Onboarding.scss'

// 로그인 → 회원가입 → 시작 방식 선택 → 도마뱀 커스텀 → 사육장 커스텀 → 관리 방식 질문 → 고민 선택 → 로딩
const STEPS = [
  { key: 'login', Component: Login },
  { key: 'signup', Component: Signup },
  { key: 'start-method', Component: StartMethod },
  { key: 'gecko-custom', Component: GeckoCustom },
  { key: 'tank-custom', Component: TankCustom },
  { key: 'care-question', Component: CareQuestion },
  { key: 'concern', Component: Concern },
  { key: 'loading', Component: Loading },
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
  // 특정 단계로 바로 이동 (예: 로그인 화면의 '회원가입' 링크)
  const goTo = (stepKey) => setStepIndex(STEPS.findIndex((step) => step.key === stepKey))

  // key: INITIAL_FORM의 최상위 키 (예: 'gecko'), value: 해당 키의 새 값
  const updateForm = (key, value) => setForm((prev) => ({ ...prev, [key]: value }))

  const { key, Component: CurrentStep } = STEPS[stepIndex]

  return (
    <div className={`onboarding onboarding-${key}`}>
      <CurrentStep form={form} updateForm={updateForm} onNext={goNext} onPrev={goPrev} goTo={goTo} />
    </div>
  )
}

export default Onboarding
