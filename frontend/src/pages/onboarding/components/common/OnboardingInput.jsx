import './OnboardingInput.scss'

// TODO: 날짜·시간 선택은 현재 type="date" 임시 사용. 디자인 받으면 휠 피커 컴포넌트로 교체
const OnboardingInput = ({ label, icon, type = 'text', name, value, placeholder, autoComplete, onChange }) => {
  return (
    <label className="onboarding-input">
      {label && <span className="onboarding-input-label">{label}</span>}

      <div className="onboarding-input-box">
        {icon && (
          <span className="onboarding-input-icon">
            <img src={icon} alt="" />
          </span>
        )}
        <input
          className="onboarding-input-field"
          type={type}
          name={name}
          value={value}
          placeholder={placeholder}
          autoComplete={autoComplete}
          aria-label={label ? undefined : placeholder}
          onChange={(e) => onChange(e.target.value)}
        />
      </div>
    </label>
  )
}

export default OnboardingInput
