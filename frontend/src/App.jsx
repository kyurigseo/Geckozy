import { Routes, Route } from 'react-router-dom'

import Guide from './pages/Guide/Guide'
import LizardInfo from './pages/Guide/LizardInfo'
import Loading from './pages/Guide/Loading'
import Step1 from './pages/Guide/Step1'
import EnclosureCheck from './pages/Guide/EnclosureCheck'
import Step2 from './pages/Guide/Step2'

const App = () => {
  return (
    <Routes>
      <Route path="/guide" element={<Guide />} />
      <Route path="/lizard-info" element={<LizardInfo />} />
      <Route path="/loading" element={<Loading />} />
      <Route path="/guide/step1" element={<Step1 />} />
      <Route path="/guide/enclosure-check" element={<EnclosureCheck />} />
      <Route path="/guide/step2" element={<Step2 />} />
    </Routes>
  )
}

export default App