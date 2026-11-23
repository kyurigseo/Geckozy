import React, { useState } from 'react';
import './RecordForm.scss';
import RecordPhotos from './Recordphotos';

import bgDecoration from '../../assets/record/bg_pattern.png';
import num1 from '../../assets/record/num_1.png';
import btnComplete from '../../assets/record/btn_complete.png';

const RecordWeight = ({ onBack, onComplete }) => {
  const [weight, setWeight] = useState('');
  const [memo, setMemo] = useState('');
  const [photos, setPhotos] = useState([]);

  return (
    <div className="record-form-page">
      <img src={bgDecoration} alt="" className="bg-decoration" />

      <header className="record-form-header">
        <button type="button" className="back-btn" onClick={onBack}>
          ‹
        </button>
        <h1 className="title">체중</h1>
      </header>

      <div className="record-form-body">
        <section className="form-section">
          <h2 className="section-title">
            <img src={num1} alt="1" className="num-badge-img" />
            <span>현재 체중</span>
          </h2>
          <div className="weight-input-box">
            <input type="number" placeholder="00" value={weight} onChange={e => setWeight(e.target.value)} />
            <span className="unit">(g)</span>
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
        <button type="button" className="submit-btn-img"  onClick={() => onComplete({ weight, memo, photos })}>
            <img src={btnComplete} alt="완료 배경" />
            <span className="btn-text">완료</span>
        </button>
        </div>
      </div>
    </div>
  );
};

export default RecordWeight;