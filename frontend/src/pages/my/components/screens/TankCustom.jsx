import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import TankCustomScreen from '../../../onboarding/components/common/TankCustomScreen'
import { TANK_REQUIRED_MESSAGE, isTankComplete } from '../../../onboarding/components/common/tankRules'
import SaveConfirmPopup from '../common/SaveConfirmPopup'
import { getMyTank, updateMyTank } from '../../../../api/my'

import './MyCustomScreen.scss'

// 마이 > 사육장 커스텀 (등록한 사육장의 꾸미기·기본정보 수정). 화면은 온보딩과 같은 TankCustomScreen
// 온보딩과 다른 점: 진행바·배지·스피커·'커스텀 완료!' 버튼 없음, 무대가 위로 올라감(compact)
// 뒤로가기: 바꾼 게 있으면 저장 확인 팝업, 없으면 바로 마이로
const TankCustom = () => {
  const navigate = useNavigate()
  const [original, setOriginal] = useState(null) // 불러온 값 (바뀌었는지 비교용)
  const [tank, setTank] = useState(null)
  const [isPopupOpen, setIsPopupOpen] = useState(false)
  const [notice, setNotice] = useState('')

  useEffect(() => {
    let isCancelled = false
    getMyTank().then((data) => {
      if (isCancelled) return
      setOriginal(data)
      setTank(data)
    })
    return () => {
      isCancelled = true
    }
  }, [])

  const isChanged = JSON.stringify(tank) !== JSON.stringify(original)
  const goBackToMy = () => navigate('/my')

  const handleChange = (fields) => {
    setTank((prev) => ({ ...prev, ...fields }))
    setNotice('')
  }

  const handleBack = () => {
    if (isChanged) setIsPopupOpen(true)
    else goBackToMy()
  }

  const handleSave = async () => {
    if (!isTankComplete(tank)) {
      setIsPopupOpen(false)
      setNotice(TANK_REQUIRED_MESSAGE)
      return
    }
    // TODO: 저장 실패 시 처리
    await updateMyTank(tank)
    goBackToMy()
  }

  if (!tank) return <div className="my-custom" />

  return (
    <div className="my-custom">
      <TankCustomScreen compact title="사육장 커스텀" onBack={handleBack} tank={tank} onChange={handleChange} notice={notice}>
        {isPopupOpen && <SaveConfirmPopup onConfirm={handleSave} onDiscard={goBackToMy} onClose={() => setIsPopupOpen(false)} />}
      </TankCustomScreen>
    </div>
  )
}

export default TankCustom
