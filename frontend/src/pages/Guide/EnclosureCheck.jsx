import { useLocation, useNavigate } from 'react-router-dom'
import './EnclosureCheck.scss'

import cherryTree from '../../assets/guide/벚꽃.svg'
import backIcon from '../../assets/guide/뒤로가기_사육장 확인.svg'
import flowerFloor from '../../assets/guide/꽃바닥.svg'
import enclosureMatch from '../../assets/guide/사육장 적합.svg'
import nextButton from '../../assets/guide/다음단계로 이동하기.svg'

const EnclosureCheck = () => {
  const navigate = useNavigate()
  const { state } = useLocation()

  const { width, depth, height, material, customMaterial, ventilation } = state || {}
  const selectedMaterial = material === '직접 입력' ? customMaterial : material

  return (
    <div className="enclosure-check">

      <button className="enclosure-check-back" onClick={() => navigate(-1)}>
        <img src={backIcon} alt="뒤로가기" />
      </button>

      <h1 className="enclosure-check-title">
        준비하신 사육장을<br />
        확인했어요
      </h1>

      <img src={cherryTree} alt="" className="enclosure-check-tree" />

      <div className="enclosure-check-info">
        <span className="enclosure-check-tag">{width}cm X {depth}cm X {height}cm</span>
        <span className="enclosure-check-tag">{selectedMaterial}</span>
        <span className="enclosure-check-tag">{ventilation} 환기</span>
      </div>

      <img src={enclosureMatch} alt="사육장 크기 적합 안내" className="enclosure-check-match" />

      <p className="enclosure-check-match-p">사육장 안에 이동 구조물을 배치할 수 있는<br />높이도 충분한지 함께 확인해주세요.</p>

      <button className="enclosure-check-next" onClick={() => navigate('/guide/step2')}>
        <img src={nextButton} alt="다음 단계로 이동하기" />
      </button>

      <img src={flowerFloor} alt="" className="enclosure-check-floor" />

    </div>
  )
}

export default EnclosureCheck