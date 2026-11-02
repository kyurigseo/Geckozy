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


const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const fetchHumidity = async () => {
  await delay(300);
  return {
    current: 78,
    measuredAt: "2026-11-01T14:32:00",
    measured: [
      { h: 0.5, v: 48 },
      { h: 1.5, v: 41 },
      { h: 2.5, v: 39 },
      { h: 3.5, v: 36 },
      { h: 4.5, v: 36 },
      { h: 7.5, v: 63 },
      { h: 8.7, v: 81 },
      { h: 9.9, v: 80 },
      { h: 11, v: 79 },
      { h: 12.2, v: 78 },
      { h: 13.3, v: 78 },
      { h: 14.4, v: 78 },
    ],
    ideal: [
      { h: 0, v: 55 }, { h: 2, v: 40 }, { h: 4.5, v: 33 }, { h: 6, v: 34 },
      { h: 7.2, v: 62 }, { h: 8.3, v: 82 }, { h: 9.2, v: 80 }, { h: 11, v: 62 },
      { h: 13, v: 45 }, { h: 15, v: 36 }, { h: 16.5, v: 35 }, { h: 17.3, v: 50 },
      { h: 18.3, v: 78 }, { h: 19, v: 84 }, { h: 20, v: 80 }, { h: 22, v: 58 },
      { h: 24, v: 46 },
    ],
    sprayZones: [[6, 9], [17, 20]],
  };
};

// ─────────── 기준값 (임의 기준이에요. 팀에서 정한 기준이 있으면 바꿔주세요) ───────────
const HIGH = 70; // 이 값 이상이면 '높은 습도'
const LOW = 40; // 이 값 미만이면 '건조'
const HIGH_STREAK = 4; // 최근 측정값 몇 개가 연속으로 높으면 '높은 습도가 계속됨'
const NIGHT = [[0, 6], [20, 24]]; // 야간 구간 (시)

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

// ─────────── 계산 함수 ───────────
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

// ─────────── 오늘의 습도 변화 그래프 ───────────
// 데이터(measured, ideal, sprayZones)만 바뀌면 점, 곡선, 분무 구간이 자동으로 다시 그려져요
function HumidityChart({ measured, ideal, sprayZones }) {
  const W = 320;
  const H = 190;
  const PAD = { l: 34, r: 6, t: 8, b: 22 };
  const plotH = H - PAD.t - PAD.b;

  const x = (h) => PAD.l + (h / 24) * (W - PAD.l - PAD.r);
  const y = (v) => PAD.t + ((100 - v) / 100) * plotH;

  // 점들을 부드러운 곡선(Catmull-Rom → Bezier)으로 연결
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

  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="chart__svg" role="img" aria-label="오늘의 습도 변화 그래프" style={{ width: "100%", height: "auto" }}>
      {/* 야간 구간 */}
      {NIGHT.map(([a, b]) => (
        <g key={`night-${a}`}>
          <rect x={x(a)} y={PAD.t} width={x(b) - x(a)} height={plotH} fill="#E8F2F7" />
          <text x={(x(a) + x(b)) / 2} y={PAD.t + 11} textAnchor="middle" fontSize="8" fill="#8FA9BD">
            야간 구간
          </text>
        </g>
      ))}

      {/* 권장 분무 시간 */}
      {sprayZones.map(([a, b]) => (
        <g key={`spray-${a}`}>
          <rect x={x(a)} y={PAD.t} width={x(b) - x(a)} height={plotH} fill="#E8F6CA" />
          <text x={(x(a) + x(b)) / 2} y={PAD.t + 11} textAnchor="middle" fontSize="8" fill="#5B911C">
            분무
          </text>
        </g>
      ))}

      {/* 가로 격자 + y축 눈금 */}
      {[0, 20, 40, 60, 80, 100].map((v) => (
        <g key={v}>
          <line x1={PAD.l} x2={W - PAD.r} y1={y(v)} y2={y(v)} stroke="#E6D9C6" strokeWidth="0.5" strokeDasharray="2 2" />
          <text x={PAD.l - 6} y={y(v) + 3} textAnchor="end" fontSize="9" fill="#492515">
            {v}
          </text>
        </g>
      ))}

      {/* x축 눈금 */}
      {[0, 6, 12, 18, 24].map((h) => (
        <text key={h} x={x(h)} y={H - 6} textAnchor={h === 0 ? "start" : h === 24 ? "end" : "middle"} fontSize="9" fill="#492515">
          {h}시
        </text>
      ))}

      {/* 올바른 습윤 사이클: 굵은 연한 선(글로우) + 주황 점선 */}
      {idealPath && (
        <>
          <path d={idealPath} fill="none" stroke="#FFDCB9" strokeWidth="8" strokeLinecap="round" opacity="0.8" />
          <path d={idealPath} fill="none" stroke="#FF9834" strokeWidth="1.2" strokeDasharray="3 2" strokeLinecap="round" />
        </>
      )}

      {/* 실제 측정값 */}
      {measured.map(({ h, v }) => (
        <circle key={h} cx={x(h)} cy={y(v)} r="3.4" fill="#6BC4FF" />
      ))}
    </svg>
  );
}

// ─────────── 화면 ───────────
export default function Humidity() {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [showWhyModal, setShowWhyModal] = useState(false);

  useEffect(() => {
    let cancelled = false;
    fetchHumidity().then((res) => {
      if (!cancelled) setData(res);
    });
    return () => {
      cancelled = true;
    };
  }, []);

  // 모달: ESC로 닫기 + 떠 있는 동안 뒤 화면 스크롤 잠금
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
          {/* 1. 현재 습도 */}
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

          {/* 2. 습윤 사이클이란? */}
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

          {/* 3. 오늘의 습도 변화 */}
          <section className="card card--chart">
            <div className="card__header">
              <h2 className="card__title">
                <img src={chartIcon} alt="" className="icon" /> 오늘의 습도 변화
              </h2>
            </div>

            <div className="chart">
              <div className="chart__unit">%</div>
              <HumidityChart measured={data.measured} ideal={data.ideal} sprayZones={data.sprayZones} />
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

          {/* 4. 관리 가이드 (습도 상태에 따라 문구가 바뀌어요) */}
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
