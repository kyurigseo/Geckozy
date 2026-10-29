import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Temperature.scss";

import backBtn from "../../assets/home/before_btn_icon.png";
import tempIcon from "../../assets/home/temp_icon.png";
import clockIcon from "../../assets/home/clock_icon.png";
import chartIcon from "../../assets/home/chart_icon.png";
import docIcon from "../../assets/home/doc_icon.png";
import stateOk from "../../assets/home/stable_icon.png";
import closeBtn from "../../assets/home/close_btn.png";

export default function Temperature() {
  const navigate = useNavigate();
  const [showInfoModal, setShowInfoModal] = useState(false);

  return (
    <div className="temperature-page">
      {/* Home 구조와 동일한 스크롤 박스 */}
      <div className="temperature-page__scroll">
        {/* 헤더 */}
        <header className="header">
          <button type="button" className="header__back" onClick={() => navigate(-1)}>
            <img src={backBtn} alt="뒤로가기" />
          </button>
          <h1 className="header__title">온도</h1>
        </header>

        <main className="temperature-page__content">
          {/* 1. 현재 온도 카드 */}
          <section className="card">
            <div className="card__header">
              <h2 className="card__title">
                <img src={tempIcon} alt="" className="icon" /> 현재 온도
              </h2>
              <span className="card__subtext">마지막 측정 14:32</span>
            </div>

            <div className="current-temp">
              <div className="current-temp__value">
                24.2<span>°C</span>
              </div>
              <div className="badge badge--ok">
                <img src={stateOk} alt="" className="badge__icon" />
                안정
              </div>
            </div>
          </section>

          {/* 2. 적정 온도 범위 카드 */}
          <section className="card">
            <div className="card__header card__header--space">
              <h2 className="card__title">
                <img src={clockIcon} alt="" className="icon" /> 적정 온도 범위
              </h2>
              <button
                type="button"
                className="info-btn"
                onClick={() => setShowInfoModal(true)}
              >
                <p>!</p>
                크레스티드 게코 기준 보기
              </button>
            </div>

            <div className="range-bar-container">
              <div className="range-bar">
                <div className="range-bar__section range-bar__section--low" />
                <div className="range-bar__section range-bar__section--ok">
                  <img src={stateOk} alt="" className="range-bar__character" />
                </div>
                <div className="range-bar__section range-bar__section--warn" />
                <div className="range-bar__section range-bar__section--high" />
              </div>

              <div className="range-bar__labels">
                <span>너무 낮아요</span>
                <span className="range-bar__mark">22°C</span>
                <span className="range-bar__mark">29°C</span>
                <span>너무 높아요</span>
              </div>
            </div>
          </section>

          {/* 3. 오늘의 온도 변화 차트 카드 */}
          <section className="card">
            <div className="card__header">
              <h2 className="card__title">
                <img src={chartIcon} alt="" className="icon" /> 오늘의 온도 변화
              </h2>
            </div>

            <div className="chart-area">
              <div className="chart-placeholder">
                <div className="chart-placeholder__unit">°C</div>
                <div className="chart-placeholder__bg">
                  <div className="chart-placeholder__night chart-placeholder__night--left">
                    <span>야간 구간</span>
                  </div>
                  <div className="chart-placeholder__night chart-placeholder__night--right">
                    <span>야간 구간</span>
                  </div>
                  <div className="chart-placeholder__tooltip">
                    최고 : 26°C 최저 : 23°C
                  </div>
                </div>
                <div className="chart-placeholder__x-axis">
                  <span>0시</span>
                  <span>6시</span>
                  <span>12시</span>
                  <span>18시</span>
                  <span>24시</span>
                </div>
              </div>

              <div className="chart-legend">
                <div className="chart-legend__item">
                  <span className="dot dot--orange" />
                  <span>시간대별 온도 범위<br/><small>(최저 ~ 최고)</small></span>
                </div>
                <div className="chart-legend__item">
                  <span className="dot dot--green" />
                  <span>적정 구간<br/><small>(22°C ~ 29°C)</small></span>
                </div>
                <div className="chart-legend__item">
                  <span className="dot dot--blue" />
                  <span>야간 구간</span>
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
              <h3>현재 온도 흐름이 안정적이에요.</h3>
              <p>
                지금은 별도로 조절할 필요가 없어요. 현재 환경을 유지하면서 갑작스러운 온도 변화가 생기는지만 계속 확인해주세요.
              </p>

              <hr className="guide-content__line" />

              <h4>추가로 이런 점도 확인해보세요.</h4>
              <ul>
                <li>사육장이 직사광선을 받지 않는지 확인해주세요.</li>
                <li>히팅 장비를 사용하는 경우, 설정값과 작동 상태를 주기적으로 점검해주세요.</li>
              </ul>
            </div>
          </section>
        </main>
      </div>

      {/* 크레스티드 게코 온도 기준 모달 팝업 */}
      {showInfoModal && (
        <div className="modal-overlay" onClick={() => setShowInfoModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <button
              type="button"
              className="modal-content__close"
              onClick={() => setShowInfoModal(false)}
            >
              <img src={closeBtn} alt="닫기" />
            </button>

            <div className="modal-content__title">
              <img src={tempIcon} alt="" />
              <h2>크레스티드 게코 온도 기준</h2>
            </div>

            <p className="modal-content__sub">
              건강한 생활을 위해 아래의 온도 범위를 참고해 주세요.
            </p>

            <div className="modal-info-group">
              <div className="modal-info-item">
                <div className="modal-info-item__label">
                  <h3>주간 온도 (낮)</h3>
                  <p>이 범위에서 가장 활발하고 안정적으로 생활할 수 있어요.</p>
                </div>
                <div className="modal-info-item__value">22 ~ 29°C</div>
              </div>

              <div className="modal-info-item">
                <div className="modal-info-item__label">
                  <h3>야간 온도 (밤)</h3>
                  <p>야간의 약간의 온도 하강은 자연스러운 변화로, 건강에 도움이 돼요.</p>
                </div>
                <div className="modal-info-item__value">18 ~ 22°C</div>
              </div>
            </div>

            <div className="modal-warning">
              <h4>주의 사항</h4>
              <ul>
                <li>29°C 이상의 높은 온도가 장시간 지속되지 않도록 해주세요.</li>
                <li>낮 22°C 이하로 계속 유지되는 경우, 실내 온도를 확인해주세요.</li>
                <li>계절, 실내 환경과 구조에 따라 적정 범위는 변동될 수 있어요.</li>
                <li>직사광선이 닿는 곳이나 외풍이 직접 닿는 곳은 피해주세요.</li>
              </ul>
            </div>

            <div className="modal-notice">
              <p>
                <strong>본 서비스는 하나의 센서에서 측정된 온도를 기준으로 안내해요.</strong><br />
                사육장 내 위치에 따라 온도가 다를 수 있으므로, 위 정보는 참고용으로 활용해 주세요.
              </p>
              <small>출처 : ReptiFiles, RSPCA, PetMD</small>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}