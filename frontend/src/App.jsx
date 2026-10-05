import { Routes, Route } from 'react-router-dom'

import Guide from './pages/Guide/Guide'
import LizardInfo from './pages/Guide/LizardInfo'
import Loading from './pages/Guide/Loading'
import Step1 from './pages/Guide/Step1'

const App = () => {
  return (
    <Routes>
      <Route path="/guide" element={<Guide />} />
      <Route path="/lizard-info" element={<LizardInfo />} />
      <Route path="/loading" element={<Loading />} />
      <Route path="/guide/step1" element={<Step1 />} />
    </Routes>
  )
}

export default App