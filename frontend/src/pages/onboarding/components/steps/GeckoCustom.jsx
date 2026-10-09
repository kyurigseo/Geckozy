import { useState } from 'react'

import GeckoCustomScreen from '../common/GeckoCustomScreen'
import QuestPopup from '../common/QuestPopup'
import { GECKO_REQUIRED_MESSAGE, getGeckoProgress, isGeckoComplete } from '../common/geckoRules'

import tankBadge from '../../../../assets/onboarding/badge-tank.png'

// 온보딩 '내 도마뱀 등록하기'. 화면은 common/GeckoCustomScreen (마이 도마뱀 커스텀과 공통)
const GeckoCustom = ({ form, updateForm, onPrev, onNext, isSoundOn, onToggleSound }) => {
  const { gecko, questSeen } = form
  const [notice, setNotice] = useState('')

  const update = (fields) => {
    updateForm('gecko', { ...gecko, ...fields })
    setNotice('')
  }

  // 사육장 배지: 필수 항목(색상·이름·종·성별)을 채웠으면 다음 단계로
  const handleNext = () => {
    if (isGeckoComplete(gecko)) {
      setNotice('')
      onNext()
    } else {
      setNotice(GECKO_REQUIRED_MESSAGE)
    }
  }

  return (
    <GeckoCustomScreen
      title="내 도마뱀 등록하기"
      onBack={onPrev}
      gecko={gecko}
      onChange={update}
      progress={getGeckoProgress(gecko)}
      badge={{ image: tankBadge, label: '사육장 꾸미기로 넘어가기', onClick: handleNext }}
      notice={notice}
      isSoundOn={isSoundOn}
      onToggleSound={onToggleSound}
    >
      {!questSeen.gecko && (
        <QuestPopup
          title="내 도마뱀의 외형을 커스텀 해주세요!"
          description="도마뱀의 색과 기본 정보를 입력해주세요."
          onClose={() => updateForm('questSeen', { ...questSeen, gecko: true })}
        />
      )}
    </GeckoCustomScreen>
  )
}

export default GeckoCustom
