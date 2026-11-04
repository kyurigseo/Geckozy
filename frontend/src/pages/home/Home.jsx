import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import GuideModal from "./GuidePopup";
import "./Home.scss";

import { getMyProfile, getMyTank } from "../../api/my";

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


const TERRARIUMS = [{ id: 1, pet: "레오", name: "1번 사육장", alerts: 2 }];

const ALERTS = [
  { id: "humidity", x: 76, y: 20, bubbleSide: "left", text: "습도가 오래 유지되고 있어요!", link: "/humidity" },
  { id: "sensor", x: 33, y: 55, bubbleSide: "right", text: "센서 연결을 확인해주세요.", link: "/my/sensor" },
];

const METRICS = [
  { id: "temperature", label: "온도", icon: temperatureIcon, value: "24.2", unit: "°C", status: "안정", statusIcon: stateok, tone: "ok", path: "/temperature" },
  { id: "humidity", label: "습도", icon: humidityIcon, value: "78", unit: "%", status: "확인 필요", statusIcon: statewarning, tone: "warn", path: "/humidity" },
];

export default function Home() {
  const navigate = useNavigate();
  const [showAlerts, setShowAlerts] = useState(false);
  const [soundOn, setSoundOn] = useState(true);
  const [showGuide, setShowGuide] = useState(false);

  // 마이페이지에서 고른 프로필/사육장
  const [profile, setProfile] = useState(null);
  const [tank, setTank] = useState(null);

  useEffect(() => {
    let cancelled = false;
    Promise.all([getMyProfile(), getMyTank()]).then(([p, t]) => {
      if (cancelled) return;
      setProfile(p);
      setTank(t);
    });
    return () => {
      cancelled = true;
    };
  }, []);

  const current = TERRARIUMS[0];
  const petName = profile?.name ?? current.pet;
  const tankName = profile?.tankName ?? current.name;
  const today = new Date();
  const dateStr = today.toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
  const dayName = today.toLocaleDateString("ko-KR", { weekday: "short" });
  const formattedDate = `${dateStr} (${dayName})`;

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
            {petName} • {tankName} • <em>새 알림 {ALERTS.length}개</em>
          </span>
          <span className="selector__arrow" aria-hidden="true"><img src={dropdownIcon} className="selector__dropdown-icon" /></span>
        </button>

        {/* 사육장 일러스트 + 알림 핀 */}
        <section className="stage">
          {/* TODO: 팀원이 TankPreview를 export하면 {tank && <TankPreview tank={tank} />}로 교체 */}
          <img 
            src={current?.image || defaultTerrarium} 
            alt={tankName || "사육장"} 
            className="stage__img" 
          />

          <button
            type="button"
            className={`stage__sound ${soundOn ? "" : "is-off"}`}
            onClick={() => setSoundOn((v) => !v)}
            aria-label="소리 켜기/끄기"
          >
            <span aria-hidden="true">
              {soundOn ? <img src={musicOn} alt="소리 켬" /> : <img src={musicOff} alt="소리 끔" />}
            </span>
          </button>

          {ALERTS.map((a) => (
            <div key={a.id} className="pin" style={{ left: `${a.x}%`, top: `${a.y}%` }}>
              {showAlerts && (
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
                onClick={() => setShowAlerts((v) => !v)}
                aria-label="알림 보기"
              >
                !
              </button>
            </div>
          ))}
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
              <p>온도는 비교적 일정했고, <br/>습도는 오후부터 높게 유지되고 있어요.</p>
            </div>
          </div>

          <div className="metrics">
            {METRICS.map((m) => (
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
            ))}
          </div>

          <div className="card coach">
            <h3>
              <img src={sproutIcon} alt="" /> {petName}의 환경 코치
            </h3>
            <p>
              분무 이후 습도가 평소보다 오래 유지되고 있어요. 환기가 원활한지 확인해보는 것을 추천드려요!
            </p>
            <button type="button" className="coach__btn" onClick={() => setShowGuide(true)}>
              관리 도감 보기 <span aria-hidden="true"><img src={darkRightBtn} alt="이동" /></span>
            </button>
          </div>
        </section>
      </div>

      {showGuide && <GuideModal onClose={() => setShowGuide(false)} />}
    </div>
  );
}