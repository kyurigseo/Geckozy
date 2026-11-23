import React, { useState } from 'react';
import './RecordForm.scss';
import RecordPhotos from './Recordphotos';
import RecordTimeSheet from './Recordtimesheet';

import bgDecoration from '../../assets/record/bg_pattern.png';
import num1 from '../../assets/record/num_1.png';
import num2 from '../../assets/record/num_2.png';
import iconClock from '../../assets/record/icon_clock.png';
import btnComplete from '../../assets/record/btn_complete.png';

const RecordEtc = ({ onBack,onComplete  }) => {
  const [etcType, setEtcType] = useState('사육장 변화');
  const [selectedTime, setSelectedTime] = useState('');
  const [isPickerOpen, setIsPickerOpen] = useState(false);
  const [memo, setMemo] = useState('');
  const [photos, setPhotos] = useState([]);

  const pickedAt = () => {
    if (!selectedTime) return undefined;
    const [hh, mm] = tempTime.split(':').map(Number);
    const now = new Date();
    const at = new Date(now.getFullYear(), Number(tempMonth) - 1, Number(tempDay), hh, mm);
    if (at > now) at.setFullYear(now.getFullYear() - 1);
    return at;
  };

  const [now] = useState(() => new Date());
  const p2 = (n) => String(n).padStart(2, '0');
  const [tempMonth, setTempMonth] = useState(p2(now.getMonth() + 1));
  const [tempDay, setTempDay] = useState(p2(now.getDate()));
  const [tempTime, setTempTime] = useState(`${p2(now.getHours())}:${now.getMinutes() >= 30 ? '30' : '00'}`);

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

        <RecordPhotos photos={photos} onChange={setPhotos} />

        <div className="submit-area">
          <button type="button" className="submit-btn-img"  onClick={() => onComplete({ etcType, recordedAt: pickedAt(), memo, photos })}>
            <img src={btnComplete} alt="완료" />
            <span className="btn-text">완료</span>
          </button>
        </div>
      </div>

      {isPickerOpen && (
        <RecordTimeSheet
          value={{ month: tempMonth, day: tempDay, time: tempTime }}
          onConfirm={({ month, day, time }) => {
            setTempMonth(month);
            setTempDay(day);
            setTempTime(time);
            setSelectedTime(`${month}월 ${day}일 ${time}`);
            setIsPickerOpen(false);
          }}
          onClose={() => setIsPickerOpen(false)}
        />
      )}
    </div>
  );
};

export default RecordEtc;