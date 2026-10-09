import { useNavigate } from 'react-router-dom'

import OnboardingHeader from '../common/OnboardingHeader'
import SoundToggle from '../common/SoundToggle'

import vineDeco from '../../../../assets/onboarding/deco-vine-wide.png'
import groundDeco from '../../../../assets/onboarding/deco-ground.png'
import geckoHeadIcon from '../../../../assets/onboarding/icon-gecko-head.png'
import sproutIcon from '../../../../assets/onboarding/icon-sprout.png'
import arrowIcon from '../../../../assets/onboarding/icon-arrow-bold.svg'

import './StartMethod.scss'

// 피그마 '현재 상황' 선택 카드. description은 피그마 줄바꿈 그대로 배열로 둔다
const START_METHOD_OPTIONS = [
  {
    value: 'owner',
    title: '도마뱀을 키우고 있어요',
    description: ['도마뱀과 사육장을 등록하고', '관리를 시작할게요!'],
    icon: geckoHeadIcon,
    color: 'yellow',
  },
  {
    value: 'preparing',
    title: '아직 준비 중이에요',
    description: ['먼저 서비스를 둘러보고,', '입양 후 언제든지 등록할 수 있어요!'],
    icon: sproutIcon,
    color: 'green',
  },
]

const StartMethod = ({ updateForm, onPrev, goTo, isSoundOn, onToggleSound }) => {
  const navigate = useNavigate()

  const handleSelect = (value) => {
    updateForm('startMethod', value)

    if (value === 'owner') {
      goTo('gecko-custom')
    } else {
      // TODO: 온보딩 완료(둘러보기) 상태를 API에 저장
      navigate('/home')
    }
  }

  return (
    <section className="start-method">
      <img className="start-method-vine" src={vineDeco} alt="" />

      <OnboardingHeader title="현재 상황" onBack={onPrev} />

      <p className="start-method-guide">
        지금 나에게 맞는
        <br />
        상황을 선택해주세요!
      </p>

      <div className="start-method-options">
        {START_METHOD_OPTIONS.map((option) => (
          <button
            key={option.value}
            type="button"
            className={`start-method-card start-method-card-${option.color}`}
            onClick={() => handleSelect(option.value)}
          >
            <span className="start-method-card-icon">
              <img src={option.icon} alt="" />
            </span>
            <span className="start-method-card-text">
              <span className="start-method-card-title">{option.title}</span>
              <span className="start-method-card-description">
                {option.description.map((line) => (
                  <span key={line}>{line}</span>
                ))}
              </span>
            </span>
            <span className="start-method-card-arrow">
              <img src={arrowIcon} alt="" />
            </span>
          </button>
        ))}
      </div>

      <div className="start-method-bottom">
        <img className="start-method-ground" src={groundDeco} alt="" />
        <div className="start-method-sound">
          <SoundToggle isOn={isSoundOn} onToggle={onToggleSound} />
        </div>
      </div>
    </section>
  )
}

export default StartMethod
