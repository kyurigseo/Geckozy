import './OnboardingButton.scss'

// variant: 'primary'(픽셀 버튼) | 'text'(텍스트 링크형)
const OnboardingButton = ({ children, type = 'button', variant = 'primary', disabled = false, onClick }) => {
  return (
    <button
      type={type}
      className={`onboarding-button onboarding-button-${variant}`}
      disabled={disabled}
      onClick={onClick}
    >
      <span className="onboarding-button-label">{children}</span>
    </button>
  )
}

export default OnboardingButton
