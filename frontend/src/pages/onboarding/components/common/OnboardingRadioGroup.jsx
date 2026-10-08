// options: [{ value, label, image }]
const OnboardingRadioGroup = ({ name, options, value, onChange }) => {
  return (
    <div className="onboarding-radio-group" role="radiogroup">
      {options.map((option) => (
        <label
          key={option.value}
          className={value === option.value ? 'onboarding-radio active' : 'onboarding-radio'}
        >
          <input
            type="radio"
            name={name}
            value={option.value}
            checked={value === option.value}
            onChange={() => onChange(option.value)}
          />
          {option.image && <img className="onboarding-radio-image" src={option.image} alt="" />}
          {option.label && <span className="onboarding-radio-label">{option.label}</span>}
        </label>
      ))}
    </div>
  )
}

export default OnboardingRadioGroup
