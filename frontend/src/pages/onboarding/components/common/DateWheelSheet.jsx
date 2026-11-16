import { useState } from 'react'

import BottomSheet from './BottomSheet'
import WheelColumn from './WheelColumn'

import './DateWheelSheet.scss'

const YEAR_RANGE = 30 // 올해부터 30년 전까지

const pad = (number) => String(number).padStart(2, '0')
const range = (start, end) => Array.from({ length: end - start + 1 }, (_, i) => start + i)

// 'YYYY-MM-DD' → { year, month, day }
const parseDate = (value) => {
  const [year, month, day] = value.split('-').map(Number)
  return { year, month, day }
}

// 날짜 휠 피커 바텀시트 (피그마 '생년월일'). 미래 날짜는 고를 수 없다.
// 열 때마다 새로 마운트해서 value로 초기화한다: {isOpen && <DateWheelSheet ... />}
const DateWheelSheet = ({ title, value, onConfirm, onClose }) => {
  const today = new Date()
  const thisYear = today.getFullYear()
  const thisMonth = today.getMonth() + 1

  const [draft, setDraft] = useState(() =>
    value ? parseDate(value) : { year: thisYear, month: thisMonth, day: today.getDate() },
  )

  // 고를 수 있는 범위 (올해는 이번 달·오늘까지)
  const years = range(thisYear - YEAR_RANGE, thisYear)
  const maxMonth = draft.year === thisYear ? thisMonth : 12
  const months = range(1, maxMonth)
  const daysInMonth = new Date(draft.year, draft.month, 0).getDate()
  const maxDay = draft.year === thisYear && draft.month === thisMonth ? today.getDate() : daysInMonth
  const days = range(1, maxDay)

  // 연·월이 바뀌어 범위를 벗어나면 마지막 값으로 맞춘다
  const month = Math.min(draft.month, maxMonth)
  const day = Math.min(draft.day, maxDay)

  const update = (field, nextValue) => setDraft((prev) => ({ ...prev, month, day, [field]: nextValue }))

  const handleConfirm = () => onConfirm(`${draft.year}-${pad(month)}-${pad(day)}`)

  return (
    <BottomSheet className="date-wheel-sheet" title={title} onConfirm={handleConfirm} onClose={onClose}>
      <div className="date-wheel-sheet-wheels">
        <WheelColumn
          ariaLabel="년"
          items={years.map((y) => `${y}년`)}
          selectedIndex={years.indexOf(draft.year)}
          onSelect={(index) => update('year', years[index])}
        />
        <WheelColumn
          ariaLabel="월"
          items={months.map((m) => `${pad(m)}월`)}
          selectedIndex={month - 1}
          onSelect={(index) => update('month', months[index])}
        />
        <WheelColumn
          ariaLabel="일"
          items={days.map((d) => `${pad(d)}일`)}
          selectedIndex={day - 1}
          onSelect={(index) => update('day', days[index])}
        />
      </div>
    </BottomSheet>
  )
}

export default DateWheelSheet
