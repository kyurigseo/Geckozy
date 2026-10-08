const OnboardingButton = ({ children, type = 'button', variant = 'primary', disabled = false, onClick }) => {
  return (
    <button
      type={type}
      className={`onboarding-button onboarding-button-${variant}`}
      disabled={disabled}
      onClick={onClick}
    >
      {children}
    </button>
  )
}

export default OnboardingButton
