import './ChoiceCard.scss'

// 선택 카드 (피그마 109x149 카드 + '선택'/'선택 해제' 버튼). children: 55x55 동그란 썸네일 안에 들어갈 내용
// 여러 장은 <ul className="choice-card-grid">로 감싼다
const ChoiceCard = ({ label, selected, onToggle, children }) => {
  return (
    <li className="choice-card">
      <span className="choice-card-thumb">{children}</span>
      <button
        type="button"
        className={selected ? 'choice-card-select selected' : 'choice-card-select'}
        aria-pressed={selected}
        aria-label={`${label} ${selected ? '선택 해제' : '선택'}`}
        onClick={onToggle}
      >
        {selected ? '선택 해제' : '선택'}
      </button>
    </li>
  )
}

export default ChoiceCard
