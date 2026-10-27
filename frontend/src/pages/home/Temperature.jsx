import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Temperature.scss";

// ── 목데이터 (나중에 API로 교체) ──
const CURRENT = { value: 24.2, status: "안정", measuredAt: "14:32" };
const HOURLY = [
  { h: 0.75, min: 18, max: 21 }, { h: 2.25, min: 19, max: 22 }, { h: 3.75, min: 19, max: 22 },
  { h: 5.25, min: 19, max: 22 }, { h: 6.75, min: 21, max: 24 }, { h: 8.25, min: 22, max: 25 },
  { h: 9.75, min: 22, max: 26 }, { h: 11.25, min: 23, max: 26 }, { h: 12.75, min: 23, max: 27 },
  { h: 14.25, min: 24, max: 27 }, { h: 15.75, min: 23, max: 26 },
];

// ── 차트 설정 ──
const W = 320, H = 190;
const PAD = { l: 34, r: 6, t: 8, b: 22 };
const Y_MIN = 16, Y_MAX = 32;
const NIGHT = [[0, 6], [21, 24]]; // 야간 구간(시)
const x = (h) => PAD.l + (h / 24) * (W - PAD.l - PAD.r);
const y = (v) => PAD.t + ((Y_MAX - v) / (Y_MAX - Y_MIN)) * (H - PAD.t - PAD.b);

function TempChart() {
  const hi = Math.max(...HOURLY.map((d) => d.max));
  const lo = Math.min(...HOURLY.map((d) => d.min));
  const barW = 9;

  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="chart" role="img" aria-label="오늘의 온도 변화 차트">
      {/* 야간 구간 */}
      {NIGHT.map(([a, b]) => (
        <g key={a}>
          <rect x={x(a)} y={PAD.t} width={x(b) - x(a)} height={H - PAD.t - PAD.b} className="chart__night" />
          <text x={(x(a) + x(b)) / 2} y={PAD.t + 10} className="chart__zone" textAnchor="middle">야간 구간</text>
        </g>
      ))}
      {/* 적정 구간: 낮 22~29, 밤 18~22 */}
      <rect x={x(6)} y={y(29)} width={x(21) - x(6)} height={y(22) - y(29)} className="chart__ok" />
      {NIGHT.map(([a, b]) => (
        <rect key={`n${a}`} x={x(a)} y={y(22)} width={x(b) - x(a)} height={y(18) - y(22)} className="chart__ok chart__ok--night" />
      ))}
      {/* 가이드 라인 */}
      {[29, 22, 18].map((v) => (
        <line key={v} x1={PAD.l} x2={W - PAD.r} y1={y(v)} y2={y(v)} className="chart__guide" />
      ))}
      {/* y축 */}
      {[16, 20, 24, 28, 32].map((v) => (
        <g key={v}>
          <line x1={PAD.l} x2={W - PAD.r} y1={y(v)} y2={y(v)} className="chart__grid" />
          <text x={PAD.l - 6} y={y(v) + 3} textAnchor="end" className="chart__tick">{v}</text>
        </g>
      ))}
      {/* x축 */}
      {[0, 6, 12, 18, 24].map((h) => (
        <text key={h} x={x(h)} y={H - 6} textAnchor={h === 0 ? "start" : h === 24 ? "end" : "middle"} className="chart__tick">
          {h}시
        </text>
      ))}
      {/* 시간대별 범위 막대 */}
      {HOURLY.map((d, i) => (
        <rect
          key={d.h}
          x={x(d.h) - barW / 2}
          y={y(d.max)}
          width={barW}
          height={y(d.min) - y(d.max)}
          rx={barW / 2}
          className={`chart__bar ${i >= 4 ? "is-hot" : ""}`}
        />
      ))}
      {/* 최고/최저 말풍선 */}
      <g transform={`translate(${x(12.5)}, ${PAD.t + 18})`}>
        <rect x={-48} y={-9} width={96} height={18} rx={4} className="chart__tip" />
        <text textAnchor="middle" y={4} className="chart__tip-text">최고 : {hi}°C 최저 : {lo}°C</text>
      </g>
    </svg>
  );
}

function CrestedGeckoModal({ onClose }) {
  // ESC로 닫기 + 팝업 떠 있는 동안 뒤 화면 스크롤 잠금
  useEffect(() => {
    const onKey = (e) => e.key === "Escape" && onClose();
    window.addEventListener("keydown", onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      window.removeEventListener("keydown", onKey);
      document.body.style.overflow = prev;
    };
  }, [onClose]);

  return (
    <div className="modal" onClick={onClose}>
      <div
        className="modal__panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="gecko-title"
        onClick={(e) => e.stopPropagation()} // 패널 안 클릭은 닫히지 않게
      >
        <header className="modal__head">
          <span className="modal__icon">🌡️</span>
          <h2 id="gecko-title">크레스티드 게코 온도 기준</h2>
          <button type="button" className="modal__close" onClick={onClose} aria-label="닫기">✕</button>
        </header>

        <p className="modal__lead">건강한 생활을 위해 아래의 온도 범위를 참고해 주세요.</p>

        <dl className="range">
          <div>
            <dt>주간 온도 (낮)</dt>
            <dd>22 ~ 29°C</dd>
            <p>이 범위에서 가장 활발하고 안정적으로 생활할 수 있어요.</p>
          </div>
          <div>
            <dt>야간 온도 (밤)</dt>
            <dd>18 ~ 22°C</dd>
            <p>야간의 약간의 온도 하강은 자연스러운 변화로, 건강에 도움이 돼요.</p>
          </div>
        </dl>

        <hr />

        <h3 className="modal__sub">주의 사항</h3>
        <ul className="modal__list">
          <li>29°C 이상의 높은 온도가 장시간 지속되지 않도록 해주세요.</li>
          <li>낮 22°C 이하로 계속 유지되는 경우, 실내 온도를 확인해주세요.</li>
          <li>계절, 실내 환경과 구조에 따라 적정 범위는 변동될 수 있어요.</li>
          <li>직사광선이 닿는 곳이나 외풍이 직접 닿는 곳은 피해주세요.</li>
        </ul>

        <div className="modal__note">
          <strong>본 서비스는 하나의 센서에서 측정된 온도를 기준으로 안내해요.</strong>
          <p>사육장 내 위치에 따라 온도가 다를 수 있으므로, 위 정보는 참고용으로 활용해 주세요.</p>
          <small>출처 : ReptiFiles, RSPCA, PetMD</small>
        </div>
      </div>
    </div>
  );
}

export default function TemperaturePage() {
  const navigate = useNavigate();
  const [showStandard, setShowStandard] = useState(false);

  return (
    <div className="temp">
      <header className="temp__header">
        <button type="button" className="temp__back" onClick={() => navigate(-1)} aria-label="뒤로가기">‹</button>
        <h1>온도</h1>
      </header>

      <main className="temp__body">
        {/* 현재 온도 */}
        <section className="panel panel--strong now">
          <div className="now__top">
            <h2><span>🌡️</span> 현재 온도</h2>
            <small>마지막 측정 {CURRENT.measuredAt}</small>
          </div>
          <div className="now__value">{CURRENT.value}°C</div>
          <span className="badge badge--ok">{CURRENT.status}</span>
        </section>

        {/* 적정 온도 범위 */}
        <section className="panel">
          <div className="panel__top">
            <h2><span>🕐</span> 적정 온도 범위</h2>
            <button type="button" className="pill" onClick={() => setShowStandard(true)}>
              ⓘ 크레스티드 게코 기준 보기
            </button>
          </div>
          <div className="gauge">
            {/* left: (현재온도 - 16) / (35 - 16) 처럼 게이지 범위에 맞춰 계산 */}
            <span className="gauge__marker" style={{ left: `${((CURRENT.value - 16) / 19) * 100}%` }}>🦎</span>
          </div>
          <div className="gauge__labels">
            <span>너무 낮아요</span><b>22°C</b><b>29°C</b><span>너무 높아요</span>
          </div>
        </section>

        {/* 오늘의 온도 변화 */}
        <section className="panel">
          <h2><span>📈</span> 오늘의 온도 변화</h2>
          <div className="chart-unit">°C</div>
          <TempChart />
          <ul className="legend">
            <li><i className="legend__bar" /><div>시간대별 온도 범위<small>(최저 ~ 최고)</small></div></li>
            <li><i className="legend__ok" /><div>적정 구간<small>(22°C ~ 29°C)</small></div></li>
            <li><i className="legend__night" /><div>야간 구간</div></li>
          </ul>
        </section>

        {/* 관리 팁 */}
        <section className="tip">
          <h2>📋 지금 이렇게 관리해주세요!</h2>
          <h3>현재 온도 흐름이 안정적이에요.</h3>
          <p>지금은 별도로 조절할 필요가 없어요. 현재 환경을 유지하면서 갑작스러운 온도 변화가 생기는지 계속 확인해주세요.</p>
          <hr />
          <h4>추가로 이런 점도 확인해보세요.</h4>
          <ul>
            <li>사육장이 직사광선을 받지 않는지 확인해주세요.</li>
            <li>히팅 장비를 사용하는 경우, 설정값과 작동 상태를 주기적으로 점검해주세요.</li>
          </ul>
        </section>
      </main>

      {showStandard && <CrestedGeckoModal onClose={() => setShowStandard(false)} />}

      <TabBar />
    </div>
  );
}
