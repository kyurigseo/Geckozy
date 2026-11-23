import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import "./Recordsaved.scss";

import bgImg from "../../assets/record/saved_bg.png"; 
import geckoImg from "../../assets/record/saved_gecko.png";
import soundOnImg from "../../assets/record/saved_sound.png"; 
import soundOffImg from "../../assets/record/saved_sound_off.png";
import dotOffImg from "../../assets/record/saved_dot_off.png"; 
import dotOnImg from "../../assets/record/saved_dot_on.png";   

const LOADING_MS = 2500; 
const DEFAULT_NEXT = "/record"; 



export default function RecordSaved() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const [soundOn, setSoundOn] = useState(true); 

  const next = typeof state?.next === "string" && state.next.startsWith("/") ? state.next : DEFAULT_NEXT;

  useEffect(() => {
    const timer = setTimeout(() => navigate(next, { replace: true }), LOADING_MS);
    return () => clearTimeout(timer);
  }, [navigate, next]);

  return (
    <main className="record-saved" style={{ backgroundImage: `url(${bgImg})` }}>
      <div className="record-saved__text" role="status">
        <h1>기록이 저장되었어요!</h1>
        <p>기록 화면으로 이동할게요</p>
      </div>

      <img className="record-saved__gecko" src={geckoImg} alt="" />

      <div className="record-saved__loading" aria-hidden="true">
        <span>Loading...</span>
        <div className="record-saved__dots">
          {[0, 1, 2].map((idx) => (
            <div key={idx} className="dot-wrapper">
              <img src={dotOffImg} className="dot-off" alt="" />
              <img src={dotOnImg} className="dot-on" alt="" />
            </div>
          ))}
        </div>
      </div>

      <button
        type="button"
        className={`record-saved__sound ${soundOn ? "" : "is-off"}`}
        aria-pressed={soundOn}
        aria-label={soundOn ? "소리 끄기" : "소리 켜기"}
        onClick={() => setSoundOn((v) => !v)}
      >
        <img src={soundOn ? soundOnImg : soundOffImg} alt="" />
      </button>
    </main>
  );
}
