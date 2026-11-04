import './PickerField.scss'

// 눌러서 시트를 여는 선택 칸 (피그마 '조명사용시간', '히팅장비사용시간' 컴포넌트: Default / filled)
// value가 있으면 굵게, 없으면 placeholder. icon: 오른쪽 아이콘
const PickerField = ({ value, placeholder, icon, ariaLabel, onClick }) => {
  return (
    <button type="button" className={value ? 'picker-field is-filled' : 'picker-field'} aria-label={ariaLabel} onClick={onClick}>
      <span className="picker-field-text">{value || placeholder}</span>
      {icon && <img className="picker-field-icon" src={icon} alt="" />}
    </button>
  )
}

export default PickerField
