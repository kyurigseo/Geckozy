import OnboardingCheckboxGroup from '../common/OnboardingCheckboxGroup'
import OnboardingButton from '../common/OnboardingButton'

// TODO: 디자인 확정 후 고민 항목 채우기. 형식: [{ value, label }]
const CONCERN_OPTIONS = []

const Concern = ({ form, updateForm, onNext, onPrev }) => {
  return (
    <section className="concern">
      <OnboardingCheckboxGroup
        name="concerns"
        options={CONCERN_OPTIONS}
        values={form.concerns}
        onChange={(values) => updateForm('concerns', values)}
      />

      <OnboardingButton variant="secondary" onClick={onPrev}>이전</OnboardingButton>
      <OnboardingButton onClick={onNext}>완료</OnboardingButton>
    </section>
  )
}

export default Concern
