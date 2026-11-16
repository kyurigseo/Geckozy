import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Temperature.scss";

import backBtn from "../../assets/home/before_btn_icon.png";
import tempIcon from "../../assets/home/temp_icon.png";
import clockIcon from "../../assets/home/clock_icon.png";
import chartIcon from "../../assets/home/chart_icon.png";
import docIcon from "../../assets/home/doc_icon.png";
import stateOk from "../../assets/home/stable_icon.png";
import closeBtn from "../../assets/home/close_btn.png";

// ─────────── 임시 데이터 (연동 전) ───────────
// TODO: 백엔드 API가 나오면 이 함수만 실제 요청으로 교체 
// 응답 모양: { current: 현재 온도(℃), measuredAt: 마지막 측정 시각(ISO 문자열), hourly: [{ h: 시각(0~24), min, max }] }
const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const fetchTemperature = async () => {
  await delay(300);
  return {
    current: 24.2,
    measuredAt: "2026-11-01T14:32:00",
    hourly: [
      { h: 0.75, min: 18, max: 21 },
      { h: 2.25, min: 19, max: 22 },
      { h: 3.75, min: 19, max: 22 },
      { h: 5.25, min: 19, max: 22 },
      { h: 6.75, min: 21, max: 24 },
      { h: 8.25, min: 22, max: 25 },
      { h: 9.75, min: 22, max: 26 },
      { h: 11.25, min: 23, max: 26 },
      { h: 12.75, min: 23, max: 27 },
      { h: 14.25, min: 24, max: 27 },
    ],
  };
};

const OK_MIN = 22; 
const OK_MAX = 29;
const BAR_MIN = 16; 
const BAR_MAX = 35;
const NIGHT = [[0, 6], [19, 24]];
const NIGHT_OK = [18, 22]; // 

const GUIDES = {
  ok: {
    title: "현재 온도 흐름이 안정적이에요.",
    body: "지금은 별도로 조절할 필요가 없어요. 현재 환경을 유지하면서 갑작스러운 온도 변화가 생기는지만 계속 확인해주세요.",
    tips: [
      "사육장이 직사광선을 받지 않는지 확인해주세요.",
      "히팅 장비를 사용하는 경우, 설정값과 작동 상태를 주기적으로 점검해주세요.",
    ],
  },
  low: {
    title: "온도가 낮은 편이에요.",
    body: "낮 22°C 이하로 계속 유지되고 있어요. 실내 온도와 히팅 장비의 작동 상태를 확인해주세요.",
    tips: [
      "히팅 장비의 설정값과 작동 상태를 점검해주세요.",
      "외풍이 직접 닿는 곳은 피해주세요.",
    ],
  },
  high: {
    title: "온도가 높은 편이에요.",
    body: "29°C 이상의 높은 온도가 장시간 지속되지 않도록 해주세요.",
    tips: [
      "직사광선이 닿는 곳은 피해주세요.",
      "히팅 장비의 설정값과 작동 상태를 점검해주세요.",
    ],
  },
};

// ─────────── 계산 함수 ───────────
const clamp = (v, lo, hi) => Math.min(hi, Math.max(lo, v));
const toPercent = (t) => clamp(((t - BAR_MIN) / (BAR_MAX - BAR_MIN)) * 100, 0, 100);

const getStatus = (t) => {
  if (t < OK_MIN) return { key: "low", label: "너무 낮아요" };
  if (t > OK_MAX) return { key: "high", label: "너무 높아요" };
  return { key: "ok", label: "안정" };
};

const formatTime = (iso) => {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "--:--";
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
};

// ─────────── 적정 온도 범위 바 ───────────
function RangeBar({ value }) {
  const lo = toPercent(OK_MIN);
  const hi = toPercent(OK_MAX);
  const pos = toPercent(value);

  const ticks = [0, 25, 50, 75, 100];

  return (
    <div className="range-bar-container">
      <div
        className="range-bar"
        style={{
          position: "relative",
          background: `linear-gradient(90deg, #C9DDF0 0%, #E8F6CA ${lo}%, #E8F6CA ${hi}%, #FFE9B8 ${hi + 10}%, #F7C9CF 100%)`,
        }}
      >
        {ticks.map((pct, i) => (
          <i 
            key={i} 
            className="range-bar__tick" 
            style={{ 
              position: "absolute",
              top: 0,
              bottom: 0,
              left: `${pct}%`,
              width: 0,
              borderLeft: "1.5px dashed #FFFFFF",
              opacity: 0.9,
              pointerEvents: "none"
            }} 
          />
        ))}

        <img className="range-bar__character" src={stateOk} alt="현재 온도 위치" style={{ left: `${pos}%` }} />
      </div>

      <div className="range-bar__labels" style={{ position: "relative", height: 18 }}>
        <span style={{ position: "absolute", left: 0 }}>너무 낮아요</span>
        <span className="range-bar__mark" style={{ position: "absolute", left: `${lo}%`, transform: "translateX(-50%)" }}>
          {OK_MIN}°C
        </span>
        <span className="range-bar__mark" style={{ position: "absolute", left: `${hi}%`, transform: "translateX(-50%)" }}>
          {OK_MAX}°C
        </span>
        <span style={{ position: "absolute", right: 0 }}>너무 높아요</span>
      </div>
    </div>
  );
}
// ─────────── 오늘의 온도 변화 그래프 ───────────
function TempChart({ hourly }) {
  const W = 320;
  const H = 200;
  const PAD = { l: 30, r: 10, t: 24, b: 24 };
  const BAR_W = 13;

  const highs = hourly.map((d) => d.max);
  const lows = hourly.map((d) => d.min);
  const hi = Math.max(...highs);
  const lo = Math.min(...lows);

  const yMin = Math.min(16, Math.floor(lo / 4) * 4);
  const yMax = Math.max(32, Math.ceil(hi / 4) * 4);
  const ticks = [];
  for (let v = yMin; v <= yMax; v += 4) ticks.push(v);

  const x = (h) => PAD.l + (h / 24) * (W - PAD.l - PAD.r);
  const y = (v) => PAD.t + ((yMax - v) / (yMax - yMin)) * (H - PAD.t - PAD.b);
  const plotH = H - PAD.t - PAD.b;

  const peak = hourly.reduce((a, b) => (b.max > a.max ? b : a), hourly[0]);
  const tipX = clamp(x(peak.h), PAD.l + 45, W - PAD.r - 45);
  const tipY = Math.max(PAD.t + 10, y(peak.max) - 18);

  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="chart__svg" role="img" aria-label="오늘의 온도 변화 그래프" style={{ width: "100%", height: "auto" }}>
      {ticks.map((v) => (
        <line key={`grid-${v}`} x1={PAD.l} x2={W - PAD.r} y1={y(v)} y2={y(v)} stroke="#E6D9C6" strokeWidth="0.8" strokeDasharray="2 2" />
      ))}

      {NIGHT.map(([a, b]) => (
        <rect key={`night-${a}`} x={x(a)} y={PAD.t} width={x(b) - x(a)} height={plotH} fill="#E8F2F7" />
      ))}

      {NIGHT.map(([a, b]) => (
        <text 
          key={`nt-txt-${a}`} 
          x={(x(a) + x(b)) / 2} 
          y={PAD.t + 15} 
          textAnchor="middle" 
          fontSize="11" 
          fontWeight="600" 
          fill="#94B9D0" 
          letterSpacing="-0.02em"
        >
          야간 구간
        </text>
      ))}

      <rect x={x(NIGHT[0][1])} y={y(OK_MAX)} width={x(NIGHT[1][0]) - x(NIGHT[0][1])} height={y(OK_MIN) - y(OK_MAX)} fill="#E8F6CA" opacity="0.85" />
      {NIGHT.map(([a, b]) => (
        <rect key={`nok-${a}`} x={x(a)} y={y(NIGHT_OK[1])} width={x(b) - x(a)} height={y(NIGHT_OK[0]) - y(NIGHT_OK[1])} fill="#E8F6CA" opacity="0.85" />
      ))}

      <line x1={PAD.l} x2={PAD.l} y1={PAD.t} y2={H - PAD.b} stroke="#E6D9C6" strokeWidth="1" />
      <line x1={PAD.l} x2={W - PAD.r} y1={H - PAD.b} y2={H - PAD.b} stroke="#E6D9C6" strokeWidth="1" />

      <line x1={x(NIGHT[0][1])} x2={x(NIGHT[1][0])} y1={y(OK_MAX)} y2={y(OK_MAX)} stroke="#5B911C" strokeWidth="1" strokeDasharray="3 2" />
      <line x1={PAD.l} x2={W - PAD.r} y1={y(OK_MIN)} y2={y(OK_MIN)} stroke="#5B911C" strokeWidth="1" strokeDasharray="3 2" />
      {NIGHT.map(([a, b]) => (
        <line key={`g-night-low-${a}`} x1={x(a)} x2={x(b)} y1={y(NIGHT_OK[0])} y2={y(NIGHT_OK[0])} stroke="#5B911C" strokeWidth="1" strokeDasharray="3 2" />
      ))}

      <text x={PAD.l - 12} y={PAD.t - 10} textAnchor="start" fontSize="12" fontWeight="600" fill="#B38A68" letterSpacing="-0.02em">
        °C
      </text>

      {ticks.map((v) => (
        <text key={`ytick-${v}`} x={PAD.l - 8} y={y(v) + 4} textAnchor="end" fontSize="12" fontWeight="600" fill="#B38A68" letterSpacing="-0.02em">
          {v}
        </text>
      ))}

      {[0, 6, 12, 18, 24].map((h) => (
        <text 
          key={h} 
          x={x(h)} 
          y={H - 4} 
          textAnchor={h === 0 ? "start" : h === 24 ? "end" : "middle"} 
          fontSize="12" 
          fontWeight="600" 
          fill="#B38A68" 
          letterSpacing="-0.02em"
        >
          {h}시
        </text>
      ))}

      {hourly.map((d) => (
        <rect
          key={d.h}
          x={x(d.h) - BAR_W / 2}
          y={y(d.max)}
          width={BAR_W}
          height={Math.max(BAR_W, y(d.min) - y(d.max))}
          rx={BAR_W / 2}
          fill={d.max >= 26 ? "#FF7A00" : "#FFB066"}
        />
      ))}

      <g transform={`translate(${tipX}, ${tipY})`}>
        <rect x={-52} y={-21} width={104} height={23} rx={7} fill="#FFEED6" stroke="#FF9834" strokeWidth="1.2" />
        <polygon points="-4,2 4,2 0,7" fill="#FFECCB" stroke="#FF9834" strokeWidth="1.2" strokeLinejoin="round" />
        <line x1={-3.5} y1={2} x2={3.5} y2={2} stroke="#FFEED6" strokeWidth="1.5" />
        <text textAnchor="middle" y={-5} fontSize="10" fontWeight="600" fill="#FF9834" >
          최고 : {hi}°C 최저 : {lo}°C
        </text>
      </g>
    </svg>
  );
}

export default function Temperature() {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [showInfoModal, setShowInfoModal] = useState(false);

  useEffect(() => {
    let cancelled = false;
    fetchTemperature().then((res) => {
      if (!cancelled) setData(res);
    });
    return () => {
      cancelled = true;
    };
  }, []);

  if (!data) return <div className="temperature-page" />;

  const status = getStatus(data.current);
  const guide = GUIDES[status.key];

  return (
    <div className="temperature-page">
      <div className="temperature-page__scroll">
        <header className="header">
          <button type="button" className="header__back" onClick={() => navigate(-1)}>
            <img src={backBtn} alt="뒤로가기" />
          </button>
          <h1 className="header__title">온도</h1>
        </header>

        <main className="temperature-page__content">
          <section className="card card--current">
            <div className="card__header">
              <h2 className="card__title">
                <img src={tempIcon} alt="" className="icon" /> 현재 온도
              </h2>
              <span className="card__subtext">마지막 측정 {formatTime(data.measuredAt)}</span>
            </div>

            <div className="current-temp">
              <div className="current-temp__value">
                {data.current.toFixed(1)}
                <span>°C</span>
              </div>
              <div className={`badge ${status.key === "ok" ? "badge--ok" : "badge--warn"}`}>
                {status.key === "ok" && <img src={stateOk} alt="" className="badge__icon" />}
                {status.label}
              </div>
            </div>
          </section>

          <section className="card card--range">
            <div className="card__header card__header--space">
              <h2 className="card__title">
                <img src={clockIcon} alt="" className="icon" /> 적정 온도 범위
              </h2>
              <button type="button" className="info-btn" onClick={() => setShowInfoModal(true)}>
                <p>!</p>
                크레스티드 게코 기준 보기
              </button>
            </div>

            <RangeBar value={data.current} />
          </section>

          <section className="card card--chart">
            <div className="card__header">
              <h2 className="card__title">
                <img src={chartIcon} alt="" className="icon" /> 오늘의 온도 변화
              </h2>
            </div>

            <div className="chart">
              <TempChart hourly={data.hourly} />
            </div>

            <div className="chart-legend">
              <div className="chart-legend__item">
                <span className="dot dot--orange" />
                <span>
                  시간대별 온도 범위
                  <br />
                  <small>(최저 ~ 최고)</small>
                </span>
              </div>
              <div className="chart-legend__item">
                <span className="dot dot--green" />
                <span>
                  적정 구간
                  <br />
                  <small>({OK_MIN}°C ~ {OK_MAX}°C)</small>
                </span>
              </div>
              <div className="chart-legend__item">
                <span className="dot dot--blue" />
                <span>야간 구간</span>
              </div>
            </div>
          </section>

          <section className="card card--guide">
            <div className="card__header">
              <h2 className="card__title">
                <img src={docIcon} alt="" className="icon" /> 지금 이렇게 관리해주세요!
              </h2>
            </div>

            <div className="guide-content">
              <h3>{guide.title}</h3>
              <p>{guide.body}</p>

              <hr className="guide-content__line" />

              <h4>추가로 이런 점도 확인해보세요.</h4>
              <ul>
                {guide.tips.map((tip) => (
                  <li key={tip}>{tip}</li>
                ))}
              </ul>
            </div>
          </section>
        </main>
      </div>

      {showInfoModal && (
        <div className="modal-overlay" onClick={() => setShowInfoModal(false)}>
          <div className="modal-content" role="dialog" aria-modal="true" onClick={(e) => e.stopPropagation()}>
            <button type="button" className="modal-content__close" onClick={() => setShowInfoModal(false)}>
              <img src={closeBtn} alt="닫기" />
            </button>

            <div className="modal-content__title">
              <img src={tempIcon} alt="" />
              <h2>크레스티드 게코 온도 기준</h2>
            </div>

            <p className="modal-content__sub">건강한 생활을 위해 아래의 온도 범위를 참고해 주세요.</p>

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
                <strong>본 서비스는 하나의 센서에서 측정된 온도를 기준으로 안내해요.</strong>
                <br />
                사육장 내 위치에 따라 온도가 다를 수 있으므로,<br/> 위 정보는 참고용으로 활용해 주세요.
              </p>
              <small>출처 : ReptiFiles, RSPCA, PetMD</small>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
