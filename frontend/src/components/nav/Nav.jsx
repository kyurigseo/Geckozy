import React from 'react'
import { useNavigate, useLocation } from 'react-router-dom'

import bottomBar from '../../assets/nav/하단바.svg'
import homeIcon from '../../assets/nav/하단바_홈.svg'
import recordIcon from '../../assets/nav/하단바_기록.svg'
import guideIcon from '../../assets/nav/하단바_가이드.svg'
import myIcon from '../../assets/nav/하단바_마이.svg'

import './Nav.scss'


const Nav = () => {
  const navigate = useNavigate()
  const location = useLocation()

  return (
    <nav className="bottom-nav">
      <img className="bottom-nav-background" src={bottomBar} alt="" />

      <div className="bottom-nav-menu">

        <button className={location.pathname.startsWith('/home') ? 'bottom-nav-item active' : 'bottom-nav-item'} onClick={() => navigate('/home')}>
          <img src={homeIcon} alt="홈" />
        </button>

        <button className={location.pathname.startsWith('/record') ? 'bottom-nav-item active' : 'bottom-nav-item'} onClick={() => navigate('/record')}>
          <img src={recordIcon} alt="기록" />
        </button>

        <button className={location.pathname.startsWith('/guide') ? 'bottom-nav-item active' : 'bottom-nav-item'} onClick={() => navigate('/guide')}>
          <img src={guideIcon} alt="가이드" />
        </button>

        <button className={location.pathname.startsWith('/my') ? 'bottom-nav-item active' : 'bottom-nav-item'} onClick={() => navigate('/my')}>
          <img src={myIcon} alt="마이" />
        </button>

      </div>
    </nav>
  )
}

export default Nav