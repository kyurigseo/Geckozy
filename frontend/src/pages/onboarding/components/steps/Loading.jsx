import { useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'

import LoadingDots from '../common/LoadingDots'
import SoundToggle from '../common/SoundToggle'
import { saveOnboarding } from '../../../../api/onboarding'

import geckoPixel from '../../../../assets/onboarding/gecko-pixel-rock.png'

import './Loading.scss'

const MIN_LOADING_TIME = 2000 // 저장이 빨리 끝나도 로딩 화면을 최소 이만큼 보여준다

// 온보딩 정보 저장 후 홈으로 이동. 진행 상태 저장값은 Onboarding이 화면을 떠날 때 지운다
const Loading = ({ form, isSoundOn, onToggleSound }) => {
  const navigate = useNavigate()
  const formRef = useRef(form)

  useEffect(() => {
    let isCancelled = false
    const minDelay = new Promise((resolve) => setTimeout(resolve, MIN_LOADING_TIME))

    // TODO: 저장 실패 시 처리 (다시 시도 안내 등)
    Promise.all([saveOnboarding(formRef.current), minDelay]).then(() => {
      if (!isCancelled) navigate('/home')
    })

    return () => {
      isCancelled = true
    }
  }, [navigate])

  return (
    <section className="loading">
      <div className="loading-message">
        <p className="loading-title">잠시만 기다려주세요!</p>
        <p className="loading-description">정보를 입력하고 홈 화면으로 이동하고 있어요</p>
      </div>

      <img className="loading-gecko" src={geckoPixel} alt="" />

      <div className="loading-progress" role="status" aria-label="로딩 중">
        <p className="loading-text">Loading...</p>
        <LoadingDots />
      </div>

      <div className="loading-sound">
        <SoundToggle isOn={isSoundOn} onToggle={onToggleSound} />
      </div>
    </section>
  )
}

export default Loading
