import { useEffect, useRef, useState } from 'react'
import { guideApi } from '../../api/guide'
import GuideStatus from './GuideStatus'

export default function SensorPanel() {
  const [lizards, setLizards] = useState([])
  const [lizardId, setLizardId] = useState('')
  const [enclosure, setEnclosure] = useState(null)
  const [sensor, setSensor] = useState(null)
  const [device, setDevice] = useState('')
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const [retry, setRetry] = useState(0)
  const lock = useRef(false)
  useEffect(() => {
    const controller = new AbortController()
    guideApi.lizards(controller.signal).then(setLizards).catch(e => {
      if (e.name !== 'AbortError') setError(e)
    })
    return () => controller.abort()
  }, [])
  useEffect(() => {
    if (!lizardId) return
    const controller = new AbortController()
    guideApi.enclosure(lizardId, controller.signal).then(async value => {
      if (controller.signal.aborted) return
      setEnclosure(value)
      const status = await guideApi.sensor(value.enclosureId, controller.signal)
      if (!controller.signal.aborted) setSensor(status)
    }).catch(e => { if (e.name !== 'AbortError') setError(e) })
    return () => controller.abort()
  }, [lizardId, retry])
  const connect = async () => {
    if (lock.current || !enclosure || !device.trim()) return
    lock.current = true; setBusy(true); setError(null)
    try {
      await guideApi.connectSensor(enclosure.enclosureId, device.trim())
      setSensor(await guideApi.sensor(enclosure.enclosureId))
    } catch (e) { setError(e) }
    finally { lock.current = false; setBusy(false) }
  }
  return <section className="api-card">
    <h2>센서 연결과 측정</h2>
    <label>사육장 선택
      <select value={lizardId} disabled={busy} onChange={e => {
        setLizardId(e.target.value); setEnclosure(null); setSensor(null); setError(null)
      }}>
        <option value="">도마뱀의 사육장을 선택해주세요</option>
        {lizards.map(item => <option key={item.lizardId} value={item.lizardId}>{item.name} · {item.species}</option>)}
      </select>
    </label>
    {!lizards.length && <p>등록된 도마뱀과 사육장이 필요해요. 마이 화면에서 등록 상태를 확인해주세요.</p>}
    {sensor && <><p>{sensor.status === 'CONNECTED' ? '센서 연결됨' : '센서 연결 끊김'}</p>
      <p>마지막 측정 온도 {sensor.lastTemperature ?? '—'}℃ · 습도 {sensor.lastHumidity ?? '—'}%</p>
      <p>마지막 측정: {sensor.lastMeasuredAt || '측정 기록 없음'}</p><p>{sensor.message}</p></>}
    {enclosure && sensor?.status !== 'CONNECTED' && <>
      <label>센서 기기 식별자<input value={device} onChange={e => setDevice(e.target.value)} placeholder="센서에 표시된 식별자를 입력해주세요" /></label>
      <button disabled={busy || !device.trim()} onClick={connect}>센서 연결하기</button>
    </>}
    <button disabled={busy || !lizardId} onClick={() => { setError(null); setRetry(v => v + 1) }}>측정 상태 새로고침</button>
    <GuideStatus error={error} busy={busy} />
  </section>
}
