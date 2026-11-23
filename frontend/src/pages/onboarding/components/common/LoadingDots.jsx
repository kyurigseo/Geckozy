import './LoadingDots.scss'

// 기존 로딩 1·2.png(밝은 점/진한 점)를 CSS로 대체. 진한 점이 순서대로 이동
const LoadingDots = ({ count = 3 }) => {
  return (
    <div className="loading-dots" role="status" aria-label="로딩 중">
      {Array.from({ length: count }, (_, i) => (
        <span key={i} className="loading-dots-dot" style={{ animationDelay: `${i * 0.3}s` }} />
      ))}
    </div>
  )
}

export default LoadingDots
