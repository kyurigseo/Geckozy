import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { getMyProfile } from '../../api/my'

import geckoBeige from '../../assets/onboarding/gecko-beige.png'
import geckoOrange from '../../assets/onboarding/gecko-orange.png'
import geckoYellow from '../../assets/onboarding/gecko-yellow.png'
import geckoGreen from '../../assets/onboarding/gecko-green.png'
import geckoBlue from '../../assets/onboarding/gecko-blue.png'
import leafIcon from '../../assets/onboarding/icon-leaf.png'
import geckoIcon from '../../assets/my/icon-gecko.png'
import tankIcon from '../../assets/my/icon-tank.png'
import documentIcon from '../../assets/my/icon-document.png'
import sensorIcon from '../../assets/my/icon-sensor.png'
import infoIcon from '../../assets/my/icon-info.png'
import logoutIcon from '../../assets/my/icon-logout.png'
import deleteIcon from '../../assets/my/icon-delete.png'
import chevronIcon from '../../assets/my/icon-chevron-right.svg'

import SaveConfirmPopup from './components/common/SaveConfirmPopup'

import './MyPage.scss'

const GECKO_IMAGES = {
  beige: geckoBeige,
  orange: geckoOrange,
  yellow: geckoYellow,
  green: geckoGreen,
  blue: geckoBlue
}

const MENU_SECTIONS = [
  {
    key: 'manage',
    title: '관리',
    items: [
      { key: 'gecko-custom', label: '도마뱀 커스텀', icon: [geckoIcon, 39, 29], path: '/my/gecko-custom' },
      { key: 'tank-custom', label: '사육장 커스텀', icon: [tankIcon, 34, 29], path: '/my/tank-custom' },
      { key: 'gecko-manage', label: '도마뱀 관리', icon: [documentIcon, 25, 27], path: '/my/lizard-manage' },
      { key: 'scrap', label: '스크랩', icon: [leafIcon, 29, 20], path: '/my/scrap' }
    ]
  },
  {
    key: 'sensor',
    title: '센서',
    items: [
      { key: 'sensor', label: '연결된 센서', icon: [sensorIcon, 34, 32], path: '/my/sensor' }
    ]
  },
  {
    key: 'setting',
    title: '설정',
    items: [
      { key: 'version', label: '버전 정보', icon: [infoIcon, 26, 26], value: 'v1.0.0' },
      { key: 'logout', label: '로그아웃', icon: [logoutIcon, 27, 25] },
      { key: 'delete-account', label: '계정 삭제', icon: [deleteIcon, 26, 25] }
    ]
  }
]

const MyPage = () => {
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)
  const [confirmType, setConfirmType] = useState(null)

  useEffect(() => {
    let isCancelled = false

    getMyProfile()
      .then((data) => {
        if (!isCancelled) setProfile(data)
      })
      .catch(() => {
        if (!isCancelled) setProfile(null)
      })

    return () => {
      isCancelled = true
    }
  }, [])

  const handleMenuClick = (item) => {
    if (item.key === 'logout' || item.key === 'delete-account') {
      setConfirmType(item.key)
      return
    }

    if (item.path) navigate(item.path)
  }

  const handleConfirm = () => {
    if (confirmType === 'logout') {
      setConfirmType(null)
      navigate('/onboarding')
      return
    }

    setConfirmType(null)
  }

  const confirmTitle = confirmType === 'logout'
    ? '로그아웃 하시겠습니까?'
    : '계정을 삭제하시겠습니까?'

  return (
    <section className="my-page">
      <div className="my-page-profile">
        <div className="my-page-avatar">
          <span className="my-page-avatar-circle">
            {profile && <img src={GECKO_IMAGES[profile.geckoColor] ?? geckoBeige} alt="" />}
          </span>
          <img className="my-page-avatar-leaf" src={leafIcon} alt="" />
        </div>
        <p className="my-page-name">{profile?.name}</p>
        <p className="my-page-info">{profile && `${profile.species} • ${profile.tankName}`}</p>
      </div>

      {MENU_SECTIONS.map((section) => (
        <section key={section.key} className="my-page-section">
          <h2 className="my-page-section-title">{section.title}</h2>
          <ul className="my-page-menu">
            {section.items.map((item) => {
              const [icon, width, height] = item.icon
              const isStatic = Boolean(item.value)
              const Tag = isStatic ? 'div' : 'button'

              return (
                <li key={item.key}>
                  <Tag
                    {...(isStatic ? {} : { type: 'button', onClick: () => handleMenuClick(item) })}
                    className="my-page-menu-item"
                  >
                    <span className="my-page-menu-icon">
                      <img src={icon} alt="" style={{ width, height }} />
                    </span>
                    <span className="my-page-menu-label">{item.label}</span>
                    {isStatic ? (
                      <span className="my-page-menu-value">{item.value}</span>
                    ) : (
                      <img className="my-page-menu-chevron" src={chevronIcon} alt="" />
                    )}
                  </Tag>
                </li>
              )
            })}
          </ul>
        </section>
      ))}

      {confirmType && (
        <SaveConfirmPopup
          title={confirmType === 'logout' ? '로그아웃 하시겠습니까?' : '계정을 삭제하시겠습니까?'}
          description={confirmType === 'delete-account' ? '삭제된 데이터는 복구가 불가능합니다.' : ''}
          onConfirm={handleConfirm}
          onDiscard={() => setConfirmType(null)}
          onClose={() => setConfirmType(null)}
        />
      )}
    </section>
  )
}

export default MyPage
