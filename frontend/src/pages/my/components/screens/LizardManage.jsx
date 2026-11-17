import {
  useEffect,
  useRef,
  useState,
} from 'react'

import { useNavigate } from 'react-router-dom'

import {
  getManagedLizards,
  deleteManagedLizard,
  selectManagedLizard,
} from '../../../../api/myManage'

import geckoBeige from '../../../../assets/onboarding/gecko-beige.png'
import geckoOrange from '../../../../assets/onboarding/gecko-orange.png'
import geckoYellow from '../../../../assets/onboarding/gecko-yellow.png'
import geckoGreen from '../../../../assets/onboarding/gecko-green.png'
import geckoBlue from '../../../../assets/onboarding/gecko-blue.png'

import deleteButton from '../../../../assets/my/영구삭제.svg'
import back from '../../../../assets/my/뒤로가기.svg'
import leafIcon from '../../../../assets/my/나뭇잎.svg'

import SaveConfirmPopup from '../common/SaveConfirmPopup'
import { useSound } from '../../../../sound/SoundProvider'

import './LizardManage.scss'

const GECKO_IMAGES = {
  beige: geckoBeige,
  orange: geckoOrange,
  yellow: geckoYellow,
  green: geckoGreen,
  blue: geckoBlue,
}

const LizardManage = () => {
  const navigate = useNavigate()
  const { playSfx } = useSound()

  const [lizards, setLizards] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [error, setError] = useState('')

  const deleting = useRef(false)
  const selecting = useRef(false)

  useEffect(() => {
    const controller = new AbortController()

    getManagedLizards(controller.signal)
      .then((items) => {
        if (!controller.signal.aborted) {
          setLizards(items)
        }
      })
      .catch((error) => {
        if (!controller.signal.aborted) {
          setError(error.message)
        }
      })

    return () => controller.abort()
  }, [])

  const handleSelect = async (lizard) => {
    if (
      selecting.current ||
      deleting.current ||
      deleteTarget !== null ||
      lizard.selected
    ) {
      return
    }

    selecting.current = true
    playSfx('button')

    try {
      await selectManagedLizard(lizard.id)

      setLizards((prev) =>
        prev.map((item) => ({
          ...item,
          selected:
            String(item.id) === String(lizard.id),
        })),
      )

      setError('')
    } catch (error) {
      setError(error.message)
    } finally {
      selecting.current = false
    }
  }

  const handleDelete = (id) => {
    if (selecting.current || deleting.current) {
      return
    }

    playSfx('button')
    setDeleteTarget(id)
  }

  const confirmDelete = async () => {
    if (deleting.current) return

    deleting.current = true
    playSfx('button')

    try {
      await deleteManagedLizard(deleteTarget)

      setLizards((prev) =>
        prev.filter(
          (item) => item.id !== deleteTarget,
        ),
      )

      setDeleteTarget(null)

      const next = await getManagedLizards()

      setLizards(next)
      setError('')
    } catch (error) {
      setError(error.message)
    } finally {
      deleting.current = false
    }
  }

  return (
    <div className="lizard-manage">
      <header className="lizard-manage-header">
        <button
          className="lizard-manage-back"
          onClick={() => navigate('/my')}
        >
          <img src={back} alt="뒤로가기" />
        </button>

        <h1>도마뱀 관리</h1>
      </header>

      <div className="lizard-manage-list">
        {error && (
          <p
            className="lizard-manage-empty"
            role="alert"
          >
            {error}
          </p>
        )}

        {!lizards && !error && (
          <p className="lizard-manage-empty">
            불러오는 중이에요.
          </p>
        )}

        {(lizards || []).map((lizard) => (
          <article
            className={`lizard-manage-card ${
              lizard.selected ? 'selected' : ''
            }`}
            key={lizard.id}
            onClick={() => handleSelect(lizard)}
          >
            <div className="lizard-manage-info">
              <div className="lizard-manage-avatar">
                <img
                  src={GECKO_IMAGES[lizard.color]}
                  alt={lizard.name}
                />
              </div>

              <img
                className="lizard-manage-leaf"
                src={leafIcon}
                alt=""
              />

              <div className="lizard-manage-details">
                <div className="lizard-manage-name-row">
                  <strong>{lizard.name}</strong>

                  {lizard.selected && (
                    <span className="lizard-manage-selected">
                      현재 선택됨
                    </span>
                  )}
                </div>

                <p>
                  {lizard.species} · {lizard.tankName}
                </p>
              </div>
            </div>

            <button
              className="lizard-manage-delete"
              onClick={(event) => {
                event.stopPropagation()
                handleDelete(lizard.id)
              }}
            >
              <img
                src={deleteButton}
                alt={`${lizard.name} 영구 삭제`}
              />
            </button>
          </article>
        ))}

        {lizards?.length === 0 && !error && (
          <p className="lizard-manage-empty">
            등록된 도마뱀이 없어요.
          </p>
        )}
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