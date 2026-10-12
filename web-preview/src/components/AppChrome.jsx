import { ChevronLeft, House, ReceiptText, Search } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'

// ════════════════════════════════════════════════════════════════
//  앱 공통 틀 — 모든 화면은 머리·탭·아이콘 버튼을 이 세 개로만 써요
//   · IconButton : 아이콘 24 + 사방 10 = 44×44, 눌리면 40px 회색 원
//   · AppHeader  : 높이 56 + 상태바 안전영역, 스크롤하면 위에 붙고 아래 1px 선
//   · TabBar     : 높이 56 + 아래 안전영역, 3칸, 선택 탭 다시 누르면 맨 위로
//  아이콘은 lucide-react 하나만 · 굵기 1.75
//  크기: 헤더·탭바 24 / 리스트·인라인 20 / 보조 화살표 '›' 16
//  모양 규칙은 styles/ui.css
// ════════════════════════════════════════════════════════════════

export const STROKE = 1.75

// 아이콘만 있는 버튼 — label 은 꼭 (화면 읽기용 이름)
// edge: 'left' | 'right' — 화면 끝에 붙을 때 음수 여백으로 아이콘을 좌우 여백(20) 선에 맞춤
export function IconButton({ icon: Icon, label, onClick, edge, color = 'var(--grey-700)', size = 24 }) {
  return (
    <button
      type="button"
      className={`ui-iconbtn${edge ? ` edge-${edge}` : ''}`}
      aria-label={label}
      title={label}
      onClick={onClick}
      style={{ color }}
    >
      <Icon size={size} strokeWidth={STROKE} aria-hidden="true" />
    </button>
  )
}

// 스크롤하는 부모를 찾아서 '맨 위가 아닌지' 알려줘요 (헤더 아래 선)
function useScrolledParent(ref) {
  const [scrolled, setScrolled] = useState(false)
  useEffect(() => {
    let p = ref.current?.parentElement
    while (p && !/(auto|scroll)/.test(getComputedStyle(p).overflowY)) p = p.parentElement
    if (!p) return
    const on = () => setScrolled(p.scrollTop > 0)
    on()
    p.addEventListener('scroll', on, { passive: true })
    return () => p.removeEventListener('scroll', on)
  }, [ref])
  return scrolled
}

// 로고 이미지: src/assets/logo-wordmark.png 를 넣으면 자동으로 이미지, 없으면 글자 '비상수단'
// (파일이 없어도 빌드가 깨지지 않게 glob 으로 찾아요)
const LOGO_SRC = Object.values(import.meta.glob('../assets/logo-wordmark.png', { eager: true, query: '?url', import: 'default' }))[0]

// 로고: 높이 height(기본 24 · 전시 대기 화면 40) · 가로는 비율대로
//  onHold 가 있으면 3초 길게 누를 때 실행 (전시 운영자 메뉴 — 관람객 눈엔 안 보이는 입구)
export function Logo({ onHold, height = 24 }) {
  const timer = useRef(null)
  const start = () => { clearTimeout(timer.current); timer.current = setTimeout(onHold, 3000) }
  const stop = () => clearTimeout(timer.current)
  useEffect(() => stop, [])
  const hold = onHold ? { onPointerDown: start, onPointerUp: stop, onPointerLeave: stop, onPointerCancel: stop } : {}
  return (
    <div className="ui-logo" {...hold}>
      {LOGO_SRC
        ? <img src={LOGO_SRC} alt="비상수단" style={{ height, width: 'auto', display: 'block' }} draggable={false} />
        : <span className="t-brand" aria-label="비상수단" style={{ fontSize: Math.round(height * 0.92) }}>비상수단</span>}
    </div>
  )
}

// 헤더 (높이 64 + 상태바 안전영역, 좌우 여백 20 = 키트 카드 여백 → 로고 왼쪽 끝 = 카드 왼쪽 끝)
//  탭 첫 화면: <AppHeader logo /> 또는 <AppHeader title="수사대" />  + right(아이콘 최대 2개)
//  하위 화면 : <AppHeader onBack={...} title="설정" />  (가운데 제목) + right(아이콘 1개)
//  onLogoHold: 로고 3초 길게 누르기 (전시 시작 화면 → 운영자 메뉴)
//  bg: 화면 배경과 같게 — 'grey'(.bs-screen) | 'white'(.bs-screen.white)
export function AppHeader({ logo = false, title, onBack, right, bg = 'grey', onLogoHold }) {
  const ref = useRef(null)
  const scrolled = useScrolledParent(ref)
  const sub = !!onBack
  return (
    <header ref={ref} className={`ui-header${scrolled ? ' scrolled' : ''}`} style={{ background: bg === 'white' ? 'var(--white)' : 'var(--grey-100)' }}>
      <div className="ui-header-row">
        {sub ? (
          <IconButton icon={ChevronLeft} label="뒤로 가기" onClick={onBack} edge="left" />
        ) : logo ? (
          <Logo onHold={onLogoHold} />
        ) : (
          <h1 className="t-title2" style={{ margin: 0 }}>{title}</h1>
        )}
        {sub && title && <span className="ui-header-title">{title}</span>}
        <div className="ui-header-right">{right}</div>
      </div>
    </header>
  )
}

// 하단 탭바 (홈 · 수사대 · 기록) — 탭 첫 화면에서만 그려요
export const TABS = [
  ['home', '홈', House],
  ['squad', '수사대', Search],
  ['record', '기록', ReceiptText],
]
export function TabBar({ value, onChange, onReselect }) {
  return (
    <nav className="ui-tabbar" role="tablist">
      {TABS.map(([id, label, Icon]) => (
        <button
          key={id}
          type="button"
          role="tab"
          aria-selected={value === id}
          className={`ui-tab${value === id ? ' on' : ''}`}
          onClick={() => (value === id ? onReselect?.(id) : onChange(id))}
        >
          <Icon size={24} strokeWidth={STROKE} aria-hidden="true" />
          <span>{label}</span>
          <i className="dot" />
        </button>
      ))}
    </nav>
  )
}
