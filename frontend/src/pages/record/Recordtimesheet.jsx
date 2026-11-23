import { useState } from 'react'

import BottomSheet from '../onboarding/components/common/BottomSheet'
import WheelColumn from '../onboarding/components/common/WheelColumn'

import '../onboarding/components/common/DateWheelSheet.scss'

const pad = (n) => String(n).padStart(2, '0')
const range = (start, end) => Array.from({ length: end - start + 1 }, (_, i) => start + i)

const MONTHS = range(1, 12)
const TIMES = Array.from({ length: 48 }, (_, i) => `${pad(Math.floor(i / 2))}:${i % 2 ? '30' : '00'}`)

const RecordTimeSheet = ({ value, onConfirm, onClose }) => {
  const [draft, setDraft] = useState(() => ({
    month: Number(value.month),
    day: Number(value.day),
    time: value.time,
  }))

  const daysInMonth = new Date(new Date().getFullYear(), draft.month, 0).getDate()
  const days = range(1, daysInMonth)
  const day = Math.min(draft.day, daysInMonth)

  const handleConfirm = () => onConfirm({ month: pad(draft.month), day: pad(day), time: draft.time })

  return (
    <BottomSheet className="date-wheel-sheet" title="기록 시각 선택" onConfirm={handleConfirm} onClose={onClose}>
      <div className="date-wheel-sheet-wheels">
        <WheelColumn
          ariaLabel="월"
          items={MONTHS.map((m) => `${pad(m)}월`)}
          selectedIndex={draft.month - 1}
          onSelect={(i) => setDraft((p) => ({ ...p, day, month: MONTHS[i] }))}
        />
        <WheelColumn
          ariaLabel="일"
          items={days.map((d) => `${pad(d)}일`)}
          selectedIndex={day - 1}
          onSelect={(i) => setDraft((p) => ({ ...p, day: days[i] }))}
        />
        <WheelColumn
          ariaLabel="시각"
          items={TIMES}
          selectedIndex={Math.max(TIMES.indexOf(draft.time), 0)}
          onSelect={(i) => setDraft((p) => ({ ...p, day, time: TIMES[i] }))}
        />
      </div>
    </BottomSheet>
  )
}

export default RecordTimeSheet