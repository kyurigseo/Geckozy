import { useEffect, useRef, useState } from 'react'

import searchIcon from '../../../../assets/onboarding/icon-search.svg'

import './SearchSelect.scss'

// 검색형 선택 (피그마 '종 선택' 컴포넌트: Default / focus / typing / select(hover) / filled)
// 입력한 글자가 들어간 항목만 목록에 보여주고, 목록에서 골라야 값이 정해진다.
// options: 문자열 배열
const SearchSelect = ({ options, value, placeholder, ariaLabel, onChange }) => {
  const [isOpen, setIsOpen] = useState(false)
  const [query, setQuery] = useState('')
  const rootRef = useRef(null)
  const inputRef = useRef(null)

  const keyword = query.trim()
  const filtered = keyword ? options.filter((option) => option.includes(keyword)) : options

  // 바깥을 누르면 고르지 않은 채로 닫기
  useEffect(() => {
    if (!isOpen) return

    const handleClickOutside = (e) => {
      if (!rootRef.current.contains(e.target)) setIsOpen(false)
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [isOpen])

  const open = () => {
    setQuery('')
    setIsOpen(true)
  }

  const handleSelect = (option) => {
    onChange(option)
    setIsOpen(false)
    inputRef.current.blur()
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Escape') {
      setIsOpen(false)
      inputRef.current.blur()
    } else if (e.key === 'Enter' && filtered.length > 0) {
      e.preventDefault()
      handleSelect(filtered[0])
    }
  }

  return (
    <div ref={rootRef} className={isOpen ? 'search-select open' : 'search-select'}>
      <div className="search-select-box">
        <div className="search-select-head">
          <input
            ref={inputRef}
            className="search-select-field"
            role="combobox"
            aria-label={ariaLabel}
            aria-expanded={isOpen}
            value={isOpen ? query : value}
            placeholder={isOpen ? '' : placeholder}
            onFocus={open}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <img className="search-select-icon" src={searchIcon} alt="" />
        </div>

        {isOpen && (
          <ul className="search-select-options" role="listbox" aria-label={ariaLabel}>
            {filtered.length > 0 ? (
              filtered.map((option) => (
                <li key={option} role="option" aria-selected={option === value}>
                  <button type="button" className="search-select-option" onClick={() => handleSelect(option)}>
                    {option}
                  </button>
                </li>
              ))
            ) : (
              <li className="search-select-empty">검색 결과가 없어요.</li>
            )}
          </ul>
        )}
      </div>
    </div>
  )
}

export default SearchSelect
