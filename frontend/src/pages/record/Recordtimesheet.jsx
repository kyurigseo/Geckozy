import { useLayoutEffect, useRef, useState } from 'react'
import { createPortal } from 'react-dom'

import './Recordtimesheet.scss'

import btnFinish from '../../assets/record/bg_btn_finish.png'
import buttonSrc from '../../assets/sounds/button.mp3'

const ITEM_HEIGHT = 42
const SCROLL_END_DELAY = 120

const pad = (n) => String(n).padStart(2, '0')
const range = (start, end) => Array.from({ length: end - start + 1 }, (_, i) => start + i)

const MONTHS = range(1, 12)
const TIMES = Array.from({ length: 48 }, (_, i) => `${pad(Math.floor(i / 2))}:${i % 2 ? '30' : '00'}`)

const playButtonSfx = () => {
  const sound = new Audio(buttonSrc)
  sound.volume = 0.5
  sound.currentTime = 0
  sound.play().catch(() => {})
}

const RecordWheel = ({ items, selectedIndex, ariaLabel, width, onSelect }) => {
  const listRef = useRef(null)
  const timerRef = useRef(null)
  const [scrollIndex, setScrollIndex] = useState(null)
  const activeIndex = scrollIndex ?? selectedIndex

  useLayoutEffect(() => {
    listRef.current.scrollTop = selectedIndex * ITEM_HEIGHT
  }, [selectedIndex, items.length])

  const handleScroll = () => {
    const index = Math.min(Math.max(Math.round(listRef.current.scrollTop / ITEM_HEIGHT), 0), items.length - 1)
    setScrollIndex(index)

    clearTimeout(timerRef.current)
    timerRef.current = setTimeout(() => {
      setScrollIndex(null)
      if (index !== selectedIndex) onSelect(index)
    }, SCROLL_END_DELAY)
  }

  const scrollTo = (index) => {
    listRef.current.scrollTo({ top: index * ITEM_HEIGHT, behavior: 'smooth' })
  }

  return (
    <ul
      ref={listRef}
      className="record-time-sheet__wheel"
      style={{ width }}
      role="listbox"
      aria-label={ariaLabel}
      onScroll={handleScroll}
    >
      {items.map((item, index) => {
        const distance = Math.abs(index - activeIndex)
        const level = distance === 0 ? 'active' : distance === 1 ? 'near' : 'far'
        return (
          <li
            key={item}
            role="option"
            aria-selected={index === selectedIndex}
            className={`record-time-sheet__item record-time-sheet__item--${level}`}
            onClick={() => scrollTo(index)}
          >
            {item}
          </li>
        )
      })}
    </ul>
  )
}

const RecordTimeSheet = ({ value, onConfirm, onClose }) => {
  const [draft, setDraft] = useState(() => ({
    month: Number(value.month),
    day: Number(value.day),
    time: value.time,
  }))

  const daysInMonth = new Date(new Date().getFullYear(), draft.month, 0).getDate()
  const days = range(1, daysInMonth)
  const day = Math.min(draft.day, daysInMonth)

  const handleConfirm = () => {
    playButtonSfx()
    onConfirm({ month: pad(draft.month), day: pad(day), time: draft.time })
  }

  return createPortal(
    <div className="record-time-sheet">
      <div className="record-time-sheet__dim" onClick={onClose} />

      <div className="record-time-sheet__panel" role="dialog" aria-modal="true" aria-label="기록 시각 선택">
        <div className="record-time-sheet__head">
          <h2 className="record-time-sheet__title">기록 시각 선택</h2>
          <button type="button" className="record-time-sheet__close" aria-label="닫기" onClick={onClose} />
        </div>

        <div className="record-time-sheet__band" />

        <div className="record-time-sheet__wheels">
          <RecordWheel
            ariaLabel="월"
            width={48}
            items={MONTHS.map((m) => `${pad(m)}월`)}
            selectedIndex={draft.month - 1}
            onSelect={(i) => setDraft((p) => ({ ...p, day, month: MONTHS[i] }))}
          />
          <RecordWheel
            ariaLabel="일"
            width={48}
            items={days.map((d) => `${pad(d)}일`)}
            selectedIndex={day - 1}
            onSelect={(i) => setDraft((p) => ({ ...p, day: days[i] }))}
          />
          <RecordWheel
            ariaLabel="시각"
            width={64}
            items={TIMES}
            selectedIndex={Math.max(TIMES.indexOf(draft.time), 0)}
            onSelect={(i) => setDraft((p) => ({ ...p, day, time: TIMES[i] }))}
          />
        </div>

        <button type="button" className="record-time-sheet__confirm" onClick={handleConfirm}>
          <img src={btnFinish} alt="" />
          <span>확인</span>
        </button>
      </div>
    </div>,
    document.body,
  )
}

export default RecordTimeSheet