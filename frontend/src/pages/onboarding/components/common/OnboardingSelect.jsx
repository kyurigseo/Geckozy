import { useEffect, useRef, useState } from 'react'

import dropdownIcon from '../../../../assets/onboarding/icon-dropdown.svg'

import './OnboardingSelect.scss'

// 커스텀 드롭다운 (피그마 드롭다운 컴포넌트: Default / 열기 / select(hover) / filled)
// options: [{ value, label }]
// variant: 'basic'(흰 배경 h51, 회원가입 이메일) | 'cream'(베이지 h40, 관리 방식 분무 빈도)
const OnboardingSelect = ({ options, value, placeholder = '선택', ariaLabel, variant = 'basic', onChange }) => {
  const [isOpen, setIsOpen] = useState(false)
  const rootRef = useRef(null)

  const selected = options.find((option) => option.value === value)
  const displayText = selected ? selected.label : placeholder

  // 바깥 클릭·Esc로 닫기
  useEffect(() => {
    if (!isOpen) return

    const handleClickOutside = (e) => {
      if (!rootRef.current.contains(e.target)) setIsOpen(false)
    }
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') setIsOpen(false)
    }

    document.addEventListener('mousedown', handleClickOutside)
    document.addEventListener('keydown', handleKeyDown)
    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [isOpen])

  const handleSelect = (optionValue) => {
    onChange(optionValue)
    setIsOpen(false)
  }

  return (
    <div
      ref={rootRef}
      className={`onboarding-select onboarding-select-${variant}${isOpen ? ' open' : ''}${selected ? ' is-filled' : ''}`}
    >
      <div className="onboarding-select-box">
        <button
          type="button"
          className="onboarding-select-trigger"
          aria-haspopup="listbox"
          aria-expanded={isOpen}
          aria-label={ariaLabel ? `${ariaLabel}: ${displayText}` : undefined}
          onClick={() => setIsOpen((prev) => !prev)}
        >
          <span>{displayText}</span>
          <span className="onboarding-select-icon">
            <img src={dropdownIcon} alt="" />
          </span>
        </button>

        {isOpen && (
          <ul className="onboarding-select-options" role="listbox" aria-label={ariaLabel}>
            {options.map((option) => (
              <li key={option.value} role="option" aria-selected={option.value === value}>
                <button type="button" className="onboarding-select-option" onClick={() => handleSelect(option.value)}>
                  {option.label}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}

export default OnboardingSelect
