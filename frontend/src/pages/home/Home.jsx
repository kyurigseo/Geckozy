import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useSound } from "../../sound/SoundProvider";
import GuideModal from "./GuidePopup";
import "./Home.scss";
import TankPreview from "../../components/tank/TankPreview"


import { fetchHome } from "../../api/home";

import sproutIcon from "../../assets/onboarding/icon-leaf.png";
import leoImg from "../../assets/onboarding/gecko-yellow.png";
import temperatureIcon from "../../assets/home/temp_icon.png";
import humidityIcon from "../../assets/home/humid_icon.png";
import calendarIcon from "../../assets/home/calendar_icon.png";
import rightBtn from "../../assets/home/rightarrow_btn.png";
import stateok from "../../assets/home/stable_icon.png";
import statewarning from "../../assets/home/warning_icon.png";
import musicOn from "../../assets/home/musicon_icon.png";
import musicOff from "../../assets/home/musicoff_icon.png";
import dateBg from "../../assets/home/date_background.png";
import wallpaper from "../../assets/home/home_wallpaper.png";
import defaultTerrarium from "../../assets/home/home_defaultroom.png";
import dropdownIcon from "../../assets/home/dropdown_icon.png"
import careBtn from "../../assets/home/care_guide_btn.png"
import darkRightBtn from "../../assets/home/darker_rightarrow_icon.png"

import buttonSrc from '../../assets/sounds/button.mp3';



// 지표 카드의 아이콘·이동 경로 (값과 상태는 서버에서 받아요)
const METRIC_META = [
  { id: "temperature", label: "온도", icon: temperatureIcon, path: "/temperature", key: "temperature" },
  { id: "humidity", label: "습도", icon: humidityIcon, path: "/humidity", key: "humidity" },
];

export default function Home() {
  const navigate = useNavigate();
  const [activePinId, setActivePinId] = useState(null);
  const [showGuide, setShowGuide] = useState(false);
  const { isSoundOn, toggleSound, playSfx } = useSound();

  // 서버에서 받은 홈 데이터 (도마뱀, 사육장, 알림, 온습도, 요약, 코치)
  const [home, setHome] = useState(null);

  useEffect(() => {
    let cancelled = false;
    fetchHome()
      .then((h) => {
        if (!cancelled) setHome(h);
      })
      .catch((error) => {
        // 로그인이 풀렸으면 처음 화면으로
        if (!cancelled && error.status === 401) navigate("/onboarding", { replace: true });
      });
    return () => {
      cancelled = true;
    };
  }, [navigate]);

  const petName = home?.petName ?? "";
  const tankName = "1번 사육장"; // TODO: 서버에 사육장 이름이 없음
  const tank = home?.tank ?? null;
  const alerts = home?.alerts ?? [];
  const today = new Date();
  const dateStr = today.toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
  const dayName = today.toLocaleDateString("ko-KR", { weekday: "short" });
  const formattedDate = `${dateStr} (${dayName})`;

  useEffect(() => {
    const unlockSfx = () => {
      // 첫 클릭 시 무음으로 한번 재생을 시도해서 브라우저 차단을 풀어버림
      playSfx('button');
      window.removeEventListener('pointerdown', unlockSfx);
    };

    window.addEventListener('pointerdown', unlockSfx);
    return () => window.removeEventListener('pointerdown', unlockSfx);
  }, [playSfx]);

  const playButtonSfx = () => {
    const sound = new Audio(buttonSrc);
    sound.volume = 0.5; 
    sound.currentTime = 0; 
    sound.play().catch((err) => {
      console.warn("효과음 재생 차단됨:", err);
    });
  };



  return (
    <div className="home">
      <div className="home__scroll">
        {/* 날짜 */}
        <div className="home__date" style={{ backgroundImage: `url(${dateBg})` }}>
          {formattedDate}
        </div>

        {/* 인사 */}
        <h1 className="home__greeting">
          안녕하세요 <span className="accent">수정님</span>
          <img src={sproutIcon} alt="" className="home__sprout" />
        </h1>

        {/* 사육장 선택 */}
        <button type="button" className="selector">
          <img src={sproutIcon} alt="" />
          <span>
            {petName} • {tankName} • <em>새 알림 {alerts.length}개</em>
          </span>
          <span className="selector__arrow" aria-hidden="true"><img src={dropdownIcon} className="selector__dropdown-icon" /></span>
        </button>

        {/* 사육장 일러스트 + 알림 핀 */}
        <section className="stage">
          <TankPreview tank={tank} className="home-tank-preview" />
        
 
              <button
                  type="button"
                  className={`stage__sound ${isSoundOn ? "" : "is-off"}`}
                  onClick={toggleSound}
                  aria-label="소리 켜기/끄기"
                >
                  <span aria-hidden="true">
                    <img 
                      src={isSoundOn ? musicOn : musicOff} 
                      alt={isSoundOn ? "소리 켬" : "소리 끔"} 
                    />
                  </span>
                </button>

          {alerts.map((a) => {
              const isOpen = activePinId === a.id;
              return (
                <div key={a.id} className="pin" style={{ left: `${a.x}%`, top: `${a.y}%` }}>
                  {isOpen && (
                    <button
                      type="button"
                      className={`pin__bubble pin__bubble--${a.bubbleSide}`}
                      onClick={() => navigate(a.link)}
                    >
                      <strong>{a.text}</strong>
                      <small>[확인하러 가기]</small>
                    </button>
                  )}

                  <button
                    type="button"
                    className="pin__dot"
                    onClick={() => setActivePinId(isOpen ? null : a.id)}
                    aria-label="알림 보기"
                  >
                    !
                  </button>
                </div>
              );
            })}
        </section>

        {/* 하단 시트 */}
        <section className="sheet">
          <h2 className="sheet__title">
            {petName}의 사육장 <img src={sproutIcon} alt="" />
          </h2>

          <div className="card summary">
            <img src={leoImg} alt={petName} className="summary__leo" />
            <div className="summary__text">
              <h3>
                <img src={calendarIcon} alt="" /> 오늘의 사육환경
              </h3>
              <p>{home?.summary ?? "아직 오늘의 데이터가 충분하지 않아요."}</p>
            </div>
          </div>

          <div className="metrics">
            {METRIC_META.map((meta) => {
              const v = home?.[meta.key];
              const m = {
                ...meta,
                value: v?.value ?? "-",
                unit: v?.unit ?? "",
                tone: v?.tone ?? "warn",
                status: v?.label ?? "확인 중",
                statusIcon: v?.tone === "ok" ? stateok : statewarning,
              };
              return (
              <button
                key={m.id}
                type="button"
                className="card metric"
                onClick={() => navigate(m.path)}
              >

                <div className="metric__head">
                    <span className={`metric__icon metric__icon--${m.id}`}>
                      <img src={m.icon} alt="" />
                    </span>
                    <span className="metric__label">{m.label}</span>
                    <span className="metric__chev" aria-hidden="true">
                      <img src={rightBtn} alt="" />
                    </span>
                  </div>

                <div className="metric__value">
                  {m.value}
                  <span>{m.unit}</span>
                </div>
                <span className={`badge badge--${m.tone}`}>
                  <img src={m.statusIcon} alt="" className="badge__icon" />
                  {m.status}</span>
              </button>
              );
            })}
          </div>

          <div className="card coach">
            <h3>
              <img src={sproutIcon} alt="" /> {petName}의 환경 코치
            </h3>
            <p>{home?.coach?.text ?? "지금은 특별히 안내할 내용이 없어요."}</p>
            <button type="button" className="coach__btn" 
      
            onClick={() => {
              console.log("버튼 클릭됨 / playSfx 호출");
              playButtonSfx();
              setShowGuide(true);}}
            >
              관리 도감 보기 <span aria-hidden="true"><img src={darkRightBtn} alt="이동" /></span>
            </button>
          </div>
        </section>
      </div>

      {showGuide && <GuideModal onClose={() => setShowGuide(false)} />}
    </div>
  );
}