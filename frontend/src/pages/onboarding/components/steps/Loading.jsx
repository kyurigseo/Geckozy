import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import LoadingDots from '../common/LoadingDots'
import OnboardingButton from '../common/OnboardingButton'
import SoundToggle from '../common/SoundToggle'
import { saveOnboarding } from '../../../../api/onboarding'

import geckoPixel from '../../../../assets/onboarding/gecko-pixel-rock.png'

import './Loading.scss'

const MIN_LOADING_TIME = 2000 // 저장이 빨리 끝나도 로딩 화면을 최소 이만큼 보여준다

// 온보딩 정보 저장 후 홈으로 이동. 진행 상태 저장값은 Onboarding이 화면을 떠날 때 지운다
// 저장에 실패하면 이유를 보여주고 다시 시도 / 이전 단계로 돌아가 입력 수정
// (다시 시도해도 이미 만든 도마뱀·사육장을 이어서 쓰므로 두 번 등록되지 않음)
const Loading = ({ form, onPrev, isSoundOn, onToggleSound }) => {
  const navigate = useNavigate()
  const formRef = useRef(form)
  const [attempt, setAttempt] = useState(0) // 다시 시도할 때마다 1씩 올려 저장을 다시 실행
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    let isCancelled = false
    const minDelay = new Promise((resolve) => setTimeout(resolve, MIN_LOADING_TIME))

    Promise.all([saveOnboarding(formRef.current), minDelay]).then(([result]) => {
      if (isCancelled) return
      if (result.success) navigate('/home')
      else setErrorMessage(result.message)
    })

    return () => {
      isCancelled = true
    }
  }, [navigate, attempt])

  const retry = () => {
    setErrorMessage('')
    setAttempt((prev) => prev + 1)
  }

  return (
    <section className="loading">
      <div className="loading-message">
        <p className="loading-title">{errorMessage ? '저장하지 못했어요' : '잠시만 기다려주세요!'}</p>
        <p className="loading-description">{errorMessage || '정보를 입력하고 홈 화면으로 이동하고 있어요'}</p>
      </div>

      <img className="loading-gecko" src={geckoPixel} alt="" />

      {errorMessage ? (
        <div className="loading-actions">
          <OnboardingButton onClick={retry}>다시 시도</OnboardingButton>
          <OnboardingButton variant="text" onClick={onPrev}>
            이전으로 돌아가 입력 수정하기
          </OnboardingButton>
        </div>
      ) : (
        <div className="loading-progress" role="status" aria-label="로딩 중">
          <p className="loading-text">Loading...</p>
          <LoadingDots />
        </div>
      )}

      <div className="loading-sound">
        <SoundToggle isOn={isSoundOn} onToggle={onToggleSound} />
      </div>
    </section>
  )
}

export default Loading
