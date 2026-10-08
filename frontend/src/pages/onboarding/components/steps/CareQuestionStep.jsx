import OnboardingRadioGroup from '../common/OnboardingRadioGroup'
import OnboardingButton from '../common/OnboardingButton'

// TODO: 디자인 확정 후 질문 채우기
// 형식: { key, title, icon, options: [{ value, label }] }
// 사용 가능 아이콘: icon-water, icon-temperature, icon-calendar, icon-clock
const CARE_QUESTIONS = []

const CareQuestionStep = ({ form, updateForm, onNext, onPrev }) => {
  const { care } = form

  const isComplete = CARE_QUESTIONS.every((question) => care[question.key])

  return (
    <section className="care-question-step">
      {CARE_QUESTIONS.map((question) => (
        <div key={question.key} className="care-question-step-question">
          {question.icon && <img src={question.icon} alt="" />}
          <span className="care-question-step-title">{question.title}</span>
          <OnboardingRadioGroup
            name={question.key}
            options={question.options}
            value={care[question.key] ?? ''}
            onChange={(value) => updateForm('care', { ...care, [question.key]: value })}
          />
        </div>
      ))}

      <OnboardingButton variant="secondary" onClick={onPrev}>이전</OnboardingButton>
      <OnboardingButton disabled={!isComplete} onClick={onNext}>다음</OnboardingButton>
    </section>
  )
}

export default CareQuestionStep
