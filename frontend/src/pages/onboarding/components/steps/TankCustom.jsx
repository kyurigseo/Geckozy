import { useState } from 'react'

import TankCustomScreen from '../common/TankCustomScreen'
import OnboardingButton from '../common/OnboardingButton'
import QuestPopup from '../common/QuestPopup'
import { TANK_REQUIRED_MESSAGE, getTankProgress, isTankComplete } from '../common/tankRules'

import geckoBadge from '../../../../assets/onboarding/badge-gecko.png'

// 온보딩 '내 사육장 등록하기'. 화면은 common/TankCustomScreen (마이 사육장 커스텀과 공통)
const TankCustom = ({ form, updateForm, onPrev, onNext, isSoundOn, onToggleSound }) => {
  const { tank, questSeen } = form
  const [notice, setNotice] = useState('')

  const update = (fields) => {
    updateForm('tank', { ...tank, ...fields })
    setNotice('')
  }

  // 커스텀 완료: 필수 항목(벽지·바닥재)을 채웠으면 다음 단계로
  const handleComplete = () => {
    if (isTankComplete(tank)) {
      setNotice('')
      onNext()
    } else {
      setNotice(TANK_REQUIRED_MESSAGE)
    }
  }

  return (
    <TankCustomScreen
      title="내 사육장 등록하기"
      onBack={onPrev}
      tank={tank}
      onChange={update}
      progress={getTankProgress(tank)}
      badge={{ image: geckoBadge, label: '도마뱀 커스텀으로 돌아가기', onClick: onPrev }}
      notice={notice}
      isSoundOn={isSoundOn}
      onToggleSound={onToggleSound}
      infoFooter={
        <div className="tank-custom-complete">
          <OnboardingButton onClick={handleComplete}>커스텀 완료!</OnboardingButton>
        </div>
      }
    >
      {!questSeen.tank && (
        <QuestPopup
          title="내 사육장을 커스텀 해주세요!"
          description="사육장의 모습과 기본 정보를 입력해주세요."
          top={362}
          onClose={() => updateForm('questSeen', { ...questSeen, tank: true })}
        />
      )}
    </TankCustomScreen>
  )
}

export default TankCustom
