import { createPortal } from 'react-dom'

import OnboardingButton from './OnboardingButton'

import closeIcon from '../../../../assets/onboarding/icon-close.svg'

import './BottomSheet.scss'

// 아래에서 올라오는 시트 (피그마 '생년월일', '조명 사용 시간' 등). 어두운 영역·닫기 버튼을 누르면 닫힌다.
// 열 때마다 새로 마운트해서 쓴다: {isOpen && <BottomSheet ... />}
const BottomSheet = ({ title, className = '', confirmLabel = '확인', onConfirm, onClose, children }) => {
  return createPortal(
    <div className={`bottom-sheet ${className}`}>
      <div className="bottom-sheet-dim" onClick={onClose} />

      <div className="bottom-sheet-panel" role="dialog" aria-modal="true" aria-label={title}>
        <div className="bottom-sheet-head">
          <h2 className="bottom-sheet-title">{title}</h2>
          <button type="button" className="bottom-sheet-close" aria-label="닫기" onClick={onClose}>
            <img src={closeIcon} alt="" />
          </button>
        </div>

        {children}

        <div className="bottom-sheet-confirm">
          <OnboardingButton onClick={onConfirm}>{confirmLabel}</OnboardingButton>
        </div>
      </div>
    </div>,
    document.body,
  )
}

export default BottomSheet
