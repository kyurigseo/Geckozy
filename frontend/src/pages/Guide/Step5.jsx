import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import background from '../../assets/guide/가이드 배경.svg'
import stepTitle from '../../assets/guide/5단계 준비.svg'
import intro from '../../assets/guide/5단계 제목.svg'
import completeButton from '../../assets/guide/준비완료.svg'
import back from '../../assets/guide/뒤로가기.svg'

import './Step5.scss'

const Step5 = () => {
  const navigate = useNavigate()

  const [checked, setChecked] = useState([
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false
  ])

  const handleCheck = (index) => {
    setChecked((prev) => prev.map((item, i) => i === index ? !item : item))
  }

  const isAllChecked = checked.every((item) => item)

  return (
    <div className="step5">
      <img className="step5-background" src={background} alt="" />

      <div className="step5-scroll">
        <div className="step5-inner">

          <button className="step5-back" onClick={() => navigate('/guide/step4')}>
            <img src={back} alt="뒤로가기" />
          </button>

          <div className="step5-progress">
            <div className="progress-dot active"></div>
            <div className="progress-line"></div>
            <div className="progress-dot active"></div>
            <div className="progress-line"></div>
            <div className="progress-dot active"></div>
            <div className="progress-line"></div>
            <div className="progress-dot active"></div>
            <div className="progress-line"></div>
            <div className="progress-dot active"></div>
          </div>

          <img className="step5-title" src={stepTitle} alt="5단계 준비 상태 확인" />

          <img className="step5-intro" src={intro} alt="" />

          <div className="step5-card">

            <div className="check-section">
              <div className="check-title">
                <span data-text="01">01</span>
                <strong>사육장</strong>
              </div>

              <label>
                <input
                  type="checkbox"
                  checked={checked[0]}
                  onChange={() => handleCheck(0)}
                />
                <span>문과 잠금장치가 잘 닫혀 있어요</span>
              </label>

              <label>
                <input
                  type="checkbox"
                  checked={checked[1]}
                  onChange={() => handleCheck(1)}
                />
                <span>가지와 은신처가 흔들리지 않게 고정되어 있어요</span>
              </label>
            </div>

            <div className="check-section">
              <div className="check-title">
                <span data-text="02">02</span>
                <strong>생활 준비</strong>
              </div>

              <label>
                <input
                  type="checkbox"
                  checked={checked[2]}
                  onChange={() => handleCheck(2)}
                />
                <span>깨끗한 물과 물그릇을 준비했어요</span>
              </label>

              <label>
                <input
                  type="checkbox"
                  checked={checked[3]}
                  onChange={() => handleCheck(3)}
                />
                <span>먹이와 급여 방법을 미리 확인했어요</span>
              </label>

              <label>
                <input
                  type="checkbox"
                  checked={checked[4]}
                  onChange={() => handleCheck(4)}
                />
                <span>입양 직후 편하게 숨을 수 있는 공간을 마련했어요</span>
              </label>
            </div>

            <div className="check-section">
              <div className="check-title">
                <span data-text="03">03</span>
                <strong>환경 및 센서</strong>
              </div>

              <label>
                <input
                  type="checkbox"
                  checked={checked[5]}
                  onChange={() => handleCheck(5)}
                />
                <span>센서가 정상적으로 연결되어 있어요</span>
              </label>

              <label>
                <input
                  type="checkbox"
                  checked={checked[6]}
                  onChange={() => handleCheck(6)}
                />
                <span>센서가 처음 설치한 위치에 잘 고정되어 있어요</span>
              </label>

              <label>
                <input
                  type="checkbox"
                  checked={checked[7]}
                  onChange={() => handleCheck(7)}
                />
                <span>7일간 온·습도 변화 확인을 완료했어요</span>
              </label>
            </div>

          </div>

            <button
                className="step5-button"
                disabled={!isAllChecked}
                onClick={() => navigate('/loading-fin')}
            >
                <img src={completeButton} alt="사육장 준비 완료" />
            </button>

        </div>
      </div>
    </div>
  )
}

export default Step5