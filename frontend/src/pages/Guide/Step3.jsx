import React from 'react'
import { useNavigate } from 'react-router-dom'
import Nav from '../../components/nav/Nav'

import background from '../../assets/guide/가이드 배경.svg'
import stepTitle from '../../assets/guide/3단계 준비.svg'
import step3intro from '../../assets/guide/3단계 인트로.svg'
import back from '../../assets/guide/뒤로가기.svg'
import sensorButton from '../../assets/guide/센서측정확인하기.svg'


import './Step3.scss'

const Step3 = () => {
  const navigate = useNavigate()

  return (
    <div className="step3">
      <img className="step3-background" src={background} alt="" />

      <div className="step3-scroll">
        <div className="step3-inner">

          <button className="step3-back" onClick={() => navigate('/guide/step2')}>
            <img src={back} alt="뒤로가기" />
          </button>

          <div className="step3-progress">
            <div className="progress-dot active"></div>
            <div className="progress-line"></div>
            <div className="progress-dot active"></div>
            <div className="progress-line"></div>
            <div className="progress-dot active"></div>
            <div className="progress-line"></div>
            <div className="progress-dot"></div>
            <div className="progress-line"></div>
            <div className="progress-dot"></div>
          </div>

          <img className="step3-title" src={stepTitle} alt="3단계 센서 설치" />

          <img className="step3-intro" src={step3intro} alt="" />

          <div className="step3-card card1">
            <div className="card-title">
              <span data-text="01">01</span>
              <strong>센서 연결하기</strong>
            </div>

            <p>
              핸드폰의 블루투스를 통해 온습도 센서를 앱과 연결해주세요.
            </p>

            <div className="sensor-connect">
              연결된 센서가 없어요.
            </div>
          </div>

          <div className="step3-card card2">
            <div className="card-title">
              <span data-text="02">02</span>
              <strong>센서 설치하기</strong>
            </div>

            <p>
              도마뱀의 생활 공간에 적절한 환경을 확인할 수 있는 위치에 설치해주세요.
            </p>

            <p>
              센서 위치에 따라 측정값이 달라질 수 있으므로, 열원이나 보금자리가 직접 닿는 곳은 피해주세요.
            </p>

            <p>설치할 때 아래 항목을 확인해주세요.</p>

            <div className="sensor-check-list">
              <label><input type="checkbox" /> 센서가 열원 바로 아래에 있지 않아요</label>
              <label><input type="checkbox" /> 몸무게 등이 센서에 직접 닿지 않아요</label>
              <label><input type="checkbox" /> 물그릇 바로 옆처럼 습도가 과도적으로 높은 곳을 피했어요</label>
              <label><input type="checkbox" /> 도마뱀의 센서를 떨어뜨리거나 손상시키지 않도록 주의했어요</label>
            </div>
          </div>

          <button className="step3-button">
            <img src={sensorButton} alt="센서 측정 확인하기" />
          </button>

        </div>
      </div>

      <Nav />
    </div>
  )
}

export default Step3