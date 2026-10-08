import OnboardingRadioGroup from '../common/OnboardingRadioGroup'
import OnboardingButton from '../common/OnboardingButton'

import tankEmpty from '../../../../assets/onboarding/tank-empty.png'
import wallYellow from '../../../../assets/onboarding/tank-wall-yellow.png'
import wallGreen from '../../../../assets/onboarding/tank-wall-green.png'
import wallCream from '../../../../assets/onboarding/tank-wall-cream.png'
import floorBark from '../../../../assets/onboarding/tank-floor-bark.png'
import floorSand from '../../../../assets/onboarding/tank-floor-sand.png'
import floorGrass from '../../../../assets/onboarding/tank-floor-grass.png'
import structureLog from '../../../../assets/onboarding/tank-structure-log.png'
import structureRock from '../../../../assets/onboarding/tank-structure-rock.png'
import structureStump from '../../../../assets/onboarding/tank-structure-stump.png'
import vineHeart from '../../../../assets/onboarding/tank-vine-heart.png'
import vineCurly from '../../../../assets/onboarding/tank-vine-curly.png'
import vineLeaf from '../../../../assets/onboarding/tank-vine-leaf.png'

// 사육장 파츠별 선택지. 미리보기는 빈 사육장 위에 선택한 파츠를 겹쳐서 표시
const TANK_PARTS = [
  {
    key: 'wall',
    label: '벽지',
    options: [
      { value: 'yellow', image: wallYellow },
      { value: 'green', image: wallGreen },
      { value: 'cream', image: wallCream },
    ],
  },
  {
    key: 'floor',
    label: '바닥재',
    options: [
      { value: 'bark', image: floorBark },
      { value: 'sand', image: floorSand },
      { value: 'grass', image: floorGrass },
    ],
  },
  {
    key: 'structure',
    label: '구조물',
    options: [
      { value: 'log', image: structureLog },
      { value: 'rock', image: structureRock },
      { value: 'stump', image: structureStump },
    ],
  },
  {
    key: 'vine',
    label: '덩굴',
    options: [
      { value: 'heart', image: vineHeart },
      { value: 'curly', image: vineCurly },
      { value: 'leaf', image: vineLeaf },
    ],
  },
]

const TankCustom = ({ form, updateForm, onNext, onPrev }) => {
  const { tank } = form

  const isComplete = TANK_PARTS.every((part) => tank[part.key])

  return (
    <section className="tank-custom">
      <div className="tank-custom-preview">
        <img src={tankEmpty} alt="" />
        {TANK_PARTS.map((part) => {
          const selected = part.options.find((option) => option.value === tank[part.key])
          return selected && <img key={part.key} className={`tank-custom-${part.key}`} src={selected.image} alt="" />
        })}
      </div>

      {TANK_PARTS.map((part) => (
        <div key={part.key} className="tank-custom-part">
          <span className="tank-custom-part-label">{part.label}</span>
          <OnboardingRadioGroup
            name={`tank-${part.key}`}
            options={part.options}
            value={tank[part.key]}
            onChange={(value) => updateForm('tank', { ...tank, [part.key]: value })}
          />
        </div>
      ))}

      <OnboardingButton variant="secondary" onClick={onPrev}>이전</OnboardingButton>
      <OnboardingButton disabled={!isComplete} onClick={onNext}>다음</OnboardingButton>
    </section>
  )
}

export default TankCustom
