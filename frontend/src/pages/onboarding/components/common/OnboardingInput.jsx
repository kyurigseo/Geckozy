import { useRef } from 'react'

import OnboardingInputEditor from './OnboardingInputEditor'

import './OnboardingInput.scss'

// variant: 'basic'(흰 배경, 회원가입 등) | 'pixel'(픽셀 폰트, 진입화면) | 'cream'(베이지 h40, 도마뱀 기본정보)
// suffix: 입력칸 오른쪽 단위 표시 (예: '(cm)'), inputMode: 화면 키보드 종류 (예: 'decimal')
// 입력칸을 누르면 입력 바(OnboardingInputEditor)가 뜨고, '완료'를 눌러야 값이 반영된다.
// 날짜 선택은 DateWheelSheet(휠 피커) 사용. type="date"는 입력 바 없이 바로 입력
const OnboardingInput = ({ label, icon, variant = 'basic', type = 'text', name, value, placeholder, autoComplete, inputMode, suffix, disabled = false, onChange }) => {
  const editorRef = useRef(null)
  const usesEditor = type !== 'date'
  const ariaLabel = label || placeholder

  const openEditor = () => {
    if (!disabled) editorRef.current.open()
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault()
      openEditor()
    }
  }

  return (
    <label className={`onboarding-input onboarding-input-${variant}${disabled ? ' is-disabled' : ''}`}>
      {label && <span className="onboarding-input-label">{label}</span>}

      <div className="onboarding-input-box">
        {icon && (
          <span className="onboarding-input-icon">
            <img src={icon} alt="" />
          </span>
        )}
        {usesEditor ? (
          <input
            className="onboarding-input-field"
            type={type}
            name={name}
            value={value}
            placeholder={placeholder}
            aria-label={label ? undefined : placeholder}
            aria-haspopup="dialog"
            disabled={disabled}
            readOnly
            onClick={openEditor}
            onKeyDown={handleKeyDown}
          />
        ) : (
          <input
            className="onboarding-input-field"
            type={type}
            name={name}
            value={value}
            aria-label={label ? undefined : placeholder}
            disabled={disabled}
            onChange={(e) => onChange(e.target.value)}
          />
        )}
        {suffix && <span className="onboarding-input-suffix">{suffix}</span>}
      </div>

      {usesEditor && (
        <OnboardingInputEditor
          ref={editorRef}
          type={type}
          value={value}
          autoComplete={autoComplete}
          inputMode={inputMode}
          ariaLabel={ariaLabel}
          onConfirm={onChange}
        />
      )}
    </label>
  )
}

export default OnboardingInput
