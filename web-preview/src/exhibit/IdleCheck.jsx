import { useEffect, useState } from 'react'
import Blob from '../components/Blob'
import { PrimaryButton, Progress } from '../components/Kit'
import { IDLE_SECOND } from './timing'

// ════════════════════════════════════════════════════════════════
//  무조작 확인 (기획서 9번) — 30초 동안 아무도 안 만지면 블롭이 "아직 있어?"
//  15초 안에 [계속할래] 또는 화면 아무 곳이나 누르면 이어서, 아니면 대기 화면으로
//  시간 값은 timing.js
// ════════════════════════════════════════════════════════════════

export default function IdleCheck({ onStay, onLeave }) {
  const [left, setLeft] = useState(IDLE_SECOND)
  useEffect(() => {
    const t = setInterval(() => setLeft((s) => s - 1), 1000)
    return () => clearInterval(t)
  }, [])
  useEffect(() => { if (left <= 0) onLeave() }, [left, onLeave])

  return (
    <div style={{ position: 'absolute', inset: 0, zIndex: 60 }} onPointerDown={onStay}>
      <div className="bs-dim kit-fade" />
      <div className="bs-sheet kit-sheet-up" style={{ textAlign: 'center' }}>
        <div className="handle" />
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: -4 }}>
          <Blob scene="corner" width={110} />
        </div>
        <div className="t-title2" style={{ marginTop: 10 }}>아직 있어?</div>
        <div className="t-body2" style={{ marginTop: 6, color: 'var(--grey-600)' }}>
          {Math.max(0, left)}초 뒤에 처음으로 돌아갈게
        </div>
        <Progress value={left / IDLE_SECOND} style={{ margin: '18px 0 20px' }} />
        <PrimaryButton style={{ width: '100%' }} onClick={onStay}>계속할래</PrimaryButton>
      </div>
    </div>
  )
}
