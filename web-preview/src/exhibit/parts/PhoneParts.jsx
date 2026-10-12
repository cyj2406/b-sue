import { useEffect, useRef, useState } from 'react'
import { playDing } from '../sound'
import { BatteryFull, Signal, Wifi } from 'lucide-react'
import { STROKE } from '../../components/AppChrome'
import Blob from '../../components/Blob'
import { POSES } from '../../data/blobPose'

// ════════════════════════════════════════════════════════════════
//  가짜 폰 부품 — 잠금화면(④) · 상단 상태바 · 알림 배너 · 시간 건너뛰기 · 블롭 층
//  UI 키트 기준: 가짜 앱 아이콘은 모두 같은 모양 (짙은 회색 + 흰 첫 글자)
//  브랜드 차이는 이름과 아이콘 글자뿐 (실제 서비스 로고·색 흉내 금지)
// ════════════════════════════════════════════════════════════════

export function AppIcon({ app, size = 38 }) {
  // 비상수단 알림: 아이콘 자리에 작은 블롭 (기획서 6번)
  if (app === '비상수단') {
    return (
      <span className="ex-appicon" style={{ width: size, height: size, background: 'var(--white)', display: 'flex', alignItems: 'flex-end', justifyContent: 'center', overflow: 'hidden' }}>
        <img src={POSES.curious} alt="" style={{ width: '86%' }} />
      </span>
    )
  }
  return (
    <span className="ex-appicon" style={{ width: size, height: size, lineHeight: `${size}px` }}>
      {app[0]}
    </span>
  )
}

// 상단 상태바: 시간 · 신호 · 와이파이 · 배터리
export function StatusBar({ time, dark = true }) {
  return (
    <div className={`ex-status ${dark ? 'on-dark' : 'on-light'}`}>
      <span>{time}</span>
      <span className="ex-status-icons">
        <Signal size={16} strokeWidth={STROKE} aria-hidden="true" />
        <Wifi size={16} strokeWidth={STROKE} aria-hidden="true" />
        <BatteryFull size={16} strokeWidth={STROKE} aria-hidden="true" />
      </span>
    </div>
  )
}

// 잠금화면 (④): 날짜 + 큰 시계 + 알림 + 아래 안내
export function LockScreen({ clock, children, hint }) {
  return (
    <div className="ex-lock">
      <StatusBar time="" />
      <div style={{ textAlign: 'center', marginTop: 48 }}>
        <div className="t-body2" style={{ opacity: 0.75 }}>{clock.date}</div>
        <div className="ex-lock-time">{clock.time}</div>
      </div>
      <div className="ex-lock-notis">{children}</div>
      <div className="ex-lock-foot">
        {hint && <div className="t-body2 ex-lock-hint">{hint}</div>}
        <div className="ex-lock-bar" />
      </div>
    </div>
  )
}

// 알림 배너: 위에서 살짝 튀며 내려오고, 진동 (갤럭시 크롬에서 동작)
export function NotificationBanner({
  app,
  title,
  body,
  onTap,
  floating = false,
  autoDismissMs,
  vibration = [80, 60, 80],
}) {
  const pointerStart = useRef(null)
  const closingRef = useRef(false)
  const closeTimer = useRef(null)
  const [closing, setClosing] = useState(false)

  const dismiss = () => {
    if (!autoDismissMs) {
      onTap?.()
      return
    }
    if (closingRef.current) return
    closingRef.current = true
    setClosing(true)
    closeTimer.current = setTimeout(() => onTap?.(), 220)
  }

  useEffect(() => {
    try {
      navigator.vibrate?.(vibration)
    } catch {
      // 진동이 안 돼도 조용히
    }
    playDing()
  }, [])

  useEffect(() => {
    if (!autoDismissMs || !onTap) return
    const timer = setTimeout(dismiss, autoDismissMs)
    return () => {
      clearTimeout(timer)
      clearTimeout(closeTimer.current)
    }
  }, [autoDismissMs, onTap])

  const onPointerDown = (event) => {
    pointerStart.current = { id: event.pointerId, y: event.clientY }
    event.currentTarget.setPointerCapture(event.pointerId)
  }

  const onPointerUp = (event) => {
    const start = pointerStart.current
    pointerStart.current = null
    if (start?.id === event.pointerId && event.clientY - start.y < -24) dismiss()
  }

  return (
    <button
      className={`ex-noti${floating ? ' floating' : ''}${closing ? ' dismissing' : ''}`}
      onClick={dismiss}
      onPointerDown={onPointerDown}
      onPointerUp={onPointerUp}
      onPointerCancel={() => { pointerStart.current = null }}
    >
      <AppIcon app={app} />
      <span className="ex-noti-text">
        <span className="ex-noti-head"><span className="t-caption semi" style={{ color: 'var(--grey-700)' }}>{app}</span><span className="t-caption">지금</span></span>
        <span className="t-body2 bold" style={{ marginTop: 2 }}>{title}</span>
        {body && <span className="t-caption">{body}</span>}
      </span>
    </button>
  )
}

// 시간 건너뛰기: 화면이 어두워지고 시계가 01:12 → 07:30 으로 돌아간 뒤 밝아짐
export function TimeSkip({ from, to, onDone }) {
  const toMin = (t) => {
    const [h, m] = t.split(':').map(Number)
    return h * 60 + m
  }
  const start = toMin(from)
  let end = toMin(to)
  if (end < start) end += 24 * 60 // 다음 날로 넘어가는 경우
  const [now, setNow] = useState(start)
  const [phase, setPhase] = useState('in') // in → roll → out

  useEffect(() => {
    const timers = []
    timers.push(setTimeout(() => setPhase('roll'), 400))
    const steps = 30
    for (let i = 1; i <= steps; i++) {
      timers.push(setTimeout(() => setNow(start + Math.round(((end - start) * i) / steps)), 400 + i * 40))
    }
    timers.push(setTimeout(() => setPhase('out'), 400 + steps * 40 + 500))
    timers.push(setTimeout(onDone, 400 + steps * 40 + 900))
    return () => timers.forEach(clearTimeout)
  }, [])

  const m = now % (24 * 60)
  const label = `${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`
  return (
    <div className={`ex-timeskip ${phase}`}>
      <div className="ex-timeskip-clock">{label}</div>
      <div className="ex-timeskip-sub">{end - start >= 6 * 60 ? '다음 날 아침' : '조금 뒤'}</div>
    </div>
  )
}

// ── 블롭 층: 가짜 앱 '위'에 떠서 밖에서 끼어든 느낌 (reference ⑤) ──
// 빼꼼: 오른쪽 끝에서 반쯤 나오고 말풍선, 4초 뒤 사라짐, 아무것도 막지 않음
// peek: { text, sub? } 또는 글자 하나
export function PeekLayer({ peek }) {
  if (!peek) return null
  const { text, sub } = typeof peek.text === 'string' ? peek : { text: peek.text?.text, sub: peek.text?.sub }
  return (
    <div key={peek.id} className="ex-peek" aria-live="polite">
      <div className="bs-bubble right kit-pop" style={{ animationDelay: '.25s' }}>
        {text}
        {sub && <div className="t-caption" style={{ fontWeight: 400 }}>{sub}</div>}
      </div>
      <div style={{ marginRight: -30 }}>
        <Blob scene="peek" width={112} mirrored />
      </div>
    </div>
  )
}
