import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './RecordList.scss';
import { deleteRecord, fetchMonthRecords, fetchPhotoBlob } from '../../api/record';

import iconBack from '../../assets/record/icon_before.png';
import iconEdit from '../../assets/record/icon_edit.png';
import iconDelete from '../../assets/record/icon_delete.png';

import iconFood from '../../assets/record/icon_feeding.png';
import iconEtc from '../../assets/record/icon_etc.png';
import iconShedding from '../../assets/record/icon_shedding.png';
import iconClean from '../../assets/record/icon_cleaning.png';
import iconSymptom from '../../assets/record/icon_symptom.png';
import iconPoop from '../../assets/record/icon_poop.png';
import iconWeight from '../../assets/record/icon_weight.png';

import bgBadgeGreen from '../../assets/record/bg_badge_green.png';
import bgBadgeOrange from '../../assets/record/bg_badge_orange.png';
import bgBtnFinish from '../../assets/record/bg_btn_finish.png';

import buttonSrc from '../../assets/sounds/button.mp3';
import selectSrc from '../../assets/sounds/select.mp3';

import lineGreen from '../../assets/record/line_green.svg';
import lineOrange from '../../assets/record/line_orange.svg';

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

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];
const TYPE_META = {
  feeding: { label: '먹이', icon: iconFood },
  shedding: { label: '탈피', icon: iconShedding },
  weight: { label: '체중', icon: iconWeight },
  cleaning: { label: '청소', icon: iconClean },
  poop: { label: '배변', icon: iconPoop },
  symptom: { label: '이상증세', icon: iconSymptom },
  etc: { label: '추가기록', icon: iconEtc },
};

const fmtTime = (hhmm) => {
  const [h, m] = hhmm.split(':').map(Number);
  return `${h < 12 ? '오전' : '오후'} ${h % 12 || 12}시${m ? ` ${m}분` : ''}`;
};

const toItem = (r, i) => ({
  id: r.id,
  date: String(Number(r.date.slice(8, 10))),
  day: WEEKDAYS[new Date(r.date).getDay()],
  color: i % 2 === 0 ? 'green' : 'orange',
  type: TYPE_META[r.type].label,
  icon: TYPE_META[r.type].icon,
  time: fmtTime(r.time),
  text: r.memo || r.tags.map((t) => t.text).join(' · '),
  imgUrl: r.photos[0] ?? null,
});

function AuthImage({ src, alt, className }) {
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

  return url ? <img src={url} alt={alt} className={className} /> : null;
}

export default function RecordList() {
  const navigate = useNavigate();
  const [records, setRecords] = useState([]);
  const [isEditing, setIsEditing] = useState(false);
  const month = new Date().getMonth() + 1;

  useEffect(() => {
    let cancelled = false;
    const now = new Date();
    fetchMonthRecords(now.getFullYear(), now.getMonth() + 1)
      .then((list) => {
        if (cancelled) return;
        const sorted = [...list].sort((a, b) => a.recordedAt.localeCompare(b.recordedAt));
        setRecords(sorted.map(toItem));
      })
      .catch((error) => {
        if (cancelled) return;
        if (error.status === 401) navigate('/onboarding', { replace: true });
        else setRecords([]);
      });
    return () => {
      cancelled = true;
    };
  }, [navigate]);

  const handleDelete = async (id) => {
    playSelectSfx();
    if (!window.confirm('이 기록을 삭제할까요?')) return;
    try {
      await deleteRecord(id);
      setRecords((prev) => prev.filter((r) => r.id !== id));
    } catch (error) {
      if (error.status === 401) navigate('/onboarding', { replace: true });
      else window.alert('기록을 삭제하지 못했어요. 다시 시도해주세요.');
    }
  };

  return (
    <div className={`record-list-page ${isEditing ? 'is-editing-mode' : ''}`}>
      <header className="list-header">
        <button
          type="button"
          className="header-btn"
          onClick={() => {
            playButtonSfx();
            navigate(-1);
          }}
        >
          <img src={iconBack} alt="뒤로가기" />
        </button>
        <h1 className="header-title">{month}월의 기록 ({records.length}건)</h1>
        <button
          type="button"
          className={`header-btn ${isEditing ? 'active' : ''}`}
          onClick={() => {
            playButtonSfx();
            setIsEditing((v) => !v);
          }}
        >
          <img src={iconEdit} alt="편집" />
        </button>
      </header>

      <main className="list-content">
        {records.map((item, index) => (
      <div key={item.id} className={`timeline-item ${item.color}`}>
        {index < records.length - 1 && (
          <img
            src={item.color === 'green' ? lineGreen : lineOrange}
            alt=""
            className="timeline-line"
          />
        )}
            <div className={`date-badge ${item.color}`}>
            <img
                src={item.color === 'green' ? bgBadgeGreen : bgBadgeOrange}
                alt=""
                className="badge-bg"
            />
            <div className="badge-text">
                <span className="date-num">{item.date}</span>
                <span className="date-day">{item.day}</span>
            </div>
            </div>

            <div className="record-card">
              {isEditing && (
                <button
                  type="button"
                  className="card-delete-btn"
                  onClick={() => handleDelete(item.id)}
                >
                  <img src={iconDelete} alt="삭제" />
                </button>
              )}

              <div className="card-main">
                <div className="card-info">
                  <div className="type-row">
                    <img src={item.icon} alt="" className="type-icon" />

                    <div className="type-text-wrap">
                        <span className="type-name">{item.type}</span>
                        <span className="time-text">{item.time}</span>
                    </div>
                    </div>
                  <p className="record-text">{item.text}</p>
                </div>

                {item.imgUrl && (
                  <AuthImage src={item.imgUrl} alt="기록 썸네일" className="record-img" />
                )}
              </div>
            </div>
          </div>
        ))}
      </main>

      {isEditing && (
        <div className="finish-btn-wrapper">
          <img src={bgBtnFinish} alt="" className="finish-btn-bg" />

          <button
            type="button"
            className="finish-btn"
            onClick={() => {
              playSelectSfx();
              setIsEditing(false);
            }}
          >
            완료
          </button>
        </div>
      )}
    </div>
  );
}