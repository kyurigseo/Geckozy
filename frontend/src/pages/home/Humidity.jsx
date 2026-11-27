import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Humidity.scss";

import backBtn from "../../assets/home/before_btn_icon.png";
import chartIcon from "../../assets/home/chart_icon.png";
import docIcon from "../../assets/home/doc_icon.png";
import stateOk from "../../assets/home/stable_icon.png";
import closeBtn from "../../assets/home/close_btn.png";
import humidityIcon from "../../assets/home/humid_icon.png";
import warnIcon from "../../assets/home/warning_icon.png";

import { fetchHumidity } from "../../api/climate";

const clamp = (v, lo, hi) => Math.min(hi, Math.max(lo, v));

const EMPTY = { current: null, measuredAt: null, measured: [], ideal: [], sprayZones: [] };

const HIGH = 70;
const LOW = 40;
const HIGH_STREAK = 4;
const NIGHT = [[0, 6], [19.67, 24]];
const SPRAY_ZONES = [[6, 8.8], [16.85, 19.67]];

const GUIDES = {
  high: {
    title: "높은 습도가 계속되고 있어요",
    body: "지금은 추가로 분무하지 마세요. 환기가 원활한지 확인하고, 바닥재나 사육장 내부가 지나치게 젖어 있다면 충분히 건조될 시간을 주세요.",
    tips: [
      "분무량이 평소보다 많지 않았는지 확인해주세요.",
      "높은 습도가 계속 유지되는 환경은 곰팡이, 곰팡이성 성장 위험을 높일 수 있어요.",
    ],
  },
  low: {
    title: "습도가 낮은 편이에요",
    body: "다음 분무 시간에 맞춰 분무해주세요. 바닥재가 완전히 말라 있다면 분무량을 조금 늘려보고 습도가 올라가는지 확인해보세요.",
    tips: [
      "분무 후 습도가 올라갔다가 다시 낮아지는지 지켜봐주세요.",
      "환기구가 너무 많이 열려 있지 않은지 확인해주세요.",
    ],
  },
  ok: {
    title: "습윤 사이클이 잘 유지되고 있어요",
    body: "분무 후 습도가 올라갔다가 다시 낮아지는 흐름이 이어지고 있어요. 지금처럼 유지하면서 계속 확인해주세요.",
    tips: [
      "분무량이 평소와 비슷한지 확인해주세요.",
      "환기가 원활한지 주기적으로 확인해주세요.",
    ],
  },
};

const FLOW_STEPS = ["분무", "습도 상승", "서서히 건조", "다음 분무"];

const getStatus = (current, measured) => {
  const recent = measured.slice(-HIGH_STREAK);
  const stuckHigh = recent.length === HIGH_STREAK && recent.every((p) => p.v >= HIGH);
  if (stuckHigh) return { key: "high", label: "확인 필요" };
  if (current < LOW) return { key: "low", label: "너무 건조해요" };
  return { key: "ok", label: "안정" };
};

const formatTime = (iso) => {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "--:--";
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
};

function HumidityChart({ measured, ideal, sprayZones }) {
  const L = 56;
  const R = 355;
  const TOP = 493;
  const BOTTOM = 657;

  const x = (h) => L + (h / 24) * (R - L);
  const y = (v) => 502 + (100 - clamp(v, 0, 100)) * 1.5;

  const smoothPath = (points) => {
    const p = points.map(({ h, v }) => [x(h), y(v)]);
    let d = `M ${p[0][0]} ${p[0][1]}`;
    for (let i = 0; i < p.length - 1; i++) {
      const p0 = p[i - 1] || p[i];
      const p1 = p[i];
      const p2 = p[i + 1];
      const p3 = p[i + 2] || p2;
      const c1 = [p1[0] + (p2[0] - p0[0]) / 6, p1[1] + (p2[1] - p0[1]) / 6];
      const c2 = [p2[0] - (p3[0] - p1[0]) / 6, p2[1] - (p3[1] - p1[1]) / 6];
      d += ` C ${c1[0]} ${c1[1]}, ${c2[0]} ${c2[1]}, ${p2[0]} ${p2[1]}`;
    }
    return d;
  };

  const idealPath = ideal.length > 1 ? smoothPath(ideal) : "";

  const sums = new Map();
  measured.forEach(({ h, v }) => {
    const hour = Math.min(23, Math.floor(h));
    const prev = sums.get(hour) ?? { total: 0, count: 0 };
    sums.set(hour, { total: prev.total + v, count: prev.count + 1 });
  });
  const dots = [...sums.entries()].map(([hour, { total, count }]) => ({ h: hour + 0.5, v: total / count }));

  const labelStyle = { fontSize: 12, fontWeight: 500, letterSpacing: "-0.24px" };

  return (
    <svg viewBox="32 475 326 205" className="chart__svg" role="img" aria-label="오늘의 습도 변화 그래프" style={{ width: "100%", height: "auto" }}>
      {NIGHT.map(([a, b]) => (
        <rect key={`night-${a}`} x={x(a)} y={TOP} width={x(b) - x(a)} height={BOTTOM - TOP - 1} fill="rgba(211,238,255,0.5)" />
      ))}

      {sprayZones.map(([a, b]) => (
        <g key={`spray-${a}`}>
          <rect x={x(a)} y={TOP} width={x(b) - x(a)} height={BOTTOM - TOP - 1} fill="rgba(200,245,147,0.4)" />
          <text x={x(b) - 7} y={506.5} textAnchor="end" fill="#5B911C" style={labelStyle}>분무</text>
        </g>
      ))}

      {[100, 80, 60, 40, 20].map((v) => (
        <line key={`grid-${v}`} x1={55} x2={355} y1={y(v)} y2={y(v)} stroke="#FFDCB9" strokeWidth="1" strokeDasharray="3 3" />
      ))}

      <line x1={L} x2={L} y1={TOP} y2={BOTTOM} stroke="#FFC58D" strokeWidth="1" />
      <line x1={55} x2={R} y1={BOTTOM} y2={BOTTOM} stroke="#FFC58D" strokeWidth="1" />

      <text x={39} y={488.5} fill="#B47952" style={labelStyle}>%</text>
      {[100, 80, 60, 40, 20, 0].map((v) => (
        <text key={`ytick-${v}`} x={50} y={y(v) + 4.5} textAnchor="end" fill="#B47952" style={labelStyle}>
          {v}
        </text>
      ))}

      <text x={116} y={506.5} textAnchor="end" fill="#A0AFCC" style={labelStyle}>야간 구간</text>
      <text x={350} y={506.5} textAnchor="end" fill="#A0AFCC" style={labelStyle}>야간 구간</text>

      {[[0, 64], [6, 131], [12, 200.5], [18, 272.5], [24, 345.5]].map(([h, cx]) => (
        <text key={h} x={cx} y={674.5} textAnchor="middle" fill="#B47952" style={labelStyle}>
          {h}시
        </text>
      ))}

      {idealPath && (
        <>
          <path d={idealPath} fill="none" stroke="#FFDCB9" strokeWidth="12" strokeLinecap="round" strokeLinejoin="round" opacity="0.8" />
          <path d={idealPath} fill="none" stroke="#FF9834" strokeWidth="1.5" strokeDasharray="3 3" strokeLinecap="round" />
        </>
      )}

      {dots.map(({ h, v }) => (
        <circle key={h} cx={x(h)} cy={y(v)} r="5.5" fill="#6BC4FF" />
      ))}
    </svg>
  );
}

export default function Humidity() {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [showWhyModal, setShowWhyModal] = useState(false);

  useEffect(() => {
    let cancelled = false;
    fetchHumidity()
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

  useEffect(() => {
    if (!showWhyModal) return undefined;
    const onKey = (e) => e.key === "Escape" && setShowWhyModal(false);
    window.addEventListener("keydown", onKey);
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      window.removeEventListener("keydown", onKey);
      document.body.style.overflow = prevOverflow;
    };
  }, [showWhyModal]);

  if (!data) return <div className="humidity-page" />;

  if (data.current == null) {
    return (
      <div className="humidity-page">
        <div className="humidity-page__scroll">
          <header className="header">
            <button type="button" className="header__back" onClick={() => navigate(-1)}>
              <img src={backBtn} alt="뒤로가기" />
            </button>
            <h1 className="header__title">습도</h1>
          </header>
          <p style={{ textAlign: "center", marginTop: 80, lineHeight: 1.6 }}>
            아직 측정된 습도가 없어요.<br />센서 연결을 확인해주세요.
          </p>
          <button type="button" style={{ display: "block", margin: "16px auto" }} onClick={() => navigate("/my/sensor")}>
            센서 확인하기
          </button>
        </div>
      </div>
    );
  }

  const status = getStatus(data.current, data.measured);
  const guide = GUIDES[status.key];

  return (
    <div className="humidity-page">
      <div className="humidity-page__scroll">
        <header className="header">
          <button type="button" className="header__back" onClick={() => navigate(-1)}>
            <img src={backBtn} alt="뒤로가기" />
          </button>
          <h1 className="header__title">습도</h1>
        </header>

        <main className="humidity-page__content">
          <section className="card card--current">
            <div className="card__header">
              <h2 className="card__title">
                <img src={humidityIcon} alt="" className="icon" /> 현재 습도
              </h2>
              <span className="card__subtext">마지막 측정 {formatTime(data.measuredAt)}</span>
            </div>

            <div className="current-humidity">
              <div className="current-humidity__value">
                {Math.round(data.current)}
                <span>%</span>
              </div>
              <div className={`badge ${status.key === "ok" ? "badge--ok" : "badge--warn"}`}>
                <img src={status.key === "ok" ? stateOk : warnIcon} alt="" className="badge__icon" />
                {status.label}
              </div>
            </div>
          </section>

          <section className="card card--cycle">
            <div className="card__header card__header--space">
              <h2 className="card__title">
                <img src={humidityIcon} alt="" className="icon" /> 습윤 사이클이란?
              </h2>
              <button type="button" className="link-btn" onClick={() => setShowWhyModal(true)}>
                왜 중요한가요? <span aria-hidden="true">›</span>
              </button>
            </div>

            <p className="card__text">
              <strong>분무 후 올라간 습도가 시간이 지나며 다시 낮아지는 과정으로,</strong> 높은 습도가 계속 유지되는 것보다, 습해진 뒤 충분히 건조되는 과정이 반복되는지 함께 확인하는 것이 중요해요.
            </p>
          </section>

          <section className="card card--chart">
            <div className="card__header">
              <h2 className="card__title">
                <img src={chartIcon} alt="" className="icon" /> 오늘의 습도 변화
              </h2>
            </div>

            <div className="chart">
              <HumidityChart measured={data.measured} ideal={data.ideal} sprayZones={data.sprayZones.length > 0 ? data.sprayZones : SPRAY_ZONES} />
            </div>

            <div className="chart-legend">
              <div className="chart-legend__item">
                <span className="dot dot--blue" />
                <span>시간대별 습도</span>
              </div>
              <div className="chart-legend__item">
                <span className="dot dot--cycle" />
                <span>올바른 습윤 사이클</span>
              </div>
              <div className="chart-legend__item">
                <span className="dot dot--green" />
                <span>
                  권장 분무 시간
                  <br />
                  <small>(아침 저녁 간격)</small>
                </span>
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

      {showWhyModal && (
        <div className="modal-overlay" onClick={() => setShowWhyModal(false)}>
          <div className="modal-content" role="dialog" aria-modal="true" onClick={(e) => e.stopPropagation()}>
            <button type="button" className="modal-content__close" onClick={() => setShowWhyModal(false)}>
              <img src={closeBtn} alt="닫기" />
            </button>

            <div className="modal-content__title">
              <img src={humidityIcon} alt="" />
              <h2>습윤 사이클이 왜 중요한가요?</h2>
            </div>

            <p className="modal-content__sub">
              높은 습도가 계속 유지되는 것보다, 분무 후 습도가 올라갔다가 다시 건조되는 흐름이 반복될 때 더 건강하게 지낼 수 있어요.
            </p>

            <div className="modal-flow">
              <strong className="modal-flow__title">이렇게 확인해보세요</strong>
              <ol className="modal-flow__steps">
                {FLOW_STEPS.map((step, i) => (
                  <li key={step}>
                    <span>{step}</span>
                    {i < FLOW_STEPS.length - 1 && <em aria-hidden="true">→</em>}
                  </li>
                ))}
              </ol>
              <small className="modal-flow__desc">
                습도가 계속 높게 유지되거나 너무 빠르게 낮아진다면 환기 상태,<br/> 분무량, 실내 환경을 함께 확인해보세요.
              </small>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}