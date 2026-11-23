import React, { useState } from 'react';
import './RecordForm.scss';
import RecordPhotos from './Recordphotos';

import bgDecoration from '../../assets/record/bg_pattern.png';
import num1 from '../../assets/record/num_1.png';
import num2 from '../../assets/record/num_2.png';
import btnComplete from '../../assets/record/btn_complete.png';

const RecordShedding = ({ onBack,onComplete }) => {
  const [status, setStatus] = useState('일부 남음');
  const [parts, setParts] = useState(['몸통']);
  const [memo, setMemo] = useState('');
  const [photos, setPhotos] = useState([]);

  const handlePartChange = (value) => {
    setParts(prev =>
      prev.includes(value) ? prev.filter(item => item !== value) : [...prev, value]
    );
  };

  return (
    <div className="record-form-page">
      <img src={bgDecoration} alt="" className="bg-decoration" />

      <header className="record-form-header">
        <button type="button" className="back-btn" onClick={onBack}>
          ‹
        </button>
        <h1 className="title">탈피</h1>
      </header>

      <div className="record-form-body">
        <section className="form-section">
          <h2 className="section-title">
            <img src={num1} alt="1" className="num-badge-img" />
            <span>탈피 상태</span>
          </h2>
          <div className="options-grid">
            {['완료', '일부 남음', '진행 중'].map(val => (
              <label key={val} className={`option-item ${status === val ? 'is-checked' : ''}`}>
                <input type="radio" name="sheddingStatus" checked={status === val} onChange={() => setStatus(val)} />
                <span className="radio-circle" />
                <span className="option-label">{val}</span>
              </label>
            ))}
          </div>
        </section>

        <section className="form-section">
          <h2 className="section-title">
            <img src={num2} alt="2" className="num-badge-img" />
            <span>남아있는 부위</span>
          </h2>
          <div className="options-grid grid-3">
            {['몸통', '머리 주변', '발가락', '꼬리', '기타'].map(val => (
              <label key={val} className={`option-item ${parts.includes(val) ? 'is-checked' : ''}`}>
                <input type="checkbox" checked={parts.includes(val)} onChange={() => handlePartChange(val)} />
                <span className="radio-circle" />
                <span className="option-label">{val}</span>
              </label>
            ))}
          </div>
        </section>

        <section className="form-section memo-section">
          <label className="memo-label">짧은 메모를 남겨보세요.</label>
          <div className="textarea-wrapper">
            <textarea maxLength={300} placeholder="예) 환기 빈도가 적절한지 모르겠어요.." value={memo} onChange={e => setMemo(e.target.value)} />
            <span className="char-count">{memo.length}/300</span>
          </div>
        </section>

        <RecordPhotos photos={photos} onChange={setPhotos} />
        <div className="submit-area">
        <button type="button" className="submit-btn-img" onClick={() => onComplete({ status, parts, memo, photos })}>
            <img src={btnComplete} alt="완료 배경" />
            <span className="btn-text">완료</span>
        </button>
        </div>


      </div>
    </div>
  );
};

export default RecordShedding;