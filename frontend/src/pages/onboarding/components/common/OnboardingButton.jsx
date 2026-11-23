import './OnboardingButton.scss'

// variant: 'primary'(픽셀 버튼) | 'filled'(작은 채움 버튼) | 'text'(텍스트 링크형)
// size: 픽셀 버튼 크기 'm'(기본, 높이 54~62) | 's'(131x39, 저장 확인 팝업)
const OnboardingButton = ({ children, type = 'button', variant = 'primary', size = 'm', disabled = false, onClick }) => {
  return (
    <button
      type={type}
      className={`onboarding-button onboarding-button-${variant}${size === 's' ? ' onboarding-button-s' : ''}`}
      disabled={disabled}
      onClick={onClick}
    >
      <span className="onboarding-button-label">{children}</span>
    </button>
  )
}

export default OnboardingButton
