import useGuideStep from './useGuideStep'
import { getGuideContext } from '../../api/guide'
import {
  useLocation,
  useNavigate,
} from 'react-router-dom'

import './EnclosureCheck.scss'

import cherryTree from '../../assets/guide/벚꽃.svg'
import backIcon from '../../assets/guide/뒤로가기_사육장 확인.svg'
import flowerFloor from '../../assets/guide/꽃바닥.svg'
import enclosureMatch from '../../assets/guide/사육장 적합안내.svg'
import nextButton from '../../assets/guide/다음단계로 이동하기.svg'

import { useSound } from '../../sound/SoundProvider'

const EnclosureCheck = () => {
  const navigate = useNavigate()
  const { playSfx } = useSound()
  const { state } = useLocation()
  const { finish } = useGuideStep(1)

  const context = getGuideContext()

  const {
    width,
    depth,
    height,
    material,
    customMaterial,
    ventilation,
  } = state || context.enclosureInfo || {}

  const selectedMaterial =
    material === '직접 입력'
      ? customMaterial
      : material

  const dimensions = [
    width,
    depth,
    height,
  ].map(Number)

  const valid = dimensions.every(
    (value) =>
      Number.isFinite(value) && value > 0,
  )

  // 요청 기준:
  // 가로 45 이하, 세로 45 이하, 높이 60 이하 중
  // 하나라도 해당하면 부적합입니다.
  const suitable =
    valid &&
    dimensions[0] > 45 &&
    dimensions[1] > 45 &&
    dimensions[2] > 60

  const speciesName =
    context.lizardDraft?.speciesName ||
    '크레스티드 게코'

  const handleNext = () => {
    playSfx('button')

    finish(() =>
      navigate('/guide/step2', {
        state: {
          entryStep: 1,
        },
      }),
    )
  }

  return (
    <div className="enclosure-check">
      <button
        className="enclosure-check-back"
        onClick={() => navigate(-1)}
      >
        <img src={backIcon} alt="뒤로가기" />
      </button>

      <h1 className="enclosure-check-title">
        준비하신 사육장을<br />
        확인했어요
      </h1>

      <img
        src={cherryTree}
        alt=""
        className="enclosure-check-tree"
      />

      <div className="enclosure-check-info">
        <span className="enclosure-check-tag">
          {width}cm X {depth}cm X {height}cm
        </span>

        <span className="enclosure-check-tag">
          {selectedMaterial}
        </span>

        <span className="enclosure-check-tag">
          {ventilation} 환기
        </span>
      </div>

      <img
        src={enclosureMatch}
        alt=""
        className="enclosure-check-match"
      />

      <h2 className="enclosure-check-match-title">
        <span className="enclosure-check-match-line">
          <span className="enclosure-check-match-species">
            성체 {speciesName}
          </span>

          <span className="enclosure-check-match-result">
            의
          </span>
        </span>

        <span className="enclosure-check-match-result">
          {!valid
            ? '사육장 크기를 확인해주세요.'
            : suitable
              ? '기본 권장 크기에 적합해요.'
              : '기본 권장 크기에 부적합해요.'}
        </span>
      </h2>

      <p className="enclosure-check-match-p">
        {suitable ? (
          <>
            사육장 안에 이동 구조물을 배치할 수 있는<br />
            높이도 충분한지 함께 확인해주세요.
          </>
        ) : (
          <>
            가로 45cm·세로 45cm·높이 60cm를<br />
            각각 초과하는 크기로 준비해주세요.
          </>
        )}
      </p>

      <button
        className="enclosure-check-next"
        onClick={handleNext}
      >
        <img
          src={nextButton}
          alt="다음 단계로 이동하기"
        />
      </button>

      <img
        src={flowerFloor}
        alt=""
        className="enclosure-check-floor"
      />
    </div>
  )
}

export default EnclosureCheck