import { useEffect, useState } from 'react'
import { Check, ChevronLeft, ChevronRight, Heart, House, MapPin, Minus, Plus, ShoppingCart, X } from 'lucide-react'
import { IconButton, STROKE } from '../../components/AppChrome'
import { MISSION_ART } from '../../data/blobPose'
import '../../styles/fakeapps.css'

// ════════════════════════════════════════════════════════════════
//  가짜 앱 공통 부품 — 지름마켓 · 배고파 · 스트림플러스 · 비상페이 (2026-10-11)
//  실제 쇼핑 · 배달 · 결제 앱에서 익숙한 순서와 위치만 참고 (로고 · 색 · 문구는 우리 것)
//  회색 바탕(#F2F4F6) + 흰 블록 · Pretendard · 4의 배수 · 버튼 56 · 할인만 빨강(--ex-sale-red)
//  모양은 styles/fakeapps.css 의 .fx-*  ·  data-fx 는 힌트 표(hints.js)가 화면을 알아보는 이름표
// ════════════════════════════════════════════════════════════════
export const krw = (n) => n.toLocaleString('ko-KR') + '원'
export const art = (key) => MISSION_ART[key]

// 헤더: ‹ 제목 · 오른쪽 장바구니(숫자 배지)
export function FxHeader({ title, onBack, cartCount, onCart, white = true }) {
  return (
    <header className={`fx-header${white ? ' white' : ''}`}>
      <IconButton icon={ChevronLeft} label="뒤로 가기" onClick={onBack} edge="left" />
      <span className="fx-header-title ui-header-title">{title}</span>
      {onCart ? (
        <span className="fx-cart-btn" data-hint-target="cart-icon">
          <IconButton icon={ShoppingCart} label="장바구니" onClick={onCart} edge="right" />
          {cartCount > 0 && <span className="fx-badge" aria-hidden="true">{cartCount}</span>}
        </span>
      ) : <span style={{ width: 48 }} />}
    </header>
  )
}

// 흰 블록 (화면 폭 꽉, 블록 사이 12)
export function Block({ children, style, className = '' }) {
  return <section className={`fx-block ${className}`} style={style}>{children}</section>
}

// 금액 줄: [['상품금액', 129000], ['할인', -90300, 'sale'], ...]
export function PriceRows({ rows, total, totalLabel = '합계' }) {
  return (
    <div className="fx-price-rows">
      {rows.map(([label, v, tone]) => (
        <div key={label} className={`fx-price-row${tone === 'sale' ? ' sale' : ''}`}>
          <span>{label}</span><span>{v === 0 ? '0원' : (v < 0 ? '-' : '') + krw(Math.abs(v))}</span>
        </div>
      ))}
      {total != null && (
        <>
          <div className="fx-divider" />
          <div className="fx-price-total"><span>{totalLabel}</span><span>{krw(total)}</span></div>
        </>
      )}
    </div>
  )
}

// 수량 −/+
export function Stepper({ value, onChange, min = 1 }) {
  return (
    <span className="fx-stepper">
      <button type="button" aria-label="하나 빼기" onClick={() => onChange(Math.max(min, value - 1))} disabled={value <= min}><Minus size={16} strokeWidth={STROKE} /></button>
      <span className="fx-stepper-n">{value}</span>
      <button type="button" aria-label="하나 더하기" onClick={() => onChange(value + 1)}><Plus size={16} strokeWidth={STROKE} /></button>
    </span>
  )
}

// 동그란 체크
export function CheckCircle({ on, onClick, label }) {
  return (
    <button type="button" className={`fx-check${on ? ' on' : ''}`} onClick={onClick} aria-pressed={on} aria-label={label}>
      <Check size={14} strokeWidth={2.5} />
    </button>
  )
}

// 켜기/끄기
export function Toggle({ on, onChange, label }) {
  return (
    <button type="button" role="switch" aria-checked={on} aria-label={label} className={`fx-toggle${on ? ' on' : ''}`} onClick={() => onChange(!on)}>
      <i />
    </button>
  )
}

// 진행 바 (무료배송 · 최소주문)
export function Progress({ ratio }) {
  return <div className="fx-progress"><i style={{ width: `${Math.round(Math.min(1, Math.max(0, ratio)) * 100)}%` }} /></div>
}

// 하단 고정 바 (버튼 규칙: 위 12 · 좌우 20 · 아래 safe-area+16(최소 34))
//  hintLane: 힌트가 나오는 화면 — 바 안쪽 위에 힌트 한 줄 자리(버튼 위 12)를 만들어 뒤 내용과 안 겹치게
export function BottomBar({ children, hintLane = false }) {
  return <div className={`bs-cta fx-bottom${hintLane ? ' fx-hint-lane' : ''}`}>{children}</div>
}

// 토스트 "장바구니에 담았어요  [보기]" — 3초
export function Toast({ text, action, onAction, onDone, ms = 3000 }) {
  useEffect(() => {
    const t = setTimeout(onDone, ms)
    return () => clearTimeout(t)
  }, [])
  return (
    <div className="fx-toast" role="status">
      <span>{text}</span>
      {action && <button type="button" className="fx-toast-action" data-hint-target="toast-action" onClick={onAction}>{action}</button>}
    </div>
  )
}

// 바텀시트 (키트 .bs-dim + .bs-sheet — 힌트는 시트 바깥 위쪽 어두운 곳에)
export function Sheet({ onDismiss, children, className = '' }) {
  return (
    <>
      <div className="bs-dim kit-fade" onClick={onDismiss} />
      <div className={`bs-sheet kit-sheet-up fx-sheet ${className}`}>
        <div className="handle" />
        {children}
      </div>
    </>
  )
}

// 줄 메뉴 (스트림플러스 멤버십 등) — 오른쪽 ›
export function MenuRow({ label, value, onClick }) {
  return (
    <div className="fx-menu-row" onClick={onClick} role={onClick ? 'button' : undefined}>
      <span>{label}</span>
      <span className="fx-menu-value">{value}{onClick && <ChevronRight size={16} strokeWidth={STROKE} />}</span>
    </div>
  )
}

// ════════════════════════════════════════════════════════════════
//  비상페이 결제 시트 (A · B 공통 · 토스페이처럼)
//  핸들 → "결제하기" → 가게 한 줄 → 카드(옆으로 넘기면 한 장 더) → 할부 → 금액 내역 → 총 결제 금액 → [N원 결제하기]
// ════════════════════════════════════════════════════════════════
const CARDS = [
  { name: '비상카드', no: '**** 0303', tone: 'dark' },
  { name: '비상체크', no: '**** 1122', tone: 'mid' },
]
export function PayFlowSheet({ merchant, rows, total, onPay, onDismiss }) {
  const [card, setCard] = useState(0)
  return (
    <Sheet onDismiss={onDismiss} className="fx-pay" >
      <div data-fx="pay-sheet">
        <div className="t-title2">결제하기</div>
        <div className="fx-pay-merchant">{merchant}</div>
        <div className="fx-pay-cards" onScroll={(e) => setCard(Math.round(e.currentTarget.scrollLeft / 236))}>
          {CARDS.map((c) => (
            <div key={c.no} className={`fx-pay-card ${c.tone}`}>
              <span className="t-brand fx-pay-card-name">{c.name}</span>
              <span className="fx-pay-card-no">{c.no}</span>
            </div>
          ))}
        </div>
        <div className="fx-dots" aria-hidden="true">{CARDS.map((c, i) => <i key={c.no} className={i === card ? 'on' : ''} />)}</div>
        <div className="fx-menu-row"><span>할부</span><span className="fx-menu-value">일시불 <ChevronRight size={16} strokeWidth={STROKE} /></span></div>
        <PriceRows rows={rows} />
        <div className="fx-divider" />
        <div className="fx-pay-total"><span>총 결제 금액</span><strong>{krw(total)}</strong></div>
        <button type="button" className="bs-btn primary" style={{ width: '100%', marginTop: 16 }} data-hint-target="pay-sheet" onClick={onPay}>{krw(total)} 결제하기</button>
      </div>
    </Sheet>
  )
}

// ════════════════════════════════════════════════════════════════
//  장바구니 부품 (지름마켓 · 배고파 공통 — 실제 쇼핑 · 배달 앱 장바구니 틀)
// ════════════════════════════════════════════════════════════════

// 헤더: ‹ 장바구니 (개수 배지) · 오른쪽 홈
//  homeDecor: 홈 아이콘을 장식으로만 (누르는 기능 없음)
export function CartHeader({ count, onBack, onHome, homeDecor = false }) {
  return (
    <header className="fx-header white fx-cart-header">
      <IconButton icon={ChevronLeft} label="뒤로 가기" onClick={onBack} edge="left" />
      <span className="fx-cart-title">장바구니{count > 0 && <span className="fx-count">{count}</span>}</span>
      {onHome ? <IconButton icon={House} label="홈" onClick={onHome} edge="right" />
        : homeDecor ? <span className="fx-decor-icon" aria-hidden="true"><House size={24} strokeWidth={STROKE} /></span>
        : <span style={{ width: 48 }} />}
    </header>
  )
}

// 배송지 한 줄
export function AddressRow({ text }) {
  return (
    <div className="fx-address"><MapPin size={20} strokeWidth={STROKE} /><span>{text}</span><ChevronRight size={16} strokeWidth={STROKE} /></div>
  )
}

// 회색 구분 띠 8px
export function Band() {
  return <div className="fx-band" />
}

// 장바구니 상품 행: 체크 · 사진 72 · 이름 / 옵션 / 정가·할인율·판매가 / 수량 · 오른쪽 위 X
//  view: 보기만 (체크 · 수량 표시만, X 없음 — 2026-10-11 '보이게만')
export function CartItem({ on, onCheck, noCheck = false, view = false, artKey, name, option, original, price, qty, onQty, onRemove }) {
  const sale = original && original > price ? Math.floor(((original - price) * 100) / original) : 0
  return (
    <div className="fx-citem">
      {!noCheck && (view ? <CheckMark /> : <CheckCircle on={on} label={`${name} 선택`} onClick={onCheck} />)}
      <div className="fx-thumb"><img src={art(artKey)} alt="" /></div>
      <div className="fx-citem-main">
        <div className="fx-citem-name">{name}</div>
        {option && <div className="fx-citem-opt">{option}</div>}
        <div className="fx-citem-price">
          {sale > 0 && <><span className="fx-strike">{krw(original * qty)}</span> <span className="fx-sale">{sale}%</span> </>}
          <b>{krw(price * qty)}</b>
        </div>
        {view ? <div style={{ marginTop: 8 }}><QtyView n={qty} /></div>
          : onQty && <div style={{ marginTop: 8 }}><Stepper value={qty} onChange={onQty} /></div>}
      </div>
      {!view && <button type="button" className="fx-citem-x" aria-label={`${name} 삭제`} onClick={onRemove}><X size={20} strokeWidth={STROKE} /></button>}
    </div>
  )
}

// 보기만 하는 체크 표시 · 수량 "− 1 +" (누르는 기능 없음)
export function CheckMark() {
  return <span className="fx-check on" aria-hidden="true"><Check size={14} strokeWidth={2.5} /></span>
}
export function QtyView({ n }) {
  return (
    <span className="fx-stepper fx-stepper-view" aria-label={`수량 ${n}개`}>
      <span><Minus size={16} strokeWidth={STROKE} /></span><span className="fx-stepper-n">{n}</span><span><Plus size={16} strokeWidth={STROKE} /></span>
    </span>
  )
}

// 진행 상자 (무료배송 · 최소주문) — 안에 추천 상품을 넣을 수 있어요
export function ProgressBox({ title, ratio, done, children }) {
  return (
    <div className="fx-pbox">
      <div className="fx-pbox-title">{title}</div>
      {!done && <Progress ratio={ratio} />}
      {children}
    </div>
  )
}

// 장바구니가 비었을 때
export function EmptyCart({ label, onGo }) {
  return (
    <div className="fx-empty">
      <div className="fx-empty-title">장바구니가 비었어요</div>
      <button type="button" className="bs-btn secondary" style={{ width: '100%', marginTop: 16 }} onClick={onGo}>{label}</button>
    </div>
  )
}

export { Heart }
