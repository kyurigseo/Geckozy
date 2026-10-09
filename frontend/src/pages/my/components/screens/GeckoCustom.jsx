import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import GeckoCustomScreen from '../../../onboarding/components/common/GeckoCustomScreen'
import { GECKO_REQUIRED_MESSAGE, isGeckoComplete } from '../../../onboarding/components/common/geckoRules'
import SaveConfirmPopup from '../common/SaveConfirmPopup'
import { getMyGecko, updateMyGecko } from '../../../../api/my'

import './MyCustomScreen.scss'

// 마이 > 도마뱀 커스텀 (등록한 도마뱀의 색·기본정보 수정). 화면은 온보딩과 같은 GeckoCustomScreen
// 뒤로가기: 바꾼 게 있으면 저장 확인 팝업, 없으면 바로 마이로
const GeckoCustom = () => {
  const navigate = useNavigate()
  const [original, setOriginal] = useState(null) // 불러온 값 (바뀌었는지 비교용)
  const [gecko, setGecko] = useState(null)
  const [isPopupOpen, setIsPopupOpen] = useState(false)
  const [notice, setNotice] = useState('')

  useEffect(() => {
    let isCancelled = false
    getMyGecko().then((data) => {
      if (isCancelled) return
      setOriginal(data)
      setGecko(data)
    })
    return () => {
      isCancelled = true
    }
  }, [])

  const isChanged = JSON.stringify(gecko) !== JSON.stringify(original)
  const goBackToMy = () => navigate('/my')

  const handleChange = (fields) => {
    setGecko((prev) => ({ ...prev, ...fields }))
    setNotice('')
  }

  const handleBack = () => {
    if (isChanged) setIsPopupOpen(true)
    else goBackToMy()
  }

  const handleSave = async () => {
    if (!isGeckoComplete(gecko)) {
      setIsPopupOpen(false)
      setNotice(GECKO_REQUIRED_MESSAGE)
      return
    }
    // TODO: 저장 실패 시 처리
    await updateMyGecko(gecko)
    goBackToMy()
  }

  if (!gecko) return <div className="my-custom" />

  return (
    <div className="my-custom">
      <GeckoCustomScreen title="도마뱀 커스텀" onBack={handleBack} gecko={gecko} onChange={handleChange} notice={notice}>
        {isPopupOpen && <SaveConfirmPopup onConfirm={handleSave} onDiscard={goBackToMy} onClose={() => setIsPopupOpen(false)} />}
      </GeckoCustomScreen>
    </div>
  )
}

export default GeckoCustom
