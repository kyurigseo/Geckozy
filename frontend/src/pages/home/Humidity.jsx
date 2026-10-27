import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Temperature.scss";
import "./Humidity.scss";

// ── 목데이터 (나중에 API로 교체) ──
const CURRENT = { value: 78, status: "확인 필요", measuredAt: "14:32" };
// 실제 측정값 (파란 점)
const MEASURED = [
  [0.5, 45], [1.5, 38], [2.5, 34], [3.5, 31], [4.5, 30],
  [7.5, 63], [8.7, 80], [9.9, 72], [11, 64], [12.2, 62], [13.3, 58], [14.4, 55], [15.5, 54],
];
// 올바른 습윤 사이클 (주황 점선)
const IDEAL = [
  [0, 47], [2, 38], [4.5, 31], [6, 32], [7.2, 62], [8.3, 82], [9.2, 80], [11, 60],
  [13, 42], [15, 30], [16.5, 29], [17.3, 45], [18.3, 78], [19, 85], [20, 82], [22, 62], [24, 52],
];

// ── 차트 설정 ──
const W = 320, H = 190;
const PAD = { l: 34, r: 6, t: 8, b: 22 };
const NIGHT = [[0, 6], [20, 24]];
const SPRAY = [[6, 9], [17, 20]];
const x = (h) => PAD.l + (h / 24) * (W - PAD.l - PAD.r);
const y = (v) => PAD.t + ((100 - v) / 100) * (H - PAD.t - PAD.b);

// 점들을 부드러운 곡선(Catmull-Rom → Bezier)으로 연결
function smoothPath(points) {
  const p = points.map(([h, v]) => [x(h), y(v)]);
  let d = `M ${p[0][0]} ${p[0][1]}`;
  for (let i = 0; i < p.length - 1; i++) {
    const p0 = p[i - 1] || p[i], p1 = p[i], p2 = p[i + 1], p3 = p[i + 2] || p2;
    const c1 = [p1[0] + (p2[0] - p0[0]) / 6, p1[1] + (p2[1] - p0[1]) / 6];
    const c2 = [p2[0] - (p3[0] - p1[0]) / 6, p2[1] - (p3[1] - p1[1]) / 6];
    d += ` C ${c1[0]} ${c1[1]}, ${c2[0]} ${c2[1]}, ${p2[0]} ${p2[1]}`;
  }
  return d;
}

function HumidityChart() {
  const plotH = H - PAD.t - PAD.b;
  const ideal = smoothPath(IDEAL);

  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="hchart" role="img" aria-label="오늘의 습도 변화 차트">
      {NIGHT.map(([a, b]) => (
        <g key={`n${a}`}>
          <rect x={x(a)} y={PAD.t} width={x(b) - x(a)} height={plotH} className="hchart__night" />
          <text x={(x(a) + x(b)) / 2} y={PAD.t + 10} textAnchor="middle" className="hchart__zone hchart__zone--night">야간 구간</text>
        </g>
      ))}
      {SPRAY.map(([a, b]) => (
        <g key={`s${a}`}>
          <rect x={x(a)} y={PAD.t} width={x(b) - x(a)} height={plotH} className="hchart__spray" />
          <text x={(x(a) + x(b)) / 2} y={PAD.t + 10} textAnchor="middle" className="hchart__zone hchart__zone--spray">분무</text>
        </g>
      ))}
      {[0, 20, 40, 60, 80, 100].map((v) => (
        <g key={v}>
          <line x1={PAD.l} x2={W - PAD.r} y1={y(v)} y2={y(v)} className="hchart__grid" />
          <text x={PAD.l - 6} y={y(v) + 3} textAnchor="end" className="hchart__tick">{v}</text>
        </g>
      ))}
      {[0, 6, 12, 18, 24].map((h) => (
        <text key={h} x={x(h)} y={H - 6} textAnchor={h === 0 ? "start" : h === 24 ? "end" : "middle"} className="hchart__tick">{h}시</text>
      ))}
      {/* 올바른 사이클: 굵은 글로우 + 점선 */}
      <path d={ideal} className="hchart__glow" />
      <path d={ideal} className="hchart__ideal" />
      {/* 실제 측정값 */}
      {MEASURED.map(([h, v]) => (
        <circle key={h} cx={x(h)} cy={y(v)} r={3.2} className="hchart__dot" />
      ))}
    </svg>
  );
}

function WhyModal({ onClose }) {
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

  const STEPS = ["분무", "습도 상승", "서서히 건조", "다음 분무"];

  return (
    <div className="modal" onClick={onClose}>
      <div
        className="modal__panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="why-title"
        onClick={(e) => e.stopPropagation()}
      >
        <header className="modal__head">
          <span className="modal__icon">💧</span>
          <h2 id="why-title">습윤 사이클이 왜 중요한가요?</h2>
          <button type="button" className="modal__close" onClick={onClose} aria-label="닫기">✕</button>
        </header>

        <p className="modal__lead">
          높은 습도가 계속 유지되는 것 보다, 분무 후 습도가 올라갔다가 다시 건조되는 흐름이 반복될 때 더 건강하게 지낼 수 있어요.
        </p>

        <div className="modal__note">
          <strong className="check-title">이렇게 확인해보세요</strong>
          <ol className="flow">
            {STEPS.map((s, i) => (
              <li key={s}>
                <span>{s}</span>
                {i < STEPS.length - 1 && <em aria-hidden="true">→</em>}
              </li>
            ))}
          </ol>
          <small className="flow__desc">
            습도가 계속 높게 유지되거나 너무 빠르게 낮아진다면 환기 상태, 분무량, 실내 환경을 함께 확인해보세요.
          </small>
        </div>
      </div>
    </div>
  );
}

export default function HumidityPage() {
  const navigate = useNavigate();
  const [showWhy, setShowWhy] = useState(false);

  return (
    <div className="temp">
      <header className="temp__header">
        <button type="button" className="temp__back" onClick={() => navigate(-1)} aria-label="뒤로가기">‹</button>
        <h1>습도</h1>
      </header>

      <main className="temp__body">
        {/* 현재 습도 */}
        <section className="panel panel--strong now">
          <div className="now__top">
            <h2><span>💧</span> 현재 습도</h2>
            <small>마지막 측정 {CURRENT.measuredAt}</small>
          </div>
          <div className="now__value">{CURRENT.value}%</div>
          <span className="badge badge--warn">{CURRENT.status}</span>
        </section>

        {/* 습윤 사이클이란? */}
        <section className="panel cycle">
          <div className="panel__top">
            <h2><span>💧</span> 습윤 사이클이란?</h2>
            <button type="button" className="cycle__more" onClick={() => setShowWhy(true)}>
              왜 중요한가요? ›
            </button>
          </div>
          <p>
            분무 후 올라간 습도가 시간이 지나며 다시 낮아지는 과정으로, 높은 습도가 계속 유지되는 것보다, 습해진 뒤 충분히 건조되는 과정이 반복되는지 함께 확인하는 것이 중요해요.
          </p>
        </section>

        {/* 오늘의 습도 변화 */}
        <section className="panel">
          <h2><span>📈</span> 오늘의 습도 변화</h2>
          <div className="chart-unit">%</div>
          <HumidityChart />
          <ul className="legend legend--h">
            <li><i className="legend__blue" /><div>시간대별 습도</div></li>
            <li><i className="legend__cycle" /><div>올바른 습윤 사이클</div></li>
            <li><i className="legend__spray" /><div>권장 분무 시간<small>(아침 저녁 간격)</small></div></li>
          </ul>
        </section>

        {/* 관리 팁 */}
        <section className="tip">
          <h2>📋 지금 이렇게 관리해주세요!</h2>
          <h3>높은 습도가 계속되고 있어요</h3>
          <p>지금은 추가로 분무하지 마세요. 환기가 원활한지 확인하고, 바닥재나 사육장 내부가 지나치게 젖어 있다면 충분히 건조될 시간을 주세요.</p>
          <hr />
          <h4>추가로 이런 점도 확인해보세요.</h4>
          <ul>
            <li>분무량이 평소보다 많지 않았는지 확인해주세요.</li>
            <li>높은 습도가 계속 유지되는 환경은 곰팡이, 곰팡이성 성장 위험을 높일 수 있어요.</li>
          </ul>
        </section>
      </main>

      {showWhy && <WhyModal onClose={() => setShowWhy(false)} />}

      <TabBar />
    </div>
  );
}
