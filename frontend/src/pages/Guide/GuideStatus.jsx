export default function GuideStatus({ error, busy, onRetry }) {
  return <div className="guide-status" aria-live="polite">
    {busy && <p role="status">불러오는 중이에요…</p>}
    {error && <><p role="alert">{error.message || error}</p>{onRetry && <button onClick={onRetry}>다시 시도</button>}</>}
  </div>
}
