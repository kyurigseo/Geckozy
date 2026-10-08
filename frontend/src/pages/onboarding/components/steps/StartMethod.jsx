import OnboardingRadioGroup from '../common/OnboardingRadioGroup'
import OnboardingButton from '../common/OnboardingButton'

import listIcon from '../../../../assets/onboarding/icon-list.png'
import paletteIcon from '../../../../assets/onboarding/icon-palette.png'

// TODO: 디자인 확정 후 문구 수정
const START_METHOD_OPTIONS = [
  { value: 'recommend', label: '추천받기', image: listIcon },
  { value: 'custom', label: '직접 꾸미기', image: paletteIcon },
]

const StartMethod = ({ form, updateForm, onNext, onPrev }) => {
  return (
    <section className="start-method">
      <OnboardingRadioGroup
        name="startMethod"
        options={START_METHOD_OPTIONS}
        value={form.startMethod}
        onChange={(value) => updateForm('startMethod', value)}
      />

      <OnboardingButton variant="secondary" onClick={onPrev}>이전</OnboardingButton>
      <OnboardingButton disabled={!form.startMethod} onClick={onNext}>다음</OnboardingButton>
    </section>
  )
}

export default StartMethod
