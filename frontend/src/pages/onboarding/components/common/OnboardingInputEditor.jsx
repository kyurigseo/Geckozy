import { useEffect, useImperativeHandle, useRef, useState } from 'react'
import { createPortal } from 'react-dom'

import './OnboardingInputEditor.scss'

const BAR_GAP = 12 // 키보드와 입력 바 사이 간격 (피그마)

// 입력 바 (피그마 '회원가입' 입력 중 화면)
// 입력칸을 누르면 화면을 어둡게 하고 키보드 위에 입력 바를 띄운다. '완료'를 눌러야 원래 입력칸에 반영된다.
// 부모는 ref.current.open()으로 연다.
const OnboardingInputEditor = ({ ref, type, value, autoComplete, inputMode, ariaLabel, placeholder = '텍스트를 입력해주세요.', onConfirm }) => {
  const [isOpen, setIsOpen] = useState(false)
  const [draft, setDraft] = useState('')
  const [keyboardHeight, setKeyboardHeight] = useState(0)
  const rootRef = useRef(null)
  const inputRef = useRef(null)

  useImperativeHandle(ref, () => ({
    open: () => {
      // iOS는 탭 이벤트 안에서 바로 focus해야 키보드가 올라와서, 렌더를 기다리지 않고 inert를 풀고 포커스
      rootRef.current.inert = false
      inputRef.current.focus()
      setDraft(value)
      setIsOpen(true)
    },
  }), [value])

  // 화면 키보드 높이만큼 입력 바를 올린다 (visualViewport 미지원 브라우저는 화면 아래에 표시)
  useEffect(() => {
    const viewport = window.visualViewport
    if (!isOpen || !viewport) return

    const handleResize = () => {
      setKeyboardHeight(Math.max(0, window.innerHeight - viewport.height - viewport.offsetTop))
    }

    viewport.addEventListener('resize', handleResize)
    viewport.addEventListener('scroll', handleResize)
    return () => {
      viewport.removeEventListener('resize', handleResize)
      viewport.removeEventListener('scroll', handleResize)
    }
  }, [isOpen])

  const close = () => {
    inputRef.current.blur()
    setIsOpen(false)
  }

  const confirm = () => {
    onConfirm(draft)
    close()
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      confirm()
    } else if (e.key === 'Escape') {
      close()
    }
  }

  return createPortal(
    <div ref={rootRef} className={isOpen ? 'onboarding-editor open' : 'onboarding-editor'} inert={!isOpen}>
      {/* 어두운 영역을 누르면 반영하지 않고 닫기 */}
      <div className="onboarding-editor-dim" onClick={close} />

      <div className="onboarding-editor-bar" style={{ bottom: `${keyboardHeight + BAR_GAP}px` }}>
        <input
          ref={inputRef}
          className="onboarding-editor-field"
          type={type}
          value={draft}
          placeholder={placeholder}
          autoComplete={autoComplete}
          inputMode={inputMode}
          aria-label={ariaLabel}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <button type="button" className="onboarding-editor-done" onClick={confirm}>
          완료
        </button>
      </div>
    </div>,
    document.body,
  )
}

export default OnboardingInputEditor
