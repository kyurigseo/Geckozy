import { getSpecies, getSpeciesGuide, getGuideProgress, getGuideStep, loadGuideAccountContext, saveGuideContext } from '../../api/guide'
import { useEffect, useRef, useState } from 'react'
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
import { useSound } from '../../sound/SoundProvider'

const GUIDE_SPECIES_NAMES = [
  '크레스티드 게코',
  '레오파드 게코',
  '비어디드 드래곤',
]

const LizardInfo = ({ step, onClose }) => {
  const navigate = useNavigate()
  const { playSfx } = useSound()
  const [birthUnknown, setBirthUnknown] = useState(false)
  const [birthDateValue, setBirthDateValue] = useState('')
  const [sizeUnknown, setSizeUnknown] = useState(false)
  const [adoptionDateValue, setAdoptionDateValue] = useState('')

  const [species, setSpecies] = useState([])
  const [speciesId, setSpeciesId] = useState('')
  const [sizeCm, setSizeCm] = useState('')
  const [weightG, setWeightG] = useState('')
  const submitting = useRef(false)

  useEffect(() => {
    const controller = new AbortController()

    getSpecies(controller.signal)
      .then((items) => {
        if (controller.signal.aborted) return

        setSpecies(
          GUIDE_SPECIES_NAMES.map((name) =>
            items.find((item) => item.name === name),
          ).filter(Boolean),
        )
      })
      .catch(() => {})

    loadGuideAccountContext(controller.signal)
      .then(({ lizard }) => {
        if (controller.signal.aborted || !lizard) return

        setSpeciesId(String(lizard.speciesId))
        setBirthUnknown(Boolean(lizard.birthDateUnknown))
        setBirthDateValue(
          lizard.birthDateUnknown ? '' : lizard.birthDate ?? '',
        )
        setSizeUnknown(Boolean(lizard.sizeWeightUnknown))
        setSizeCm(
          lizard.sizeWeightUnknown
            ? ''
            : String(lizard.sizeCm ?? ''),
        )
        setWeightG(
          lizard.sizeWeightUnknown
            ? ''
            : String(lizard.weightG ?? ''),
        )
      })
      .catch((error) => {
        if (!controller.signal.aborted) {
          window.alert(error.message)
        }
      })

    return () => controller.abort()
  }, [])

  const handleComplete = async () => {
    if (submitting.current) return

    playSfx('button')

    if (!speciesId) {
      window.alert('도마뱀 종을 선택해주세요.')
      return
    }

    const selectedSpecies = species.find(
      (item) =>
        String(item.speciesId ?? item.name) === speciesId,
    )

    if (!selectedSpecies) {
      window.alert('도마뱀 종을 다시 선택해주세요.')
      return
    }

    const isCrested =
      selectedSpecies.name.replace(/\s/g, '') ===
      '크레스티드게코'

    const lizardDraft = {
      speciesId: selectedSpecies.speciesId ?? null,
      speciesName: selectedSpecies.name,
      birthDate: birthUnknown
        ? null
        : birthDateValue || null,
      birthDateUnknown: birthUnknown,
      sizeCm:
        sizeUnknown || !sizeCm
          ? null
          : Number(sizeCm),
      weightG:
        sizeUnknown || !weightG
          ? null
          : Number(weightG),
      sizeWeightUnknown: sizeUnknown,
      adoptionDate: adoptionDateValue || null,
    }

    submitting.current = true

    try {
      if (!selectedSpecies.speciesId) {
        throw new Error('종 정보를 서버에서 불러오지 못했습니다.')
      }

      const guide = await getSpeciesGuide(
        selectedSpecies.speciesId,
      )

      if (!guide?.preGuideId) {
        throw new Error('가이드 정보를 불러오지 못했습니다.')
      }

      saveGuideContext({
        speciesId: selectedSpecies.speciesId,
        preGuideId: guide.preGuideId,
        guideMode: 'api',
      })

      await getGuideProgress()
      await getGuideStep(step)

      saveGuideContext({ lizardDraft })

      navigate('/loading', { state: { step } })
    } catch (error) {
      if (isCrested) {
        // 크레스티드 게코는 기존 하드코딩 화면으로 이동합니다.
        // 이전에 선택한 다른 종의 가이드 ID는 사용하지 않습니다.
        saveGuideContext({
          speciesId: selectedSpecies.speciesId ?? null,
          preGuideId: null,
          guideMode: 'static',
          lizardDraft,
        })

        navigate('/loading', { state: { step } })
        return
      }

      window.alert(error.message)
    } finally {
      submitting.current = false
    }
  }

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
              <select
                className="info-select"
                value={speciesId}
                onChange={(e) => setSpeciesId(e.target.value)}
              >
                <option value="" disabled>
                  해당하는 종을 선택해주세요.
                </option>

                {species
                  .filter(
                    (item, index, items) =>
                      items.findIndex(
                        (other) =>
                          other.name.replace(/\s/g, '') ===
                          item.name.replace(/\s/g, ''),
                      ) === index,
                  )
                  .map((item) => (
                    <option
                      key={item.speciesId ?? item.name}
                      value={item.speciesId ?? item.name}
                    >
                      {item.name}
                    </option>
                  ))}
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
              <input type="number" className="small-input" placeholder="크기    (cm)" value={sizeCm} onChange={(e) => setSizeCm(e.target.value)} disabled={sizeUnknown} />

              <input type="number" className="small-input" placeholder="체중    (g)" value={weightG} onChange={(e) => setWeightG(e.target.value)} disabled={sizeUnknown} />

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
            onClick={handleComplete}
            >
            <img src={complete} alt="완료" />
            </button>

        </div>

      </div>

    </div>
  )
}

export default LizardInfo
