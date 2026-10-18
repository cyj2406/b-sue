import { useEffect, useMemo, useState } from 'react'
import { Eye, Heart, Star } from 'lucide-react'
import { STROKE } from '../components/AppChrome'
import { WeakButton } from '../components/Kit'
import { byId } from '../data/guardians'
import Dispatch from '../screens/Dispatch'
import { PRESS_LOADING } from './timing'
import {
  AddressRow, Band, Block, BottomBar, CartHeader, CartItem, CheckMark, FxHeader, PayFlowSheet, PriceRows, ProgressBox, Toast, art, krw,
} from './apps/FakeKit'

// ════════════════════════════════════════════════════════════════
//  전시 체험 A — 가짜 쇼핑앱 '지름마켓' (2026-10-11 실제 앱 흐름으로 다시 짬)
//  ① 상품 상세 — 390×844 스크롤 없이 한 화면 (2026-10-11)
//     사진 240(넘김 점 포함) · 판매자 · 이름 · 정가/할인율/판매가 · 타임딜 바 · 별점 · 보는 사람 · 옵션 한 줄(표시만) · 배송
//     사이즈는 260으로 미리 골라져 있어요 (칩 없음 · scenarios.js product.size)
//     하단 [♡ 56] [장바구니] [바로 구매] — 두 버튼 같은 너비 (DESIGN.md 11장 예외)
//  ② [장바구니] → 토스트 "장바구니에 담았어요 [보기]" (3초) → [보기] 또는 오른쪽 위 장바구니
//  ③ 장바구니: 보이게만 — 배송지 · 전체선택 · 상품 행(표시) · 무료배송 상자(양말 [담기] 선택) · 금액 요약 → [N원 구매하기]
//  ④ 장바구니 [N원 구매하기] 또는 상세 [바로 구매] → 바로 비상페이 시트 → 출동 (어느 길로 와도 같아요)
//  꼭 누를 것은 화면마다 하나 (힌트 hints.js) · 수량 · 추천 담기 · ♡ · 체크는 해도 되고 안 해도 돼요
//  문구 · 숫자는 scenarios.js a · 모양은 FakeKit.jsx / styles/fakeapps.css
// ════════════════════════════════════════════════════════════════
const pct = (price, original) => Math.floor(((original - price) * 100) / original)

export default function ShopFlow({ s, onPeek, onResult, onBack }) {
  const p = s.product
  const [step, setStep] = useState('detail') // detail | cart | dispatch
  const [from, setFrom] = useState('cart')        // 결제창을 연 곳 (detail: 바로 구매 · cart: 장바구니)
  const size = p.size ?? 260                      // 미리 골라진 사이즈 (2026-10-11 칩 삭제)
  const [inCart, setInCart] = useState(false)
  const [extras, setExtras] = useState({})        // 추천 상품 { id: true } (담겨 있음)
  const [heart, setHeart] = useState(false)
  const [toast, setToast] = useState(null)        // { text, action }
  const [paying, setPaying] = useState(false)
  const [slide, setSlide] = useState(0)
  const [upsellPeeked, setUpsellPeeked] = useState(false)

  // 상세에 들어오고 1.5초 뒤 빼꼼
  useEffect(() => {
    if (step !== 'detail') return
    const t = setTimeout(() => onPeek(s.peeks.detail), 1500)
    return () => clearTimeout(t)
  }, [step])

  // ── 금액 ──
  const extraList = s.upsells.filter((u) => extras[u.id])
  const lines = [
    { name: p.name, price: p.price, original: p.original, qty: 1 },
    ...extraList.map((u) => ({ name: u.name, price: u.price, original: u.price, qty: 1 })),
  ]
  const sum = (k) => lines.reduce((a, l) => a + l[k] * l.qty, 0)
  const items = sum('price')
  const original = sum('original')
  const shipping = items === 0 || items >= p.freeShipAt ? 0 : p.shippingFee
  const total = items + shipping
  const guardian = byId(extraList.length ? s.guardian.withUpsell : s.guardian.default)
  const cartCount = (inCart ? 1 : 0) + extraList.length
  const priceRows = [['상품금액', original], ['할인', -(original - items), 'sale'], ['배송비', shipping]]

  const addToCart = () => {
    setInCart(true)
    setToast({ text: '장바구니에 담았어요', action: '보기' })
  }
  const goCart = () => { setToast(null); setStep('cart') }
  const buyNow = () => { setToast(null); setFrom('detail'); setPaying(true) } // 장바구니 없이 바로 비상페이 결제창
  const addExtra = (id) => {
    setExtras((e) => ({ ...e, [id]: true }))
    if (!upsellPeeked) { setUpsellPeeked(true); onPeek(s.peeks.upsell) }
  }

  const toastEl = toast && (
    <Toast key={toast.text + cartCount} text={toast.text} action={toast.action} onAction={goCart} onDone={() => setToast(null)} />
  )

  // 결제 → 출동 (바로 구매 · 장바구니 어느 쪽에서 와도 같아요)
  const payFor = (f) => {
    const n = f === 'detail' ? 1 : lines.length
    const its = f === 'detail' ? p.price : items
    const ship = its >= p.freeShipAt ? 0 : p.shippingFee
    return { count: n, items: its, shipping: ship, total: its + ship, guardian: f === 'detail' ? byId(s.guardian.default) : guardian }
  }
  const pay = payFor(from)
  const paySheet = paying && step !== 'dispatch' && (
    <PayFlowSheet
      merchant={`${s.opening.app} · 주문 ${pay.count}건`}
      rows={[['상품', pay.items], ['배송비', pay.shipping]]}
      total={pay.total}
      onDismiss={() => setPaying(false)}
      onPay={() => { setPaying(false); onPeek(null); setStep('dispatch') }} // 출동 화면 위에 빼꼼이 남지 않게
    />
  )
  const dispatchEl = step === 'dispatch' && (
    <Dispatch
      guardian={pay.guardian}
      amount={pay.total}
      app={s.opening.app}
      resultSheets={false}
      pressDelay={PRESS_LOADING}
      onFinished={(result) => onResult({ result, amount: pay.total, guardian: pay.guardian.id })}
    />
  )

  // ① 상품 상세 — 스크롤 없이 한 화면
  if (step === 'detail' || (step === 'dispatch' && from === 'detail')) {
    return (
      <>
        <div key="detail" className="fx-screen" data-fx="a-detail" data-size={size} data-added={inCart ? '1' : undefined}>
          <div className="fx-scroll">
            <FxHeader title="지름마켓" onBack={onBack} cartCount={cartCount} onCart={goCart} />
            <div style={{ background: 'var(--white)' }}>
              <div className="fx-gallery" onScroll={(e) => setSlide(Math.round(e.currentTarget.scrollLeft / e.currentTarget.clientWidth))}>
                {[s.guardian.default, s.guardian.default, 'free_shipping'].map((k, i) => (
                  <div key={i}><img src={art(k)} alt="" /></div>
                ))}
              </div>
              <div className="fx-dots" style={{ background: 'var(--grey-50)', paddingBottom: 12 }}>{[0, 1, 2].map((i) => <i key={i} className={i === slide ? 'on' : ''} />)}</div>
            </div>
            <Block>
              <div className="fx-seller">{p.store} ›</div>
              <div className="fx-name">{p.title}</div>
              <div className="fx-original">{krw(p.original)}</div>
              <div className="fx-price"><span className="fx-sale">{pct(p.price, p.original)}%</span><span>{krw(p.price)}</span></div>
              <TimeDeal start={p.timer} total={p.timerTotal} />
              <div className="fx-meta"><Star size={16} strokeWidth={STROKE} fill="var(--grey-900)" /> <b>{p.rating}</b> · 리뷰 {p.reviews.toLocaleString('ko-KR')}개</div>
              <div className="fx-meta"><Eye size={16} strokeWidth={STROKE} /> 지금 {p.viewers}명이 보고 있어요</div>
            </Block>
            <Block>
              {/* 옵션 한 줄 — 표시만 (누르는 기능 없음) */}
              <div className="fx-option-row"><span className="fx-muted">옵션</span><span>{p.color} / {size} ›</span></div>
              <div className="fx-small" style={{ marginTop: 12 }}><b>{p.arrival}</b> · {krw(p.freeShipAt)} 이상 무료배송</div>
            </Block>
          </div>
          {toastEl}
          <BottomBar hintLane>
            <div className="fx-buybar even">
              <button type="button" className={`fx-heart${heart ? ' on' : ''}`} aria-label="찜하기" aria-pressed={heart} onClick={() => setHeart(!heart)}>
                <Heart size={24} strokeWidth={STROKE} fill={heart ? 'currentColor' : 'none'} />
              </button>
              <button type="button" className="bs-btn secondary" data-hint-target="add-cart" onClick={addToCart}>장바구니</button>
              <button type="button" className="bs-btn primary" data-hint-target="buy-now" onClick={buyNow}>바로 구매</button>
            </div>
          </BottomBar>
          {from === 'detail' && paySheet}
        </div>
        {dispatchEl}
      </>
    )
  }

  // ③ 장바구니 — 실제 쇼핑앱처럼 '보이게만' (2026-10-11 목업 오른쪽)
  //  배송지 · 전체선택 · 체크 · 수량 · 홈 아이콘은 표시만 (누르는 기능 없음)
  //  무료배송 상자 안 양말 [담기]는 눌러도 되고 안 눌러도 돼요 · 꼭 누를 것은 [N원 구매하기] 하나 → 바로 비상페이 결제창
  const left = Math.max(0, p.freeShipAt - items)
  const recos = s.upsells.filter((u) => !extras[u.id])
  const count = lines.length
  return (
    <>
      <div key="cart" className="fx-screen white" data-fx="a-cart">
        <div className="fx-scroll">
          <CartHeader count={count} onBack={() => { setPaying(false); setStep('detail') }} homeDecor />
          <AddressRow text={`${s.address.label} · ${s.address.line}`} />
          <div className="fx-selectall"><CheckMark /><span>전체선택 ({count}/{count})</span></div>
          <Band />
          <div className="fx-cart-seller"><span>{s.opening.app}</span><span className="fx-chip-grey">{p.arrival} 보장</span></div>
          <CartItem view artKey={s.guardian.default} name={p.name} option={`${p.color} / ${size ?? 260}`} original={p.original} price={p.price} qty={1} />
          {extraList.map((u) => (
            <CartItem key={u.id} view artKey={u.art} name={u.name} price={u.price} qty={1} />
          ))}
          <ProgressBox title={left > 0 ? `무료배송까지 ${krw(left)}` : '무료배송 달성! · 배송비 0원'} ratio={items / p.freeShipAt} done={left === 0}>
            {left > 0 && recos.map((u) => (
              <div key={u.id} className="fx-reco-row">
                <div className="fx-thumb" style={{ width: 48, height: 48 }}><img src={art(u.art)} alt="" style={{ width: 40 }} /></div>
                <span className="fx-reco-row-text">{u.name}<br /><b>{krw(u.price)}</b></span>
                <WeakButton onClick={() => addExtra(u.id)}>담기</WeakButton>
              </div>
            ))}
          </ProgressBox>
          <Band />
          <div className="fx-summary">
            <PriceRows rows={[['상품금액', original], ['할인금액', -(original - items), 'sale'], ['배송비', shipping]]} total={total} totalLabel="결제 예정 금액" />
          </div>
          <div style={{ height: 160 }} />
        </div>
        <BottomBar hintLane>
          <button type="button" className="bs-btn primary" style={{ width: '100%' }} data-hint-target="buy" onClick={() => { setFrom('cart'); setPaying(true) }}>{krw(total)} 구매하기</button>
        </BottomBar>
        {from === 'cart' && paySheet}
      </div>
      {dispatchEl}
    </>
  )
}

// 타임딜 남은 시간 바 (1초씩 줄어요)
function TimeDeal({ start, total }) {
  const toSec = (t) => { const [h, m, s] = t.split(':').map(Number); return h * 3600 + m * 60 + s }
  const [left, setLeft] = useState(() => toSec(start))
  useEffect(() => {
    const t = setInterval(() => setLeft((v) => Math.max(0, v - 1)), 1000)
    return () => clearInterval(t)
  }, [])
  const label = useMemo(() => {
    const p = (v) => String(v).padStart(2, '0')
    return `${p(Math.floor(left / 3600))}:${p(Math.floor((left % 3600) / 60))}:${p(left % 60)}`
  }, [left])
  return (
    <div className="fx-timedeal">
      <div className="fx-timedeal-top"><span className="fx-sale">타임딜</span><span>{label} 남음</span></div>
      <div className="fx-timedeal-bar"><i style={{ width: `${(left / total) * 100}%` }} /></div>
    </div>
  )
}
