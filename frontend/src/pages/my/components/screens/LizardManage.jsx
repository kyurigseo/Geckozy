import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import geckoBeige from '../../../../assets/onboarding/gecko-beige.png'
import geckoOrange from '../../../../assets/onboarding/gecko-orange.png'
import geckoYellow from '../../../../assets/onboarding/gecko-yellow.png'
import geckoGreen from '../../../../assets/onboarding/gecko-green.png'
import geckoBlue from '../../../../assets/onboarding/gecko-blue.png'
import deleteButton from '../../../../assets/my/영구삭제.svg'
import back from '../../../../assets/my/뒤로가기.svg'
import leafIcon from '../../../../assets/my/나뭇잎.svg'

import SaveConfirmPopup from '../common/SaveConfirmPopup'

import './LizardManage.scss'

const GECKO_IMAGES = {
  beige: geckoBeige,
  orange: geckoOrange,
  yellow: geckoYellow,
  green: geckoGreen,
  blue: geckoBlue
}

const INITIAL_LIZARDS = [
  { id: 1, name: '레오', color: 'orange', species: '크레스티드 게코', tankName: '1번 사육장', selected: true },
  { id: 2, name: '대박이', color: 'green', species: '크라운드 게코', tankName: '2번 사육장', selected: false },
  { id: 3, name: '모찌', color: 'blue', species: '크라운드 게코', tankName: '3번 사육장', selected: false }
]

const LizardManage = () => {
  const navigate = useNavigate()
  const [lizards, setLizards] = useState(INITIAL_LIZARDS)
  const [deleteTarget, setDeleteTarget] = useState(null)

  const handleDelete = (id) => {
    setDeleteTarget(id)
  }

  const confirmDelete = () => {
    setLizards((prev) => prev.filter((lizard) => lizard.id !== deleteTarget))
    setDeleteTarget(null)
  }

  return (
    <div className="lizard-manage">
      <header className="lizard-manage-header">
        <button className="lizard-manage-back" onClick={() => navigate('/my')}>
          <img src={back} alt="뒤로가기" />
        </button>
        <h1>도마뱀 관리</h1>
      </header>

      <div className="lizard-manage-list">
        {lizards.map((lizard) => (
          <article className={`lizard-manage-card ${lizard.selected ? 'selected' : ''}`} key={lizard.id}>
            <div className="lizard-manage-info">
              <div className="lizard-manage-avatar">
                <img src={GECKO_IMAGES[lizard.color]} alt={lizard.name} />
              </div>
              <img className="lizard-manage-leaf" src={leafIcon} alt="" />

              <div className="lizard-manage-details">
                <div className="lizard-manage-name-row">
                  <strong>{lizard.name}</strong>
                  {lizard.selected && <span className="lizard-manage-selected">현재 선택됨</span>}
                </div>
                <p>{lizard.species} · {lizard.tankName}</p>
              </div>
            </div>

            <button className="lizard-manage-delete" onClick={() => handleDelete(lizard.id)}>
              <img src={deleteButton} alt={`${lizard.name} 영구 삭제`} />
            </button>
          </article>
        ))}

        {lizards.length === 0 && <p className="lizard-manage-empty">등록된 도마뱀이 없어요.</p>}
      </div>

      {deleteTarget !== null && (
        <SaveConfirmPopup
          title="도마뱀을 영구 삭제 하시겠습니까?"
          
          onConfirm={confirmDelete}
          onDiscard={() => setDeleteTarget(null)}
          onClose={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}

export default LizardManage
