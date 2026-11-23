import { useState } from 'react'

import QuestionLayout from '../common/QuestionLayout'
import RadioOption from '../common/RadioOption'
import OnboardingSelect from '../common/OnboardingSelect'
import PickerField from '../common/PickerField'
import TimeRangeSheet from '../common/TimeRangeSheet'

import waterIcon from '../../../../assets/onboarding/icon-water.png'
import calendarIcon from '../../../../assets/onboarding/icon-calendar.png'
import clockIcon from '../../../../assets/onboarding/icon-clock.png'
import temperatureIcon from '../../../../assets/onboarding/icon-temperature.png'
import clockLineIcon from '../../../../assets/onboarding/icon-clock-line.svg'

import './CareQuestion.scss'

const MIST_METHODS = [
  { value: 'manual', label: '직접 분무' },
  { value: 'auto', label: '자동 분무' },
  { value: 'both', label: '둘다' },
]

const MIST_FREQUENCIES = [
  { value: 'over3', label: '하루 3회 이상' },
  { value: 'twice', label: '하루 2회' },
  { value: 'once', label: '하루 1회' },
  { value: 'irregular', label: '정해진 주기 없음' },
]

const MIST_TIMES = [
  { value: 'morning', label: '아침' },
  { value: 'day', label: '낮' },
  { value: 'evening', label: '저녁' },
  { value: 'night', label: '밤' },
]

const HEATING_METHODS = [
  { value: 'scheduled', label: '정해진 시간에 사용해요.' },
  { value: 'auto', label: '온도에 따라 자동 조절해요.' },
  { value: 'asNeeded', label: '필요할 때만 사용해요.' },
  { value: 'none', label: '사용하지 않아요.' },
]

// 시간 시트를 처음 열 때 휠 위치 (피그마 값)
const DEFAULT_LIGHT_RANGE = { start: '08:00', end: '20:00' }
const DEFAULT_HEATING_RANGE = { start: '10:00', end: '14:00' }

// { start, end } | 'none' → 칸에 보일 문구
const formatRange = (range) => {
  if (range === 'none') return '사용하지 않음'
  return range ? `${range.start} ~ ${range.end}` : ''
}

// 아이콘 + 항목 제목 (아이콘 크기가 항목마다 다름)
const IconTitle = ({ icon, iconSize, children, extra }) => (
  <div className="care-question-title">
    <img src={icon} alt="" style={{ width: iconSize[0], height: iconSize[1] }} />
    <h3>{children}</h3>
    {extra && <span className="care-question-title-extra">{extra}</span>}
  </div>
)

// 모든 항목 선택 사항 (아무것도 안 골라도 다음으로)
const CareQuestion = ({ form, updateForm, onPrev, onNext }) => {
  const { care } = form
  const [openSheet, setOpenSheet] = useState(null) // 'light' | 'heating' | null

  const update = (fields) => updateForm('care', { ...care, ...fields })

  const toggleMistTime = (value) => {
    const mistTimes = care.mistTimes.includes(value)
      ? care.mistTimes.filter((time) => time !== value)
      : [...care.mistTimes, value]
    update({ mistTimes })
  }

  return (
    <QuestionLayout
      className="care-question"
      titleLines={['현재 어떤식으로', '관리하고 계신가요?']}
      onBack={onPrev}
      buttonLabel="다음"
      onSubmit={onNext}
    >
      <section className="care-question-field">
        <IconTitle icon={waterIcon} iconSize={[19, 25]}>분무 방식</IconTitle>
        <div className="care-question-options care-question-mist-method" role="radiogroup" aria-label="분무 방식">
          {MIST_METHODS.map((option) => (
            <RadioOption
              key={option.value}
              size="m"
              label={option.label}
              checked={care.mistMethod === option.value}
              onClick={() => update({ mistMethod: option.value })}
            />
          ))}
        </div>
      </section>

      <section className="care-question-field">
        <IconTitle icon={calendarIcon} iconSize={[25, 26]}>분무 빈도</IconTitle>
        <OnboardingSelect
          variant="cream"
          ariaLabel="분무 빈도"
          placeholder="빈도를 선택해주세요."
          options={MIST_FREQUENCIES}
          value={care.mistFrequency}
          onChange={(mistFrequency) => update({ mistFrequency })}
        />
      </section>

      <section className="care-question-field">
        <IconTitle icon={clockIcon} iconSize={[27, 27]} extra="복수 선택 가능">분무 시간대</IconTitle>
        <div className="care-question-options care-question-mist-times" role="group" aria-label="분무 시간대">
          {MIST_TIMES.map((option) => (
            <RadioOption
              key={option.value}
              size="m"
              role="checkbox"
              label={option.label}
              checked={care.mistTimes.includes(option.value)}
              onClick={() => toggleMistTime(option.value)}
            />
          ))}
        </div>
      </section>

      <section className="care-question-field">
        <IconTitle icon={temperatureIcon} iconSize={[20, 30]}>조명 사용 시간</IconTitle>
        <PickerField
          ariaLabel="조명 사용 시간"
          placeholder="사용 시간대를 선택해주세요."
          value={formatRange(care.lightTime)}
          icon={clockLineIcon}
          onClick={() => setOpenSheet('light')}
        />
      </section>

      <section className="care-question-field care-question-field-heating">
        <IconTitle icon={temperatureIcon} iconSize={[20, 30]}>히팅 장비 사용 방식</IconTitle>
        <div className="care-question-heating" role="radiogroup" aria-label="히팅 장비 사용 방식">
          {HEATING_METHODS.map((option) => (
            <div key={option.value} className="care-question-heating-item">
              <RadioOption
                size="m"
                label={option.label}
                checked={care.heatingMethod === option.value}
                onClick={() => update({ heatingMethod: option.value })}
              />
              {option.value === 'scheduled' && care.heatingMethod === 'scheduled' && (
                <div className="care-question-heating-time">
                  <PickerField
                    ariaLabel="히팅 장비 사용 시간"
                    placeholder="사용 시간대를 선택해주세요."
                    value={formatRange(care.heatingTime)}
                    icon={clockLineIcon}
                    onClick={() => setOpenSheet('heating')}
                  />
                </div>
              )}
            </div>
          ))}
        </div>
      </section>

      {openSheet === 'light' && (
        <TimeRangeSheet
          title="조명 사용 시간"
          allowNone
          value={care.lightTime}
          defaultRange={DEFAULT_LIGHT_RANGE}
          onConfirm={(lightTime) => {
            update({ lightTime })
            setOpenSheet(null)
          }}
          onClose={() => setOpenSheet(null)}
        />
      )}

      {openSheet === 'heating' && (
        <TimeRangeSheet
          title="히팅 장비 사용 방식"
          value={care.heatingTime}
          defaultRange={DEFAULT_HEATING_RANGE}
          onConfirm={(heatingTime) => {
            update({ heatingTime })
            setOpenSheet(null)
          }}
          onClose={() => setOpenSheet(null)}
        />
      )}
    </QuestionLayout>
  )
}

export default CareQuestion
