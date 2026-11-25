import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import "./Record.scss";
import "./Recorddetail.scss";

import bgImg from "../../assets/record/bg.svg";
import addBtnImg from "../../assets/record/add_btn.png";
import feedingIcon from "../../assets/record/icon_feeding.png";
import sheddingIcon from "../../assets/record/icon_shedding.png";
import weightIcon from "../../assets/record/icon_weight.png";
import cleaningIcon from "../../assets/record/icon_cleaning.png";
import poopIcon from "../../assets/record/icon_poop.png";
import symptomIcon from "../../assets/record/icon_symptom.png";
import etcIcon from "../../assets/record/icon_etc.png";
import deleteIcon from "../../assets/record/icon_trash.png";
import buttonSrc from "../../assets/sounds/button.mp3";
import selectSrc from "../../assets/sounds/select.mp3";


import sproutIcon from "../../assets/record/icon_sprout.png";
import trendIcon from "../../assets/record/icon_trend.png";
import docIcon from "../../assets/record/icon_doc.png";
import thermoIcon from "../../assets/record/icon_thermo.png";
import dropIcon from "../../assets/record/icon_drop.png";
import { deleteRecord, fetchMonthRecords, fetchPhotoBlob, fetchRecordEnv, POINTS, CENTER } from "../../api/record";

function AuthImage({ src, alt }) {
  const [url, setUrl] = useState(null);

  useEffect(() => {
    let cancelled = false;
    let objectUrl = null;
    setUrl(null);
    fetchPhotoBlob(src)
      .then((u) => {
        if (cancelled) URL.revokeObjectURL(u);
        else {
          objectUrl = u;
          setUrl(u);
        }
      })
      .catch(() => {});
    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [src]);

  return url ? <img src={url} alt={alt} style={{ display: "block", width: "100%", height: "100%", objectFit: "cover" }} /> : null;
}

const playSfx = (src) => {
  const sound = new Audio(src);
  sound.volume = 0.5;
  sound.currentTime = 0;
  sound.play().catch(() => {});
};
const playButtonSfx = () => playSfx(buttonSrc);
const playSelectSfx = () => playSfx(selectSrc);

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
  const before = pts.slice(0, CENTER).filter(Boolean);
  const after = pts.slice(CENTER + 1).filter(Boolean);
  if (before.length === 0 || after.length === 0) {
    return "기록 전후 측정 데이터가 부족해서 변화를 확인하기 어려워요.";
  }
  const dT = mean(after.map(mid)) - mean(before.map(mid));
  const dH = mean(after.map((p) => p.h)) - mean(before.map((p) => p.h));
  const bits = [];
  if (Math.abs(dT) >= 1.5) bits.push(`온도가 ${Math.abs(dT).toFixed(1)}°C ${dT > 0 ? "올랐" : "내렸"}어요.`);
  if (Math.abs(dH) >= 8) bits.push(`습도가 ${Math.round(Math.abs(dH))}%p ${dH > 0 ? "올랐" : "내렸"}어요.`);
  return bits.length
    ? `기록 이후 ${bits.join(" ")}`
    : "기록 전후로 온습도에서 눈에 띄는 변화는 확인되지 않았어요.";
};


const VB_W = 350;
const VB_H = 212;
const PL = 23;
const PR = 323;
const CH_TOP = 31;
const CH_BOT = 184;
const BAR_W = 8;

const COLOR = {
  grid: "#FFDCB9",
  axis: "#FFC58D",
  temp: "#FF9834",
  tempSoft: "#FFC58D",
  tempCap: "#FF6B01",
  hum: "#6BC4FF",
  tick: "#B47952",
  brown: "#5B3524",
  tip: "#B47952",
  tipText: "#FDF7EE",
};

const FONT = { fontFamily: "Pretendard, sans-serif", fontWeight: 500, fontSize: 12, letterSpacing: "-0.24px" };

const mix = (a, b, t) => {
  const p = (hex) => [1, 3, 5].map((i) => parseInt(hex.slice(i, i + 2), 16));
  const [r1, g1, b1] = p(a);
  const [r2, g2, b2] = p(b);
  const c = (x, y) => Math.round(x + (y - x) * t);
  return `rgb(${c(r1, r2)}, ${c(g1, g2)}, ${c(b1, b2)})`;
};

function EnvChart({ points, recordTime, dateLabel }) {
  const step = (PR - PL - 20) / (POINTS - 1);
  const x = (i) => PL + 10 + i * step;
  const yT = (t) => CH_TOP + ((32 - Math.min(32, Math.max(16, t))) / 16) * (CH_BOT - CH_TOP);
  const yH = (h) => CH_TOP + ((80 - Math.min(80, Math.max(0, h))) / 80) * (CH_BOT - CH_TOP);
  const rows = [0, 1, 2, 3, 4].map((i) => CH_TOP + (i * (CH_BOT - CH_TOP)) / 4);
  const cx = x(CENTER);
  const tip = `기록 시점 : ${fmtTime(recordTime, false)}`;
  const xTicks = [
    [PL, "-24h", "start", false],
    [PL + 70, "-12h", "start", false],
    [cx, dateLabel, "middle", true],
    [PR - 98, "+12h", "start", false],
    [PR - 27, "+24h", "start", false],
  ];

  return (
    <svg
      className="env-chart"
      viewBox={`0 0 ${VB_W} ${VB_H}`}
      width="100%"
      role="img"
      aria-label="기록 전후 24시간 온도와 습도 변화"
    >
      <text x={5} y={14} fill={COLOR.temp} style={FONT}>℃</text>
      <text x={VB_W - 24} y={14} fill={COLOR.hum} style={FONT}>%</text>

      {rows.map((y, i) => (
        <g key={i}>
          {i < 4 && <line x1={PL} x2={PR} y1={y} y2={y} stroke={COLOR.grid} strokeWidth="1" strokeDasharray="2 3" />}
          <text x={4} y={y + 4} fill={COLOR.temp} style={FONT}>{32 - i * 4}</text>
          <text x={PR + 3} y={y + 4} fill={COLOR.hum} style={FONT}>{80 - i * 20}</text>
        </g>
      ))}

      <line x1={PL} x2={PR} y1={CH_BOT + 2} y2={CH_BOT + 2} stroke={COLOR.axis} strokeWidth="1" />
      <line x1={PL} x2={PL} y1={CH_TOP - 9} y2={CH_BOT + 2} stroke={COLOR.axis} strokeWidth="1" />
      <line x1={PR} x2={PR} y1={CH_TOP - 9} y2={CH_BOT + 2} stroke={COLOR.axis} strokeWidth="1" />

      <line
        x1={cx}
        x2={cx}
        y1={CH_TOP - 2}
        y2={CH_BOT + 2}
        stroke={COLOR.brown}
        strokeWidth="1.5"
        strokeDasharray="2 3"
        strokeLinecap="round"
      />

      {points.map((p, i) => {
        if (!p) return null;
        const dist = Math.abs(i - CENTER) / CENTER;
        const top = yT(p.tmax);
        const bottom = yT(p.tmin);
        const h = Math.max(BAR_W, bottom - top);
        const y0 = top - (h - (bottom - top)) / 2;
        return (
          <g key={i}>
            <rect
              x={x(i) - BAR_W / 2}
              y={y0}
              width={BAR_W}
              height={h}
              rx={BAR_W / 2}
              fill={mix(COLOR.temp, COLOR.tempSoft, dist)}
            />
            <circle cx={x(i)} cy={y0 + BAR_W / 2} r={BAR_W / 2} fill={mix(COLOR.tempCap, COLOR.tempSoft, dist)} />
            <circle cx={x(i)} cy={yH(p.h)} r="5" fill={COLOR.hum} />
          </g>
        );
      })}

      <circle cx={cx} cy={CH_BOT + 2} r="4" fill={COLOR.brown} />

      <g style={{ filter: "drop-shadow(0 2px 2.5px rgba(0,0,0,0.25))" }}>
        <rect x={cx - 46.5} y={0.75} width="93" height="21" rx="6" fill={COLOR.tip} stroke={COLOR.brown} strokeWidth="1.5" />
        <path d={`M${cx - 5} 21 L${cx} 27 L${cx + 5} 21 Z`} fill={COLOR.tip} stroke={COLOR.brown} strokeWidth="1.5" strokeLinejoin="round" />
        <rect x={cx - 4} y={19.5} width="8" height="3" fill={COLOR.tip} />
      </g>
      <text
        x={cx}
        y={15}
        textAnchor="middle"
        fill={COLOR.tipText}
        style={{ fontFamily: "Pretendard, sans-serif", fontWeight: 600, fontSize: 10 }}
      >
        {tip}
      </text>

      {xTicks.map(([tx, label, anchor, strong]) => (
        <text
          key={label}
          x={tx}
          y={CH_BOT + 22}
          textAnchor={anchor}
          fill={strong ? COLOR.brown : COLOR.tick}
          style={{ ...FONT, fontWeight: strong ? 600 : 500 }}
        >
          {label}
        </text>
      ))}
    </svg>
  );
}

export default function RecordDetail() {
  const navigate = useNavigate();
  const { date: dateParam } = useParams();
  const start = useMemo(() => parseDate(dateParam), [dateParam]);

  const [ym, setYm] = useState({ y: start.y, m: start.m });
  const [day, setDay] = useState(start.d);
  const [records, setRecords] = useState(null);
  const [activeId, setActiveId] = useState(null);
  const [env, setEnv] = useState(null);
  const [envMissing, setEnvMissing] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setRecords(null);
    fetchMonthRecords(ym.y, ym.m)
      .then((res) => {
        if (!cancelled) setRecords(res);
      })
      .catch((error) => {
        if (cancelled) return;
        if (error.status === 401) navigate("/onboarding", { replace: true });
        else setRecords([]);
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
    setEnvMissing(false);
    fetchRecordEnv(active)
      .then((res) => {
        if (cancelled) return;
        setEnv(res.length ? res : null);
        setEnvMissing(res.length === 0);
      })
      .catch(() => {
        if (!cancelled) setEnvMissing(true);
      });
    return () => {
      cancelled = true;
    };
  }, [activeKey]);

  const envStats = useMemo(() => {
    if (!env) return null;
    const pts = env.filter(Boolean);
    return {
      temp: mean(pts.map((p) => (p.tmin + p.tmax) / 2)),
      humid: mean(pts.map((p) => p.h)),
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

  const [deleting, setDeleting] = useState(false);

  const handleDelete = async () => {
    playButtonSfx();
    if (!active || deleting) return;
    if (!window.confirm("선택한 기록을 삭제할까요?")) return;
    setDeleting(true);
    try {
      await deleteRecord(active.id);
      setRecords((prev) => (prev ?? []).filter((r) => r.id !== active.id));
      setActiveId(null);
    } catch (error) {
      if (error.status === 401) navigate("/onboarding", { replace: true });
      else window.alert("기록을 삭제하지 못했어요. 다시 시도해주세요.");
    } finally {
      setDeleting(false);
    }
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
                  <img src={deleteIcon} alt="" />
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
                        <ul className="record-detail__photos" style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 8, padding: 0, margin: "12px 0 0", listStyle: "none" }}>
                          {r.photos.slice(0, 3).map((src, i) => (
                            <li key={src} style={{ width: "auto", minWidth: 0, aspectRatio: "1 / 1", overflow: "hidden", borderRadius: 12 }}>
                              <AuthImage src={src} alt={`${t.label} 기록 사진 ${i + 1}`} />
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
                <img src={sproutIcon} alt="" /> 당시 사육환경
              </h3>
              {envStats ? (
                <div className="record-detail__stats">
                  <div className="record-detail__stat">
                    <img src={thermoIcon} alt="" />
                    <div>
                      <span className="record-detail__stat-label">평균 온도</span>
                      <strong className="record-detail__stat-value">{envStats.temp.toFixed(1)}°C</strong>
                    </div>
                  </div>
                  <div className="record-detail__stat">
                    <img src={dropIcon} alt="" />
                    <div>
                      <span className="record-detail__stat-label">평균 습도</span>
                      <strong className="record-detail__stat-value">{Math.round(envStats.humid)}%</strong>
                    </div>
                  </div>
                </div>
              ) : (
                <p className="record-detail__loading">{envMissing ? "이 시간대의 환경 데이터가 없어요." : "환경 데이터를 불러오는 중이에요."}</p>
              )}


              <h3 className="record-detail__h">
               <img src={trendIcon} alt="" /> {TYPES[active.type]?.label ?? "기록"} 전후 24시간 환경 변화
              </h3>
              {env && (
                <EnvChart points={env} recordTime={active.time} dateLabel={`${ym.m}/${day}`} />
              )}

              {envStats && (
                <div className="record-detail__note">
                  <div className="record-detail__note-head">
                    <img src={docIcon} alt="" /> <strong>환경 데이터 요약</strong>
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