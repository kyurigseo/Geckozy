import React, { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import board from '../../assets/guide/나무판자.svg'
import leaf from '../../assets/guide/나뭇잎.svg'
import lizardType from '../../assets/guide/도마뱀 종류.svg'
import arrowBtm from '../../assets/guide/arrow_btm.svg'
import birthDate from '../../assets/guide/태어난 일시.svg'
import sizeWeight from '../../assets/guide/크기 또는 체중.svg'
import adoptionDate from '../../assets/guide/입양 예정일.svg'
import calendar from '../../assets/guide/달력.svg'
import complete from '../../assets/guide/완료버튼.svg'

import './LizardInfo.scss'

const LizardInfo = ({ step, onClose }) => {
  const navigate = useNavigate()
  const [birthUnknown, setBirthUnknown] = useState(false)
  const [birthDateValue, setBirthDateValue] = useState('')
  const [sizeUnknown, setSizeUnknown] = useState(false)
  const [adoptionDateValue, setAdoptionDateValue] = useState('')

  const birthDateInput = useRef(null)
  const adoptionDateInput = useRef(null)

  return (
    <div className="lizard-info-overlay" onClick={onClose}>

      <div className="lizard-info-modal" onClick={(e) => e.stopPropagation()}>

        <img className="lizard-info-board" src={board} alt="어떤 도마뱀을 데려올 예정이신가요?" />

        <div className="lizard-info-content">

          <section className="info-section">
            <div className="info-title">
              <img className="info-leaf" src={leaf} alt="" />
              <img className="info-title-text" src={lizardType} alt="도마뱀 종류" />
            </div>

            <div className="select-box">
              <select className="info-select" defaultValue="">
                <option value="" disabled>해당하는 종을 선택해주세요.</option>
                <option value="crested-gecko">크레스티드게코</option>
              </select>

              <img className="select-arrow" src={arrowBtm} alt="" />
            </div>
          </section>


          <section className="info-section birth-section">
            <div className="info-title">
              <img className="info-leaf" src={leaf} alt="" />
              <img className="info-title-text" src={birthDate} alt="태어난 일시" />
            </div>

            <label className="radio-row">
              <input type="radio" name="birth" checked={!birthUnknown} onChange={() => setBirthUnknown(false)} />
              <span>정확한 생년월일을 알고 있어요.</span>
            </label>

            <div className="date-input" onClick={() => birthDateInput.current?.showPicker()}>
              <span className={birthDateValue ? 'date-value' : 'date-placeholder'}>
                {birthDateValue || '날짜를 선택해주세요.'}
              </span>

              <img className="calendar-icon" src={calendar} alt="" />

              <input
                ref={birthDateInput}
                type="date"
                className="date-picker"
                value={birthDateValue}
                onChange={(e) => setBirthDateValue(e.target.value)}
                disabled={birthUnknown}
              />
            </div>

            <label className="radio-row">
              <input type="radio" name="birth" checked={birthUnknown} onChange={() => setBirthUnknown(true)} />
              <span>정확히 모르겠어요.</span>
            </label>
          </section>


          <section className="info-section size-section">
            <div className="info-title">
              <img className="info-leaf" src={leaf} alt="" />
              <img className="info-title-text" src={sizeWeight} alt="크기 또는 체중" />
            </div>

            <div className="size-row">
              <input type="number" className="small-input" placeholder="크기    (cm)" disabled={sizeUnknown} />

              <input type="number" className="small-input" placeholder="체중    (g)" disabled={sizeUnknown} />

              <label className="size-unknown">
                <input type="checkbox" checked={sizeUnknown} onChange={() => setSizeUnknown(!sizeUnknown)} />
                <span>잘 모르겠어요.</span>
              </label>
            </div>
          </section>


          <section className="info-section adoption-section">
            <div className="info-title">
              <img className="info-leaf" src={leaf} alt="" />
              <img className="info-title-text" src={adoptionDate} alt="입양 예정일" />
            </div>

            <div className="date-input" onClick={() => adoptionDateInput.current?.showPicker()}>
              <span className={adoptionDateValue ? 'date-value' : 'date-placeholder'}>
                {adoptionDateValue || '날짜를 선택해주세요.'}
              </span>

              <img className="calendar-icon" src={calendar} alt="" />

              <input
                ref={adoptionDateInput}
                type="date"
                className="date-picker"
                value={adoptionDateValue}
                onChange={(e) => setAdoptionDateValue(e.target.value)}
              />
            </div>
          </section>


            <button
            className="complete-button"
            onClick={() => navigate('/loading', { state: { step: step } })}
            >
            <img src={complete} alt="완료" />
            </button>

        </div>

      </div>

    </div>
  )
}

export default LizardInfo