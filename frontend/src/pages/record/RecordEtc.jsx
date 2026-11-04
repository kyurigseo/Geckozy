import React, { useState, useRef, useEffect } from 'react';
import './RecordForm.scss';

import bgDecoration from '../../assets/record/bg_pattern.png';
import num1 from '../../assets/record/num_1.png';
import num2 from '../../assets/record/num_2.png';
import iconClock from '../../assets/record/icon_clock.png';
import btnAddPhoto from '../../assets/record/btn_add_photo.png';
import photoEmpty from '../../assets/record/photo_empty.png';
import btnComplete from '../../assets/record/btn_complete.png';
import iconClose from '../../assets/record/icon_close.png';
import btnConfirm from '../../assets/record/btn_confirm.png';

const MONTHS = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'));
const DAYS = Array.from({ length: 31 }, (_, i) => String(i + 1).padStart(2, '0'));
const TIMES = Array.from({ length: 48 }, (_, i) => {
  const h = String(Math.floor(i / 2)).padStart(2, '0');
  const m = i % 2 === 0 ? '00' : '30';
  return `${h}:${m}`;
});

const ITEM_HEIGHT = 44; // 아이템 높이 (px)

function WheelColumn({ items, unit, defaultValue, onChange }) {
  const scrollRef = useRef(null);
  const [selectedIndex, setSelectedIndex] = useState(() => {
    const idx = items.indexOf(defaultValue);
    return idx !== -1 ? idx : 0;
  });

  // 모달이 완전히 화면에 그려진 후 초깃값 위치로 이동
  useEffect(() => {
    const idx = items.indexOf(defaultValue);
    if (idx !== -1) {
      setSelectedIndex(idx);
      const timer = setTimeout(() => {
        if (scrollRef.current) {
          scrollRef.current.scrollTop = idx * ITEM_HEIGHT;
        }
      }, 50);
      return () => clearTimeout(timer);
    }
  }, [defaultValue, items]);

  // 단순 스크롤 계산 (상태 재렌더링 최소화)
  const handleScroll = (e) => {
    const scrollTop = e.currentTarget.scrollTop;
    let idx = Math.round(scrollTop / ITEM_HEIGHT);

    if (idx < 0) idx = 0;
    if (idx >= items.length) idx = items.length - 1;

    if (idx !== selectedIndex) {
      setSelectedIndex(idx);
      onChange(items[idx]);
    }
  };

  const handleClickItem = (idx) => {
    setSelectedIndex(idx);
    onChange(items[idx]);
    if (scrollRef.current) {
      scrollRef.current.scrollTop = idx * ITEM_HEIGHT;
    }
  };

  return (
    <div className="wheel-column">
      <div 
        className="picker-wheel" 
        ref={scrollRef} 
        onScroll={handleScroll}
      >
        <div className="picker-padding" />

        {items.map((item, idx) => {
          const diff = idx - selectedIndex;
          let statusClass = 'hidden';

          if (diff === 0) statusClass = 'active';
          else if (Math.abs(diff) === 1) statusClass = 'dim1';
          else if (Math.abs(diff) === 2) statusClass = 'dim2';

          return (
            <div
              key={item}
              className={`wheel-item ${statusClass}`}
              onClick={() => handleClickItem(idx)}
            >
              {item}{unit}
            </div>
          );
        })}

        <div className="picker-padding" />
      </div>
    </div>
  );
}

const RecordEtc = ({ onBack,onComplete  }) => {
  const [etcType, setEtcType] = useState('사육장 변화');
  const [selectedTime, setSelectedTime] = useState('');
  const [isPickerOpen, setIsPickerOpen] = useState(false);
  const [memo, setMemo] = useState('');

  const [tempMonth, setTempMonth] = useState('05');
  const [tempDay, setTempDay] = useState('18');
  const [tempTime, setTempTime] = useState('21:00');

  const handleConfirmTime = () => {
    setSelectedTime(`${tempMonth}월 ${tempDay}일 ${tempTime}`);
    setIsPickerOpen(false);
  };

  return (
    <div className="record-form-page">
      <img src={bgDecoration} alt="" className="bg-decoration" />

      <header className="record-form-header">
        <button type="button" className="back-btn" onClick={onBack}>
          ‹
        </button>
        <h1 className="title">추가 기록</h1>
      </header>

      <div className="record-form-body">
        <section className="form-section">
          <h2 className="section-title">
            <img src={num1} alt="1" className="num-badge-img" />
            <span>기록 유형</span>
          </h2>
          <div className="options-grid">
            {['행동 변화', '사육장 변화', '장비 변경', '기타'].map((val) => (
              <label key={val} className={`option-item ${etcType === val ? 'is-checked' : ''}`}>
                <input
                  type="radio"
                  name="etcType"
                  checked={etcType === val}
                  onChange={() => setEtcType(val)}
                />
                <span className="radio-circle" />
                <span className="option-label">{val}</span>
              </label>
            ))}
          </div>
        </section>

        <section className="form-section">
          <h2 className="section-title">
            <img src={num2} alt="2" className="num-badge-img" />
            <span>기록 시각 선택</span>
          </h2>
          <div className="time-select-btn" onClick={() => setIsPickerOpen(true)}>
            <span>{selectedTime || '발생 시간대를 선택해주세요.'}</span>
            <img src={iconClock} alt="시계" className="clock-icon-img" />
          </div>
        </section>

        <section className="form-section memo-section">
          <label className="memo-label">짧은 메모를 남겨보세요.</label>
          <div className="textarea-wrapper">
            <textarea
              maxLength={300}
              placeholder="예) 환기 빈도가 적절한지 모르겠어요.."
              value={memo}
              onChange={(e) => setMemo(e.target.value)}
            />
            <span className="char-count">{memo.length}/300</span>
          </div>
        </section>

        <section className="form-section photo-section">
          <h2 className="photo-title">사진 추가</h2>
          <p className="photo-sub">최대 3장의 사진을 업로드할 수 있어요.</p>
          <div className="photo-grid">
            <button type="button" className="photo-btn-img">
              <img src={btnAddPhoto} alt="사진 추가" />
            </button>
            <div className="photo-placeholder-img">
              <img src={photoEmpty} alt="빈 슬롯" />
            </div>
            <div className="photo-placeholder-img">
              <img src={photoEmpty} alt="빈 슬롯" />
            </div>
          </div>
        </section>

        <div className="submit-area">
          <button type="button" className="submit-btn-img"  onClick={onComplete}>
            <img src={btnComplete} alt="완료" />
            <span className="btn-text">완료</span>
          </button>
        </div>
      </div>

      {isPickerOpen && (
        <div className="picker-overlay" onClick={() => setIsPickerOpen(false)}>
          <div className="picker-modal" onClick={(e) => e.stopPropagation()}>
            <div className="picker-header">
              <h3>기록 시각 선택</h3>
              <button type="button" className="close-btn" onClick={() => setIsPickerOpen(false)}>
                <img src={iconClose} alt="닫기" />
              </button>
            </div>

            <div className="picker-wheels">
              <div className="picker-selection-highlight" />

              <WheelColumn
                items={MONTHS}
                unit="월"
                defaultValue={tempMonth}
                onChange={setTempMonth}
              />
              <WheelColumn
                items={DAYS}
                unit="일"
                defaultValue={tempDay}
                onChange={setTempDay}
              />
              <WheelColumn
                items={TIMES}
                unit=""
                defaultValue={tempTime}
                onChange={setTempTime}
              />
            </div>

            <button
              type="button"
              className="picker-confirm-btn-img"
              onClick={handleConfirmTime}
            >
              <img src={btnConfirm} alt="확인" />
              <span className="btn-text">확인</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default RecordEtc;