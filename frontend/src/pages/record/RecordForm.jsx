import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import RecordShedding from "./RecordShedding";
import RecordFeed from "./RecordFeed";
import RecordWeight from "./RecordWeight";
import RecordCleaning from "./RecordCleaning";
import RecordBowel from "./RecordBowel";
import RecordSymptom from "./RecordSymptom";
import RecordEtc from "./RecordEtc";
import RecordSaved from "./RecordSaved";
import "./RecordForm.scss";
import { ApiError, errorMessage } from "../../api/client";
import { saveRecord } from "../../api/record";

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

const RecordForm = () => {
  const { type } = useParams();
  const navigate = useNavigate();
  const [isSaved] = useState(false);
  const [saving, setSaving] = useState(false);

  const handleBack = () => {
    playButtonSfx();
    navigate(-1);
  };

  const saveType = { feed: "feeding", bowel: "poop" }[type] ?? type;

  const handleComplete = async (values) => {
    if (saving) return;
    playButtonSfx();
    setSaving(true);
    try {
      const result = await saveRecord(saveType, values);
      if (result.photoFailed > 0) {
        window.alert(`기록은 저장됐지만 사진 ${result.photoFailed}장은 올리지 못했어요.`);
      }
      navigate("/record/saved", {
        replace: true,
        state: { next: `/record/detail/${result.date}` },
      });
    } catch (error) {
      setSaving(false);
      const fallback = "저장에 실패했어요. 다시 시도해주세요.";
      let message = fallback;
      try {
        message = errorMessage(
          error,
          {
            INVALID_REQUEST: "입력한 내용을 확인해주세요. (체중은 0보다 커야 하고, 직접 입력은 비워둘 수 없어요.)",
            RECORD_ALREADY_EXISTS: "이 날은 이미 기록이 있어요. 하루에 한 건만 작성할 수 있어요.",
            INVALID_PHOTO: "사진 형식을 확인해주세요. (jpg, png, webp)",
          },
          fallback,
        );
      } catch {
      }
      if (error instanceof ApiError && error.status === 401) {
        navigate("/onboarding", { replace: true });
        return;
      }
      window.alert(message);
    }
  };

  const formProps = {
    onBack: handleBack,
    onComplete: handleComplete,
    playButtonSfx,
    playSelectSfx,
  };

  const renderSelectedForm = () => {
    switch (type) {
      case "shedding":
        return <RecordShedding {...formProps} />;
      case "feeding":
      case "feed":
        return <RecordFeed {...formProps} />;
      case "weight":
        return <RecordWeight {...formProps} />;
      case "cleaning":
        return <RecordCleaning {...formProps} />;
      case "poop":
      case "bowel":
        return <RecordBowel {...formProps} />;
      case "symptom":
        return <RecordSymptom {...formProps} />;
      case "etc":
        return <RecordEtc {...formProps} />;
      default:
        return <RecordShedding {...formProps} />;
    }
  };

  if (isSaved) {
    return <RecordSaved />;
  }

  return (
    <div className="record-form-wrapper">
      {renderSelectedForm()}
    </div>
  );
};

export default RecordForm;