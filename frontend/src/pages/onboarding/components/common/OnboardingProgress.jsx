import './OnboardingProgress.scss'

// 진행바 (피그마 261x38). value: 0 ~ 1
const OnboardingProgress = ({ value }) => {
  const percent = Math.round(Math.min(Math.max(value, 0), 1) * 100)

  return (
    <div className="onboarding-progress" role="progressbar" aria-valuemin={0} aria-valuemax={100} aria-valuenow={percent}>
      <div className="onboarding-progress-fill" style={{ '--progress': value }} />
    </div>
  )
}

export default OnboardingProgress
