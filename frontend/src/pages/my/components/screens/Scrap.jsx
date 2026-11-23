import { useNavigate } from 'react-router-dom'

import back from '../../../../assets/my/뒤로가기.svg'
import scrapBackground from '../../../../assets/my/스크랩 배경.svg'
import ventilationImage from '../../../../assets/my/스크랩_환기관리.svg'
import checkImage from '../../../../assets/my/스크랩_확인할곳.svg'
import sprayImage from '../../../../assets/my/스크랩_분무.svg'
import waterImage from '../../../../assets/my/스크랩_물관리.svg'
import hideoutImage from '../../../../assets/my/스크랩_은신처.svg'

import './Scrap.scss'

const SCRAPS = [
  { id: 1, title: '사육장 환기 관리', date: '2026.09.01', image: ventilationImage },
  { id: 2, title: '환기할 때 확인할 것', date: '2026.08.14', image: checkImage },
  { id: 3, title: '올바른 분무 주기', date: '2026.08.01', image: sprayImage },
  { id: 4, title: '깨끗한 물그릇 관리', date: '2026.07.29', image: waterImage },
  { id: 5, title: '은신처의 중요성', date: '2026.07.06', image: hideoutImage }
]

const Scrap = () => {
  const navigate = useNavigate()

  return (
    <div className="scrap">
      <header className="scrap-header">
        <button className="scrap-back" onClick={() => navigate('/my')}>
          <img src={back} alt="뒤로가기" />
        </button>
        <h1>스크랩</h1>
      </header>

      <main className="scrap-content">
        <div className="scrap-grid">
          {SCRAPS.map((item) => (
            <article className="scrap-card" key={item.id}>
              <img className="scrap-card-background" src={scrapBackground} alt="" />
              <img className="scrap-card-image" src={item.image} alt="" />
              <strong className="scrap-card-title">{item.title}</strong>
              <span className="scrap-card-date">{item.date}</span>
            </article>
          ))}
        </div>
      </main>
    </div>
  )
}

export default Scrap
