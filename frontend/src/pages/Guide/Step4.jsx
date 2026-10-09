import React from 'react'
import { useNavigate } from 'react-router-dom'

import background from '../../assets/guide/가이드 배경.svg'
import stepTitle from '../../assets/guide/4단계 준비.svg'
import title1 from '../../assets/guide/4단계 제목 1.svg'
import title2 from '../../assets/guide/4단계 제목 2.svg'
import leaf from '../../assets/guide/나뭇잎.svg'
import temperatureIcon from '../../assets/guide/온도계.svg'
import humidityIcon from '../../assets/guide/물방울.svg'
import gecko from '../../assets/guide/미니도마뱀.svg'
import dayGraph from '../../assets/guide/day-graph.svg'
import checkButton from '../../assets/guide/환경 확인중 버튼.svg'
import back from '../../assets/guide/뒤로가기.svg'
import finalButton from '../../assets/guide/마지막 단계.svg'

import './Step4.scss'

const Step4 = () => {
  const navigate = useNavigate()

  const currentDay = 7

  return (
    <div className="step4">
      <img className="step4-background" src={background} alt="" />

      <div className="step4-scroll">
        <div className={`step4-inner ${currentDay === 7 ? 'complete' : ''}`}>

          <button className="step4-back" onClick={() => navigate('/guide/step3')}>
            <img src={back} alt="뒤로가기" />
          </button>

          <div className="step4-progress">
            <div className="progress-dot active"></div>
            <div className="progress-line"></div>
            <div className="progress-dot active"></div>
            <div className="progress-line"></div>
            <div className="progress-dot active"></div>
            <div className="progress-line"></div>
            <div className="progress-dot active"></div>
            <div className="progress-line"></div>
            <div className="progress-dot"></div>
          </div>

          <img className="step4-title" src={stepTitle} alt="4단계 환경 사전 점검" />

          <img className="step4-title1" src={title1} alt="" />

          <div className="step4-check-card">
            <div className="step4-check-title">
                <img src={leaf} alt="" />
                <strong>온습도 체크 중</strong>
            </div>

            <p>도마뱀을 데려오기 전, 센서로 온·습도 변화를 7일 동안 확인해요.</p>
            <p>실제로 사육할 때처럼 조명과 분무 등을 관리해주세요.</p>

            <div className="day-graph">
                <img className="day-graph-image" src={dayGraph} alt="" />

                <div
                    className="day-graph-fill"
                    style={{ width: `${11 + (currentDay - 1) * 50}px` }}
                ></div>

                <img
                    className="day-graph-gecko"
                    src={gecko}
                    alt=""
                    style={{ left: `${6 + (currentDay - 1) * 50}px` }}
                />

                <div className="day-labels">
                    {Array.from({ length: 7 }, (_, index) => {
                    const day = index + 1

                    return (
                        <span
                        key={day}
                        className={day === currentDay ? 'current' : day > currentDay ? 'future' : ''}
                        >
                        {day}일
                        </span>
                    )
                    })}
                </div>
            </div>
          </div>

          <div className="step4-current">
            <div className="current-item">
              <img src={temperatureIcon} alt="" />
              <div>
                <span>현재 온도</span>
                <strong>24.1℃</strong>
              </div>
            </div>

            <div className="current-item">
              <img src={humidityIcon} alt="" />
              <div>
                <span>현재 습도</span>
                <strong>54%</strong>
              </div>
            </div>
          </div>

          <span className="last-measured">마지막 측정 14:30</span>

          <img className="step4-title2" src={title2} alt="" />

          <div className="step4-result">

            <div className="result-section">
              <div className="result-title">
                <span data-text="01">01</span>
                <strong>온도</strong>
                <b>22.8~25.6℃ 사이에서 유지됐어요</b>
              </div>
              <p className='p_1'>낮에는 주로 24~25℃, 밤에는 23℃ 안팎으로 낮아졌어요.<br/>두 날 모두 급격하게 오르거나 내려가는 변화는 없었어요.</p>
            </div>

            <div className="result-section">
              <div className="result-title">
                <span data-text="02">02</span>
                <strong>습도</strong>
                <b>습윤-건조 흐름이 정상적으로 나타났어요</b>
              </div>

              <p>분무 후 약 78~82%까지 상승한 뒤,<br/>시간이 지나면서 50~60%대까지 낮아졌어요.</p>
            </div>

            {currentDay !== 7 && (
            <div className="result-warning">
                <div className="warning-title">
                <span></span>
                <strong>확인할 점</strong>
                </div>

                <p><strong>습도가 높은 상태로 오래 유지된 날</strong>이 있었어요.</p>
                <p>2일차에는 분무 후 70% 이상인 상태가 약 5시간 지속됐어요.<br/>남은 기간에도 건조가 늦어지는 날이 반복되는지 확인해볼게요.</p>
            </div>
            )}

          </div>

          <span className="last-update">마지막 업데이트 9월 1일 00:01</span>

            <img
            className="step4-button"
            src={currentDay === 7 ? finalButton : checkButton}
            alt={currentDay === 7 ? '마지막 단계' : '환경 확인 중'}
            onClick={currentDay === 7 ? () => navigate('/guide/step5') : undefined}
            />

        </div>
      </div>
    </div>
  )
}

export default Step4