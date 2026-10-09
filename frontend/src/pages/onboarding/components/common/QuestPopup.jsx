import { createPortal } from 'react-dom'

import './QuestPopup.scss'

// QUEST 안내 팝업 (피그마 358x164). 화면 아무 곳이나 누르면 닫힌다. top: 화면 위에서 팝업까지 거리
const QuestPopup = ({ title, description, top = 344, onClose }) => {
  return createPortal(
    <div className="quest-popup" role="dialog" aria-modal="true" aria-label={title} style={{ paddingTop: top }} onClick={onClose}>
      <div className="quest-popup-box">
        <span className="quest-popup-tag">QUEST</span>
        <p className="quest-popup-title">{title}</p>
        <p className="quest-popup-description">{description}</p>
      </div>
    </div>,
    document.body,
  )
}

export default QuestPopup
