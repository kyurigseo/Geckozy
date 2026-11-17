import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { guideApi, readGuideSession } from '../../api/guide'
import { useSound } from '../../sound/SoundProvider'
import GuideStatus from './GuideStatus'
import SensorPanel from './SensorPanel'
import EnvironmentPanel from './EnvironmentPanel'
import EnclosureInfo from './EnclosureInfo'
import background from '../../assets/guide/가이드 배경.svg'
import back from '../../assets/guide/뒤로가기.svg'
import title1 from '../../assets/guide/1단계 준비.svg'
import title2 from '../../assets/guide/2단계 준비.svg'
import title3 from '../../assets/guide/3단계 준비.svg'
import title4 from '../../assets/guide/4단계 준비.svg'
import title5 from '../../assets/guide/5단계 준비.svg'
import { getStep1Fallback } from './data/step1Fallback'
import './GuideApi.scss'

const titles = [title1, title2, title3, title4, title5]
export default function ApiStep({ stepNumber }) {
  const navigate = useNavigate()
  const { playSfx } = useSound()
  const [session] = useState(readGuideSession)

  const [data, setData] = useState(() =>
    stepNumber === 1 ? getStep1Fallback(session?.speciesKey) : null
  )
  const [progress, setProgress] = useState(null)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const [saving, setSaving] = useState(false)
  const [retry, setRetry] = useState(0)
  const [environment, setEnvironment] = useState(null)
  const [enclosureOpen, setEnclosureOpen] = useState(false)
  const lock = useRef(false)
  const guideId = session?.preGuideId
  const updateEnvironment = useCallback(value => setEnvironment(value), [])
  
  useEffect(() => {
    if (!guideId) {
      if (stepNumber !== 1) {
        navigate('/guide', { replace: true })
      }
      return
    }

    const controller = new AbortController()

    // 안내 조회와 진행 상태 조회를 분리합니다.
    // 진행 상태 조회가 실패해도 안내 내용은 표시합니다.
    guideApi
      .step(guideId, stepNumber, controller.signal)
      .then((content) => {
        if (controller.signal.aborted) return

        const sections = Array.isArray(content?.sections)
          ? content.sections
          : []

        if (stepNumber === 1 && sections.length === 0) {
          setError(
            new Error(
              '서버의 1단계 안내가 비어 있어 기본 안내를 표시하고 있어요.'
            )
          )
          return
        }

        setData({
          ...content,
          sections,
          checkItems: [...(content.checkItems || [])].sort(
            (a, b) => a.itemOrder - b.itemOrder
          ),
        })
      })
      .catch((error) => {
        if (error.name !== 'AbortError') {
          setError(error)
        }
      })

    guideApi
      .progress(guideId, controller.signal)
      .then((state) => {
        if (controller.signal.aborted) return

        if (stepNumber > state.currentStep) {
          navigate(
            `/guide/step${Math.min(5, Math.max(1, state.currentStep))}`,
            { replace: true }
          )
          return
        }

        setProgress(state)
      })
      .catch((error) => {
        if (error.name !== 'AbortError') {
          setError(error)
        }
      })

    return () => controller.abort()
  }, [guideId, stepNumber, navigate, retry])
  const toggle = async (id) => {
    if (lock.current) return
    lock.current = true; setSaving(true); setError(null)
    const checks = data.checkItems.map(item => ({ checkItemId: item.checkItemId, checked: item.checkItemId === id ? !item.checked : item.checked }))
    try {
      await guideApi.saveChecks(guideId, stepNumber, checks)
      setData(current => ({ ...current, checkItems: current.checkItems.map(item => ({ ...item, checked: checks.find(c => c.checkItemId === item.checkItemId).checked })) }))
    } catch (e) { setError(e) }
    finally { lock.current = false; setSaving(false) }
  }
  const advance = async () => {
    if (lock.current) return
    lock.current = true; setBusy(true); setError(null)
    try {
      if (stepNumber === 4) {
        const current = await guideApi.environment(guideId)
        setEnvironment(current)
        if (current.status !== 'COMPLETED') throw new Error('7일 환경 점검을 먼저 완료해주세요.')
      }
      const updated = progress.completedStep >= stepNumber ? progress : await guideApi.advance(guideId, stepNumber)
      setProgress(updated)
      if (stepNumber === 5) {
        const result = await guideApi.complete(guideId)
        if (!result.completed) throw new Error('가이드 완료 여부를 확인하지 못했어요.')
        navigate('/loading-fin', { state: { completed: true } })
      } else navigate(`/guide/step${Math.min(5, updated.currentStep)}`)
      playSfx('button')
    } catch (e) { setError(e) }
    finally { lock.current = false; setBusy(false) }
  }
  return <div className="api-guide-step" style={{ backgroundImage: `url(${background})` }}>
    <header>
      <button className="api-back" disabled={busy || saving} onClick={() => navigate(stepNumber === 1 ? '/guide' : `/guide/step${stepNumber - 1}`)}><img src={back} alt="뒤로가기" /></button>
      <ol aria-label="가이드 진행 단계">{titles.map((_, i) => <li key={i} aria-current={i + 1 === stepNumber ? 'step' : undefined} className={i + 1 <= stepNumber ? 'active' : ''}>{i + 1}</li>)}</ol>
      <img className="api-step-title" src={titles[stepNumber - 1]} alt={`${stepNumber}단계`} />
      <p>{session?.title}</p>
    </header>
    <GuideStatus error={error} busy={!data && !error} onRetry={() => { setError(null); setRetry(v => v + 1) }} />
    
    {data?.localPreview && (
      <section className="api-card">
        <p>
          기본 안내를 표시하고 있어요.
          서버 연결 전에는 체크 저장과 단계 완료를 사용할 수 없어요.
        </p>

        <button onClick={() => navigate('/guide')}>
          종 선택으로 돌아가기
        </button>
      </section>
    )}

    {data && <>
      {data.sections?.map(section => <section className="api-card" key={section.sectionId}>
        <h2>{section.title}</h2>{section.content && <p>{section.content}</p>}
        {section.details?.map((detail, index) => <details key={index} open>
          <summary>{detail.question}</summary>
          {detail.contents?.map(content => <article key={content.contentId}>
            {content.contentTitle && <h3>{content.contentTitle}</h3>}{content.badge && <small>{content.badge}</small>}<p>{content.contentText}</p>
          </article>)}
          {detail.blocks?.map((block, i) => <div key={i}><h3>{block.blockTitle}</h3><ul>{block.items?.map((item, j) => <li key={j}>{item.itemContent}</li>)}</ul></div>)}
        </details>)}
      </section>)}
      {stepNumber === 1 && <button onClick={() => setEnclosureOpen(true)}>준비한 사육장 정보 확인하기</button>}
      {stepNumber === 3 && <SensorPanel />}
      {stepNumber === 4 && <EnvironmentPanel guideId={guideId} onStatus={updateEnvironment} />}
      <section className="api-card"><h2>준비 항목 확인</h2>
        {!data.checkItems.length && <p>이 단계에는 체크 항목이 없어요.</p>}
        {data.checkItems.map(item => <label className="api-check" key={item.checkItemId}>
          <input type="checkbox" checked={item.checked} disabled={busy || saving} onChange={() => toggle(item.checkItemId)} /><span>{item.checkTitle}</span>
        </label>)}
        {saving && <p role="status">체크 상태 저장 중…</p>}
      </section>
      <button
        className="api-primary"
        disabled={
          busy ||
          saving ||
          data.localPreview ||
          !progress ||
          (stepNumber === 4 && environment?.status !== 'COMPLETED')
        }
        onClick={advance}
      >
        {busy
          ? '저장 중…'
          : stepNumber === 5
            ? '입양 준비 완료하기'
            : '단계 완료하고 다음으로'}
      </button>
    </>}
    {enclosureOpen && <EnclosureInfo onClose={() => setEnclosureOpen(false)} />}
  </div>
}
