import { useState } from 'react'

import CustomScreen from '../common/CustomScreen'
import ChoiceCard from '../common/ChoiceCard'
import OnboardingInput from '../common/OnboardingInput'
import OnboardingButton from '../common/OnboardingButton'
import QuestPopup from '../common/QuestPopup'
import LeafTitle from '../common/LeafTitle'
import RadioOption from '../common/RadioOption'

import wallpaper from '../../../../assets/onboarding/bg-pattern-orange.png'
import geckoBadge from '../../../../assets/onboarding/badge-gecko.png'
import paletteIcon from '../../../../assets/onboarding/icon-palette.png'
import listIcon from '../../../../assets/onboarding/icon-list.png'
import checkBadgeIcon from '../../../../assets/onboarding/icon-check-badge.svg'
import wallCream from '../../../../assets/onboarding/tank-wall-cream.png'
import wallGreen from '../../../../assets/onboarding/tank-wall-green.png'
import wallYellow from '../../../../assets/onboarding/tank-wall-yellow.png'
import floorBark from '../../../../assets/onboarding/tank-floor-bark.png'
import floorSand from '../../../../assets/onboarding/tank-floor-sand.png'
import floorGrass from '../../../../assets/onboarding/tank-floor-grass.png'
import vineHeart from '../../../../assets/onboarding/tank-vine-heart.png'
import vineCurly from '../../../../assets/onboarding/tank-vine-curly.png'
import vineLeaf from '../../../../assets/onboarding/tank-vine-leaf.png'
import structureLog from '../../../../assets/onboarding/tank-structure-log.png'
import structureRock from '../../../../assets/onboarding/tank-structure-rock.png'
import structureStump from '../../../../assets/onboarding/tank-structure-stump.png'

import './TankCustom.scss'

// 미리보기에서 각 이미지의 위치 [left, top, width, height?] (무대 기준 px)
// 이미지마다 캔버스 여백이 달라서, 그림 영역이 피그마 예시(크림 벽지·하트 덩굴·통나무)와 겹치도록 계산한 값
const CREAM_WALL_RECT = [55, 159, 280.5]

// 사육장 본체: 벽지·빈 사육장 이미지가 각각 따로 그려져 안쪽 모양이 조금씩 달라서,
// 기준인 크림 벽지 이미지 하나를 쓰고 벽면 색만 바꾼다 (TankCustom.scss의 .tank-custom-wall-tint-*)
// 빈 사육장은 회색, 초록·노랑은 각 벽지 이미지의 벽 색에 맞춘 필터
// TODO: 같은 원본에서 벽 색만 바꾼 빈 사육장·벽지 이미지를 받으면 이미지로 교체

// 바닥재: 사육장 바닥 마름모(무대 기준 꼭짓점 왼(80,350.7) 뒤(193.3,293.8) 오른(308.1,352.2) 앞(194.8,415))에
// 각 바닥재 이미지의 윗면을 늘려 맞추고 바닥 모양대로 잘라낸다. fill: 잘라낸 영역 안에서 이미지 위치 [left, top, width, height]
const FLOOR_AREA = { left: 80, top: 293.8, width: 228.1, height: 121.3 }

// 피그마 순서. type: 섹션 키 (form.tank의 키와 같음)
const TANK_PARTS = [
  {
    type: 'wall',
    title: '벽지',
    options: [
      { value: 'cream', label: '크림 벽지', image: wallCream },
      { value: 'green', label: '초록 벽지', image: wallGreen },
      { value: 'yellow', label: '노랑 벽지', image: wallYellow },
    ],
  },
  {
    type: 'floor',
    title: '바닥재',
    options: [
      { value: 'bark', label: '나무껍질 바닥재', image: floorBark, fill: [-10.5, -68.9, 244.1, 204.9] },
      { value: 'sand', label: '모래 바닥재', image: floorSand, fill: [-7, -66.8, 242.5, 198.3] },
      { value: 'grass', label: '잔디 바닥재', image: floorGrass, fill: [-4.8, -50, 238.7, 184.8] },
    ],
  },
  {
    type: 'vine',
    title: '덩굴',
    options: [
      { value: 'heart', label: '하트잎 덩굴', image: vineHeart, rect: [73.6, 191.9, 133.1] },
      { value: 'curly', label: '꼬불 덩굴', image: vineCurly, rect: [78.4, 195.3, 124.9] },
      { value: 'leaf', label: '잎사귀 덩굴', image: vineLeaf, rect: [69.2, 196.7, 131.1] },
    ],
  },
  {
    type: 'structure',
    title: '구조물',
    options: [
      { value: 'log', label: '통나무 은신처', image: structureLog, rect: [96, 253, 156] },
      { value: 'rock', label: '바위 은신처', image: structureRock, rect: [91.9, 258.8, 159.3] },
      { value: 'stump', label: '그루터기', image: structureStump, rect: [87.6, 259.9, 163.3] },
    ],
  },
]

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

// 진행바에 반영하는 항목 (입력을 마친 개수 / 전체)
const PROGRESS_CHECKS = [
  (t) => t.wall,
  (t) => t.floor,
  (t) => t.vine,
  (t) => t.structure,
  (t) => t.width && t.depth && t.height,
  (t) => (t.material === 'custom' ? t.materialCustom.trim() : t.material),
  (t) => t.ventilation,
]
const REQUIRED_CHECKS = [(t) => t.wall, (t) => t.floor]

const rectStyle = ([left, top, width, height]) => ({ left, top, width, height })
const onlyNumber = (value) => value.replace(/[^0-9.]/g, '')

const TankCustom = ({ form, updateForm, onPrev, onNext, isSoundOn, onToggleSound }) => {
  const { tank, questSeen } = form
  const [activeTab, setActiveTab] = useState('decor')
  const [notice, setNotice] = useState('')

  const update = (fields) => {
    updateForm('tank', { ...tank, ...fields })
    setNotice('')
  }

  const progress = PROGRESS_CHECKS.filter((check) => check(tank)).length / PROGRESS_CHECKS.length
  const selectedOption = (part) => part.options.find((option) => option.value === tank[part.type])
  const [wallPart, floorPart, ...decorParts] = TANK_PARTS
  const wallTint = selectedOption(wallPart)?.value ?? 'empty'
  const floor = selectedOption(floorPart)

  const closeQuest = () => updateForm('questSeen', { ...questSeen, tank: true })

  // 커스텀 완료: 필수 항목(벽지·바닥재)을 채웠으면 다음 단계로
  const handleComplete = () => {
    if (REQUIRED_CHECKS.every((check) => check(tank))) {
      setNotice('')
      onNext()
    } else {
      setNotice('벽지와 바닥재를 선택해주세요.')
    }
  }

  return (
    <CustomScreen
      className="tank-custom"
      title="내 사육장 등록하기"
      onBack={onPrev}
      background={wallpaper}
      progress={progress}
      badge={{ image: geckoBadge, label: '도마뱀 커스텀으로 돌아가기', onClick: onPrev }}
      notice={notice}
      isSoundOn={isSoundOn}
      onToggleSound={onToggleSound}
      soundSize="small"
      stage={
        <div className="tank-custom-preview" aria-label="내 사육장 미리보기" role="img">
          <img src={wallCream} alt="" style={rectStyle(CREAM_WALL_RECT)} />
          {wallTint !== 'cream' && (
            <img className={`tank-custom-wall-tint tank-custom-wall-tint-${wallTint}`} src={wallCream} alt="" style={rectStyle(CREAM_WALL_RECT)} />
          )}
          {floor && (
            <div className="tank-custom-floor" style={FLOOR_AREA}>
              <div className="tank-custom-floor-fill">
                <img src={floor.image} alt="" style={rectStyle(floor.fill)} />
              </div>
            </div>
          )}
          {decorParts.map((part) => {
            const option = selectedOption(part)
            return (
              option && (
                <img key={part.type} className={`tank-custom-${part.type}`} src={option.image} alt="" style={rectStyle(option.rect)} />
              )
            )
          })}
        </div>
      }
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

          <div className="tank-custom-complete">
            <OnboardingButton onClick={handleComplete}>커스텀 완료!</OnboardingButton>
          </div>
        </div>
      )}

      {!questSeen.tank && (
        <QuestPopup
          title="내 사육장을 커스텀 해주세요!"
          description="사육장의 모습과 기본 정보를 입력해주세요."
          top={362}
          onClose={closeQuest}
        />
      )}
    </CustomScreen>
  )
}

export default TankCustom
