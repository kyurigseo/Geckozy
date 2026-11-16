import { useLayoutEffect, useRef, useState } from 'react'

import './WheelColumn.scss'

const WHEEL_ITEM_HEIGHT = 38 // WheelColumn.scss의 $item-height와 같게
const SCROLL_END_DELAY = 120

// 휠 피커 한 열 (가운데 줄 위아래 구분선 포함). 스크롤을 멈춘 위치의 항목이 선택된다. items: 화면에 보일 문자열 배열
const WheelColumn = ({ items, selectedIndex, ariaLabel, onSelect }) => {
  const listRef = useRef(null)
  const timerRef = useRef(null)
  // 스크롤 중에는 스크롤 위치 기준으로 강조, 멈추면 selectedIndex 기준
  const [scrollIndex, setScrollIndex] = useState(null)
  const activeIndex = scrollIndex ?? selectedIndex

  useLayoutEffect(() => {
    listRef.current.scrollTop = selectedIndex * WHEEL_ITEM_HEIGHT
  }, [selectedIndex, items.length])

  const handleScroll = () => {
    const index = Math.min(Math.max(Math.round(listRef.current.scrollTop / WHEEL_ITEM_HEIGHT), 0), items.length - 1)
    setScrollIndex(index)

    clearTimeout(timerRef.current)
    timerRef.current = setTimeout(() => {
      setScrollIndex(null)
      if (index !== selectedIndex) onSelect(index)
    }, SCROLL_END_DELAY)
  }

  const scrollTo = (index) => {
    listRef.current.scrollTo({ top: index * WHEEL_ITEM_HEIGHT, behavior: 'smooth' })
  }

  return (
    <div className="wheel">
      <ul ref={listRef} className="wheel-column" role="listbox" aria-label={ariaLabel} onScroll={handleScroll}>
        {items.map((item, index) => {
          const distance = Math.abs(index - activeIndex)
          const level = distance === 0 ? 'active' : distance === 1 ? 'near' : 'far'
          return (
            <li key={item} role="option" aria-selected={index === selectedIndex} className={`wheel-item wheel-item-${level}`} onClick={() => scrollTo(index)}>
              {item}
            </li>
          )
        })}
      </ul>
    </div>
  )
}

export default WheelColumn
