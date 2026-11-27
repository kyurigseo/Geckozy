import React, { useState } from 'react';
import './RecordForm.scss';
import RecordPhotos from './Recordphotos';

import bgDecoration from '../../assets/record/bg_pattern.png';
import iconBack from '../../assets/record/icon_back.png';
import num1 from '../../assets/record/num_1.png';
import num2 from '../../assets/record/num_2.png';
import btnComplete from '../../assets/record/btn_complete.png';

const RecordBowel = ({ onBack, onComplete }) => {
  const [status, setStatus] = useState('평소와 다름');
  const [diffs, setDiffs] = useState(['묽음']);
  const [memo, setMemo] = useState('');
  const [photos, setPhotos] = useState([]);

  const handleDiffChange = (val) => {
    setDiffs(prev => prev.includes(val) ? prev.filter(i => i !== val) : [...prev, val]);
  };

  return (
    <div className="record-form-page">
      <img src={bgDecoration} alt="" className="bg-decoration" />

      <header className="record-form-header">
        <button type="button" className="back-btn" onClick={onBack} aria-label="뒤로 가기">
          <img src={iconBack} alt="" />
        </button>
        <h1 className="title">배변</h1>
      </header>

      <div className="record-form-body">
        <section className="form-section">
          <h2 className="section-title">
            <img src={num1} alt="1" className="num-badge-img" />
            <span>배변 상태</span>
          </h2>
          <div className="options-rows">
            {[['평소와 비슷함', '평소와 다름']].map((row, i) => (
              <div key={i} className="options-row">
                {row.map(val => (
                  <label key={val} className={`option-item ${status === val ? 'is-checked' : ''}`}>
                    <input type="radio" name="bowelStatus" checked={status === val} onChange={() => setStatus(val)} />
                    <span className="radio-circle" />
                    <span className="option-label">{val}</span>
                  </label>
                ))}
              </div>
            ))}
          </div>
        </section>

        <section className="form-section">
          <h2 className="section-title">
            <img src={num2} alt="2" className="num-badge-img" />
            <span>달랐던 점</span>
          </h2>
          <div className="options-rows">
            {[['묽음', '단단함', '색'], ['양', '기타']].map((row, i) => (
              <div key={i} className="options-row">
                {row.map(val => (
                  <label key={val} className={`option-item ${diffs.includes(val) ? 'is-checked' : ''}`}>
                    <input type="checkbox" checked={diffs.includes(val)} onChange={() => handleDiffChange(val)} />
                    <span className="radio-circle" />
                    <span className="option-label">{val}</span>
                  </label>
                ))}
              </div>
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
        <button type="button" className="submit-btn-img"  onClick={() => onComplete({ status, diffs, memo, photos })}>
            <img src={btnComplete} alt="완료 배경" />
            <span className="btn-text">완료</span>
        </button>
        </div>


      </div>
    </div>
  );
};

export default RecordBowel;