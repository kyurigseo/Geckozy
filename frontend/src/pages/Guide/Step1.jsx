import React from 'react'

import { useNavigate } from 'react-router-dom'
import Nav from '../../components/nav/Nav'

import background from '../../assets/guide/가이드 배경.svg'
import stepTitle from '../../assets/guide/1단계 준비.svg'
import step1intro from '../../assets/guide/1단계 인트로.svg'
import back from '../../assets/guide/뒤로가기.svg'
import enclosureButton from '../../assets/guide/사육장 버튼.svg'
import enclosureButtonPressed from '../../assets/guide/사육장 버튼 눌림.svg'

import './Step1.scss'

const Step1 = () => {
  const navigate = useNavigate()

  return (
    <div className="step1">

      <img className="step1-background" src={background} alt="" />

      <button className="step1-back" onClick={() => navigate('/guide')}>
        <img src={back} alt="뒤로가기" />
      </button>

      <div className="step1-progress">
        <div className="progress-dot active"></div>
        <div className="progress-line"></div>
        <div className="progress-dot"></div>
        <div className="progress-line"></div>
        <div className="progress-dot"></div>
        <div className="progress-line"></div>
        <div className="progress-dot"></div>
        <div className="progress-line"></div>
        <div className="progress-dot"></div>
      </div>

      <img className="step1-title" src={stepTitle} alt="1단계 사육장 준비" />

      <img className="step1-intro" src={step1intro} alt="" />

      <div className="step1-card card1">
        <div className="card-title">
          <span>01</span>
          <strong>사육장 형태</strong>
          <span className="card-question">왜 세로형이 필요한가요? 〉</span>
        </div>

        <p>
          높이가 충분한 세로형이 좋아요. 크레스티드 게코는 나무를 타고
          생활하는 수목성 도마뱀이에요. 바닥 면적뿐 아니라 위아래로 이동할 수 있는
          높이를 충분히 확보해주세요.
        </p>
      </div>

      <div className="step1-card card2">
        <div className="card-title">
          <span>02</span>
          <strong>사육장 크기</strong>
        </div>

        <p>
          성장 단계에 맞는 공간을 준비해주세요. 성체 한 마리라면 우선
          45 × 45 × 60cm 이상을 기준으로 확인해주세요.
        </p>
      </div>

      <div className="step1-card card3">
        <div className="card-title">
          <span>03</span>
          <strong>소재와 환기</strong>
          <span className="card-question">소재는 무엇을 골라야 하나요? 〉</span>
        </div>

        <p>
          습도 유지와 환기를 함께 확인해주세요. 유리·PVC·아크릴 등 소재만 보기보다
          습도를 유지하면서 공기가 통할 수 있는 구조인지 확인해주세요.
        </p>
      </div>

      <div className="step1-card card4">
        <div className="card-title">
          <span>04</span>
          <strong>설치 위치</strong>
        </div>

        <p>
          온도가 크게 흔들리지 않는 곳에 놓아주세요. 직사광선, 난방기,
          에어컨 바람이 직접 닿는 위치는 피해주세요.
        </p>
      </div>

      <button
        className="step1-button"
        onClick={() => navigate('/lizard-info')}
      >
        <img className="button-normal" src={enclosureButton} alt="사육장 정보 입력하기" />
        <img className="button-pressed" src={enclosureButtonPressed} alt="사육장 정보 입력하기" />
      </button>

      <Nav />

    </div>
  )
}

export default Step1