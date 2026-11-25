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

import { fetchTemperature } from "../../api/climate";

const EMPTY = { current: null, measuredAt: null, hourly: [] };

const OK_MIN = 22;
const OK_MAX = 29;
const BAR_MIN = 16;
const BAR_MAX = 35;
const NIGHT = [[0, 6], [19.67, 24]];
const NIGHT_OK = [18, 22];

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
function TempChart({ hourly }) {
  const L = 56;
  const R = 355;
  const TOP = 502;
  const BOTTOM = 666;
  const BAR_W = 15;
  const BAR_MIN_H = 33;
  const BUCKET_H = 2;
  const TIP_W = 114;
  const TIP_H = 21;

  const x = (h) => L + (h / 24) * (R - L);
  const y = (v) => 511 + (32 - clamp(v, 16, 32)) * 9.5;
  const isNight = (h) => NIGHT.some(([a, b]) => h >= a && h < b);

  const merged = [];
  hourly.forEach((d) => {
    const i = Math.floor(d.h / BUCKET_H);
    const prev = merged[i];
    merged[i] = prev
      ? { h: prev.h, min: Math.min(prev.min, d.min), max: Math.max(prev.max, d.max) }
      : { h: i * BUCKET_H + BUCKET_H / 2, min: d.min, max: d.max };
  });
  const bars = merged.filter(Boolean).map((d) => {
    const rawTop = y(d.max);
    const rawBottom = y(d.min);
    const height = Math.min(BOTTOM - TOP, Math.max(BAR_MIN_H, rawBottom - rawTop));
    const top = clamp((rawTop + rawBottom) / 2 - height / 2, TOP, BOTTOM - height);
    return { ...d, top, height };
  });
  const peak = bars.length ? bars.reduce((a, b) => (b.max > a.max ? b : a), bars[0]) : null;

  const tipLeft = peak ? clamp(x(peak.h) - TIP_W / 2, L, R - TIP_W) : 0;
  const tipTop = peak ? Math.max(TOP - 4, peak.top - 31) : 0;
  const tailX = peak ? clamp(x(peak.h), tipLeft + 14, tipLeft + TIP_W - 14) : 0;

  const labelStyle = { fontSize: 12, fontWeight: 500, letterSpacing: "-0.24px" };

  return (
    <svg viewBox="32 484 326 204" className="chart__svg" role="img" aria-label="오늘의 온도 변화 그래프" style={{ width: "100%", height: "auto" }}>
      {[32, 28, 24, 20].map((v) => (
        <line key={`grid-${v}`} x1={55} x2={355} y1={y(v)} y2={y(v)} stroke="#FFDCB9" strokeWidth="1" strokeDasharray="3 3" />
      ))}

      {NIGHT.map(([a, b]) => (
        <rect key={`night-${a}`} x={x(a)} y={TOP} width={x(b) - x(a)} height={BOTTOM - TOP - 1} fill="rgba(211,238,255,0.5)" />
      ))}

      <rect x={x(NIGHT[0][1])} y={y(OK_MAX)} width={x(NIGHT[1][0]) - x(NIGHT[0][1])} height={y(OK_MIN) - y(OK_MAX)} fill="#E8F6CA" opacity="0.85" />
      {NIGHT.map(([a, b]) => (
        <rect key={`nok-${a}`} x={x(a)} y={y(NIGHT_OK[1])} width={x(b) - x(a)} height={y(NIGHT_OK[0]) - y(NIGHT_OK[1])} fill="#E8F6CA" opacity="0.85" />
      ))}

      <line x1={L} x2={L} y1={TOP} y2={BOTTOM} stroke="#FFC58D" strokeWidth="1" />
      <line x1={55} x2={R} y1={BOTTOM} y2={BOTTOM} stroke="#FFC58D" strokeWidth="1" />

      <line x1={x(NIGHT[0][1])} x2={x(NIGHT[1][0])} y1={y(OK_MAX)} y2={y(OK_MAX)} stroke="#5B911C" strokeWidth="1" strokeDasharray="3 2" />
      <line x1={L} x2={R} y1={y(OK_MIN)} y2={y(OK_MIN)} stroke="#5B911C" strokeWidth="1" strokeDasharray="3 2" />
      {NIGHT.map(([a, b]) => (
        <line key={`g-night-low-${a}`} x1={x(a)} x2={x(b)} y1={y(NIGHT_OK[0])} y2={y(NIGHT_OK[0])} stroke="#5B911C" strokeWidth="1" strokeDasharray="3 2" />
      ))}

      <text x={33} y={497.5} fill="#B47952" style={labelStyle}>℃</text>
      {[32, 28, 24, 20, 16].map((v) => (
        <text key={`ytick-${v}`} x={32} y={y(v) + 4.5} fill="#B47952" style={labelStyle}>
          {v}
        </text>
      ))}

      <text x={116} y={515.5} textAnchor="end" fill="#A0AFCC" style={labelStyle}>야간 구간</text>
      <text x={350} y={515.5} textAnchor="end" fill="#A0AFCC" style={labelStyle}>야간 구간</text>

      {[[0, 64], [6, 131], [12, 200.5], [18, 272.5], [24, 345.5]].map(([h, cx]) => (
        <text key={h} x={cx} y={683.5} textAnchor="middle" fill="#B47952" style={labelStyle}>
          {h}시
        </text>
      ))}

      {bars.map((d, i) => {
        const bx = x(d.h) - BAR_W / 2;
        const night = isNight(d.h);
        const warmH = d.min >= OK_MIN ? d.height : clamp(y(OK_MIN) - d.top, 0, d.height);
        return (
          <g key={d.h}>
            <clipPath id={`temp-bar-${i}`}>
              <rect x={bx} y={d.top} width={BAR_W} height={d.height} rx={BAR_W / 2} />
            </clipPath>
            <g clipPath={`url(#temp-bar-${i})`}>
              <rect x={bx} y={d.top} width={BAR_W} height={d.height} fill={night ? "#FFC58D" : "#FFB66F"} />
              {!night && d.max > OK_MIN && <rect x={bx} y={d.top} width={BAR_W} height={warmH} fill="#FF9834" />}
              {d === peak && <rect x={bx} y={d.top} width={BAR_W} height={Math.min(21, d.height)} fill="#FF6B01" />}
            </g>
          </g>
        );
      })}

      {peak && (
        <g style={{ filter: "drop-shadow(0 2px 2.5px rgba(0,0,0,0.25))" }}>
          <rect x={tipLeft} y={tipTop} width={TIP_W} height={TIP_H} rx={8} fill="#FFF1DA" stroke="#FF9834" strokeWidth="1" />
          <polygon points={`${tailX - 5},${tipTop + TIP_H} ${tailX},${tipTop + TIP_H + 5} ${tailX + 5},${tipTop + TIP_H}`} fill="#FFF1DA" stroke="#FF9834" strokeWidth="1" strokeLinejoin="round" />
          <line x1={tailX - 4} x2={tailX + 4} y1={tipTop + TIP_H} y2={tipTop + TIP_H} stroke="#FFF1DA" strokeWidth="2" />
          <text x={tipLeft + TIP_W / 2} y={tipTop + 14.5} textAnchor="middle" fill="#FF9834" style={{ fontSize: 10, fontWeight: 600 }}>
            최고 : {Math.round(peak.max)}℃ 최저 : {Math.round(peak.min)}℃
          </text>
        </g>
      )}
    </svg>
  );
}

export default function Temperature() {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [showInfoModal, setShowInfoModal] = useState(false);

  useEffect(() => {
    let cancelled = false;
    fetchTemperature()
      .then((res) => {
        if (!cancelled) setData(res);
      })
      .catch(() => {
        if (!cancelled) setData(EMPTY);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  if (!data) return <div className="temperature-page" />;

  if (data.current == null) {
    return (
      <div className="temperature-page">
        <div className="temperature-page__scroll">
          <header className="header">
            <button type="button" className="header__back" onClick={() => navigate(-1)}>
              <img src={backBtn} alt="뒤로가기" />
            </button>
            <h1 className="header__title">온도</h1>
          </header>
          <p style={{ textAlign: "center", marginTop: 80, lineHeight: 1.6 }}>
            아직 측정된 온도가 없어요.<br />센서 연결을 확인해주세요.
          </p>
          <button type="button" style={{ display: "block", margin: "16px auto" }} onClick={() => navigate("/my/sensor")}>
            센서 확인하기
          </button>
        </div>
      </div>
    );
  }

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