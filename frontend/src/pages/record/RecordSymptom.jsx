import React, { useState } from 'react';
import './RecordForm.scss';

import bgDecoration from '../../assets/record/bg_pattern.png';
import num1 from '../../assets/record/num_1.png';
import num2 from '../../assets/record/num_2.png';
import num3 from '../../assets/record/num_3.png';
import btnAddPhoto from '../../assets/record/btn_add_photo.png';
import photoEmpty from '../../assets/record/photo_empty.png';
import btnComplete from '../../assets/record/btn_complete.png';

const RecordSymptom = ({ onBack, onComplete }) => {
  const [changes, setChanges] = useState(['먹이 반응 저하']);
  const [startTime, setStartTime] = useState('오늘 처음');
  const [observed, setObserved] = useState('현재도 관찰됨');
  const [memo, setMemo] = useState('');

  const handleChangeSelect = (val) => {
    setChanges(prev => prev.includes(val) ? prev.filter(i => i !== val) : [...prev, val]);
  };

  return (
    <div className="record-form-page">
      <img src={bgDecoration} alt="" className="bg-decoration" />

      <header className="record-form-header">
        <button type="button" className="back-btn" onClick={onBack}>
          ‹
        </button>
        <h1 className="title">이상 증세</h1>
      </header>

      <div className="record-form-body">
        <section className="form-section">
          <h2 className="section-title">
            <img src={num1} alt="1" className="num-badge-img" />
            <span>어떤 변화가 있었나요?</span>
          </h2>
          <div className="options-grid grid-3">
            {['활동 감소', '먹이 반응 저하', '호흡', '외형 변화', '움직임 이상', '기타'].map(val => (
              <label key={val} className={`option-item ${changes.includes(val) ? 'is-checked' : ''}`}>
                <input type="checkbox" checked={changes.includes(val)} onChange={() => handleChangeSelect(val)} />
                <span className="radio-circle" />
                <span className="option-label">{val}</span>
              </label>
            ))}
          </div>
        </section>

        <section className="form-section">
          <h2 className="section-title">
            <img src={num2} alt="2" className="num-badge-img" />
            <span>언제부터 관찰됐나요?</span>
          </h2>
          <div className="options-grid">
            {['오늘 처음', '2~3일 전부터', '그보다 오래됨', '잘 모르겠음'].map(val => (
              <label key={val} className={`option-item ${startTime === val ? 'is-checked' : ''}`}>
                <input type="radio" name="startTime" checked={startTime === val} onChange={() => setStartTime(val)} />
                <span className="radio-circle" />
                <span className="option-label">{val}</span>
              </label>
            ))}
          </div>
        </section>

        <section className="form-section">
          <h2 className="section-title">
            <img src={num3} alt="3" className="num-badge-img" />
            <span>현재도 관찰되나요?</span>
          </h2>
          <div className="options-grid">
            {['현재도 관찰됨', '지금은 보이지 않음'].map(val => (
              <label key={val} className={`option-item ${observed === val ? 'is-checked' : ''}`}>
                <input type="radio" name="observed" checked={observed === val} onChange={() => setObserved(val)} />
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
    <button type="button" className="submit-btn-img"  onClick={onComplete}>
        <img src={btnComplete} alt="완료 배경" />
        <span className="btn-text">완료</span>
    </button>
    </div>
      </div>
    </div>
  );
};

export default RecordSymptom;