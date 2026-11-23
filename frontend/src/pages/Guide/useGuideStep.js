import { useEffect, useRef, useState } from 'react'
import {
  completeGuideStep,
  getGuideStep,
  saveGuideChecks,
  getGuideContext,
  saveGuideContext,
} from '../../api/guide'

export default function useGuideStep(
  step,
  count = 0,
  { localChecklist = false } = {},
) {
  const [data, setData] = useState(null)
  const [busy, setBusy] = useState(false)
  const lock = useRef(false)

  const localMode =
    getGuideContext().guideMode === 'static'
  const storeChecksLocally = localMode || localChecklist

  useEffect(() => {
    const controller = new AbortController()

    Promise.resolve()
      .then(() =>
        localMode ? null : getGuideStep(step, controller.signal),
      )
      .then((result) => {
        if (controller.signal.aborted) return

        let checkItems
        if (storeChecksLocally) {
          const saved =
            getGuideContext().localChecks?.[step] || []

          checkItems = Array.from({ length: count }, (_, index) => ({
            itemOrder: index + 1,
            checked: Boolean(saved[index]),
          }))
        } else {
          checkItems = [...result.checkItems].sort(
            (a, b) => a.itemOrder - b.itemOrder,
          )
        }

        if (checkItems.length !== count) {
          throw new Error(
            '서버 체크 항목 수와 현재 가이드 화면이 일치하지 않습니다.',
          )
        }

        setData({
          ...result,
          checkItems,
        })
      })
      .catch((error) => {
        if (!controller.signal.aborted) {
          window.alert(error.message)
        }
      })

    return () => controller.abort()
  }, [step, count, localMode, storeChecksLocally])

  const checked =
    data?.checkItems.map((item) => item.checked) ||
    Array(count).fill(false)

  const run = async (action) => {
    if (lock.current) return

    if (!data) {
      window.alert(
        '가이드 정보를 불러온 후 다시 시도해주세요.',
      )
      return
    }

    lock.current = true
    setBusy(true)

    try {
      await action()
    } catch (error) {
      window.alert(error.message)
    } finally {
      lock.current = false
      setBusy(false)
    }
  }

  const handleCheck = (index) =>
    run(async () => {
      const items = data.checkItems.map((item, i) =>
        i === index
          ? { ...item, checked: !item.checked }
          : item,
      )

      if (storeChecksLocally) {
        const context = getGuideContext()

        saveGuideContext({
          localChecks: {
            ...context.localChecks,
            [step]: items.map((item) => item.checked),
          },
        })
      } else {
        await saveGuideChecks(
          step,
          items.map(({ checkItemId, checked }) => ({
            checkItemId,
            checked,
          })),
        )
      }

      setData({
        ...data,
        checkItems: items,
      })
    })

  const finish = (after) =>
    run(async () => {
      if (!localMode) {
        await completeGuideStep(step)
      }

      await after()
    })

  return {
    data,
    checked,
    handleCheck,
    finish,
    run,
    busy,
    isAllChecked:
      !!data && checked.every(Boolean) && !busy,
  }
}