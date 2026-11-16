import React, { useState } from 'react';
import './RecordForm.scss';

import bgDecoration from '../../assets/record/bg_pattern.png';
import num1 from '../../assets/record/num_1.png';
import num2 from '../../assets/record/num_2.png';
import btnAddPhoto from '../../assets/record/btn_add_photo.png';
import photoEmpty from '../../assets/record/photo_empty.png';
import btnComplete from '../../assets/record/btn_complete.png';

const RecordBowel = ({ onBack }) => {
  const [status, setStatus] = useState('평소와 다름');
  const [diffs, setDiffs] = useState(['묽음']);
  const [memo, setMemo] = useState('');

  const handleDiffChange = (val) => {
    setDiffs(prev => prev.includes(val) ? prev.filter(i => i !== val) : [...prev, val]);
  };

  return (
    <div className="record-form-page">
      <img src={bgDecoration} alt="" className="bg-decoration" />

      <header className="record-form-header">
        <button type="button" className="back-btn" onClick={onBack}>
          ‹
        </button>
        <h1 className="title">배변</h1>
      </header>

      <div className="record-form-body">
        <section className="form-section">
          <h2 className="section-title">
            <img src={num1} alt="1" className="num-badge-img" />
            <span>배변 상태</span>
          </h2>
          <div className="options-grid">
            {['평소와 비슷함', '평소와 다름'].map(val => (
              <label key={val} className={`option-item ${status === val ? 'is-checked' : ''}`}>
                <input type="radio" name="bowelStatus" checked={status === val} onChange={() => setStatus(val)} />
                <span className="radio-circle" />
                <span className="option-label">{val}</span>
              </label>
            ))}
          </div>
        </section>

        <section className="form-section">
          <h2 className="section-title">
            <img src={num2} alt="2" className="num-badge-img" />
            <span>달랐던 점</span>
          </h2>
          <div className="options-grid grid-3">
            {['묽음', '단단함', '색', '양', '기타'].map(val => (
              <label key={val} className={`option-item ${diffs.includes(val) ? 'is-checked' : ''}`}>
                <input type="checkbox" checked={diffs.includes(val)} onChange={() => handleDiffChange(val)} />
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

        <section className="form-section photo-section">
          <h2 className="photo-title">사진 추가</h2>
          <p className="photo-sub">최대 3장의 사진을 업로드할 수 있어요.</p>
          <div className="photo-grid">
            <button type="button" className="photo-btn-img"><img src={btnAddPhoto} alt="사진 추가" /></button>
            <div className="photo-placeholder-img"><img src={photoEmpty} alt="빈 슬롯" /></div>
            <div className="photo-placeholder-img"><img src={photoEmpty} alt="빈 슬롯" /></div>
          </div>
        </section>
        <div className="submit-area">
        <button type="button" className="submit-btn-img">
            <img src={btnComplete} alt="완료 배경" />
            <span className="btn-text">완료</span>
        </button>
        </div>


      </div>
    </div>
  );
};

export default RecordBowel;