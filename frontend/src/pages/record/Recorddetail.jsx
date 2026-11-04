import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

// 달력/페이지 뼈대(record-page, record-cal__*, record-add, record-menu, record-sheet)는 Record.scss를 그대로 씀
import "./Record.scss";
import "./RecordDetail.scss";

import bgImg from "../../assets/record/bg.svg";
import addBtnImg from "../../assets/record/add_btn.png";
import feedingIcon from "../../assets/record/icon_feeding.png";
import sheddingIcon from "../../assets/record/icon_shedding.png";
import weightIcon from "../../assets/record/icon_weight.png";
import cleaningIcon from "../../assets/record/icon_cleaning.png";
import poopIcon from "../../assets/record/icon_poop.png";
import symptomIcon from "../../assets/record/icon_symptom.png";
import etcIcon from "../../assets/record/icon_etc.png";

import buttonSrc from "../../assets/sounds/button.mp3";
import selectSrc from "../../assets/sounds/select.mp3";

const playSfx = (src) => {
  const sound = new Audio(src);
  sound.volume = 0.5;
  sound.currentTime = 0;
  sound.play().catch(() => {});
};
const playButtonSfx = () => playSfx(buttonSrc);
const playSelectSfx = () => playSfx(selectSrc);

// TODO: Record.jsx와 중복이라 나중에 constants 파일로 빼면 좋음
const TYPES = {
  feeding: { label: "먹이", icon: feedingIcon },
  shedding: { label: "탈피", icon: sheddingIcon },
  weight: { label: "체중", icon: weightIcon },
  cleaning: { label: "청소", icon: cleaningIcon },
  poop: { label: "배변", icon: poopIcon },
  symptom: { label: "이상 증세", icon: symptomIcon },
  etc: { label: "추가 기록", icon: etcIcon },
};

const MENU = [
  { type: "shedding", x: 0, y: -103 },
  { type: "feeding", x: -88, y: -59 },
  { type: "weight", x: 85, y: -61 },
  { type: "cleaning", x: -107, y: 30 },
  { type: "poop", x: 107, y: 30 },
  { type: "symptom", x: -42, y: 100 },
  { type: "etc", x: 42, y: 100 },
];

const WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"];

// ─────────── 목 데이터 (API 붙이면 이 부분만 교체) ───────────
const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const pad = (n) => String(n).padStart(2, "0");

const SAMPLE_DAYS = [
  [1, "feeding"], [4, "etc"], [9, "shedding"], [10, "shedding"], [14, "cleaning"],
  [22, "feeding"], [24, "symptom"], [25, "poop", true], [27, "weight"],
];

const SAMPLE_TAGS = {
  shedding: [["일부 남음", "pink"], ["몸통", "orange"]],
  feeding: [["귀뚜라미", "orange"]],
  weight: [["체중 측정", "orange"]],
  cleaning: [["전체 청소", "orange"]],
  poop: [["이상 있음", "pink"]],
  symptom: [["확인 필요", "pink"]],
  etc: [["기타", "orange"]],
};

// photos에는 이미지 URL 배열이 들어옴 (없으면 사진 영역이 안 보임)
const fetchMonth = async (year, month) => {
  await delay(200);
  const lastDay = new Date(year, month, 0).getDate();
  return SAMPLE_DAYS.filter(([d]) => d <= lastDay).map(([d, type, alert]) => ({
    id: `${year}-${pad(month)}-${pad(d)}-${type}`,
    date: `${year}-${pad(month)}-${pad(d)}`,
    time: d === 9 ? "19:35" : "10:00",
    type,
    alert: Boolean(alert),
    tags: (SAMPLE_TAGS[type] ?? []).map(([text, tone]) => ({ text, tone })),
    memo: d === 9 ? "발가락 쪽에 탈피가 조금 남아 있음" : "",
    photos: [],
  }));
};

// 기록 시점 기준 앞뒤 24시간, 2시간 간격 25개
const POINTS = 25;
const CENTER = 12;
const fetchEnv = async () => {
  await delay(250);
  return Array.from({ length: POINTS }, (_, i) => {
    const wave = Math.sin((i / 24) * Math.PI * 2 - 1);
    const mid = 23 + wave * 1.6 + (i === CENTER ? 1 : 0);
    return {
      tmin: +(mid - 2.2).toFixed(1),
      tmax: +(mid + 2.2).toFixed(1),
      h: Math.round(52 + Math.cos((i / 24) * Math.PI * 2) * 7),
    };
  });
};

// ─────────── 유틸 ───────────
const parseDate = (s) => {
  const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(s ?? "");
  if (m) return { y: +m[1], m: +m[2], d: +m[3] };
  const t = new Date();
  return { y: t.getFullYear(), m: t.getMonth() + 1, d: t.getDate() };
};

const buildCells = (year, month) => {
  const blanks = new Date(year, month - 1, 1).getDay();
  const days = new Date(year, month, 0).getDate();
  return [...Array(blanks).fill(null), ...Array.from({ length: days }, (_, i) => i + 1)];
};

const fmtTime = (hhmm, withMinute = true) => {
  const [h, m] = hhmm.split(":").map(Number);
  const h12 = h % 12 || 12;
  return `${h < 12 ? "오전" : "오후"} ${h12}시${withMinute && m ? ` ${m}분` : ""}`;
};

const mean = (arr) => arr.reduce((a, b) => a + b, 0) / (arr.length || 1);

const summarize = (pts) => {
  const mid = (p) => (p.tmin + p.tmax) / 2;
  const before = pts.slice(0, CENTER);
  const after = pts.slice(CENTER + 1);
  const dT = mean(after.map(mid)) - mean(before.map(mid));
  const dH = mean(after.map((p) => p.h)) - mean(before.map((p) => p.h));
  const bits = [];
  if (Math.abs(dT) >= 1.5) bits.push(`온도가 ${Math.abs(dT).toFixed(1)}°C ${dT > 0 ? "올랐" : "내렸"}어요.`);
  if (Math.abs(dH) >= 8) bits.push(`습도가 ${Math.round(Math.abs(dH))}%p ${dH > 0 ? "올랐" : "내렸"}어요.`);
  return bits.length
    ? `기록 이후 ${bits.join(" ")}`
    : "기록 전후로 온습도에서 눈에 띄는 변화는 확인되지 않았어요.";
};

// ─────────── 작은 아이콘 (이미지 에셋 없이 SVG) ───────────
const Sprout = () => (
  <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
    <path d="M12 21V12" stroke="#6b3f1f" strokeWidth="2" strokeLinecap="round" />
    <path d="M12 13C12 8 8 6 4 6c0 4 3 7 8 7Z" fill="#7bb33a" stroke="#4f7d1c" strokeWidth="1.5" strokeLinejoin="round" />
    <path d="M12 11c0-4 3-6 7-6 0 4-3 6-7 6Z" fill="#9ccc4c" stroke="#4f7d1c" strokeWidth="1.5" strokeLinejoin="round" />
  </svg>
);
const Trend = () => (
  <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
    <path d="M3 3v18h18" fill="none" stroke="#6b3f1f" strokeWidth="2" strokeLinecap="round" />
    <path d="M7 15l4-4 3 3 5-6" fill="none" stroke="#f0731f" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);
const Doc = () => (
  <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
    <rect x="4" y="3" width="16" height="18" rx="3" fill="#fff" stroke="#6b3f1f" strokeWidth="2" />
    <path d="M8 16v-3M12 16V9M16 16v-5" stroke="#f0731f" strokeWidth="2.2" strokeLinecap="round" />
  </svg>
);
const Trash = () => (
  <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
    <path d="M5 7h14M10 7V5h4v2M7 7l1 12h8l1-12" fill="none" stroke="#a2724a" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);
const Thermo = () => (
  <svg viewBox="0 0 32 32" width="40" height="40" aria-hidden="true">
    <rect x="12" y="3" width="8" height="18" rx="4" fill="#fff" stroke="#8a4b2a" strokeWidth="2" />
    <rect x="14.5" y="10" width="3" height="12" fill="#e8484f" />
    <circle cx="16" cy="24" r="6" fill="#e8484f" stroke="#8a4b2a" strokeWidth="2" />
  </svg>
);
const Drop = () => (
  <svg viewBox="0 0 32 32" width="40" height="40" aria-hidden="true">
    <path d="M16 3S6 14 6 20a10 10 0 0 0 20 0C26 14 16 3 16 3Z" fill="#5cb6f6" stroke="#2f6fa8" strokeWidth="2" strokeLinejoin="round" />
    <path d="M11 20a5 5 0 0 0 4 5" fill="none" stroke="#fff" strokeWidth="2" strokeLinecap="round" />
  </svg>
);

// ─────────── 전후 24시간 차트 ───────────
const VB_W = 300;
const VB_H = 172;
const PL = 30;
const PR = 270;
const PT = 26;
const PB = 140;

function EnvChart({ points, recordTime, dateLabel }) {
  const x = (i) => PL + (i * (PR - PL)) / (POINTS - 1);
  const yT = (t) => PT + ((32 - Math.min(32, Math.max(16, t))) / 16) * (PB - PT);
  const yH = (h) => PT + ((80 - Math.min(80, Math.max(0, h))) / 80) * (PB - PT);
  const cx = x(CENTER);
  const tip = `기록 시점 : ${fmtTime(recordTime, false)}`;
  const xTicks = [
    [0, "-24h"],
    [6, "-12h"],
    [12, dateLabel],
    [18, "+12h"],
    [24, "+24h"],
  ];

  return (
    <svg
      className="env-chart"
      viewBox={`0 0 ${VB_W} ${VB_H}`}
      role="img"
      aria-label="기록 전후 24시간 온도와 습도 변화"
    >
      <text className="env-chart__unit env-chart__unit--t" x={4} y={PT - 6}>°C</text>
      <text className="env-chart__unit env-chart__unit--h" x={VB_W - 4} y={PT - 6} textAnchor="end">%</text>

      {[0, 1, 2, 3, 4].map((i) => {
        const y = PT + (i * (PB - PT)) / 4;
        return (
          <g key={i}>
            <line className="env-chart__grid" x1={PL} x2={PR} y1={y} y2={y} />
            <text className="env-chart__tick env-chart__tick--t" x={PL - 5} y={y + 3} textAnchor="end">{32 - i * 4}</text>
            <text className="env-chart__tick env-chart__tick--h" x={PR + 5} y={y + 3}>{80 - i * 20}</text>
          </g>
        );
      })}

      {points.map((p, i) => {
        const dist = Math.abs(i - CENTER) / CENTER;
        return (
          <g key={i}>
            <line
              className="env-chart__bar"
              x1={x(i)}
              x2={x(i)}
              y1={yT(p.tmax)}
              y2={yT(p.tmin)}
              style={{ opacity: 0.35 + 0.65 * (1 - dist) }}
            />
            <circle className="env-chart__dot" cx={x(i)} cy={yH(p.h)} r="2.6" />
          </g>
        );
      })}

      <line className="env-chart__mark" x1={cx} x2={cx} y1={PT - 6} y2={PB} />
      <circle className="env-chart__mark-dot" cx={cx} cy={PB} r="3" />
      <g>
        <rect className="env-chart__tip" x={cx - 48} y="2" width="96" height="16" rx="4" />
        <text className="env-chart__tip-text" x={cx} y="13" textAnchor="middle">{tip}</text>
      </g>

      {xTicks.map(([i, label]) => (
        <text key={i} className="env-chart__tick env-chart__tick--x" x={x(i)} y={PB + 17} textAnchor="middle">
          {label}
        </text>
      ))}
    </svg>
  );
}

// ─────────── 화면 ───────────
export default function RecordDetail() {
  const navigate = useNavigate();
  const { date: dateParam } = useParams(); // 예: /record/detail/2026-09-09
  const start = useMemo(() => parseDate(dateParam), [dateParam]);

  const [ym, setYm] = useState({ y: start.y, m: start.m });
  const [day, setDay] = useState(start.d);
  const [records, setRecords] = useState(null);
  const [activeId, setActiveId] = useState(null);
  const [env, setEnv] = useState(null);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setRecords(null);
    fetchMonth(ym.y, ym.m).then((res) => {
      if (!cancelled) setRecords(res);
    });
    return () => {
      cancelled = true;
    };
  }, [ym]);

  useEffect(() => {
    if (!menuOpen) return undefined;
    const onKey = (e) => e.key === "Escape" && setMenuOpen(false);
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [menuOpen]);

  const cells = useMemo(() => buildCells(ym.y, ym.m), [ym]);

  const byDay = useMemo(() => {
    const map = new Map();
    (records ?? []).forEach((r) => {
      const d = Number(r.date.slice(8, 10));
      map.set(d, [...(map.get(d) ?? []), r].sort((a, b) => a.time.localeCompare(b.time)));
    });
    return map;
  }, [records]);

  const dayRecords = day ? byDay.get(day) ?? [] : [];
  const active = dayRecords.find((r) => r.id === activeId) ?? dayRecords[0] ?? null;
  const activeKey = active?.id;

  useEffect(() => {
    if (!activeKey) {
      setEnv(null);
      return undefined;
    }
    let cancelled = false;
    setEnv(null);
    fetchEnv(activeKey).then((res) => {
      if (!cancelled) setEnv(res);
    });
    return () => {
      cancelled = true;
    };
  }, [activeKey]);

  const envStats = useMemo(() => {
    if (!env) return null;
    return {
      temp: mean(env.map((p) => (p.tmin + p.tmax) / 2)),
      humid: mean(env.map((p) => p.h)),
      note: summarize(env),
    };
  }, [env]);

  const moveMonth = (diff) => {
    setDay(null);
    setActiveId(null);
    setYm(({ y, m }) => {
      const d = new Date(y, m - 1 + diff, 1);
      return { y: d.getFullYear(), m: d.getMonth() + 1 };
    });
  };

  const selectDay = (d) => {
    playButtonSfx();
    setDay(d);
    setActiveId(null);
  };

  const openForm = (type) => {
    setMenuOpen(false);
    navigate(`/record/new/${type}`);
  };

  const handleDelete = () => {
    playButtonSfx();
    if (!window.confirm("이 기록을 삭제할까요?")) return;
    // TODO: 삭제 API 연결
    navigate("/record");
  };

  return (
    <div className="record-page record-detail" style={{ backgroundImage: `url(${bgImg})` }}>
      <div className="record-page__scroll">
        <header className="record-cal__nav">
          <button
            type="button"
            className="record-cal__arrow"
            onClick={() => {
              playButtonSfx();
              moveMonth(-1);
            }}
            aria-label="이전 달"
          >
            ‹
          </button>
          <h1 className="record-cal__title" aria-live="polite">
            {ym.y}년 <span>{ym.m}월</span>
          </h1>
          <button
            type="button"
            className="record-cal__arrow"
            onClick={() => {
              playButtonSfx();
              moveMonth(1);
            }}
            aria-label="다음 달"
          >
            ›
          </button>
        </header>

        <div className="record-cal__weekdays" aria-hidden="true">
          {WEEKDAYS.map((w) => (
            <span key={w}>{w}</span>
          ))}
        </div>
        <ul className="record-cal__grid">
          {cells.map((d, i) => {
            const recs = d ? byDay.get(d) : null;
            const rec = recs?.[0];
            const type = rec ? TYPES[rec.type] : null;
            const cls = ["record-cal__cell", rec && "has-record", d && d === day && "is-selected"]
              .filter(Boolean)
              .join(" ");
            const pick = rec ? () => selectDay(d) : undefined;
            return (
              <li
                key={d ?? `blank-${i}`}
                className={cls}
                onClick={pick}
                onKeyDown={rec ? (e) => (e.key === "Enter" || e.key === " ") && pick() : undefined}
                role={rec ? "button" : undefined}
                tabIndex={rec ? 0 : undefined}
                aria-pressed={rec ? d === day : undefined}
              >
                {d && <span className="record-cal__num">{d}</span>}
                {rec && type && (
                  <span className="record-cal__icon">
                    <img src={type.icon} alt={`${d}일 ${type.label}`} />
                    {rec.alert && <i className="record-cal__dot" aria-label="확인이 필요한 기록" />}
                  </span>
                )}
              </li>
            );
          })}
        </ul>

        {menuOpen && <div className="record-menu__backdrop" onClick={() => setMenuOpen(false)} aria-hidden="true" />}
        <div className={`record-add ${menuOpen ? "is-open" : ""}`}>
          <button
            type="button"
            className="record-add__btn"
            style={{ backgroundImage: `url(${addBtnImg})` }}
            aria-haspopup="menu"
            aria-expanded={menuOpen}
            onClick={() => {
              playButtonSfx();
              setMenuOpen((v) => !v);
            }}
          >
            기록 추가하기
          </button>

          {menuOpen && (
            <div className="record-menu" role="menu" aria-label="기록 종류">
              {MENU.map(({ type, x, y }) => (
                <button
                  key={type}
                  type="button"
                  role="menuitem"
                  className="record-menu__item"
                  style={{ "--x": `${x}px`, "--y": `${y}px` }}
                  onClick={() => {
                    playSelectSfx();
                    openForm(type);
                  }}
                >
                  <img src={TYPES[type].icon} alt="" />
                  <span>{TYPES[type].label}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        <section className="record-sheet record-detail__sheet">
          {!day && records && (
            <p className="record-sheet__empty">달력에서 날짜를 눌러 기록을 확인해보세요.</p>
          )}
          {day && records && dayRecords.length === 0 && (
            <>
              <div className="record-sheet__head">
                <h2>
                  {ym.m}월 {day}일 <span>(0건)</span>
                </h2>
              </div>
              <p className="record-sheet__empty">이 날은 기록이 없어요.</p>
            </>
          )}

          {day && dayRecords.length > 0 && (
            <>
              <div className="record-sheet__head">
                <h2>
                  {ym.m}월 {day}일 <span>({dayRecords.length}건)</span>
                </h2>
                <button type="button" className="record-detail__trash" onClick={handleDelete} aria-label="기록 삭제">
                  <Trash />
                </button>
              </div>

              <div className="record-detail__entries">
                {dayRecords.map((r) => {
                  const t = TYPES[r.type] ?? { label: r.type, icon: etcIcon };
                  const isActive = r.id === active.id;
                  return (
                    <article
                      key={r.id}
                      className={`record-detail__entry ${isActive ? "is-active" : ""}`}
                      onClick={() => setActiveId(r.id)}
                    >
                      <div className="record-detail__entry-head">
                        <span className="record-detail__badge">
                          <img src={t.icon} alt="" />
                        </span>
                        <div className="record-detail__entry-main">
                          <strong className="record-detail__type">{t.label}</strong>
                          {r.tags.length > 0 && (
                            <ul className="record-detail__tags">
                              {r.tags.map((tag) => (
                                <li key={tag.text} className={`record-detail__tag is-${tag.tone}`}>
                                  {tag.text}
                                </li>
                              ))}
                            </ul>
                          )}
                        </div>
                      </div>

                      {r.photos.length > 0 && (
                        <ul className="record-detail__photos">
                          {r.photos.map((src, i) => (
                            <li key={src}>
                              <img src={src} alt={`${t.label} 기록 사진 ${i + 1}`} />
                            </li>
                          ))}
                        </ul>
                      )}

                      {r.memo && <p className="record-detail__memo">{r.memo}</p>}
                      <time className="record-detail__time">{fmtTime(r.time)}</time>
                    </article>
                  );
                })}
              </div>

              <div className="record-detail__band" aria-hidden="true" />

              <h3 className="record-detail__h">
                <Sprout /> 당시 사육환경
              </h3>
              {envStats ? (
                <div className="record-detail__stats">
                  <div className="record-detail__stat">
                    <Thermo />
                    <div>
                      <span className="record-detail__stat-label">평균 온도</span>
                      <strong className="record-detail__stat-value">{envStats.temp.toFixed(1)}°C</strong>
                    </div>
                  </div>
                  <div className="record-detail__stat">
                    <Drop />
                    <div>
                      <span className="record-detail__stat-label">평균 습도</span>
                      <strong className="record-detail__stat-value">{Math.round(envStats.humid)}%</strong>
                    </div>
                  </div>
                </div>
              ) : (
                <p className="record-detail__loading">환경 데이터를 불러오는 중이에요.</p>
              )}

              <div className="record-detail__band" aria-hidden="true" />

              <h3 className="record-detail__h">
                <Trend /> {TYPES[active.type]?.label ?? "기록"} 전후 24시간 환경 변화
              </h3>
              {env && (
                <EnvChart points={env} recordTime={active.time} dateLabel={`${ym.m}/${day}`} />
              )}

              {envStats && (
                <div className="record-detail__note">
                  <div className="record-detail__note-head">
                    <Doc /> <strong>환경 데이터 요약</strong>
                  </div>
                  <p>{envStats.note}</p>
                </div>
              )}
            </>
          )}
        </section>
      </div>
    </div>
  );
}
