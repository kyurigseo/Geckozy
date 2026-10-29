import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Humidity.scss";

// asset 경로들은 프로젝트 폴더 구조에 맞춰 확인해 주세요!
import backBtn from "../../assets/home/before_btn_icon.png";
import humidIcon from "../../assets/home/humid_icon.png";
import chartIcon from "../../assets/home/chart_icon.png";
import docIcon from "../../assets/home/doc_icon.png";
import stateWarn from "../../assets/home/warning_icon.png";
import closeBtn from "../../assets/home/close_btn.png";
import rightArrow from "../../assets/home/darker_rightarrow_icon.png";

export default function Humidity() {
  const navigate = useNavigate();
  // 습윤 사이클 모달 팝업 상태
  const [showCycleModal, setShowCycleModal] = useState(false);

  return (
    <div className="humidity-page">
      <div className="humidity-page__scroll">
        {/* 헤더 */}
        <header className="header">
          <button type="button" className="header__back" onClick={() => navigate(-1)}>
            <img src={backBtn} alt="뒤로가기" />
          </button>
          <h1 className="header__title">습도</h1>
        </header>

        <main className="humidity-page__content">
          {/* 1. 현재 습도 카드 */}
          <section className="card">
            <div className="card__header">
              <h2 className="card__title">
                <img src={humidIcon} alt="" className="icon" /> 현재 습도
              </h2>
              <span className="card__subtext">마지막 측정 14:32</span>
            </div>

            <div className="current-humid">
              <div className="current-humid__value">
                78<span>%</span>
              </div>
              <div className="badge badge--warn">
                <img src={stateWarn} alt="" className="badge__icon" />
                확인 필요
              </div>
            </div>
          </section>

          {/* 2. 습윤 사이클이란? 카드 */}
          <section className="card">
            <div className="card__header">
              <h2 className="card__title">
                <img src={humidIcon} alt="" className="icon" /> 습윤 사이클이란?
              </h2>
              <button
                type="button"
                className="cycle-summary__btn"
                onClick={() => setShowCycleModal(true)}
              >
                왜 중요한가요? <img src={rightArrow} alt="" />
              </button>
            </div>
            <p className="cycle-summary__desc">
              분무 후 올라간 습도가 시간이 지나며 다시 낮아지는 과정으로, 높은 습도가 계속 유지되는 것보다, 습해진 뒤 충분히 건조되는 과정이 반복되는지 함께 확인하는 것이 중요해요.
            </p>
          </section>

          {/* 3. 오늘의 습도 변화 차트 카드 */}
          <section className="card">
            <div className="card__header">
              <h2 className="card__title">
                <img src={chartIcon} alt="" className="icon" /> 오늘의 습도 변화
              </h2>
            </div>

            <div className="chart-area">
              <div className="chart-placeholder">
                <div className="chart-placeholder__unit">%</div>
                <div className="chart-placeholder__bg">
                  <div className="chart-placeholder__tag tag--night-1">야간 구간</div>
                  <div className="chart-placeholder__tag tag--spray-1">분무</div>
                  <div className="chart-placeholder__tag tag--spray-2">분무</div>
                  <div className="chart-placeholder__tag tag--night-2">야간 구간</div>
                </div>
                <div className="chart-placeholder__x-axis">
                  <span>0시</span>
                  <span>6시</span>
                  <span>12시</span>
                  <span>18시</span>
                  <span>24시</span>
                </div>
              </div>

              {/* 그래프 범례 */}
              <div className="chart-legend">
                <div className="chart-legend__item">
                  <span className="dot dot--blue" />
                  <span>시간대별 습도</span>
                </div>
                <div className="chart-legend__item">
                  <span className="line line--orange" />
                  <span>올바른 습윤 사이클</span>
                </div>
                <div className="chart-legend__item">
                  <span className="dot dot--green" />
                  <span>권장 분무 시간<br/><small>(아침/저녁 간격)</small></span>
                </div>
              </div>
            </div>
          </section>

          {/* 4. 가이드 카드 */}
          <section className="card card--guide">
            <div className="card__header">
              <h2 className="card__title">
                <img src={docIcon} alt="" className="icon" /> 지금 이렇게 관리해주세요!
              </h2>
            </div>

            <div className="guide-content">
              <h3>높은 습도가 계속되고 있어요</h3>
              <p>
                지금은 추가로 분무하지 마세요. 환기가 원활한지 확인하고, 바닥재나 사육장 내부가 지나치게 젖어 있다면 충분히 건조될 시간을 주세요.
              </p>

              <hr className="guide-content__line" />

              <h4>추가로 이런 점도 확인해보세요.</h4>
              <ul>
                <li>분무량이 평소보다 많지 않았는지 확인해주세요.</li>
                <li>높은 습도가 계속 유지되는 환경은 곰팡이, 곰팡이성 성장 위험을 높일 수 있어요.</li>
              </ul>
            </div>
          </section>
        </main>
      </div>

      {/* 습윤 사이클 모달 팝업 */}
      {showCycleModal && (
        <div className="modal-overlay" onClick={() => setShowCycleModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <button
              type="button"
              className="modal-content__close"
              onClick={() => setShowCycleModal(false)}
            >
              <img src={closeBtn} alt="닫기" />
            </button>

            <div className="modal-content__title">
              <img src={humidIcon} alt="" />
              <h2>습윤 사이클이 왜 중요한가요?</h2>
            </div>

            <p className="modal-content__sub">
              높은 습도가 계속 유지되는 것 보다, 분무 후 습도가 올라갔다가 다시 건조되는 흐름이 반복될 때 더 건강하게 지낼 수 있어요.
            </p>

            <div className="modal-cycle-box">
              <h4>이렇게 확인해보세요</h4>
              <div className="modal-cycle-flow">
                <span>분무</span>
                <span className="arrow">→</span>
                <span>습도 상승</span>
                <span className="arrow">→</span>
                <span>서서히 건조</span>
                <span className="arrow">→</span>
                <span>다음 분무</span>
              </div>
              <p className="modal-cycle-box__sub">
                습도가 계속 높게 유지되거나 너무 빠르게 낮아진다면 환기 상태, 분무량, 실내 환경을 함께 확인해보세요.
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}