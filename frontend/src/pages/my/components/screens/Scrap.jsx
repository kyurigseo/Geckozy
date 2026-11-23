import GuidePopup from '../../../home/GuidePopup'
import { VENTILATION_GUIDEBOOK_ID } from '../../../../api/myManage'
import {
  useCallback,
  useEffect,
  useState,
} from 'react'
import { useNavigate } from 'react-router-dom'

import {
  formatMyDate,
  getMyScraps,
  mediaUrl,
} from '../../../../api/myManage'

import ScrapGuidePopup from '../common/ScrapGuidePopup'

import back from '../../../../assets/my/뒤로가기.svg'
import scrapBackground from '../../../../assets/my/스크랩 배경.svg'
import ventilationImage from '../../../../assets/my/스크랩_환기관리.svg'
import checkImage from '../../../../assets/my/스크랩_확인할곳.svg'
import sprayImage from '../../../../assets/my/스크랩_분무.svg'
import waterImage from '../../../../assets/my/스크랩_물관리.svg'
import hideoutImage from '../../../../assets/my/스크랩_은신처.svg'
import leafImage from '../../../../assets/my/나뭇잎.svg'

import './Scrap.scss'

const LOCAL_IMAGES = {
  '사육장 환기 관리': ventilationImage,
  '환기할 때 확인할 것': checkImage,
  '올바른 분무 주기': sprayImage,
  '깨끗한 물그릇 관리': waterImage,
  '은신처의 중요성': hideoutImage,
}

const Scrap = () => {
  const navigate = useNavigate()

  const [scraps, setScraps] = useState([])
  const [openPopup, setOpenPopup] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const closePopup = useCallback(() => {
    setOpenPopup(null)
  }, [])

  useEffect(() => {
    const controller = new AbortController()

    getMyScraps(controller.signal)
      .then((items) => {
        if (!controller.signal.aborted) {
          setScraps(items)
        }
      })
      .catch((error) => {
        if (!controller.signal.aborted) {
          setError(error.message)
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      })

    return () => controller.abort()
  }, [])

  const handleScrapChange = (result) => {
    if (!result.scrapped) {
      setScraps((prev) =>
        prev.filter(
          (item) =>
            item.guidebookId !== result.guidebookId,
        ),
      )
    } else {
      setScraps((prev) => [
        ...prev.filter(
          (item) =>
            item.guidebookId !== result.guidebookId,
        ),
        {
          ...openPopup,
          scrappedAt: result.scrappedAt,
        },
      ])
    }
  }

  return (
    <div className="scrap">
      <header className="scrap-header">
        <button
          className="scrap-back"
          onClick={() => navigate('/my')}
        >
          <img src={back} alt="뒤로가기" />
        </button>

        <h1>스크랩</h1>
      </header>

      <main className="scrap-content">
        {error && <p role="alert">{error}</p>}

        {loading && <p>불러오는 중이에요.</p>}

        {!loading &&
          !error &&
          scraps.length === 0 && (
            <p>스크랩한 도감이 없어요.</p>
          )}

        <div className="scrap-grid">
          {scraps.map((item) => {
            const fallback =
              LOCAL_IMAGES[item.title] || leafImage

            return (
              <button
                type="button"
                key={item.guidebookId}
                className="scrap-card scrap-card--clickable"
                onClick={() => setOpenPopup(item)}
              >
                <img
                  className="scrap-card-background"
                  src={scrapBackground}
                  alt=""
                />

                <img
                  className="scrap-card-image"
                  src={
                    mediaUrl(item.thumbnailUrl) ||
                    fallback
                  }
                  alt=""
                  onError={(event) => {
                    const image = event.currentTarget

                    if (!image.dataset.fallback) {
                      image.dataset.fallback = 'true'
                      image.src = fallback
                    }
                  }}
                />

                <strong className="scrap-card-title">
                  {item.title}
                </strong>

                <span className="scrap-card-date">
                  {formatMyDate(item.scrappedAt)}
                </span>
              </button>
            )
          })}
        </div>
      </main>

      {openPopup && (
        openPopup.guidebookId === VENTILATION_GUIDEBOOK_ID ? (
          <GuidePopup
            initialScrapped
            onClose={closePopup}
            onScrapChange={handleScrapChange}
          />
        ) : (
          <ScrapGuidePopup
            key={openPopup.guidebookId}
            guidebook={openPopup}
            onClose={closePopup}
            onScrapChange={handleScrapChange}
          />
        )
      )}
    </div>
  )
}

export default Scrap