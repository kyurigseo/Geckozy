// 여러 개 선택 가능한 옵션 그룹. options: [{ value, label, image }], values: 선택된 value 배열
const OnboardingCheckboxGroup = ({ name, options, values, onChange }) => {
  const toggle = (optionValue) => {
    if (values.includes(optionValue)) {
      onChange(values.filter((v) => v !== optionValue))
    } else {
      onChange([...values, optionValue])
    }
  }

  return (
    <div className="onboarding-checkbox-group">
      {options.map((option) => (
        <label
          key={option.value}
          className={values.includes(option.value) ? 'onboarding-checkbox active' : 'onboarding-checkbox'}
        >
          <input
            type="checkbox"
            name={name}
            value={option.value}
            checked={values.includes(option.value)}
            onChange={() => toggle(option.value)}
          />
          {option.image && <img className="onboarding-checkbox-image" src={option.image} alt="" />}
          {option.label && <span className="onboarding-checkbox-label">{option.label}</span>}
        </label>
      ))}
    </div>
  )
}

export default OnboardingCheckboxGroup
