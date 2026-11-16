import './OnboardingField.scss'

// 라벨 + 입력 요소(children) + 안내 문구 묶음. message: 검사 결과 등 추가 안내
const OnboardingField = ({ label, hint, message, children }) => {
  return (
    <div className="onboarding-field">
      <span className="onboarding-field-label">{label}</span>

      <div className="onboarding-field-body">
        {children}
        {hint && <p className="onboarding-field-hint">{hint}</p>}
        {message && <p className="onboarding-field-message" role="status">{message}</p>}
      </div>
    </div>
  )
}

export default OnboardingField
