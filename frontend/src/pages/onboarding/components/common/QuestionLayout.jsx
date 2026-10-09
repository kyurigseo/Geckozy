import OnboardingHeader from './OnboardingHeader'
import OnboardingButton from './OnboardingButton'

import vineDeco from '../../../../assets/onboarding/deco-vine-small.png'
import groundDeco from '../../../../assets/onboarding/deco-ground.png'

import './QuestionLayout.scss'

// 질문 화면 공통 틀 (피그마 '관리 방식', '고민')
// 뒤로가기 + 오른쪽 위 덩굴 + 큰 제목(titleLines) / 내용 / 아래 안내 문구(note) + 버튼 + 흙바닥
const QuestionLayout = ({ className = '', titleLines, onBack, note, buttonLabel, onSubmit, children }) => {
  return (
    <section className={`question-layout ${className}`}>
      <img className="question-layout-vine" src={vineDeco} alt="" />
      <OnboardingHeader onBack={onBack} />

      <h2 className="question-layout-title">
        {titleLines.map((line) => (
          <span key={line}>{line}</span>
        ))}
      </h2>

      <div className="question-layout-body">{children}</div>

      <div className="question-layout-footer">
        {note && <p className="question-layout-note">{note}</p>}
        <div className="question-layout-button">
          <OnboardingButton onClick={onSubmit}>{buttonLabel}</OnboardingButton>
        </div>
        <img className="question-layout-ground" src={groundDeco} alt="" />
      </div>
    </section>
  )
}

export default QuestionLayout
