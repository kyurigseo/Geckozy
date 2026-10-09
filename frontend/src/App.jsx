import { Routes, Route } from "react-router-dom";

import Layout from "./components/layout/Layout";
import Onboarding from "./pages/onboarding/Onboarding";
import MyPage from "./pages/my/MyPage";
import MyGeckoCustom from "./pages/my/components/screens/GeckoCustom";
import MyTankCustom from "./pages/my/components/screens/TankCustom";

import Guide from "./pages/Guide/Guide";
import LizardInfo from "./pages/Guide/LizardInfo";
import Loading from "./pages/Guide/Loading";
import Step1 from "./pages/Guide/Step1";
import EnclosureCheck from "./pages/Guide/EnclosureCheck";
import Step2 from "./pages/Guide/Step2";
import Step3 from "./pages/Guide/Step3";
import Step4 from "./pages/Guide/Step4";
import Step5 from "./pages/Guide/Step5";
import LoadingFin from "./pages/Guide/Loading_fin";

function App() {
  return (
    <div className="app">
      <Routes>
        <Route path="/onboarding" element={<Onboarding />} />
        <Route path="/my/gecko-custom" element={<MyGeckoCustom />} />
        <Route path="/my/tank-custom" element={<MyTankCustom />} />

        <Route path="/lizard-info" element={<LizardInfo />} />
        <Route path="/loading" element={<Loading />} />
        <Route path="/guide/enclosure-check" element={<EnclosureCheck />} />
        <Route path="/loading-fin" element={<LoadingFin />} />

        <Route element={<Layout />}>
          <Route path="/my" element={<MyPage />} />
          <Route path="/guide" element={<Guide />} />
          <Route path="/guide/step1" element={<Step1 />} />
          <Route path="/guide/step2" element={<Step2 />} />
          <Route path="/guide/step3" element={<Step3 />} />
          <Route path="/guide/step4" element={<Step4 />} />
          <Route path="/guide/step5" element={<Step5 />} />
          <Route path="*" element={null} />
        </Route>
      </Routes>
    </div>
  );
}

export default App;