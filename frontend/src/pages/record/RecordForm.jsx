import { useNavigate, useParams } from "react-router-dom";

const LABELS = {
  feeding: "먹이",
  shedding: "탈피",
  weight: "체중",
  cleaning: "청소",
  poop: "배변",
  symptom: "이상 증세",
  etc: "기타",
};

export default function RecordForm() {
  const { type } = useParams();
  const navigate = useNavigate();

  return (
    <div style={{ padding: 24 }}>
      <button type="button" onClick={() => navigate(-1)}>
        ‹ 뒤로
      </button>
      <h1>{LABELS[type] ?? type} 기록 (준비 중)</h1>
    </div>
  );
}
