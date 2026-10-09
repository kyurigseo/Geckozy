import { useNavigate } from 'react-router-dom'

import back from '../../../../assets/my/뒤로가기.svg'
import sensorImage from '../../../../assets/my/센서.svg'
import batteryIcon from '../../../../assets/my/배터리.svg'
import connectButton from '../../../../assets/my/새 센서 연결하기.svg'
import graphIcon from '../../../../assets/my/그래프.svg'

import './SensorManage.scss'

const SENSORS = [
  { id: 1, name: '온습도 센서', tank: '사육장 1', battery: 82, updated: '2분 전' },
  { id: 2, name: '온습도 센서', tank: '사육장 2', battery: 60, updated: '8분 전' },
  { id: 3, name: '온습도 센서', tank: '사육장 3', battery: 80, updated: '10분 전' }
]

const SensorManage = () => {
  const navigate = useNavigate()

  const handleConnect = () => {
    alert('새 센서 연결 기능은 추후 연동할 예정이에요.')
  }

  return (
    <div className="sensor-manage">
      <header className="sensor-manage-header">
        <button className="sensor-manage-back" onClick={() => navigate('/my')}>
          <img src={back} alt="뒤로가기" />
        </button>
        <h1>연결된 센서</h1>
      </header>

      <main className="sensor-manage-content">
        <div className="sensor-manage-list">
          {SENSORS.map((sensor) => (
            <article className="sensor-card" key={sensor.id}>
              <img className="sensor-card-image" src={sensorImage} alt="온습도 센서" />

              <div className="sensor-card-info">
                <div className="sensor-card-heading">
                  <strong>{sensor.name}</strong>
                  <span className="sensor-card-tank">{sensor.tank}</span>
                </div>

                <div className="sensor-card-status">
                  <span className="sensor-status-dot" />
                  <span>연결됨</span>
                </div>

                <div className="sensor-card-battery">
                  <img className="battery-icon" src={batteryIcon} alt="배터리" />
                  <span>{sensor.battery}%</span>
                </div>
              </div>

              <div className="sensor-card-update">
                <img className="sensor-update-icon" src={graphIcon} alt="" />
                <div>
                  <span className="sensor-update-label">최근 업데이트</span>
                  <strong>{sensor.updated}</strong>
                </div>
              </div>
            </article>
          ))}
        </div>

        <button className="sensor-connect-button" onClick={handleConnect}>
          <img src={connectButton} alt="새 센서 연결하기" />
        </button>
      </main>
    </div>
  )
}

export default SensorManage
