// 드롭다운. options: [{ value, label }]
const OnboardingSelect = ({ label, name, options, value, placeholder, onChange }) => {
  return (
    <label className="onboarding-select">
      {label && <span className="onboarding-select-label">{label}</span>}

      <select
        className="onboarding-select-field"
        name={name}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      >
        {placeholder && <option value="" disabled>{placeholder}</option>}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  )
}

export default OnboardingSelect
