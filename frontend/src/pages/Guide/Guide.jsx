import React, { useState } from 'react'
import Nav from '../../components/nav/Nav'

import LizardInfo from './LizardInfo' 

import background from '../../assets/guide/단계 선택 배경.svg'

import stage1 from '../../assets/guide/1단계.svg'
import stage2 from '../../assets/guide/2단계.svg'
import stage3 from '../../assets/guide/3단계.svg'
import stage4 from '../../assets/guide/4단계.svg'
import stage5 from '../../assets/guide/5단계.svg'

import title1 from '../../assets/guide/1단계 글씨.svg'
import title2 from '../../assets/guide/2단계 글씨.svg'
import title3 from '../../assets/guide/3단계 글씨.svg'
import title4 from '../../assets/guide/4단계 글씨.svg'
import title5 from '../../assets/guide/5단계 글씨.svg'

import backgroundMusic from '../../assets/guide/배경음악.svg'

import arrow from '../../assets/guide/arrow.svg'
import arrowPressed from '../../assets/guide/arrow_pressed.svg'

import start from '../../assets/guide/start.svg'
import startAction from '../../assets/guide/start_action.svg'

import guideText from '../../assets/guide/단계선택안내글.svg'

import './Guide.scss'

const Guide = () => {
  const [step, setStep] = useState(1)
  const [isModalOpen, setIsModalOpen] = useState(false) 

  const prevStep = () => {
    if (step > 1) {
      setStep(step - 1)
    }
  }

  const nextStep = () => {
    if (step < 5) {
      setStep(step + 1)
    }
  }

  const handleStart = () => {
    setIsModalOpen(true)
  }

  return (
    <div className="guide">

      <img className="guide-background" src={background} alt="" />

      <div className="guide-stage">

        {step !== 1 && (
          <button className="guide-arrow guide-arrow-left" onClick={prevStep}>
            <img className="arrow-normal" src={arrow} alt="이전" />
            <img className="arrow-hover" src={arrowPressed} alt="이전" />
          </button>
        )}

        {step === 1 && (
          <>
            <img className="stage-image stage1" src={stage1} alt="1단계 사육장 준비" />
            <img className="stage-title" src={title1} alt="1단계 사육장 준비" />
            <p className="stage-description">
              도마뱀에게 맞는 사육장의 크기와<br />
              소재를 확인해요.
            </p>
          </>
        )}

        {step === 2 && (
          <>
            <img className="stage-image stage2" src={stage2} alt="2단계 내부 환경 세팅" />
            <img className="stage-title" src={title2} alt="2단계 내부 환경 세팅" />
            <p className="stage-description">
              도마뱀에게 필요한 조명과 은신처,<br />
              바닥재 등을 준비해요.
            </p>
          </>
        )}

        {step === 3 && (
          <>
            <img className="stage-image stage3" src={stage3} alt="3단계 센서 설치" />
            <img className="stage-title" src={title3} alt="3단계 센서 설치" />
            <p className="stage-description">
              사육장에 센서를 설치하고 온습도가<br />
              잘 측정되는지 확인해요.
            </p>
          </>
        )}

        {step === 4 && (
          <>
            <img className="stage-image stage4" src={stage4} alt="4단계 환경 사전 점검" />
            <img className="stage-title" src={title4} alt="4단계 환경 사전 점검" />
            <p className="stage-description">
              센서 데이터를 통해 사육장의 온습도<br />
              변화를 확인해요.
            </p>
          </>
        )}

        {step === 5 && (
          <>
            <img className="stage-image stage5" src={stage5} alt="5단계 준비 상태 확인" />
            <img className="stage-title" src={title5} alt="5단계 준비 상태 확인" />
            <p className="stage-description">
              도마뱀을 데려오기 전 사육장이 잘<br />
              준비되었는지 최종 확인해요.
            </p>
          </>
        )}

        {step !== 5 && (
          <button className="guide-arrow guide-arrow-right" onClick={nextStep}>
            <img className="arrow-normal" src={arrow} alt="다음" />
            <img className="arrow-hover" src={arrowPressed} alt="다음" />
          </button>
        )}

      </div>

      <img className="guide-background-music" src={backgroundMusic} alt="배경음악" />

      <button className="guide-start" onClick={handleStart}>
        <img className="start-normal" src={start} alt="Start" />
        <img className="start-hover" src={startAction} alt="Start" />
      </button>

      <img className="guide-start-text" src={guideText} alt="원하는 단계를 선택해 시작해주세요." />

      {/* <Nav /> */}
      
      {isModalOpen && (
        <LizardInfo
            step={step}
            onClose={() => setIsModalOpen(false)}
        />
      )}

    </div>
  )
}

export default Guide