import { useState } from 'react'

import BottomSheet from './BottomSheet'
import WheelColumn from './WheelColumn'
import RadioOption from './RadioOption'

import './TimeRangeSheet.scss'

// 00:00 ~ 23:30, 30분 단위
const TIMES = Array.from({ length: 48 }, (_, i) => `${String(Math.floor(i / 2)).padStart(2, '0')}:${i % 2 ? '30' : '00'}`)

// 시작~종료 시간 휠 피커 시트 (피그마 '조명 사용 시간', '히팅 장비 사용 방식')
// value: { start, end } | 'none'(사용하지 않음) | null, defaultRange: 처음 열 때 휠 위치
// allowNone: '사용하지 않음' 선택지 표시 여부
// 열 때마다 새로 마운트해서 value로 초기화한다: {isOpen && <TimeRangeSheet ... />}
const TimeRangeSheet = ({ title, value, defaultRange, allowNone = false, onConfirm, onClose }) => {
  const initialRange = value && value !== 'none' ? value : defaultRange
  const [range, setRange] = useState(initialRange)
  const [isNone, setIsNone] = useState(value === 'none')

  const updateTime = (field) => (index) => {
    setRange((prev) => ({ ...prev, [field]: TIMES[index] }))
    setIsNone(false)
  }

  const handleConfirm = () => onConfirm(isNone ? 'none' : range)

  return (
    <BottomSheet className="time-range-sheet" title={title} onConfirm={handleConfirm} onClose={onClose}>
      <div className="time-range-sheet-labels" aria-hidden="true">
        <span>시작 시간</span>
        <span>~</span>
        <span>종료 시간</span>
      </div>

      <div className="time-range-sheet-wheels">
        <WheelColumn ariaLabel="시작 시간" items={TIMES} selectedIndex={TIMES.indexOf(range.start)} onSelect={updateTime('start')} />
        <WheelColumn ariaLabel="종료 시간" items={TIMES} selectedIndex={TIMES.indexOf(range.end)} onSelect={updateTime('end')} />
      </div>

      {allowNone && (
        <div className="time-range-sheet-none">
          <RadioOption size="m" role="checkbox" label="사용하지 않음" checked={isNone} onClick={() => setIsNone((prev) => !prev)} />
        </div>
      )}
    </BottomSheet>
  )
}

export default TimeRangeSheet
