import QuestionLayout from '../common/QuestionLayout'

import './Concern.scss'

const CONCERNS = ['온도 관리', '습도 조절', '분무 시점', '환기', '탈피', '먹이 급여', '센서 / 장비 사용', '사육 환경']
const MAX_LENGTH = 300

// 고민 선택 (복수 선택) + 자유 입력. 고민이 없으면 아무것도 안 고르고 완료해도 된다
const Concern = ({ form, updateForm, onPrev, onNext }) => {
  const { concerns, concernText } = form

  const toggleConcern = (concern) => {
    updateForm('concerns', concerns.includes(concern) ? concerns.filter((c) => c !== concern) : [...concerns, concern])
  }

  return (
    <QuestionLayout
      className="concern"
      titleLines={['요즘 어떤 점이', '고민이신가요?']}
      onBack={onPrev}
      note="고민이 없다면 선택 없이 완료 버튼을 눌러주세요."
      buttonLabel="완료"
      onSubmit={onNext}
    >
      <div className="concern-chips" role="group" aria-label="고민">
        {CONCERNS.map((concern) => (
          <button
            key={concern}
            type="button"
            className={concerns.includes(concern) ? 'concern-chip selected' : 'concern-chip'}
            aria-pressed={concerns.includes(concern)}
            onClick={() => toggleConcern(concern)}
          >
            {concern}
          </button>
        ))}
      </div>

      {/* 여러 줄 긴 글이라 입력 바 대신 칸 안에서 바로 입력 */}
      <label className="concern-memo">
        <span className="concern-memo-label">고민 내용을 자유롭게 적어주세요.</span>
        <span className="concern-memo-box">
          <textarea
            className="concern-memo-field"
            placeholder="예) 환기 빈도가 적절한지 모르겠어요.."
            maxLength={MAX_LENGTH}
            value={concernText}
            onChange={(e) => updateForm('concernText', e.target.value)}
          />
          <span className="concern-memo-count">
            {concernText.length}/{MAX_LENGTH}
          </span>
        </span>
      </label>
    </QuestionLayout>
  )
}

export default Concern
