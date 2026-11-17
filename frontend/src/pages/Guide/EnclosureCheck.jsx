import useGuideStep from './useGuideStep'
import { getGuideContext } from '../../api/guide'
import { useLocation, useNavigate } from 'react-router-dom'

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

  const {
    width,
    depth,
    height,
    material,
    customMaterial,
    ventilation,
  } = state || getGuideContext().enclosureInfo || {}

  const selectedMaterial =
    material === '직접 입력' ? customMaterial : material

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
            성체 크레스티드 게코
          </span>
          <span className="enclosure-check-match-result">
            의
          </span>
        </span>

        <span className="enclosure-check-match-result">
          기본 권장 크기에 적합해요.
        </span>
      </h2>

      <p className="enclosure-check-match-p">
        사육장 안에 이동 구조물을 배치할 수 있는<br />
        높이도 충분한지 함께 확인해주세요.
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