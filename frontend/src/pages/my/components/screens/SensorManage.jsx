import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import {
  getMySensors,
  connectMySensor,
} from '../../../../api/myManage'

import back from '../../../../assets/my/뒤로가기.svg'
import sensorImage from '../../../../assets/my/센서.svg'
import batteryIcon from '../../../../assets/my/배터리.svg'
import connectButton from '../../../../assets/my/새 센서 연결하기.svg'
import graphIcon from '../../../../assets/my/그래프.svg'

import { useSound } from '../../../../sound/SoundProvider'

import './SensorManage.scss'

const SensorManage = () => {
  const navigate = useNavigate()
  const { playSfx } = useSound()

  const [sensors, setSensors] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [now, setNow] = useState(() => Date.now())

  const connecting = useRef(false)

  useEffect(() => {
    const timer = setInterval(() => {
      setNow(Date.now())
    }, 60000)

    return () => clearInterval(timer)
  }, [])

  useEffect(() => {
    const controller = new AbortController()

    getMySensors(controller.signal)
      .then((items) => {
        if (controller.signal.aborted) return

        setSensors((current) => [
          ...items.filter((item) => !current.some(
            (sensor) => String(sensor.enclosureId) === String(item.enclosureId),
          )),
          ...current,
        ])
        setError('')
      })
      .catch((error) => {
        if (!controller.signal.aborted) {
          setError(error.message)
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      })

    return () => controller.abort()
  }, [])

  const handleConnect = async () => {
    if (connecting.current) return

    playSfx('button')

    const identifier = window.prompt(
      '센서에 표시된 기기 식별자를 입력해주세요.',
    )

    if (!identifier?.trim()) return

    connecting.current = true

    try {
      const sensor = await connectMySensor(identifier)

      setSensors((prev) => [
        ...prev.filter(
          (item) =>
            String(item.enclosureId) !==
            String(sensor.enclosureId),
        ),
        sensor,
      ])

      setError('')
    } catch (error) {
      window.alert(error.message)
    } finally {
      connecting.current = false
    }
  }

  const measuredAt = (value) => {
    if (!value) return '—'

    const date = new Date(value)

    if (Number.isNaN(date.getTime())) return '—'

    const minutes = Math.max(
      0,
      Math.floor((now - date.getTime()) / 60000),
    )

    if (minutes < 1) return '방금 전'
    if (minutes < 60) return `${minutes}분 전`

    if (minutes < 1440) {
      return `${Math.floor(minutes / 60)}시간 전`
    }

    return `${Math.floor(minutes / 1440)}일 전`
  }

  return (
    <div className="sensor-manage">
      <header className="sensor-manage-header">
        <button
          className="sensor-manage-back"
          onClick={() => navigate('/my')}
        >
          <img src={back} alt="뒤로가기" />
        </button>

        <h1>연결된 센서</h1>
      </header>

      <main className="sensor-manage-content">
        <div className="sensor-manage-list">
          {error && <p role="alert">{error}</p>}

          {loading && <p>불러오는 중이에요.</p>}

          {!loading &&
            !error &&
            sensors.length === 0 && (
              <p>연결된 센서가 없어요.</p>
            )}

          {sensors.map((sensor) => (
            <article
              className="sensor-card"
              key={`${sensor.enclosureId}-${sensor.sensorId}`}
            >
              <img
                className="sensor-card-image"
                src={sensorImage}
                alt="온습도 센서"
              />

              <div className="sensor-card-info">
                <div className="sensor-card-heading">
                  <strong>
                    {sensor.name || '온습도 센서'}
                  </strong>

                  <span className="sensor-card-tank">
                    {sensor.tank}
                  </span>
                </div>

                <div className="sensor-card-status">
                  <span
                    className="sensor-status-dot"
                    style={
                      sensor.status === 'CONNECTED'
                        ? undefined
                        : {
                            backgroundColor: '#AFA7A3',
                          }
                    }
                  />

                  <span>
                    {sensor.status === 'CONNECTED'
                      ? '연결됨'
                      : sensor.status === 'DISCONNECTED'
                        ? '연결 끊김'
                        : '확인 필요'}
                  </span>
                </div>

                <div className="sensor-card-battery">
                  <img
                    className="battery-icon"
                    src={batteryIcon}
                    alt="배터리"
                  />

                  <span>
                    {sensor.battery == null
                      ? '—'
                      : `${sensor.battery}%`}
                  </span>
                </div>
              </div>

              <div className="sensor-card-update">
                <img
                  className="sensor-update-icon"
                  src={graphIcon}
                  alt=""
                />

                <div>
                  <span className="sensor-update-label">
                    최근 업데이트
                  </span>

                  <strong>
                    {measuredAt(sensor.lastMeasuredAt)}
                  </strong>
                </div>
              </div>
            </article>
          ))}
        </div>

        <button
          className="sensor-connect-button"
          onClick={handleConnect}
        >
          <img
            src={connectButton}
            alt="새 센서 연결하기"
          />
        </button>
      </main>
    </div>
  )
}

export default SensorManage
