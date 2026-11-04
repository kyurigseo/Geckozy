import { useEffect, useRef, useState } from "react";
import "./GuidePopup.scss";

import closeImg from "../../assets/home/close.png";
import ventImg from "../../assets/home/vent.png";
import checkImg from "../../assets/home/check.png";
import warnImg from "../../assets/home/warn.png"; 
import sproutIcon from "../../assets/home/icon-leaf.png"; 
import frameImg from "../../assets/home/frame.png"; 
import prevImg from "../../assets/home/prev.png"; 
import nextImg from "../../assets/home/next.png"; 

import buttonSrc from "../../assets/sounds/button.mp3";
import selectSrc from "../../assets/sounds/select.mp3";

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

const PAGES = [
  {
    key: "why",
    title: "환기는 왜 필요한가요?",
    lead: "분무 후 사육장 내부의 수분이 자연스럽게 빠져나가면서 습도가 다시 낮아질 수 있도록 도와줘요.",
    body: "적절한 공기 흐름은 습윤 - 건조 사이클을 만드는 데 중요해요.",
  },
  {
    key: "check",
    title: "환기할 때 확인할 곳",
    items: [
      { strong: "상단이나 측면의 환기구", rest: "가 장식물에 가려져 있지 않나요?" },
      { strong: "사육장이 벽이나 가구에", rest: " 너무 밀착되어 있지 않나요?" },
      { strong: "분무 후 벽면이나 내부의 물기", rest: "가 지나치게 오래 남아 있지는 않나요?" },
      { strong: "바닥재가 계속 축축한 상태", rest: "로 유지되고 있지는 않나요?" },
    ],
  },
  {
    key: "manage",
    title: "이렇게 관리해보세요",
    lead: "같은 상황이 반복되면, 분무량을 조절해주세요",
    body: "환기 상태에 문제가 없는데도 습한 상태가 오래 이어진다면, 다음 분무 때 물의 양을 조금 줄여보고 건조 속도가 달라지는지 비교해보세요.",
    warn: {
      title: "주의 사항",
      text: "습도를 빠르게 낮추기 위해 선풍기나 에어컨 바람을 사육장에 직접 쐬지 마세요.",
    },
  },
];

const LAST = PAGES.length - 1;

function PageBody({ page, scrapped, onScrap }) {
  const heading = (
    <h3 className="guide-popup__sub">
      <img src={sproutIcon} alt="" /> {page.title}
    </h3>
  );

  if (page.key === "why") {
    return (
      <>
        <img src={ventImg} alt="환기구가 있는 사육장 일러스트" className="guide-popup__hero" />
        {heading}
        <p className="guide-popup__text">
          <strong>{page.lead}</strong> <br />{page.body}
        </p>
      </>
    );
  }

  if (page.key === "check") {
    return (
      <>
        {heading}
        <ul className="guide-popup__checks">
          {page.items.map((item) => (
            <li key={item.strong}>
              <img src={checkImg} alt="" className="guide-popup__check" />
              <p>
                <strong>{item.strong}</strong>
                {item.rest}
              </p>
            </li>
          ))}
        </ul>
      </>
    );
  }

  return (
    <>
      {heading}
      <p className="guide-popup__text">
        <strong>{page.lead}</strong>
        <br />
        {page.body}
      </p>

      <div className="guide-popup__warn">
        <h4>
          <img src={warnImg} alt="" /> {page.warn.title}
        </h4>
        <p>{page.warn.text}</p>
      </div>

      <button
        type="button"
        className={`guide-popup__scrap ${scrapped ? "is-on" : ""}`}
        onClick={() => {
          playSelectSfx(); 
          onScrap();
        }}
        aria-pressed={scrapped}
      >
        {scrapped ? "스크랩됨" : "스크랩"}
      </button>
    </>
  );
}


export default function GuidePopup({ onClose, initialScrapped = false }) {
  const [page, setPage] = useState(0);
  const [scrapped, setScrapped] = useState(initialScrapped); // TODO: 서버/로컬 저장소와 연결
  const panelRef = useRef(null);

  const prev = () => setPage((p) => Math.max(0, p - 1));
  const next = () => setPage((p) => Math.min(LAST, p + 1));

  useEffect(() => {
    const onKey = (e) => {
      if (e.key === "Escape") onClose();
      if (e.key === "ArrowLeft") setPage((p) => Math.max(0, p - 1));
      if (e.key === "ArrowRight") setPage((p) => Math.min(LAST, p + 1));
    };
    window.addEventListener("keydown", onKey);

    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    const opener = document.activeElement;
    panelRef.current?.focus();

    return () => {
      window.removeEventListener("keydown", onKey);
      document.body.style.overflow = prevOverflow;
      opener?.focus?.();
    };
  }, [onClose]);

  return (
    <div className="guide-popup" onClick={onClose}>
      <div
        ref={panelRef}
        className="guide-popup__panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="guide-popup-title"
        tabIndex={-1}
        style={{ backgroundImage: `url(${frameImg})` }}
        onClick={(e) => e.stopPropagation()}
      >
        <button 
          type="button" 
          className="guide-popup__close" 
          onClick={() => {
            playButtonSfx(); // 🎯 닫기 버튼 효과음
            onClose();
          }} 
          aria-label="닫기"
        >
          <img src={closeImg} alt="" />
        </button>

        <h2 id="guide-popup-title" className="guide-popup__title">
          사육장 환기 관리
        </h2>

        <div className="guide-popup__page" key={page}>
          <PageBody page={PAGES[page]} scrapped={scrapped} onScrap={() => setScrapped((v) => !v)} />
        </div>

        <footer className="guide-popup__pager">
          <button 
            type="button" 
            className="guide-popup__arrow" 
            onClick={() => {
              if (page > 0) playButtonSfx(); // 🎯 첫 페이지가 아닐 때만 효과음 재생
              prev();
            }} 
            disabled={page === 0} 
            aria-label="이전 페이지"
          >
            <img src={prevImg} alt="" />
          </button>
          <span className="guide-popup__count" aria-live="polite">
            {page + 1}/{PAGES.length}
          </span>
          <button 
            type="button" 
            className="guide-popup__arrow" 
            onClick={() => {
              if (page !== LAST) playButtonSfx(); // 🎯 마지막 페이지가 아닐 때만 효과음 재생
              next();
            }} 
            disabled={page === LAST} 
            aria-label="다음 페이지"
          >
            <img src={nextImg} alt="" />
          </button>
        </footer>
      </div>
    </div>
  );
}