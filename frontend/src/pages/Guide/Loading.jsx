import React, { useEffect } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'

import background from '../../assets/guide/Loading.svg'
import loadingText from '../../assets/guide/Loading....svg'
import loadinglizard from '../../assets/guide/로딩도마뱀.svg'
import loadingStone from '../../assets/guide/로딩중 돌.svg'
import loadingStoneSelected from '../../assets/guide/로딩중 돌 선택.svg'
import backgroundMusic from '../../assets/guide/배경음악.svg'

import './Loading.scss'

const Loading = () => {
  const navigate = useNavigate()
  const location = useLocation()

  const step = location.state?.step || 1

  useEffect(() => {
    const timer = setTimeout(() => {
      navigate(`/guide/step${step}`)
    }, 2000)

    return () => clearTimeout(timer)
  }, [navigate, step])

  return (
    <div className="loading">

      <img className="loading-background" src={background} alt="" />
      <img className="loading-lizard" src={loadinglizard} alt="" />

      <div className="loading-content">

        <img className="loading-text" src={loadingText} alt="Loading..." />

        <div className="loading-stones">

          <div className="loading-stone">
            <img src={loadingStone} alt="" />
            <img className="selected" src={loadingStoneSelected} alt="" />
          </div>

          <div className="loading-stone">
            <img src={loadingStone} alt="" />
            <img className="selected" src={loadingStoneSelected} alt="" />
          </div>

          <div className="loading-stone">
            <img src={loadingStone} alt="" />
            <img className="selected" src={loadingStoneSelected} alt="" />
          </div>

        </div>

        <img className="backgroundMusic" src={backgroundMusic} alt="" />

      </div>

    </div>
  )
}

export default Loading