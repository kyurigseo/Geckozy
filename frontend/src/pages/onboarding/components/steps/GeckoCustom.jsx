import { useState } from 'react'

import CustomScreen from '../common/CustomScreen'
import ChoiceCard from '../common/ChoiceCard'
import OnboardingInput from '../common/OnboardingInput'
import QuestPopup from '../common/QuestPopup'
import LeafTitle from '../common/LeafTitle'
import SearchSelect from '../common/SearchSelect'
import RadioOption from '../common/RadioOption'
import DateWheelSheet from '../common/DateWheelSheet'

import wallpaper from '../../../../assets/onboarding/bg-pattern-yellow.png'
import tankBadge from '../../../../assets/onboarding/badge-tank.png'
import logDeco from '../../../../assets/onboarding/deco-log.png'
import paletteIcon from '../../../../assets/onboarding/icon-palette.png'
import listIcon from '../../../../assets/onboarding/icon-list.png'
import calendarIcon from '../../../../assets/onboarding/icon-calendar-line.svg'
import geckoBeige from '../../../../assets/onboarding/gecko-beige.png'
import geckoOrange from '../../../../assets/onboarding/gecko-orange.png'
import geckoYellow from '../../../../assets/onboarding/gecko-yellow.png'
import geckoGreen from '../../../../assets/onboarding/gecko-green.png'
import geckoBlue from '../../../../assets/onboarding/gecko-blue.png'
import swatchBeige from '../../../../assets/onboarding/swatch-beige.png'
import swatchOrange from '../../../../assets/onboarding/swatch-orange.png'
import swatchYellow from '../../../../assets/onboarding/swatch-yellow.png'
import swatchGreen from '../../../../assets/onboarding/swatch-green.png'
import swatchBlue from '../../../../assets/onboarding/swatch-blue.png'

import './GeckoCustom.scss'

// 피그마 순서 (1줄: 주황·노랑·파랑, 2줄: 초록·베이지)
const GECKO_COLORS = [
  { value: 'orange', label: '주황', swatch: swatchOrange, gecko: geckoOrange },
  { value: 'yellow', label: '노랑', swatch: swatchYellow, gecko: geckoYellow },
  { value: 'blue', label: '파랑', swatch: swatchBlue, gecko: geckoBlue },
  { value: 'green', label: '초록', swatch: swatchGreen, gecko: geckoGreen },
  { value: 'beige', label: '베이지', swatch: swatchBeige, gecko: geckoBeige },
]
const DEFAULT_GECKO = geckoBeige // 색을 고르기 전 미리보기

// TODO: 종 목록 확정 필요 (피그마 컴포넌트에 나온 종 + 대표 종)
const GECKO_SPECIES = [
  '레오파드 게코',
  '크레스티드 게코',
  '가고일 게코',
  '크라운드 게코',
  '크로커다일 게코',
  '크로커다일 스킨크',
  '크로커다일 모니터',
  '아프리칸 펫테일 게코',
  '리키에너스 게코',
  '토케이 게코',
  '비어디드 드래곤',
  '블루텅 스킨크',
]

const GENDERS = [
  { value: 'male', label: '수컷' },
  { value: 'female', label: '암컷' },
]

const TABS = [
  { key: 'color', label: '색상', icon: paletteIcon, iconSize: [36, 33] },
  { key: 'info', label: '기본정보', icon: listIcon, iconSize: [26, 30] },
]

// 진행바에 반영하는 항목 (입력을 마친 개수 / 전체)
const PROGRESS_CHECKS = [
  (g) => g.color,
  (g) => g.name.trim(),
  (g) => g.species,
  (g) => g.birthKnown === 'unknown' || g.birthDate,
  (g) => g.gender,
  (g) => g.sizeUnknown || g.size.trim() || g.weight.trim(),
]
const REQUIRED_CHECKS = [(g) => g.color, (g) => g.name.trim(), (g) => g.species, (g) => g.gender]

// 'YYYY-MM-DD' → 'YYYY년 MM월 DD일'
const formatDate = (value) => {
  const [year, month, day] = value.split('-')
  return `${year}년 ${month}월 ${day}일`
}

const GeckoCustom = ({ form, updateForm, onPrev, onNext, isSoundOn, onToggleSound }) => {
  const { gecko, questSeen } = form
  const [activeTab, setActiveTab] = useState('color')
  const [isDateSheetOpen, setIsDateSheetOpen] = useState(false)
  const [notice, setNotice] = useState('')

  const update = (fields) => {
    updateForm('gecko', { ...gecko, ...fields })
    setNotice('')
  }

  const progress = PROGRESS_CHECKS.filter((check) => check(gecko)).length / PROGRESS_CHECKS.length
  const previewGecko = GECKO_COLORS.find((color) => color.value === gecko.color)?.gecko ?? DEFAULT_GECKO

  const closeQuest = () => updateForm('questSeen', { ...questSeen, gecko: true })

  // 사육장 배지: 필수 항목(색상·이름·종·성별)을 채웠으면 다음 단계로
  const handleNext = () => {
    if (REQUIRED_CHECKS.every((check) => check(gecko))) {
      setNotice('')
      onNext()
    } else {
      setNotice('색상, 이름, 도마뱀 종, 성별을 입력해주세요.')
    }
  }

  const handleSizeUnknown = () => {
    const sizeUnknown = !gecko.sizeUnknown
    update(sizeUnknown ? { sizeUnknown, size: '', weight: '' } : { sizeUnknown })
  }

  return (
    <CustomScreen
      className="gecko-custom"
      title="내 도마뱀 등록하기"
      onBack={onPrev}
      background={wallpaper}
      progress={progress}
      badge={{ image: tankBadge, label: '사육장 꾸미기로 넘어가기', onClick: handleNext }}
      notice={notice}
      isSoundOn={isSoundOn}
      onToggleSound={onToggleSound}
      stage={
        <>
          <img className="gecko-custom-log" src={logDeco} alt="" />
          <img className="gecko-custom-preview" src={previewGecko} alt="내 도마뱀 미리보기" />
        </>
      }
      tabs={TABS}
      activeTab={activeTab}
      onTabChange={setActiveTab}
    >
        {activeTab === 'color' ? (
          <ul className="choice-card-grid gecko-custom-colors">
            {GECKO_COLORS.map((color) => (
              <ChoiceCard
                key={color.value}
                label={color.label}
                selected={gecko.color === color.value}
                onToggle={() => update({ color: gecko.color === color.value ? '' : color.value })}
              >
                <img className="gecko-custom-swatch" src={color.swatch} alt="" />
              </ChoiceCard>
            ))}
          </ul>
        ) : (
          <div className="gecko-custom-info">
            <section className="gecko-custom-field">
              <LeafTitle>이름</LeafTitle>
              <OnboardingInput
                variant="cream"
                name="geckoName"
                placeholder="이름을 입력해주세요."
                value={gecko.name}
                onChange={(name) => update({ name })}
              />
            </section>

            <section className="gecko-custom-field">
              <LeafTitle>도마뱀 종</LeafTitle>
              <SearchSelect
                ariaLabel="도마뱀 종"
                placeholder="해당하는 종을 선택해주세요."
                options={GECKO_SPECIES}
                value={gecko.species}
                onChange={(species) => update({ species })}
              />
            </section>

            <section className="gecko-custom-field gecko-custom-field-birth">
              <LeafTitle>태어난 일시</LeafTitle>
              <div className="gecko-custom-birth" role="radiogroup" aria-label="태어난 일시">
                <RadioOption
                  label="정확한 생년월일을 알고 있어요."
                  checked={gecko.birthKnown === 'known'}
                  onClick={() => update({ birthKnown: 'known' })}
                />
                {gecko.birthKnown === 'known' && (
                  <button type="button" className="gecko-custom-date" onClick={() => setIsDateSheetOpen(true)}>
                    <span className={gecko.birthDate ? 'gecko-custom-date-value' : 'gecko-custom-date-placeholder'}>
                      {gecko.birthDate ? formatDate(gecko.birthDate) : '날짜를 선택해주세요.'}
                    </span>
                    <img src={calendarIcon} alt="" />
                  </button>
                )}
                <RadioOption
                  label="정확히 모르겠어요."
                  checked={gecko.birthKnown === 'unknown'}
                  onClick={() => update({ birthKnown: 'unknown', birthDate: '' })}
                />
              </div>
            </section>

            <section className="gecko-custom-field gecko-custom-field-gender">
              <LeafTitle>성별</LeafTitle>
              <div className="gecko-custom-genders" role="radiogroup" aria-label="성별">
                {GENDERS.map((gender) => (
                  <button
                    key={gender.value}
                    type="button"
                    role="radio"
                    aria-checked={gecko.gender === gender.value}
                    className={gecko.gender === gender.value ? 'gecko-custom-gender selected' : 'gecko-custom-gender'}
                    onClick={() => update({ gender: gender.value })}
                  >
                    {gender.label}
                  </button>
                ))}
              </div>
            </section>

            <section className="gecko-custom-field gecko-custom-field-size">
              <LeafTitle>크기 또는 체중</LeafTitle>
              <div className="gecko-custom-size">
                <OnboardingInput
                  variant="cream"
                  name="geckoSize"
                  placeholder="크기"
                  suffix="(cm)"
                  inputMode="decimal"
                  disabled={gecko.sizeUnknown}
                  value={gecko.size}
                  onChange={(size) => update({ size: size.replace(/[^0-9.]/g, '') })}
                />
                <OnboardingInput
                  variant="cream"
                  name="geckoWeight"
                  placeholder="체중"
                  suffix="(g)"
                  inputMode="decimal"
                  disabled={gecko.sizeUnknown}
                  value={gecko.weight}
                  onChange={(weight) => update({ weight: weight.replace(/[^0-9.]/g, '') })}
                />
                <RadioOption label="잘 모르겠어요." role="checkbox" checked={gecko.sizeUnknown} onClick={handleSizeUnknown} />
              </div>
            </section>
          </div>
        )}

      {!questSeen.gecko && (
        <QuestPopup
          title="내 도마뱀의 외형을 커스텀 해주세요!"
          description="도마뱀의 색과 기본 정보를 입력해주세요."
          onClose={closeQuest}
        />
      )}

      {isDateSheetOpen && (
        <DateWheelSheet
          title="생년월일"
          value={gecko.birthDate}
          onConfirm={(birthDate) => {
            update({ birthDate })
            setIsDateSheetOpen(false)
          }}
          onClose={() => setIsDateSheetOpen(false)}
        />
      )}
    </CustomScreen>
  )
}

export default GeckoCustom
