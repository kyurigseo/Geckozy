import { useEffect, useState } from 'react'

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
  signup: { id: '', password: '', passwordConfirm: '', emailId: '', emailDomain: '' },
  startMethod: '', // 'owner'(키우고 있어요) | 'preparing'(준비 중)
  gecko: {
    color: '',
    name: '',
    species: '',
    birthKnown: '', // 'known' | 'unknown'
    birthDate: '', // 'YYYY-MM-DD'
    gender: '', // 'male' | 'female'
    size: '', // cm
    weight: '', // g
    sizeUnknown: false,
  },
  tank: {
    wall: '', // 'cream' | 'green' | 'yellow'
    floor: '', // 'bark' | 'sand' | 'grass'
    vine: '', // 'heart' | 'curly' | 'leaf'
    structure: '', // 'log' | 'rock' | 'stump'
    width: '', // cm
    depth: '', // cm
    height: '', // cm
    material: '', // 'glass' | 'acrylic' | 'pvc' | 'mesh' | 'custom'
    materialCustom: '', // material이 'custom'일 때 직접 입력한 값
    ventilation: '', // 'top' | 'side' | 'both' | 'unknown'
  },
  care: {
    mistMethod: '', // 'manual' | 'auto' | 'both'
    mistFrequency: '', // 'over3' | 'twice' | 'once' | 'irregular'
    mistTimes: [], // 'morning' | 'day' | 'evening' | 'night' (복수)
    lightTime: null, // { start: 'HH:MM', end: 'HH:MM' } | 'none'
    heatingMethod: '', // 'scheduled' | 'auto' | 'asNeeded' | 'none'
    heatingTime: null, // heatingMethod가 'scheduled'일 때 { start, end }
  },
  concerns: [], // 고른 고민 (복수)
  concernText: '', // 자유 입력 (최대 300자)
  questSeen: { gecko: false, tank: false }, // 단계별 QUEST 팝업을 이미 봤는지
}

// 새로고침해도 진행 중인 단계와 입력값을 유지 (탭을 닫으면 사라지는 sessionStorage)
const STORAGE_KEY = 'geckozy-onboarding'

const loadSaved = () => {
  try {
    const saved = JSON.parse(sessionStorage.getItem(STORAGE_KEY))
    const isValid = saved?.history?.length > 0 && saved.history.every((key) => STEPS.some((step) => step.key === key))
    return isValid ? saved : null
  } catch {
    return null
  }
}

// 저장된 값을 기본값 위에 항목별로 덮어쓴다 (입력 항목이 추가돼도 새 항목은 기본값으로 채워짐)
const mergeForm = (savedForm = {}) =>
  Object.fromEntries(
    Object.entries(INITIAL_FORM).map(([key, initial]) => {
      const value = savedForm[key]
      if (value === undefined) return [key, initial]
      const isObject = initial && typeof initial === 'object' && !Array.isArray(initial)
      return [key, isObject ? { ...initial, ...value } : value]
    }),
  )

// 비밀번호는 저장하지 않는다
const withoutPasswords = (form) => ({
  ...form,
  login: { ...form.login, password: '' },
  signup: { ...form.signup, password: '', passwordConfirm: '' },
})

const Onboarding = () => {
  const [saved] = useState(loadSaved)
  // 지나온 단계 기록. 뒤로가기는 실제로 왔던 단계로 돌아간다 (로그인→시작 방식 / 회원가입→시작 방식)
  const [history, setHistory] = useState(saved?.history ?? [STEPS[0].key])
  const [form, setForm] = useState(() => mergeForm(saved?.form))
  const [isSoundOn, setIsSoundOn] = useState(saved?.isSoundOn ?? true)

  useEffect(() => {
    try {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ history, form: withoutPasswords(form), isSoundOn }))
    } catch {
      // 저장소를 쓸 수 없는 환경(사파리 개인정보 보호 모드 등)에서는 저장 없이 진행
    }
  }, [history, form, isSoundOn])

  // 온보딩을 떠나면(홈 이동 등) 저장값 삭제. 새로고침은 언마운트가 아니라서 유지된다
  useEffect(() => () => sessionStorage.removeItem(STORAGE_KEY), [])

  const goTo = (stepKey) => setHistory((prev) => [...prev, stepKey])
  const goNext = () =>
    setHistory((prev) => {
      const index = STEPS.findIndex((step) => step.key === prev[prev.length - 1])
      return index < STEPS.length - 1 ? [...prev, STEPS[index + 1].key] : prev
    })
  const goPrev = () => setHistory((prev) => (prev.length > 1 ? prev.slice(0, -1) : prev))
  // 기록을 비우고 이동 (예: 회원가입 완료 → 로그인. 뒤로가기로 회원가입에 돌아가지 않게)
  const resetTo = (stepKey) => setHistory([stepKey])

  // key: INITIAL_FORM의 최상위 키 (예: 'gecko'), value: 해당 키의 새 값
  const updateForm = (key, value) => setForm((prev) => ({ ...prev, [key]: value }))
  const toggleSound = () => setIsSoundOn((prev) => !prev)

  const currentKey = history[history.length - 1]
  const { Component: CurrentStep } = STEPS.find((step) => step.key === currentKey)

  return (
    <div className={`onboarding onboarding-${currentKey}`}>
      <CurrentStep
        form={form}
        updateForm={updateForm}
        onNext={goNext}
        onPrev={goPrev}
        goTo={goTo}
        resetTo={resetTo}
        isSoundOn={isSoundOn}
        onToggleSound={toggleSound}
      />
    </div>
  )
}

export default Onboarding
