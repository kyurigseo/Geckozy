import OnboardingRadioGroup from '../common/OnboardingRadioGroup'
import OnboardingInput from '../common/OnboardingInput'
import OnboardingButton from '../common/OnboardingButton'

import geckoBeige from '../../../../assets/onboarding/gecko-beige.png'
import geckoOrange from '../../../../assets/onboarding/gecko-orange.png'
import geckoYellow from '../../../../assets/onboarding/gecko-yellow.png'
import geckoGreen from '../../../../assets/onboarding/gecko-green.png'
import geckoBlue from '../../../../assets/onboarding/gecko-blue.png'
import swatchBeige from '../../../../assets/onboarding/swatch-beige.png'
import swatchOrange from '../../../../assets/onboarding/swatch-orange.png'
import swatchYellow from '../../../../assets/onboarding/swatch-yellow.png'
import swatchGreen from '../../../../assets/onboarding/swatch-green.png'
import swatchBlue from '../../../../assets/onboarding/swatch-blue.png'

const GECKO_COLORS = [
  { value: 'beige', swatch: swatchBeige, gecko: geckoBeige },
  { value: 'orange', swatch: swatchOrange, gecko: geckoOrange },
  { value: 'yellow', swatch: swatchYellow, gecko: geckoYellow },
  { value: 'green', swatch: swatchGreen, gecko: geckoGreen },
  { value: 'blue', swatch: swatchBlue, gecko: geckoBlue },
]

const GeckoCustom = ({ form, updateForm, onNext, onPrev }) => {
  const { gecko } = form
  const selected = GECKO_COLORS.find((color) => color.value === gecko.color)

  const handleChange = (field) => (value) => updateForm('gecko', { ...gecko, [field]: value })

  return (
    <section className="gecko-custom">
      <div className="gecko-custom-preview">
        {selected && <img src={selected.gecko} alt="선택한 도마뱀" />}
      </div>

      <OnboardingRadioGroup
        name="geckoColor"
        options={GECKO_COLORS.map(({ value, swatch }) => ({ value, image: swatch }))}
        value={gecko.color}
        onChange={handleChange('color')}
      />

      <OnboardingInput label="이름" name="geckoName" value={gecko.name} onChange={handleChange('name')} />

      <OnboardingButton variant="secondary" onClick={onPrev}>이전</OnboardingButton>
      <OnboardingButton disabled={!gecko.color || !gecko.name} onClick={onNext}>다음</OnboardingButton>
    </section>
  )
}

export default GeckoCustom
