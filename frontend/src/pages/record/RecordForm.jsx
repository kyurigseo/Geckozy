import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import RecordShedding from './RecordShedding';
import RecordFeed from './RecordFeed';
import RecordWeight from './RecordWeight';
import RecordCleaning from './RecordCleaning';
import RecordBowel from './RecordBowel';
import RecordSymptom from './RecordSymptom';
import RecordEtc from './RecordEtc';
import './RecordForm.scss';

const RecordForm = () => {
  const { type } = useParams(); // 👈 URL의 /record/new/:type 에서 type 값을 가져옴
  const navigate = useNavigate();

  // 뒤로가기 공통 핸들러
  const handleBack = () => {
    navigate(-1); // 이전 페이지(메인 달력)로 이동
  };

  // type 문자열 매핑 (Record.jsx의 MENU type들과 일치)
  const renderSelectedForm = () => {
    switch (type) {
      case 'shedding':
        return <RecordShedding onBack={handleBack} />;
      case 'feeding':
      case 'feed':
        return <RecordFeed onBack={handleBack} />;
      case 'weight':
        return <RecordWeight onBack={handleBack} />;
      case 'cleaning':
        return <RecordCleaning onBack={handleBack} />;
      case 'poop':
      case 'bowel':
        return <RecordBowel onBack={handleBack} />;
      case 'symptom':
        return <RecordSymptom onBack={handleBack} />;
      case 'etc':
        return <RecordEtc onBack={handleBack} />;
      default:
        return <RecordShedding onBack={handleBack} />;
    }
  };

  return (
    <div className="record-form-wrapper">
      {renderSelectedForm()}
    </div>
  );
};

export default RecordForm;