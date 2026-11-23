import { createContext, useContext, useEffect, useRef, useState } from 'react'

import bgmSrc from '../assets/sounds/bgm.mp3'
import selectSrc from '../assets/sounds/select.mp3'
import buttonSrc from '../assets/sounds/button.mp3'
import popupSrc from '../assets/sounds/popup.mp3'

const STORAGE_KEY = 'geckozy-sound-on'
const BGM_VOLUME = 0.3
const SFX_VOLUME = 0.6

// 효과음 목록
// select: 도마뱀/사육장 꾸미기 창에서 아이템 선택/해제할 때
// button: 로그인/회원가입/도마뱀을 키우고 있어요!/확인/커스텀완료/다음/완료 버튼
// popup : 도마뱀/사육장 커스텀 해주세요! 팝업이 올라올 때
const SFX_SOURCES = {
  select: selectSrc,
  button: buttonSrc,
  popup: popupSrc,
}

const SoundContext = createContext(null)

// 저장된 on/off 값 읽기 (처음 방문이면 켜짐)
const readSavedSoundOn = () => {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    return saved === null ? true : saved === 'true'
  } catch {
    return true
  }
}

export const SoundProvider = ({ children }) => {
  const [isSoundOn, setIsSoundOn] = useState(readSavedSoundOn)
  const audioRef = useRef(null)
  const isSoundOnRef = useRef(isSoundOn)

  // 앱 전체에서 Audio 객체는 하나만 사용 → 페이지가 바뀌어도 음악이 끊기지 않음
  if (audioRef.current === null) {
    const audio = new Audio(bgmSrc)
    audio.loop = true
    audio.volume = BGM_VOLUME
    audioRef.current = audio
  }

  const tryPlay = () => {
    audioRef.current.play().catch(() => {
      // 브라우저 자동재생 정책으로 막힘 → 첫 클릭/터치 때 다시 시도
    })
  }

  // on/off 상태 저장 + 꺼지면 정지
  useEffect(() => {
    isSoundOnRef.current = isSoundOn
    try {
      localStorage.setItem(STORAGE_KEY, String(isSoundOn))
    } catch {
      // 저장 실패해도 동작에는 영향 없음
    }
    if (!isSoundOn) audioRef.current.pause()
  }, [isSoundOn])

  // 처음 진입 시 재생 시도 + 막혔으면 첫 사용자 입력 때 재생
  useEffect(() => {
    const audio = audioRef.current
    if (isSoundOnRef.current) tryPlay()

    const unlock = () => {
      if (isSoundOnRef.current && audio.paused) tryPlay()
    }
    window.addEventListener('pointerdown', unlock)
    window.addEventListener('keydown', unlock)

    return () => {
      window.removeEventListener('pointerdown', unlock)
      window.removeEventListener('keydown', unlock)
      audio.pause()
    }
  }, [])

  // 효과음 재생: 음소거 상태면 재생하지 않음
  // cloneNode()로 매번 새로 재생 → 버튼을 빠르게 연달아 눌러도 소리가 겹쳐서 남
  const sfxRef = useRef(null)
  if (sfxRef.current === null) {
    sfxRef.current = Object.fromEntries(
      Object.entries(SFX_SOURCES).map(([name, src]) => {
        const audio = new Audio(src)
        audio.preload = 'auto'
        return [name, audio]
      })
    )
  }

  const playSfx = (name) => {
    if (!isSoundOnRef.current) return
    const base = sfxRef.current[name]
    if (!base) return
    const sound = base.cloneNode()
    sound.volume = SFX_VOLUME
    sound.play().catch(() => {})
  }

  // 토글 버튼: 클릭 핸들러 안에서 바로 play()해야 iOS Safari에서도 재생됨
  const toggleSound = () => {
    const next = !isSoundOn
    if (next) tryPlay()
    else audioRef.current.pause()
    setIsSoundOn(next)
  }

  return (
    <SoundContext.Provider value={{ isSoundOn, toggleSound, playSfx }}>
      {children}
    </SoundContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export const useSound = () => {
  const context = useContext(SoundContext)
  if (!context) throw new Error('useSound는 SoundProvider 안에서만 사용할 수 있어요.')
  return context
}
