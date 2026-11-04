import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './RecordList.scss';

import iconBack from '../../assets/record/icon_before.png';
import iconEdit from '../../assets/record/icon_edit.png';
import iconDelete from '../../assets/record/icon_delete.png'; 

import iconFood from '../../assets/record/icon_feeding.png';
import iconEtc from '../../assets/record/icon_etc.png';
import iconShedding from '../../assets/record/icon_shedding.png';
import iconClean from '../../assets/record/icon_cleaning.png';
import iconSymptom from '../../assets/record/icon_symptom.png';
import iconPoop from '../../assets/record/icon_poop.png';
import iconWeight from '../../assets/record/icon_weight.png';

import bgBadgeGreen from '../../assets/record/bg_badge_green.png';
import bgBadgeOrange from '../../assets/record/bg_badge_orange.png';
import bgBtnFinish from '../../assets/record/bg_btn_finish.png';

const INITIAL_DATA = [
  { id: 1, date: '1', day: '화', color: 'green', type: '먹이', icon: iconFood, time: '오후 7시 35분', text: '밀웜 5마리를 줬는데 다 먹지 않고 자러...', imgUrl: 'https://via.placeholder.com/80' },
  { id: 2, date: '4', day: '금', color: 'orange', type: '추가기록', icon: iconEtc, time: '오후 7시 35분', text: '새 은신처로 바꿨는데 들어가서 잘 쉬...', imgUrl: 'https://via.placeholder.com/80' },
  { id: 3, date: '9', day: '수', color: 'green', type: '탈피', icon: iconShedding, time: '오후 7시 35분', text: '발가락 쪽에 탈피가 조금 남아 있음', imgUrl: 'https://via.placeholder.com/80' },
  { id: 4, date: '10', day: '목', color: 'orange', type: '탈피', icon: iconShedding, time: '오후 7시 35분', text: '어제부터 탈피 시작했는데 오늘 보니...', imgUrl: 'https://via.placeholder.com/80' },
  { id: 5, date: '14', day: '월', color: 'green', type: '청소', icon: iconClean, time: '오후 7시 35분', text: '바닥재 부분 교체하고 물그릇도 같이...', imgUrl: 'https://via.placeholder.com/80' },
  { id: 6, date: '22', day: '화', color: 'orange', type: '먹이', icon: iconFood, time: '오후 7시 35분', text: '밀웜 5마리 줬는데 2마리만 먹고 더 안...', imgUrl: 'https://via.placeholder.com/80' },
  { id: 7, date: '24', day: '목', color: 'green', type: '이상증세', icon: iconSymptom, time: '오후 7시 35분', text: '오늘은 평소보다 움직임이 적고 은신처...', imgUrl: 'https://via.placeholder.com/80' },
  { id: 8, date: '25', day: '금', color: 'orange', type: '배변', icon: iconPoop, time: '오후 7시 35분', text: '평소보다 양이 적고 조금 단단해 보임', imgUrl: 'https://via.placeholder.com/80' },
  { id: 9, date: '27', day: '일', color: 'green', type: '체중', icon: iconWeight, time: '오후 7시 35분', text: '오늘 재보니 38.6g, 지난번보다 조금...', imgUrl: 'https://via.placeholder.com/80' },
];

export default function RecordList() {
  const navigate = useNavigate();
  const [records, setRecords] = useState(INITIAL_DATA);
  const [isEditing, setIsEditing] = useState(false); 

  const handleDelete = (id) => {
    setRecords((prev) => prev.filter((item) => item.id !== id));
  };

  return (
    <div className={`record-list-page ${isEditing ? 'is-editing-mode' : ''}`}>
      <header className="list-header">
        <button type="button" className="header-btn" onClick={() => navigate(-1)}>
          <img src={iconBack} alt="뒤로가기" />
        </button>
        <h1 className="header-title">9월의 기록 ({records.length}건)</h1>
        <button 
          type="button" 
          className={`header-btn ${isEditing ? 'active' : ''}`}
          onClick={() => setIsEditing((v) => !v)}
        >
          <img src={iconEdit} alt="편집" />
        </button>
      </header>

      <main className="list-content">
        {records.map((item) => (
          <div key={item.id} className="timeline-item">
            <div className={`date-badge ${item.color}`}>
            <img 
                src={item.color === 'green' ? bgBadgeGreen : bgBadgeOrange} 
                alt="" 
                className="badge-bg"
            />
            <div className="badge-text">
                <span className="date-num">{item.date}</span>
                <span className="date-day">{item.day}</span>
            </div>
            </div>

            <div className="record-card">
              {isEditing && (
                <button 
                  type="button" 
                  className="card-delete-btn"
                  onClick={() => handleDelete(item.id)}
                >
                  <img src={iconDelete} alt="삭제" />
                </button>
              )}

              <div className="card-main">
                <div className="card-info">
                  <div className="type-row">
                    <img src={item.icon} alt="" className="type-icon" />
                    
                    <div className="type-text-wrap">
                        <span className="type-name">{item.type}</span>
                        <span className="time-text">{item.time}</span>
                    </div>
                    </div>
                  <p className="record-text">{item.text}</p>
                </div>
                
                {item.imgUrl && (
                  <img src={item.imgUrl} alt="기록 썸네일" className="record-img" />
                )}
              </div>
            </div>
          </div>
        ))}
      </main>

      {isEditing && (
  <div className="finish-btn-wrapper">
    <img src={bgBtnFinish} alt="" className="finish-btn-bg" />
    
    <button 
      type="button" 
      className="finish-btn" 
      onClick={() => setIsEditing(false)}
    >
      완료
    </button>
  </div>
)}
    </div>
  );
}