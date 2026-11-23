import radioOffIcon from '../../../../assets/onboarding/icon-radio-off.svg'
import radioOnIcon from '../../../../assets/onboarding/icon-radio-on.svg'

import './RadioOption.scss'

// 동그라미 선택 항목 (피그마 '선택' 22x22 + 문구). role: 'radio'(여럿 중 하나) | 'checkbox'(켜고 끄기)
// size: 's'(14px, 도마뱀 기본정보) | 'm'(16px, 사육장 기본정보)
const RadioOption = ({ label, checked, role = 'radio', size = 's', onClick }) => {
  return (
    <button type="button" className={`radio-option radio-option-${size}`} role={role} aria-checked={checked} onClick={onClick}>
      <img src={checked ? radioOnIcon : radioOffIcon} alt="" />
      <span>{label}</span>
    </button>
  )
}

export default RadioOption
