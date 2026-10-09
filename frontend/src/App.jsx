import { Routes, Route } from "react-router-dom";

import Layout from "./components/layout/Layout";
import Onboarding from "./pages/onboarding/Onboarding";
import MyPage from "./pages/my/MyPage";
import MyGeckoCustom from "./pages/my/components/screens/GeckoCustom";
import MyTankCustom from "./pages/my/components/screens/TankCustom";
import LizardManage from './pages/my/components/screens/LizardManage'
import Scrap from './pages/my/components/screens/Scrap'
import SensorManage from './pages/my/components/screens/SensorManage'


function App() {
  return (
    <div className="app">
      <Routes>
        {/* Nav 없는 페이지 */}
        <Route path="/onboarding" element={<Onboarding />} />
        {/* TODO: 마이 > 도마뱀·사육장 커스텀 디자인 받으면 하단바 여부 확정 (지금은 온보딩 커스텀처럼 하단바 없음) */}
        <Route path="/my/gecko-custom" element={<MyGeckoCustom />} />
        <Route path="/my/tank-custom" element={<MyTankCustom />} />
        <Route path="/my/lizard-manage" element={<LizardManage />} />
        <Route path="/my/scrap" element={<Scrap />} />
        <Route path="/my/sensor" element={<SensorManage />} />

        {/* Nav 있는 페이지: 페이지가 생기면 여기에 추가 (예: /home, /record, /guide, /my) */}
        <Route element={<Layout />}>
          <Route path="/my" element={<MyPage />} />
          {/* TODO: 임시. 페이지가 아직 없어서 온보딩 외 모든 주소에서 Nav만 보이도록 함. 페이지 추가 후 삭제 */}
          <Route path="*" element={null} />
        </Route>
      </Routes>
    </div>
  );
}

export default App;
