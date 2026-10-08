import { Routes, Route } from "react-router-dom";

import Layout from "./components/layout/Layout";
import Onboarding from "./pages/onboarding/Onboarding";

function App() {
  return (
    <div className="app">
      <Routes>
        {/* Nav 없는 페이지 */}
        <Route path="/onboarding" element={<Onboarding />} />

        {/* Nav 있는 페이지: 페이지가 생기면 여기에 추가 (예: /home, /record, /guide, /my) */}
        <Route element={<Layout />}>
          {/* TODO: 임시. 페이지가 아직 없어서 온보딩 외 모든 주소에서 Nav만 보이도록 함. 페이지 추가 후 삭제 */}
          <Route path="*" element={null} />
        </Route>
      </Routes>
    </div>
  );
}

export default App;
