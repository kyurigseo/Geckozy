import backIcon from '../../../../assets/onboarding/icon-back.svg'

import './OnboardingHeader.scss'

// 뒤로가기 버튼 + 가운데 제목 (title 없으면 버튼만)
const OnboardingHeader = ({ title, onBack }) => {
  return (
    <header className="onboarding-header">
      {onBack && (
        <button type="button" className="onboarding-header-back" aria-label="뒤로가기" onClick={onBack}>
          <img src={backIcon} alt="" />
        </button>
      )}
      {title && <h1 className="onboarding-header-title">{title}</h1>}
    </header>
  )
}

export default OnboardingHeader
