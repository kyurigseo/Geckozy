import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Record.scss";

import iconShedding from "../../assets/record/icon_shedding.png"; // 탈피
import iconFood from "../../assets/record/icon_food.png";         // 먹이
import iconWeight from "../../assets/record/icon_weight.png";     // 체중
import iconClean from "../../assets/record/icon_clean.png";       // 청소
import iconPoop from "../../assets/record/icon_poop.png";         // 배변
import iconSymptom from "../../assets/record/icon_symptom.png";   // 이상 증세
import iconEtc from "../../assets/record/icon_etc.png";           // 기타

// 기록 종류 7개 정의
const RECORD_TYPES = [
  { id: "shedding", label: "탈피", icon: iconShedding, path: "/record/shedding" },
  { id: "food", label: "먹이", icon: iconFood, path: "/record/food" },
  { id: "weight", label: "체중", icon: iconWeight, path: "/record/weight" },
  { id: "clean", label: "청소", icon: iconClean, path: "/record/clean" },
  { id: "poop", label: "배변", icon: iconPoop, path: "/record/poop" },
  { id: "symptom", label: "이상 증세", icon: iconSymptom, path: "/record/symptom" },
  { id: "etc", label: "기타", icon: iconEtc, path: "/record/etc" },
];

export default function Record() {
  const navigate = useNavigate();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [currentYear, setCurrentYear] = useState(2026);
  const [currentMonth, setCurrentMonth] = useState(9);

  // 기록 메뉴 버튼 클릭 시 라우팅
  const handleSelectRecord = (path) => {
    setIsMenuOpen(false);
    navigate(path);
  };

  return (
    <div className={`record-page ${isMenuOpen ? "is-menu-open" : ""}`}>
      {/* 1. 상단 달력 영역 */}
      <div className="record-calendar-section">
        {/* 월 이동 헤더 */}
        <div className="calendar-header">
          <button 
            type="button" 
            className="month-btn"
            onClick={() => setCurrentMonth((prev) => (prev === 1 ? 12 : prev - 1))}
          >
            &lt;
          </button>
          <h2>{currentYear}년 {currentMonth}월</h2>
          <button 
            type="button" 
            className="month-btn"
            onClick={() => setCurrentMonth((prev) => (prev === 12 ? 1 : prev + 1))}
          >
            &gt;
          </button>
        </div>

        {/* 요일 헤더 */}
        <div className="calendar-weekdays">
          <span>일</span>
          <span>월</span>
          <span>화</span>
          <span>수</span>
          <span>목</span>
          <span>금</span>
          <span>토</span>
        </div>

        {/* 캘린더 그리드 (예시 데모 그리드) */}
        <div className="calendar-grid">
          {/* 날짜 데이터 map 반복 구문... */}
        </div>

        {/* 2. 기록 추가하기 버튼 & 방사형 7개 서브 메뉴 */}
        <div className="record-add-container">
          {/* 기록 추가하기 메인 버튼 */}
          <button
            type="button"
            className="record-add-btn"
            onClick={() => setIsMenuOpen(!isMenuOpen)}
          >
            기록 추가하기
          </button>

          {/* 7개 기록 선택 메뉴 (isMenuOpen 상태일 때 노출) */}
          {isMenuOpen && (
            <div className="record-radial-menu">
              {RECORD_TYPES.map((type, idx) => (
                <button
                  key={type.id}
                  type="button"
                  className={`radial-item radial-item--${type.id}`}
                  onClick={() => handleSelectRecord(type.path)}
                >
                  <div className="icon-wrapper">
                    <img src={type.icon} alt={type.label} />
                  </div>
                  <span>{type.label}</span>
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* 메뉴 열림 시 오버레이 배경 (클릭 시 메뉴 닫힘) */}
      {isMenuOpen && (
        <div 
          className="record-menu-overlay" 
          onClick={() => setIsMenuOpen(false)} 
        />
      )}

      {/* 3. 하단 요약 통계 영역 (9월의 기록) */}
      <div className="record-summary-section">
        <div className="summary-header">
          <h3>
            9월의 기록 <span>(9건)</span>
          </h3>
          <button type="button" className="more-btn">
            자세히 보기 &gt;
          </button>
        </div>
        <p className="summary-subtext">지난달 대비 변화가 큰 기록을 확인해보세요.</p>

        {/* 요약 리스트 */}
        <ul className="summary-list">
          <li className="summary-item">
            <div className="icon-box shedding">
              <img src={iconShedding} alt="탈피" />
            </div>
            <div className="info">
              <div className="top">
                <strong>탈피</strong>
                <div className="bar-group">
                  <span className="dot active" />
                  <span className="dot active" />
                  <span className="dot active" />
                  <span className="dot active" />
                  <span className="dot active" />
                  <span className="dot" />
                  <span className="dot" />
                  <span className="dot" />
                </div>
                <span className="percent up">▲ 50%</span>
              </div>
              <p>이번 달에는 탈피가 1회 확인됐어요.</p>
            </div>
          </li>

          <li className="summary-item">
            <div className="icon-box poop">
              <img src={iconPoop} alt="배변" />
            </div>
            <div className="info">
              <div className="top">
                <strong>배변</strong>
                <div className="bar-group">
                  <span className="dot active poop" />
                  <span className="dot active poop" />
                  <span className="dot active poop" />
                  <span className="dot active poop" />
                  <span className="dot active poop" />
                  <span className="dot active poop" />
                </div>
                <span className="percent up">▲ 80%</span>
              </div>
              <p>지난달보다 배변 이상 기록이 늘었어요.</p>
            </div>
          </li>

          <li className="summary-item">
            <div className="icon-box clean">
              <img src={iconClean} alt="청소" />
            </div>
            <div className="info">
              <div className="top">
                <strong>청소</strong>
                <div className="bar-group">
                  <span className="dot active clean" />
                  <span className="dot active clean" />
                  <span className="dot active clean" />
                  <span className="dot active clean" />
                </div>
                <span className="percent down">▼ 40%</span>
              </div>
              <p>사육장 청소 주기가 지난달보다 길어졌어요.</p>
            </div>
          </li>
        </ul>
      </div>
    </div>
  );
}