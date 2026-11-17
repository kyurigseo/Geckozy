import { useEffect, useRef, useState } from 'react'

import {
  changeScrap,
  getScrapPage,
  mediaUrl,
} from '../../../../api/myManage'

import '../../../home/GuidePopup.scss'

import closeImg from '../../../../assets/home/close.png'
import sproutIcon from '../../../../assets/home/icon-leaf.png'
import frameImg from '../../../../assets/home/frame.png'
import prevImg from '../../../../assets/home/prev.png'
import nextImg from '../../../../assets/home/next.png'

import { useSound } from '../../../../sound/SoundProvider'

export default function ScrapGuidePopup({
  guidebook,
  onClose,
  onScrapChange,
}) {
  const [pageNumber, setPageNumber] = useState(1)

  const [resource, setResource] = useState({
    data: null,
    error: '',
  })

  const [scrapped, setScrapped] = useState(true)
  const [saving, setSaving] = useState(false)

  const lock = useRef(false)
  const panelRef = useRef(null)
  const { playSfx } = useSound()

  const data = resource.data

  useEffect(() => {
    const controller = new AbortController()

    getScrapPage(
      guidebook.guidebookId,
      pageNumber,
      controller.signal,
    )
      .then((result) => {
        if (
          !result?.content ||
          !Number.isInteger(result.totalPages) ||
          result.totalPages < 1
        ) {
          throw new Error(
            '도감 내용을 불러올 수 없습니다.',
          )
        }

        if (!controller.signal.aborted) {
          setResource({
            data: result,
            error: '',
          })
        }
      })
      .catch((error) => {
        if (!controller.signal.aborted) {
          setResource({
            data: null,
            error: error.message,
          })
        }
      })

    return () => controller.abort()
  }, [guidebook.guidebookId, pageNumber])

  useEffect(() => {
    const overflow = document.body.style.overflow
    const opener = document.activeElement

    document.body.style.overflow = 'hidden'
    panelRef.current?.focus()

    return () => {
      document.body.style.overflow = overflow
      opener?.focus?.()
    }
  }, [])

  const move = (next) => {
    playSfx('button')

    setResource({
      data: null,
      error: '',
    })

    setPageNumber(next)
  }

  const toggleScrap = async () => {
    if (lock.current) return

    lock.current = true
    setSaving(true)
    playSfx('button')

    try {
      const result = await changeScrap(
        guidebook.guidebookId,
        !scrapped,
      )

      if (typeof result?.scrapped !== 'boolean') {
        throw new Error(
          '스크랩 상태를 확인할 수 없습니다.',
        )
      }

      setScrapped(result.scrapped)
      onScrapChange(result)
    } catch (error) {
      window.alert(error.message)
    } finally {
      lock.current = false
      setSaving(false)
    }
  }

  return (
    <div
      className="guide-popup"
      onClick={onClose}
    >
      <div
        ref={panelRef}
        className="guide-popup__panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="my-guide-popup-title"
        tabIndex={-1}
        style={{
          backgroundImage: `url(${frameImg})`,
        }}
        onClick={(event) =>
          event.stopPropagation()
        }
        onKeyDown={(event) => {
          if (event.key === 'Escape') {
            event.stopPropagation()
            onClose()
          }

          if (
            event.key === 'ArrowLeft' &&
            data?.hasPrevious
          ) {
            move(pageNumber - 1)
          }

          if (
            event.key === 'ArrowRight' &&
            data?.hasNext
          ) {
            move(pageNumber + 1)
          }
        }}
      >
        <button
          type="button"
          className="guide-popup__close"
          onClick={() => {
            playSfx('button')
            onClose()
          }}
          aria-label="닫기"
        >
          <img src={closeImg} alt="" />
        </button>

        <h2
          id="my-guide-popup-title"
          className="guide-popup__title"
        >
          {data?.title || guidebook.title}
        </h2>

        <div
          className="guide-popup__page"
          key={pageNumber}
        >
          {resource.error ? (
            <p
              className="guide-popup__text"
              role="alert"
            >
              {resource.error}
            </p>
          ) : !data ? (
            <p className="guide-popup__text">
              불러오는 중이에요.
            </p>
          ) : (
            <>
              {mediaUrl(data.content.imageUrl) && (
                <img
                  src={mediaUrl(data.content.imageUrl)}
                  alt=""
                  className="guide-popup__hero"
                  onError={(event) => {
                    event.currentTarget.hidden = true
                  }}
                />
              )}

              <h3 className="guide-popup__sub">
                <img src={sproutIcon} alt="" />{' '}
                {data.content.title}
              </h3>

              <p
                className="guide-popup__text"
                style={{ whiteSpace: 'pre-line' }}
              >
                {data.content.description}
              </p>

              {!data.hasNext && (
                <button
                  type="button"
                  className={`guide-popup__scrap ${
                    scrapped ? 'is-on' : ''
                  }`}
                  aria-pressed={scrapped}
                  disabled={saving}
                  onClick={toggleScrap}
                >
                  {scrapped ? '스크랩됨' : '스크랩'}
                </button>
              )}
            </>
          )}
        </div>

        <footer className="guide-popup__pager">
          <button
            type="button"
            className="guide-popup__arrow"
            disabled={!data?.hasPrevious}
            onClick={() => move(pageNumber - 1)}
            aria-label="이전 페이지"
          >
            <img src={prevImg} alt="" />
          </button>

          <span
            className="guide-popup__count"
            aria-live="polite"
          >
            {pageNumber}/{data?.totalPages || '—'}
          </span>

          <button
            type="button"
            className="guide-popup__arrow"
            disabled={!data?.hasNext}
            onClick={() => move(pageNumber + 1)}
            aria-label="다음 페이지"
          >
            <img src={nextImg} alt="" />
          </button>
        </footer>
      </div>
    </div>
  )
}