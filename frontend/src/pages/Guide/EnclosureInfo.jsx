import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import leaf from '../../assets/guide/나뭇잎.svg'
import sizeTitle from '../../assets/guide/크기.svg'
import materialTitle from '../../assets/guide/소재.svg'
import ventilationTitle from '../../assets/guide/환기 구조.svg'
import complete from '../../assets/guide/완료버튼.svg'

import './EnclosureInfo.scss'

const EnclosureInfo = ({ onClose }) => {
  const navigate = useNavigate()

  const [size, setSize] = useState({
    width: '',
    depth: '',
    height: '',
  })

  const [material, setMaterial] = useState('유리')
  const [customMaterial, setCustomMaterial] = useState('')
  const [ventilation, setVentilation] = useState('상단')

  const handleComplete = () => {
    const enclosureInfo = {
      ...size,
      material,
      customMaterial,
      ventilation,
    }

    console.log('사육장 정보:', enclosureInfo)
    navigate('/guide/enclosure-check', { state: enclosureInfo })
  }

  return (
    <div className="enclosure-overlay" onClick={onClose}>
      <div className="enclosure-modal" onClick={(e) => e.stopPropagation()}>

        <section className="enclosure-section">
          <h3 className="enclosure-title">
            <img className="enclosure-leaf" src={leaf} alt="" />
            <img className="enclosure-title-text" src={sizeTitle} alt="크기" />
          </h3>

          <div className="enclosure-size-row">
            <input type="number" placeholder="가로 (cm)" value={size.width} onChange={(e) => setSize({ ...size, width: e.target.value })} />
            <span>×</span>
            <input type="number" placeholder="세로 (cm)" value={size.depth} onChange={(e) => setSize({ ...size, depth: e.target.value })} />
            <span>×</span>
            <input type="number" placeholder="높이 (cm)" value={size.height} onChange={(e) => setSize({ ...size, height: e.target.value })} />
          </div>
        </section>

        <section className="enclosure-section">
          <h3 className="enclosure-title">
            <img className="enclosure-leaf" src={leaf} alt="" />
            <img className="enclosure-title-text" src={materialTitle} alt="소재" />
          </h3>

          <div className="enclosure-options material-options">
            {['유리', '아크릴', 'PVC', '메쉬', '직접 입력'].map((item) => (
              <label key={item} className={`enclosure-option ${item === '메쉬' || item === '직접 입력' ? 'enclosure-option-break' : ''}`}>
                <input type="radio" name="material" value={item} checked={material === item} onChange={() => setMaterial(item)} />
                <span>{item}</span>
              </label>
            ))}
          </div>

          <input className="enclosure-custom-input" type="text" placeholder="소재를 입력해주세요." value={customMaterial} onChange={(e) => setCustomMaterial(e.target.value)} />
        </section>

        <section className="enclosure-section">
          <h3 className="enclosure-title">
            <img className="enclosure-leaf" src={leaf} alt="" />
            <img className="enclosure-title-text" src={ventilationTitle} alt="환기 구조" />
          </h3>

          <div className="enclosure-options ventilation-options">
            {['상단', '측면', '둘 다', '잘 모르겠어요'].map((item) => (
              <label key={item} className={`enclosure-option ${item === '잘 모르겠어요' ? 'enclosure-option-break' : ''}`}>
                <input type="radio" name="ventilation" value={item} checked={ventilation === item} onChange={() => setVentilation(item)} />
                <span>{item}</span>
              </label>
            ))}
          </div>
        </section>

        <button className="enclosure-complete" onClick={handleComplete}>
          <img src={complete} alt="완료" />
        </button>

      </div>
    </div>
  )
}

export default EnclosureInfo