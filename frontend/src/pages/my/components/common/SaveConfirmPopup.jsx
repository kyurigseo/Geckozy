import { createPortal } from 'react-dom'

import OnboardingButton from '../../../onboarding/components/common/OnboardingButton'

import './SaveConfirmPopup.scss'

const SaveConfirmPopup = ({
  title = '변경하신 내용을 저장하시겠습니까?',
  description = '',
  onConfirm,
  onDiscard,
  onClose
}) => {
  return createPortal(
    <div className="save-confirm">
      <div className="save-confirm-dim" onClick={onClose} />
      <div className="save-confirm-box" role="alertdialog" aria-modal="true" aria-labelledby="save-confirm-title">
        <p id="save-confirm-title" className="save-confirm-title">{title}</p>
        {description && <p className="save-confirm-description">{description}</p>}
        <div className="save-confirm-buttons">
          <OnboardingButton size="s" onClick={onConfirm}>예</OnboardingButton>
          <OnboardingButton size="s" onClick={onDiscard}>아니오</OnboardingButton>
        </div>
      </div>
    </div>,
    document.body,
  )
}

export default SaveConfirmPopup