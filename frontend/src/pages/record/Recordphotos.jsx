import { useEffect, useRef } from "react";

import btnAddPhoto from "../../assets/record/btn_add_photo.png";
import photoEmpty from "../../assets/record/photo_empty.png";

const MAX = 3;

export default function RecordPhotos({ photos, onChange }) {
  const inputRef = useRef(null);
  const urls = useRef(new Map());

  const previewOf = (file) => {
    if (!urls.current.has(file)) urls.current.set(file, URL.createObjectURL(file));
    return urls.current.get(file);
  };

  useEffect(() => {
    const map = urls.current;
    return () => map.forEach((url) => URL.revokeObjectURL(url));
  }, []);

  const handlePick = (e) => {
    const picked = Array.from(e.target.files ?? []);
    e.target.value = "";
    if (picked.length) onChange([...photos, ...picked].slice(0, MAX));
  };

  const remove = (file) => onChange(photos.filter((p) => p !== file));

  return (
    <section className="form-section photo-section">
      <h2 className="photo-title">사진 추가</h2>
      <p className="photo-sub">최대 3장의 사진을 업로드할 수 있어요.</p>
      <input ref={inputRef} type="file" accept="image/jpeg,image/png,image/webp" multiple hidden onChange={handlePick} />
      <div className="photo-grid">
        {photos.length < MAX && (
          <button type="button" className="photo-btn-img" onClick={() => inputRef.current?.click()}>
            <img src={btnAddPhoto} alt="사진 추가" />
          </button>
        )}
        {photos.map((file) => (
          <button
            key={previewOf(file)}
            type="button"
            className="photo-placeholder-img"
            onClick={() => remove(file)}
            aria-label="사진 빼기"
            style={{ padding: 0, border: 0, background: "none" }}
          >
            <img src={previewOf(file)} alt="첨부한 사진" style={{ width: "100%", height: "100%", objectFit: "cover" }} />
          </button>
        ))}
        {Array.from({ length: Math.max(0, MAX - photos.length - (photos.length < MAX ? 1 : 0)) }, (_, i) => (
          <div key={`empty-${i}`} className="photo-placeholder-img">
            <img src={photoEmpty} alt="빈 슬롯" />
          </div>
        ))}
      </div>
    </section>
  );
}