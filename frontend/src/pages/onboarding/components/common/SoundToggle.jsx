import soundOnIcon from '../../../../assets/onboarding/icon-sound-on.png'
import soundOffIcon from '../../../../assets/onboarding/icon-sound-off.png'

import './SoundToggle.scss'

// 배경음악 켜기/끄기 버튼. 상태는 SoundProvider(useSound의 isSoundOn·toggleSound)에서 관리
// size: 'large'(52x56, 진입화면·도마뱀 커스텀) | 'small'(38x41, 사육장 커스텀)
const SoundToggle = ({ isOn, onToggle, size = 'large' }) => {
  return (
    <button
      type="button"
      className={`sound-toggle sound-toggle-${size}`}
      aria-label={isOn ? '배경음악 끄기' : '배경음악 켜기'}
      onClick={onToggle}
    >
      <img src={isOn ? soundOnIcon : soundOffIcon} alt="" />
    </button>
  )
}

export default SoundToggle
