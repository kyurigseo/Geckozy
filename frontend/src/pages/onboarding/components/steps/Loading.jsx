import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

import LoadingDots from '../common/LoadingDots'

import geckoPixel from '../../../../assets/onboarding/gecko-pixel-rock.png'

const LOADING_DURATION = 2000

const Loading = () => {
  const navigate = useNavigate()

  useEffect(() => {
    // TODO: 온보딩 결과 저장 API 연결 후, 완료되면 이동하도록 변경
    const timer = setTimeout(() => navigate('/home'), LOADING_DURATION)
    return () => clearTimeout(timer)
  }, [navigate])

  return (
    <section className="loading">
      <img className="loading-image" src={geckoPixel} alt="" />
      <p className="loading-text">사육장을 준비하고 있어요</p>
      <LoadingDots />
    </section>
  )
}

export default Loading
