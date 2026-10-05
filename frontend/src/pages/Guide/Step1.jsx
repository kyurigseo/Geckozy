import React, { useState } from 'react'
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
  const [popup, setPopup] = useState(null)

  return (
    <div className="step1">
      <img
        className="step1-background"
        src={background}
        alt=""
      />

      <div className="step1-scroll">
        <div className="step1-inner">

          <button
            className="step1-back"
            onClick={() => navigate('/guide')}
          >
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

          <img
            className="step1-title"
            src={stepTitle}
            alt="1단계 사육장 준비"
          />

          <img
            className="step1-intro"
            src={step1intro}
            alt=""
          />

          <div className="step1-card card1">
            <div className="card-title">
              <span data-text="01">01</span>
              <strong>사육장 형태</strong>

              <button
                className="card-question"
                onClick={() => setPopup('vertical')}
              >
                왜 세로형이 필요한가요? 〉
              </button>
            </div>

            <p>
              높이가 충분한 세로형이 좋아요. 크레스티드 게코는 나무를 타고<br />
              생활하는 수목성 도마뱀이에요. 바닥 면적뿐 아니라 위아래로<br />
              이동할 수 있는 높이를 충분히 확보해주세요.
            </p>
          </div>

          <div className="step1-card card2">
            <div className="card-title">
              <span data-text="02">02</span>
              <strong>사육장 크기</strong>
            </div>

            <p>
              성장 단계에 맞는 공간을 준비해주세요. 성체 한 마리라면 우선<br />
              45 × 45 × 60cm 이상을 기준으로 확인해주세요.
            </p>
          </div>

          <div className="step1-card card3">
            <div className="card-title">
              <span data-text="03">03</span>
              <strong>소재와 환기</strong>

              <button
                className="card-question"
                onClick={() => setPopup('material')}
              >
                소재는 무엇을 골라야 하나요? 〉
              </button>
            </div>

            <p>
              습도 유지와 환기를 함께 확인해주세요. 유리·PVC·아크릴 등<br />
              소재만 보기보다 습도를 유지하면서 공기가 통할 수 있는 구조인지<br />
              확인해주세요.
            </p>
          </div>

          <div className="step1-card card4">
            <div className="card-title">
              <span data-text="04">04</span>
              <strong>설치 위치</strong>
            </div>

            <p>
              온도가 크게 흔들리지 않는 곳에 놓아주세요. 직사광선, 난방기,<br />
              에어컨 바람이 직접 닿는 위치는 피해주세요.
            </p>
          </div>

          <button
            className="step1-button"
            onClick={() => navigate('')}
          >
            <img
              className="button-normal"
              src={enclosureButton}
              alt="사육장 정보 입력하기"
            />

            <img
              className="button-pressed"
              src={enclosureButtonPressed}
              alt="사육장 정보 입력하기"
            />
          </button>

        </div>
      </div>

      <Nav />

      {popup && (
        <div className="guide-popup-overlay">
          <div className="guide-popup">

            <button
              className="guide-popup-close"
              onClick={() => setPopup(null)}
            >
              ×
            </button>

            {popup === 'vertical' && (
              <>
                <div className="guide-popup-title">
                  <span data-text="01">01</span>
                  <strong>왜 세로형이 필요한가요?</strong>
                </div>

                <p className="guide-popup-text">
                  크레스티드 게코는 가지와 식생을 오르내리며 생활하기
                  때문에 수직 공간을 적극적으로 사용해요.
                </p>

                <p className="guide-popup-text">
                  그래서 낮고 넓은 형태보다 높이가 확보된 사육장이 좋고,
                  은신 공간을 여러 높이로 배치할 수 있는 구조가 적합해요.
                </p>
              </>
            )}

            {popup === 'material' && (
              <>
                <div className="guide-popup-title">
                  <span data-text="03">03</span>
                  <strong>소재는 무엇을 골라야 하나요?</strong>
                </div>

                <p className="guide-popup-text">
                  특정 소재 하나를 정답으로 지정하기보다는,
                  환기구와 배수 구조가 있는지, 습도 관리와 청소가
                  가능한지를 함께 확인해야 해요.
                </p>

                <div className="guide-popup-recommend">
                    <div className="recommend-title">
                        <span className="recommend-bar"></span>
                        <strong>기관 권고 사항</strong>
                    </div>
                    <p>
                        · RSPCA는 높은 습도 환경 때문에 목재보다 유리 사육장을<br/> 선호한다고 안내해요.
                    </p>

                    <p>
                        · PetMD는 적절한 환기가 가능한 스크린 구조를 강조해요.
                    </p>
                </div>
              </>
            )}

          </div>
        </div>
      )}
    </div>
  )
}

export default Step1