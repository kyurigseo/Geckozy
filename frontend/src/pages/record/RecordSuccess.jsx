import React, { useEffect } from 'react';
import './RecordSuccess.scss'; // 또는 스타일 파일

import lizardImg from '../../assets/record/success_lizard.png'; // 도마뱀 이미지
import bgPattern from '../../assets/record/bg_pattern.png'; // 배경 패턴

const RecordSuccess = ({ onComplete }) => {
  useEffect(() => {
    // 2초 후 상세보기 화면으로 이동
    const timer = setTimeout(() => {
      onComplete();
    }, 2000);

    return () => clearTimeout(timer);
  }, [onComplete]);

  return (
    <div className="record-success-page">
      <img src={bgPattern} alt="" className="bg-decoration" />
      
      <div className="content-wrapper">
        <h1 className="success-title">기록이 저장되었어요!</h1>
        <p className="success-sub">기록 화면으로 이동할게요</p>

        <div className="lizard-character">
          <img src={lizardImg} alt="도마뱀" />
        </div>

        <div className="loading-area">
          <span className="loading-text">Loading...</span>
          <div className="loading-dots">
            <span className="dot dot-1" />
            <span className="dot dot-2" />
            <span className="dot dot-3" />
          </div>
        </div>
      </div>
    </div>
  );
};

export default RecordSuccess;