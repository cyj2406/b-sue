import { useEffect, useRef, useState } from 'react'
import Blob from '../components/Blob'
import { ButtonRow, PrimaryButton } from '../components/Kit'
import { MISSION_ART } from '../data/blobPose'
import { TIMING } from './rules'

// ── S·M: 오른쪽 아래 구석 블롭 + 말풍선 ──────────────────────────
// 시선 따라가기 · 끌면 늘어남(모찌) · 톡 · 길게 누르기 · 졸기
export function CornerBlob({ companion, bottom, gaze }) {
  const { scene, bubble, emit, closeBubble } = companion
  const [drag, setDrag] = useState(null) // { dx, dy }
  const press = useRef(null)

  const onDown = (e) => {
    e.stopPropagation()
    e.currentTarget.setPointerCapture(e.pointerId)
    press.current = {
      x: e.clientX, y: e.clientY, moved: false, long: false,
      timer: setTimeout(() => { press.current.long = true; emit('longPressBlob') }, TIMING.longPressMs),
    }
  }
  const onMove = (e) => {
    const p = press.current
    if (!p) return
    const dx = e.clientX - p.x
    const dy = e.clientY - p.y
    if (!p.moved && Math.hypot(dx, dy) > 8) { p.moved = true; clearTimeout(p.timer) }
    if (p.moved) setDrag({ dx, dy })
  }
  const onUp = () => {
    const p = press.current
    press.current = null
    if (!p) return
    clearTimeout(p.timer)
    if (!p.moved && !p.long) emit('tapBlob')
    setDrag(null) // 놓으면 구석으로 말랑하게 복귀 (CSS transition)
  }

  // 모찌처럼: 끌수록 그 방향으로 늘어남
  const dist = drag ? Math.min(Math.hypot(drag.dx, drag.dy), 160) : 0
  const stretch = 1 + dist / 400
  const lean = gaze ? Math.max(-1, Math.min(1, gaze)) * 7 : 0

  return (
    <div className="corner" style={{ bottom }}>
      {bubble && (
        <div key={bubble.id} className="bs-bubble right kit-pop" onClick={closeBubble} role="button">
          {bubble.lines[0]}
          {bubble.lines[1] && <div className="t-caption" style={{ fontWeight: 400 }}>{bubble.lines[1]}</div>}
        </div>
      )}
      <div
        role="button"
        aria-label="블롭 비서"
        className={`corner-circle ${drag ? 'dragging' : ''} ${scene === 'cornerDoze' ? 'dozing' : ''}`}
        style={{
          transform: drag
            ? `translate(${drag.dx}px, ${drag.dy}px) scale(${1 / Math.sqrt(stretch)}, ${stretch})`
            : `rotate(${lean}deg)`,
        }}
        onPointerDown={onDown}
        onPointerMove={onMove}
        onPointerUp={onUp}
        onPointerCancel={onUp}
      >
        <Blob scene={scene} width={64} />
        {scene === 'cornerDoze' && <span className="zzz">z<b>z</b><i>z</i></span>}
      </div>
    </div>
  )
}

// ── L: 가운데 개입 — 구석에서 커지며 날아와서, 시트 위로 튀어나옴 ──────────
export function CenterIntervention({ content, onPrimary, onSecondary }) {
  const [leaving, setLeaving] = useState(false)
  const close = (fn) => { setLeaving(true); setTimeout(fn, 240) }
  useEffect(() => setLeaving(false), [content])

  return (
    <div className={`center-wrap ${leaving ? 'leaving' : ''}`}>
      <div className="bs-dim kit-fade" />
      <div className="bs-sheet kit-sheet-up" style={{ paddingTop: 40 }}>
        {/* 블롭은 시트 위로 튀어나와요 (아래 28px 걸침) */}
        <div className="center-blob" style={{ top: content.art ? -152 : -192 }}>
          {content.art
            ? <img src={MISSION_ART[content.art]} alt="" style={{ width: 180, height: 180, objectFit: 'contain' }} />
            : <Blob scene={content.scene} width={220} />}
        </div>
        <div className="t-title2" style={{ whiteSpace: 'pre-line' }}>{content.title}</div>
        <div className="t-body2 c-sub" style={{ marginTop: 8 }}>{content.body}</div>
        {content.caption && <div className="t-caption" style={{ marginTop: 4 }}>{content.caption}</div>}
        <div style={{ marginTop: 24 }}>
          {content.secondary
            ? <ButtonRow secondary={content.secondary} onSecondary={() => close(onSecondary)} primary={content.primary} onPrimary={() => close(onPrimary)} />
            : <PrimaryButton style={{ width: '100%' }} onClick={() => close(onPrimary)}>{content.primary}</PrimaryButton>}
        </div>
      </div>
    </div>
  )
}
