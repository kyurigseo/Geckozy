import { useEffect, useState } from "react";
import "./GuidePopup.scss";



const CHECKS = [
  "상단이나 측면의 환기구가 장식물에 가려져 있지 않나요?",
  "사육장이 벽이나 가구에 너무 밀착되어 있지 않나요?",
  "분무 후 벽면이나 내부의 물기가 지나치게 오래 남아 있지는 않나요?",
  "바닥재가 계속 축축한 상태로 유지되고 있지는 않나요?",
];

// 각 페이지를 함수로 두면 페이지를 추가/수정하기 쉬워요
const PAGES = [
  function Why() {
    return (
      <>
        <img src={ventImg} alt="환기구가 있는 사육장 일러스트" className="guide__hero" />
        <h3 className="guide__sub"><img src={sproutIcon} alt="" /> 환기는 왜 필요한가요?</h3>
        <p className="guide__text">
          <strong>분무 후 사육장 내부의 수분이 자연스럽게 빠져나가면서 습도가 다시 낮아질 수 있도록 도와줘요.</strong>{" "}
          적절한 공기 흐름은 습윤 - 건조 사이클을 만드는 데 중요해요.
        </p>
      </>
    );
  },
  function Check() {
    return (
      <>
        <h3 className="guide__sub"><img src={sproutIcon} alt="" /> 환기할 때 확인할 곳</h3>
        <ul className="guide__checks">
          {CHECKS.map((c) => (
            <li key={c}>
              <span className="guide__check" aria-hidden="true">✓</span>
              <p>{c}</p>
            </li>
          ))}
        </ul>
      </>
    );
  },
  function Manage({ scrapped, onScrap }) {
    return (
      <>
        <h3 className="guide__sub"><img src={sproutIcon} alt="" /> 이렇게 관리해보세요</h3>
        <p className="guide__text">
          <strong>같은 상황이 반복되면, 분무량을 조절해주세요</strong>
          <br />
          환기 상태에 문제가 없는데도 습한 상태가 오래 이어진다면, 다음 분무 때 물의 양을 조금 줄여보고 건조 속도가 달라지는지 비교해보세요.
        </p>
        <div className="guide__warn">
          <h4><span aria-hidden="true">⚠️</span> 주의 사항</h4>
          <p>습도를 빠르게 낮추기 위해 선풍기나 에어컨 바람을 사육장에 직접 쐬지 마세요.</p>
        </div>
        <button
          type="button"
          className={`guide__scrap ${scrapped ? "is-on" : ""}`}
          onClick={onScrap}
          aria-pressed={scrapped}
        >
          {scrapped ? "스크랩됨" : "스크랩"}
        </button>
      </>
    );
  },
];

export default function GuideModal({ onClose }) {
  const [page, setPage] = useState(0);
  const [scrapped, setScrapped] = useState(false); // 실제로는 서버/로컬 저장소와 연결
  const last = PAGES.length - 1;
  const Page = PAGES[page];

  const prev = () => setPage((p) => Math.max(0, p - 1));
  const next = () => setPage((p) => Math.min(last, p + 1));

  // 키보드: ← → 이동, ESC 닫기 / 팝업 중 뒤 화면 스크롤 잠금
  useEffect(() => {
    const onKey = (e) => {
      if (e.key === "Escape") onClose();
      if (e.key === "ArrowLeft") prev();
      if (e.key === "ArrowRight") next();
    };
    window.addEventListener("keydown", onKey);
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      window.removeEventListener("keydown", onKey);
      document.body.style.overflow = prevOverflow;
    };
  }, [onClose]); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <div className="guide" onClick={onClose}>
      <div
        className="guide__panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="guide-title"
        onClick={(e) => e.stopPropagation()}
      >
        <button type="button" className="guide__close" onClick={onClose} aria-label="닫기">✕</button>

        <h2 id="guide-title" className="guide__title">사육장 환기 관리</h2>

        {/* key={page}: 페이지가 바뀔 때마다 새로 그려서 전환 애니메이션이 다시 실행됨 */}
        <div className="guide__page" key={page}>
          <Page scrapped={scrapped} onScrap={() => setScrapped((v) => !v)} />
        </div>

        <footer className="guide__pager">
          <button type="button" className="guide__arrow" onClick={prev} disabled={page === 0} aria-label="이전">‹</button>
          <span aria-live="polite">{page + 1}/{PAGES.length}</span>
          <button type="button" className="guide__arrow" onClick={next} disabled={page === last} aria-label="다음">›</button>
        </footer>
      </div>
    </div>
  );
}
