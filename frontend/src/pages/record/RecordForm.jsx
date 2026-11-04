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
  const [isSaved, setIsSaved] = useState(false);

  // 뒤로가기 공통 핸들러
  const handleBack = () => {
    playButtonSfx();
    navigate(-1);
  };

  const handleComplete = () => {
     playButtonSfx(); 
  const d = new Date();
  const date = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
  navigate("/record/saved", {
    replace: true, 
    state: { next: `/record/detail/${date}` },
  });
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