import { useState } from 'react'

import CustomScreen from './CustomScreen'
import ChoiceCard from './ChoiceCard'
import OnboardingInput from './OnboardingInput'
import LeafTitle from './LeafTitle'
import RadioOption from './RadioOption'

import TankPreview from '../../../../components/tank/TankPreview'
import { TANK_PARTS } from '../../../../components/tank/tankParts'

import wallpaper from '../../../../assets/onboarding/bg-pattern-orange.png'
import paletteIcon from '../../../../assets/onboarding/icon-palette.png'
import listIcon from '../../../../assets/onboarding/icon-list.png'
import checkBadgeIcon from '../../../../assets/onboarding/icon-check-badge.svg'

import './TankCustomScreen.scss'

const SIZE_FIELDS = [
  { key: 'width', label: '가로' },
  { key: 'depth', label: '세로' },
  { key: 'height', label: '높이' },
]

const MATERIALS = [
  { value: 'glass', label: '유리' },
  { value: 'acrylic', label: '아크릴' },
  { value: 'pvc', label: 'PVC' },
  { value: 'mesh', label: '매쉬' },
  { value: 'custom', label: '직접 입력' },
]

const VENTILATIONS = [
  { value: 'top', label: '상단' },
  { value: 'side', label: '측면' },
  { value: 'both', label: '둘 다' },
  { value: 'unknown', label: '잘 모르겠어요' },
]

const TABS = [
  { key: 'decor', label: '꾸미기', icon: paletteIcon, iconSize: [36, 33] },
  { key: 'info', label: '기본정보', icon: listIcon, iconSize: [26, 30] },
]

const onlyNumber = (value) => value.replace(/[^0-9.]/g, '')

// 사육장 커스텀 화면 (온보딩 '내 사육장 등록하기'·마이 '사육장 커스텀' 공통)
// 무대(사육장 미리보기) + 탭(꾸미기·기본정보). tank: 입력값, onChange(바뀐 항목만)
// progress·badge·sound는 온보딩에서만 넘긴다. compact: 진행바 줄이 없어 무대가 위로 올라간 배치 (마이)
// infoFooter: 기본정보 탭 맨 아래 (온보딩 '커스텀 완료!' 버튼). children: 화면 위에 띄울 팝업
const TankCustomScreen = ({
  title,
  onBack,
  tank,
  onChange,
  progress,
  badge,
  notice,
  isSoundOn,
  onToggleSound,
  compact = false,
  infoFooter,
  children,
}) => {
  const [activeTab, setActiveTab] = useState('decor')

  const update = onChange

  return (
    <CustomScreen
      className={compact ? 'tank-custom tank-custom-compact' : 'tank-custom'}
      title={title}
      onBack={onBack}
      background={wallpaper}
      progress={progress}
      badge={badge}
      notice={notice}
      isSoundOn={isSoundOn}
      onToggleSound={onToggleSound}
      soundSize="small"
      stage={<TankPreview tank={tank} />}
      tabs={TABS}
      activeTab={activeTab}
      onTabChange={setActiveTab}
    >
      {activeTab === 'decor' ? (
        <div className="tank-custom-decor">
          {TANK_PARTS.map((part) => (
            <section key={part.type} className="tank-custom-part">
              <h3 className="tank-custom-part-title">{part.title}</h3>
              <ul className="choice-card-grid">
                {part.options.map((option) => {
                  const isSelected = tank[part.type] === option.value
                  return (
                    <ChoiceCard
                      key={option.value}
                      label={option.label}
                      selected={isSelected}
                      onToggle={() => update({ [part.type]: isSelected ? '' : option.value })}
                    >
                      <img className={`tank-custom-thumb tank-custom-thumb-${part.type}-${option.value}`} src={option.image} alt="" />
                    </ChoiceCard>
                  )
                })}
              </ul>
            </section>
          ))}
        </div>
      ) : (
        <div className="tank-custom-info">
          <section className="tank-custom-field">
            <LeafTitle>크기</LeafTitle>
            <div className="tank-custom-size">
              {SIZE_FIELDS.map((field, index) => (
                <div key={field.key} className="tank-custom-size-item">
                  {index > 0 && <span className="tank-custom-size-x" aria-hidden="true">x</span>}
                  <OnboardingInput
                    variant="cream"
                    name={`tank-${field.key}`}
                    placeholder={field.label}
                    suffix="(cm)"
                    inputMode="decimal"
                    value={tank[field.key]}
                    onChange={(value) => update({ [field.key]: onlyNumber(value) })}
                  />
                </div>
              ))}
            </div>
            <p className="tank-custom-note">
              <img src={checkBadgeIcon} alt="" />
              사육장 크기는 환경 데이터를 해석하는 데 참고할게요!
            </p>
          </section>

          <section className="tank-custom-field">
            <LeafTitle>소재</LeafTitle>
            <div className="tank-custom-options tank-custom-materials" role="radiogroup" aria-label="소재">
              {MATERIALS.map((material) => (
                <RadioOption
                  key={material.value}
                  size="m"
                  label={material.label}
                  checked={tank.material === material.value}
                  onClick={() => update({ material: material.value })}
                />
              ))}
            </div>
            {tank.material === 'custom' && (
              <div className="tank-custom-material-input">
                <OnboardingInput
                  variant="cream"
                  name="tank-material"
                  placeholder="소재를 입력해주세요."
                  value={tank.materialCustom}
                  onChange={(materialCustom) => update({ materialCustom })}
                />
              </div>
            )}
            <p className="tank-custom-note">
              <img src={checkBadgeIcon} alt="" />
              소재에 따라 온도와 습도가 유지되는 정도가 달라질 수 있어요.
            </p>
          </section>

          <section className="tank-custom-field">
            <LeafTitle>환기 구조</LeafTitle>
            <div className="tank-custom-options tank-custom-ventilations" role="radiogroup" aria-label="환기 구조">
              {VENTILATIONS.map((ventilation) => (
                <RadioOption
                  key={ventilation.value}
                  size="m"
                  label={ventilation.label}
                  checked={tank.ventilation === ventilation.value}
                  onClick={() => update({ ventilation: ventilation.value })}
                />
              ))}
            </div>
          </section>

          {infoFooter}
        </div>
      )}

      {children}
    </CustomScreen>
  )
}

export default TankCustomScreen
