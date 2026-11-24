import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import useGuideStep from './useGuideStep'

import {
  getEnvironmentCheck,
  getEnvironmentSummary,
  getSensorStatus,
} from '../../api/guide'

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
import { useSound } from '../../sound/SoundProvider'

const Step4 = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { playSfx } = useSound()

  const entryStep = Number(location.state?.entryStep) || 4

  const { finish } = useGuideStep(4)

  const [environment, setEnvironment] = useState(null)
  const [summary, setSummary] = useState(null)
  const [sensor, setSensor] = useState(null)

  const currentDay = Math.min(
    7,
    Math.max(1, environment?.elapsedDays || 1),
  )

  const isComplete = environment?.status === 'COMPLETED'

  const formatTime = (value) =>
    value ? new Date(value).toLocaleString('ko-KR') : '—'

  const handleBack = () => {
    if (entryStep === 4) {
      navigate('/guide')
    } else {
      navigate('/guide/step3', {
        state: { entryStep },
      })
    }
  }

  const handleNext = () => {
    playSfx('button')

    finish(() =>
      navigate('/guide/step5', {
        state: { entryStep },
      }),
    )
  }

  useEffect(() => {
    const controller = new AbortController()
    let lastError = ''

    const refresh = async () => {
      const results = await Promise.allSettled([
        Promise.resolve().then(() =>
          getEnvironmentCheck(controller.signal),
        ),
        Promise.resolve().then(() =>
          getEnvironmentSummary(controller.signal),
        ),
        Promise.resolve().then(() =>
          getSensorStatus(controller.signal),
        ),
      ])

      if (controller.signal.aborted) return

      const setters = [
        setEnvironment,
        setSummary,
        setSensor,
      ]

      results.forEach((result, index) => {
        if (result.status === 'fulfilled') {
          setters[index](result.value)
        } else {
          setters[index](null)
        }
      })

      const failure = results.find(
        (result) => result.status === 'rejected',
      )

      if (
        failure &&
        failure.reason.message !== lastError
      ) {
        window.alert(failure.reason.message)
      }

      lastError = failure?.reason.message || ''
    }

    refresh()

    const timer = setInterval(refresh, 30000)

    return () => {
      controller.abort()
      clearInterval(timer)
    }
  }, [])

  return (
    <div className="step4">
      <img
        className="step4-background"
        src={background}
        alt=""
      />

      <div className="step4-scroll">
        <div
          className={`step4-inner ${
            isComplete ? 'complete' : ''
          }`}
        >
          <button
            className="step4-back"
            onClick={handleBack}
          >
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

          <img
            className="step4-title"
            src={stepTitle}
            alt="4단계 환경 사전 점검"
          />

          <img
            className="step4-title1"
            src={title1}
            alt=""
          />

          <div className="step4-check-card">
            <div className="step4-check-title">
              <img src={leaf} alt="" />
              <strong>온습도 체크 중</strong>
            </div>

            <p>
              도마뱀을 데려오기 전, 센서로 온·습도 변화를 7일 동안 확인해요.
            </p>
            <p>
              실제로 사육할 때처럼 조명과 분무 등을 관리해주세요.
            </p>

            <div className="day-graph">
              <img
                className="day-graph-image"
                src={dayGraph}
                alt=""
              />

              <div
                className="day-graph-fill"
                style={{
                  width: `${
                    11 + (currentDay - 1) * 50
                  }px`,
                }}
              ></div>

              <img
                className="day-graph-gecko"
                src={gecko}
                alt=""
                style={{
                  left: `${
                    6 + (currentDay - 1) * 50
                  }px`,
                }}
              />

              <div className="day-labels">
                {Array.from(
                  { length: 7 },
                  (_, index) => {
                    const day = index + 1

                    return (
                      <span
                        key={day}
                        className={
                          day === currentDay
                            ? 'current'
                            : day > currentDay
                              ? 'future'
                              : ''
                        }
                      >
                        {day}일
                      </span>
                    )
                  },
                )}
              </div>
            </div>
          </div>

          <div className="step4-current">
            <div className="current-item">
              <img src={temperatureIcon} alt="" />
              <div>
                <span>현재 온도</span>
                <strong>
                  {sensor?.lastTemperature == null
                    ? '—'
                    : `${sensor.lastTemperature}℃`}
                </strong>
              </div>
            </div>

            <div className="current-item">
              <img src={humidityIcon} alt="" />
              <div>
                <span>현재 습도</span>
                <strong>
                  {sensor?.lastHumidity == null
                    ? '—'
                    : `${sensor.lastHumidity}%`}
                </strong>
              </div>
            </div>
          </div>

          <span className="last-measured">
            마지막 측정{' '}
            {formatTime(
              environment?.latestMeasuredAt ||
              sensor?.lastMeasuredAt,
            )}
          </span>

          <div className="step4-summary">
          <img
            className="step4-title2"
            src={title2}
            alt=""
          />

          <div className="step4-result">
            <div className="result-section">
              <div className="result-title">
                <span data-text="01">01</span>
                <strong>온도</strong>
                <b>
                  {summary?.sufficientData
                    ? summary.temperatureSummary
                    : summary?.guidance ||
                      '환경 데이터를 기다리고 있어요.'}
                </b>
              </div>

              <p className="p_1">
                {summary?.sufficientData
                  ? summary.dayNightSummary
                  : '—'}
              </p>
            </div>

            <div className="result-section">
              <div className="result-title">
                <span data-text="02">02</span>
                <strong>습도</strong>
                <b>
                  {summary?.sufficientData
                    ? summary.humiditySummary
                    : '—'}
                </b>
              </div>

              <p>
                {summary?.sufficientData
                  ? summary.durationSummary
                  : '—'}
              </p>
            </div>

            {(summary?.checkPoints?.length > 0 ||
              environment?.missingData) && (
              <div className="result-warning">
                <div className="warning-title">
                  <span></span>
                  <strong>확인할 점</strong>
                </div>

                <p>
                  <strong>
                    {summary?.checkPoints?.[0] ||
                      '센서 데이터 공백을 확인해주세요.'}
                  </strong>
                </p>

                <p>
                  {summary?.checkPoints
                    ?.slice(1)
                    .join(' ') ||
                    summary?.guidance}
                </p>
              </div>
            )}
          </div>

          <span className="last-update">
            마지막 업데이트{' '}
            {formatTime(environment?.latestMeasuredAt)}
          </span>

          <img
            className="step4-button"
            src={isComplete ? finalButton : checkButton}
            alt={
              isComplete
                ? '마지막 단계'
                : '환경 확인 중'
            }
            onClick={isComplete ? handleNext : undefined}
          />
          </div>
        </div>
      </div>
    </div>
  )
}

export default Step4