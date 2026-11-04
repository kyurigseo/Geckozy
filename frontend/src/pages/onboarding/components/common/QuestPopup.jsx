import { useEffect, useRef } from 'react'
import { createPortal } from 'react-dom'

import { useSound } from '../../../../sound/SoundProvider'

import './QuestPopup.scss'

// QUEST 안내 팝업 (피그마 358x164). 화면 아무 곳이나 누르면 닫힌다. top: 화면 위에서 팝업까지 거리
const QuestPopup = ({ title, description, top = 344, onClose }) => {
  const { playSfx } = useSound()
  const hasPlayedRef = useRef(false)

  // 팝업이 올라올 때 한 번만 효과음 (개발 모드 StrictMode에서 effect가 두 번 실행돼도 한 번만)
  useEffect(() => {
    if (hasPlayedRef.current) return
    hasPlayedRef.current = true
    playSfx('popup')
  }, [playSfx])

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
