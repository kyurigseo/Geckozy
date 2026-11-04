import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useSound } from "../../sound/SoundProvider";

import "./Record.scss";

import bgImg from "../../assets/record/bg.svg"; 
import addBtnImg from "../../assets/record/add_btn.png"; 
import feedingIcon from "../../assets/record/icon_feeding.png"; 
import sheddingIcon from "../../assets/record/icon_shedding.png"; 
import weightIcon from "../../assets/record/icon_weight.png"; 
import cleaningIcon from "../../assets/record/icon_cleaning.png"; 
import poopIcon from "../../assets/record/icon_poop.png"; 
import symptomIcon from "../../assets/record/icon_symptom.png"; 
import etcIcon from "../../assets/record/icon_etc.png"; 

// 🎯 효과음 파일 import
import buttonSrc from '../../assets/sounds/button.mp3';
import selectSrc from '../../assets/sounds/select.mp3';

// 🎯 BGM On/Off 상태와 무관하게 항상 소리가 나는 독립 효과음 함수
const playButtonSfx = () => {
  const sound = new Audio(buttonSrc);
  sound.volume = 0.5;
  sound.currentTime = 0;
  sound.play().catch(() => {});
};

const playSelectSfx = () => {
  const sound = new Audio(selectSrc);
  sound.volume = 0.5;
  sound.currentTime = 0;
  sound.play().catch(() => {});
};

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

const SUMMARY_COLOR = { shedding: "#5B911C", poop: "#FF9834", cleaning: "#FF646E" };

const WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"];

const SAMPLE_DAYS = [
  [1, "feeding"], [4, "etc"], [9, "shedding"], [10, "shedding"], [14, "cleaning"],
  [22, "feeding"], [24, "symptom"], [25, "poop", true], [27, "weight"],
];

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const fetchRecords = async (year, month) => {
  await delay(200);
  const lastDay = new Date(year, month, 0).getDate();
  const pad = (n) => String(n).padStart(2, "0");
  return {
    records: SAMPLE_DAYS.filter(([d]) => d <= lastDay).map(([d, type, alert]) => ({
      date: `${year}-${pad(month)}-${pad(d)}`,
      type,
      alert: Boolean(alert),
    })),
    summary: [
      { type: "shedding", percent: 50, message: "이번 달에는 탈피가 1회 확인됐어요." },
      { type: "poop", percent: 80, message: "지난 달보다 배변 이상 기록이 늘었어요." },
      { type: "cleaning", percent: -40, message: "사육장 청소 주기가 지난달보다 길어졌어요." },
    ],
  };
};

// ─────────── 달력 계산 ───────────
const buildCells = (year, month) => {
  const blanks = new Date(year, month - 1, 1).getDay();
  const days = new Date(year, month, 0).getDate();
  return [...Array(blanks).fill(null), ...Array.from({ length: days }, (_, i) => i + 1)];
};

// ─────────── 요약 한 줄 (아이콘 + 막대 + 증감 + 설명) ───────────
function SummaryRow({ item }) {
  const { label, icon } = TYPES[item.type] ?? { label: item.type, icon: etcIcon };
  const color = SUMMARY_COLOR[item.type] ?? "#B47952";
  const filled = Math.min(10, Math.round(Math.abs(item.percent) / 10));
  const up = item.percent >= 0;

  return (
    <li className="record-summary__row" style={{ "--c": color }}>
      <img className="record-summary__icon" src={icon} alt="" />
      <div className="record-summary__body">
        <div className="record-summary__head">
          <strong className="record-summary__label">{label}</strong>
          <div className="record-summary__bar" role="img" aria-label={`지난달 대비 ${up ? "증가" : "감소"} ${Math.abs(item.percent)}%`}>
            {Array.from({ length: 10 }, (_, i) => (
              <i
                key={i}
                className={i < filled ? "is-on" : ""}
                style={i < filled ? { opacity: 1 - i * 0.07 } : undefined}
              />
            ))}
          </div>
          <span className="record-summary__delta">
            {up ? "▲" : "▼"} {Math.abs(item.percent)}%
          </span>
        </div>
        <p className="record-summary__msg">{item.message}</p>
      </div>
    </li>
  );
}

// ─────────── 화면 ───────────
export default function Record() {
  const navigate = useNavigate();
  const today = new Date();
  const [ym, setYm] = useState({ y: today.getFullYear(), m: today.getMonth() + 1 });
  const [data, setData] = useState({ records: [], summary: [] });
  const [menuOpen, setMenuOpen] = useState(false);
  const { isSoundOn, toggleSound, playSfx } = useSound();

  useEffect(() => {
    let cancelled = false;
    fetchRecords(ym.y, ym.m).then((res) => {
      if (!cancelled) setData(res);
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
    data.records.forEach((r) => {
      const day = Number(r.date.slice(8, 10));
      if (!map.has(day)) map.set(day, r);
    });
    return map;
  }, [data.records]);

  const moveMonth = (diff) =>
    setYm(({ y, m }) => {
      const d = new Date(y, m - 1 + diff, 1);
      return { y: d.getFullYear(), m: d.getMonth() + 1 };
    });

  const openForm = (type) => {
    setMenuOpen(false);
    navigate(`/record/new/${type}`);
  };

  return (
    <div className="record-page" style={{ backgroundImage: `url(${bgImg})` }}>
      <div className="record-page__scroll">
        <header className="record-cal__nav">
          <button 
            type="button" 
            className="record-cal__arrow" 
            onClick={() => {
              playButtonSfx(); // 🎯 이전 달 클릭 효과음
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
              playButtonSfx(); // 🎯 다음 달 클릭 효과음
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
          {cells.map((day, i) => {
            const rec = day ? byDay.get(day) : null;
            const type = rec ? TYPES[rec.type] : null;
            return (
              <li key={day ?? `blank-${i}`} className="record-cal__cell">
                {day && <span className="record-cal__num">{day}</span>}
                {rec && type && (
                  <span className="record-cal__icon">
                    <img src={type.icon} alt={`${day}일 ${type.label}`} />
                    {rec.alert && <i className="record-cal__dot" aria-label="확인이 필요한 기록" />}
                  </span>
                )}
              </li>
            );
          })}
        </ul>

        {/* 기록 추가하기 + 7개 아이콘 메뉴 */}
        {menuOpen && <div className="record-menu__backdrop" onClick={() => setMenuOpen(false)} aria-hidden="true" />}
        <div className={`record-add ${menuOpen ? "is-open" : ""}`}>
          <button
            type="button"
            className="record-add__btn"
            style={{ backgroundImage: `url(${addBtnImg})` }}
            aria-haspopup="menu"
            aria-expanded={menuOpen}
            onClick={() => {
              playButtonSfx(); // 🎯 기록 추가하기 버튼 클릭 효과음
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
                    playSelectSfx(); // 🎯 7가지 카테고리 항목 선택 효과음
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

        <section className="record-sheet">
          <div className="record-sheet__head">
            <h2>
              {ym.m}월의 기록 <span>({data.records.length}건)</span>
            </h2>
            <button 
              type="button" 
              className="record-sheet__more" 
              onClick={() => {
                playButtonSfx(); // 🎯 자세히 보기 버튼 효과음
                navigate("/record/list");
              }}
            >
              자세히 보기 <span aria-hidden="true">›</span>
            </button>
          </div>
          <p className="record-sheet__sub">지난달 대비 변화가 큰 기록을 확인해보세요.</p>

          {data.summary.length > 0 ? (
            <ul className="record-summary">
              {data.summary.map((item) => (
                <SummaryRow key={item.type} item={item} />
              ))}
            </ul>
          ) : (
            <p className="record-sheet__empty">이 달에는 아직 기록이 없어요.</p>
          )}
        </section>
      </div>
    </div>
  );
}