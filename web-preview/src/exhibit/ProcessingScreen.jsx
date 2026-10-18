import { useEffect, useRef, useState } from 'react'
import { MISSION_ART } from '../data/blobPose'
import { processingText } from './result'
import { PROCESS_DONE, PROCESS_MAX, PROCESS_MIN } from './timing'

// ════════════════════════════════════════════════════════════════
//  처리 중 표시 2 · 3단계 — 기록 중 화면 → 체크 완료 (DESIGN.md '처리 중 표시')
//  · 화면 자체가 #191F28 (어두운 막을 덮는 방식이 아님)
//  · 가운데 만난 요원(높이 120)이 콩콩(위아래 8 · 0.6초) + 20 아래 흰 17 SemiBold 한 줄 + 점 3개
//  · 이 동안 prepare() — 영수증 내용 · 랜덤 포즈 · QR 주소 · 그림/글꼴 미리 불러오기
//    최소 PROCESS_MIN 초는 보여주고, PROCESS_MAX 초가 넘으면 덜 끝났어도 다음으로
//  · 완료: 블롭 자리에 주황 원(0.3초) + 흰 체크(0.2초), 글은 "기록 완료" → PROCESS_DONE 초 뒤 onDone
// ════════════════════════════════════════════════════════════════
export default function ProcessingScreen({ guardian, result, prepare, onDone }) {
  const [done, setDone] = useState(false)
  const finished = useRef(false)

  useEffect(() => {
    const t0 = Date.now()
    let alive = true
    const finish = () => {
      if (!alive || finished.current) return
      finished.current = true
      setDone(true)
      setTimeout(() => alive && onDone(), PROCESS_DONE * 1000)
    }
    const max = setTimeout(finish, PROCESS_MAX * 1000) // 준비가 오래 걸려도 3초면 다음으로
    Promise.resolve()
      .then(prepare)
      .catch(() => {}) // 준비가 실패해도 관람객 화면은 그대로 진행
      .then(() => {
        const wait = Math.max(0, PROCESS_MIN * 1000 - (Date.now() - t0))
        setTimeout(finish, wait)
      })
    return () => { alive = false; clearTimeout(max) }
  }, [])

  return (
    <div className="ex-process" role="status" aria-live="polite">
      <div className="ex-process-art">
        {done ? (
          <svg className="ex-process-check" viewBox="0 0 96 96" width="96" height="96" aria-hidden="true">
            <circle cx="48" cy="48" r="44" />
            <path d="M28 49 L42 63 L68 35" />
          </svg>
        ) : (
          <img className="ex-process-blob" src={MISSION_ART[guardian]} alt="" draggable={false} />
        )}
      </div>
      <div className="ex-process-text">
        {done ? '기록 완료' : <>{processingText(result)}<span className="ex-process-dots" aria-hidden="true"><i /><i /><i /></span></>}
      </div>
    </div>
  )
}
