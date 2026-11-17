import { useEffect, useRef, useState } from 'react'
import { guideApi } from '../../api/guide'
import GuideStatus from './GuideStatus'

export default function EnvironmentPanel({ guideId, onStatus }) {
  const [status, setStatus] = useState(null)
  const [summary, setSummary] = useState(null)
  const [notStarted, setNotStarted] = useState(false)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const [retry, setRetry] = useState(0)
  const lock = useRef(false)
  useEffect(() => {
    const controller = new AbortController()
    guideApi.environment(guideId, controller.signal).then(async value => {
      if (controller.signal.aborted) return
      setStatus(value); onStatus(value); setNotStarted(false)
      const result = await guideApi.summary(guideId, controller.signal)
      if (!controller.signal.aborted) setSummary(result)
    }).catch(e => {
      if (e.name === 'AbortError') return
      if (e.code === 'ENVIRONMENT_CHECK_NOT_FOUND') setNotStarted(true)
      else setError(e)
    })
    return () => controller.abort()
  }, [guideId, retry, onStatus])
  const start = async () => {
    if (lock.current) return
    lock.current = true; setBusy(true); setError(null)
    try { await guideApi.startEnvironment(guideId); setRetry(v => v + 1) }
    catch (e) {
      if (e.code === 'ENVIRONMENT_CHECK_ALREADY_STARTED') setRetry(v => v + 1)
      else setError(e)
    } finally { lock.current = false; setBusy(false) }
  }
  return <section className="api-card">
    <h2>7일 환경 사전 점검</h2>
    {notStarted && <><p>실제로 사육할 때처럼 조명과 분무를 관리하며 센서로 환경을 확인해요.</p><button disabled={busy} onClick={start}>7일 점검 시작하기</button></>}
    {status && <>
      <p>{status.startDate} 시작 · {status.elapsedDays} / {status.totalDays}일</p>
      <progress value={Math.min(status.elapsedDays, status.totalDays)} max={status.totalDays} />
      <p>{status.status === 'COMPLETED' ? '점검 완료' : '환경 확인 중'}</p>
      <p>센서: {status.sensorStatus === 'CONNECTED' ? '연결됨' : '연결 상태를 확인해주세요'}</p>
      <p>최근 측정: {status.latestMeasuredAt || '측정 기록 없음'}</p>
      {status.missingData && <p role="alert">수집되지 않은 데이터가 있어요. 센서 연결을 확인해주세요.</p>}
    </>}
    {summary && <>
      {summary.sufficientData ? <>{['temperatureSummary', 'dayNightSummary', 'humiditySummary', 'durationSummary', 'patternSummary'].map(key => summary[key] && <p key={key}>{summary[key]}</p>)}
        {summary.checkPoints?.length > 0 && <ul>{summary.checkPoints.map((point, i) => <li key={i}>{point}</li>)}</ul>}</> : <p>{summary.guidance || '데이터가 더 쌓이면 환경을 판단할 수 있어요.'}</p>}
    </>}
    <button disabled={busy} onClick={() => { setError(null); setRetry(v => v + 1) }}>점검 상태 새로고침</button>
    <GuideStatus busy={busy} error={error} />
  </section>
}
