import { ChevronRight } from 'lucide-react'
import { useCallback, useEffect, useRef, useState } from 'react'
import { STROKE } from './AppChrome'

// ════════════════════════════════════════════════════════════════
//  공통 컴포넌트 — UI 키트(styles/bsue-tokens.css)의 .bs-* 클래스만 써요
//  마크업은 bsue_ui_kit/reference.html 과 같은 구조
//  (움직임만 styles/motion.css — 색·그림자·테두리는 추가하지 않아요)
//  머리·탭·아이콘 버튼은 components/AppChrome.jsx (AppHeader · TabBar · IconButton)
// ════════════════════════════════════════════════════════════════

// ── 버튼 ── .bs-btn primary / secondary(on-grey · on-dark) / weak / text
export function PrimaryButton({ children, onClick, disabled, style }) {
  return <button className="bs-btn primary" onClick={onClick} disabled={disabled} style={style}>{children}</button>
}
// on: 'white'(흰 화면·시트 위, 기본) | 'grey'(회색 화면 위) | 'dark'(출동 화면 위)
export function SecondaryButton({ children, onClick, on = 'white', style }) {
  const tone = on === 'grey' ? ' on-grey' : on === 'dark' ? ' on-dark' : ''
  return <button className={`bs-btn secondary${tone}`} onClick={onClick} style={style}>{children}</button>
}
export function WeakButton({ children, onClick, style }) {
  return <button className="bs-btn weak" onClick={onClick} style={style}>{children}</button>
}
export function TextButton({ children, onClick, style }) {
  return <button className="bs-btn text" onClick={onClick} style={style}>{children}</button>
}

// 버튼 두 개: 보조 왼쪽(1) : 주 버튼 오른쪽(2) — .bs-btn-row
export function ButtonRow({ secondary, onSecondary, primary, onPrimary, secondaryOn = 'white' }) {
  return (
    <div className="bs-btn-row">
      <SecondaryButton on={secondaryOn} onClick={onSecondary}>{secondary}</SecondaryButton>
      <PrimaryButton onClick={onPrimary}>{primary}</PrimaryButton>
    </div>
  )
}

// 하단 고정 버튼 — .bs-cta (회색 화면이면 onGrey)
export function BottomCta({ primary, onPrimary, secondary, onSecondary, onGrey = false }) {
  return (
    <div className={`bs-cta${onGrey ? ' on-grey' : ''}`}>
      {secondary ? (
        <ButtonRow secondary={secondary} onSecondary={onSecondary} primary={primary} onPrimary={onPrimary} />
      ) : (
        <PrimaryButton onClick={onPrimary} style={{ width: '100%' }}>{primary}</PrimaryButton>
      )}
    </div>
  )
}

// ── 카드 ── .bs-card (+ .bs-card-head 제목 · 더보기)
export function Card({ title, more, onMore, tint = false, children, style }) {
  return (
    <div className={`bs-card${tint ? ' tint' : ''}`} style={style}>
      {title && (
        <div className="bs-card-head">
          <span className="t-title3">{title}</span>
          {more && <button type="button" className="more ui-more" onClick={onMore}>{more}<Chevron /></button>}
        </div>
      )}
      {children}
    </div>
  )
}

// ── 리스트 행 ── .bs-row > .bs-avatar · .main · .end
export function Avatar({ src, large = false }) {
  return <div className={`bs-avatar${large ? ' lg' : ''}`}><img src={src} alt="" /></div>
}
export function ListRow({ avatar, avatarLarge, title, titleBold, subtitle, end, endClass = 't-body1 bold', onClick }) {
  return (
    <div className={`bs-row${onClick ? ' ui-row-btn' : ''}`} onClick={onClick} role={onClick ? 'button' : undefined}>
      {avatar && <Avatar src={avatar} large={avatarLarge} />}
      <div className="main">
        <div className={`t-body1${titleBold ? ' bold' : ''}`}>{title}</div>
        {subtitle && <div className="t-caption">{subtitle}</div>}
      </div>
      {end != null && <div className={`end ${endClass}`}>{end}</div>}
    </div>
  )
}

// ── 칩 · 배지 · 진행률 · 구분선 ──
export function Chip({ children, on, onClick }) {
  return <span className={`bs-chip${on ? ' on' : ''}`} onClick={onClick} role="button">{children}</span>
}
// tone: 기본(회색) | 'orange' | 'solid'
export function Badge({ children, tone, style }) {
  return <span className={`bs-badge${tone ? ` ${tone}` : ''}`} style={style}>{children}</span>
}
export function Progress({ value, style }) {
  return <div className="bs-progress" style={style}><i style={{ width: `${Math.round(Math.min(1, Math.max(0, value)) * 100)}%` }} /></div>
}
export function Divider({ style }) {
  return <div className="bs-divider" style={style} />
}

// ── 바텀시트 ── .bs-dim + .bs-sheet (.handle)
export function BottomSheet({ title, onDismiss, children }) {
  return (
    <>
      <div className="bs-dim kit-fade" onClick={onDismiss} />
      <div className="bs-sheet kit-sheet-up">
        <div className="handle" />
        {title && <div className="t-title3" style={{ marginBottom: 16 }}>{title}</div>}
        {children}
      </div>
    </>
  )
}

// ── 말풍선 ── .bs-bubble (down: 아래 꼬리 / right: 오른쪽 꼬리)
export function Bubble({ children, tail = 'down', style }) {
  return <div className={`bs-bubble ${tail}`} style={style}>{children}</div>
}

// ── 토스트 ── .bs-toast (2초)
export function useToast() {
  const [toast, setToast] = useState(null)
  const timer = useRef(null)
  const show = useCallback((message) => {
    clearTimeout(timer.current)
    setToast({ message, id: Date.now() })
    timer.current = setTimeout(() => setToast(null), 2000)
  }, [])
  useEffect(() => () => clearTimeout(timer.current), [])
  return { toast, show }
}
export function Toast({ toast, bottom }) {
  if (!toast) return null
  return <div key={toast.id} className="bs-toast kit-toast" style={bottom != null ? { bottom } : undefined}>{toast.message}</div>
}

// ── 보조 화살표 '›' (16) · 인라인 아이콘 (20) ──
export function Chevron() {
  return <ChevronRight size={16} strokeWidth={STROKE} className="ui-chevron" aria-hidden="true" />
}
export function InlineIcon({ icon: Icon }) {
  return <Icon size={20} strokeWidth={STROKE} className="ui-inline-icon" aria-hidden="true" />
}
