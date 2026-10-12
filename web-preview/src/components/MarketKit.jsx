import { useEffect, useState } from 'react'
import { ShoppingBag } from 'lucide-react'
import { AppHeader, IconButton } from './AppChrome'
import { Badge, BottomSheet, PrimaryButton, WeakButton } from './Kit'

// ════════════════════════════════════════════════════════════════
//  지름마켓 · 비상페이 부품 — reference ⑤ 상세 · ⑥ 장바구니 + 결제 시트
//  가짜 앱도 같은 키트 (README 4: 브랜드 차이는 이름과 아이콘 글자뿐 · 파랑 금지)
//  앱 미리보기(screens/Market.jsx)와 전시 체험(exhibit/ShopFlow.jsx)이 같이 써요
// ════════════════════════════════════════════════════════════════

const krw = (n) => n.toLocaleString('ko-KR') + '원'
const pct = (price, original) => Math.floor(((original - price) * 100) / original)

// "02:47:13" 에서 1초씩 줄어드는 시계 (from 이 없으면 자정까지)
export function useCountdown(from) {
  const startSec = () => {
    if (from) {
      const [h, m, s] = from.split(':').map(Number)
      return h * 3600 + m * 60 + s
    }
    const n = new Date()
    return Math.floor((new Date(n.getFullYear(), n.getMonth(), n.getDate() + 1) - n) / 1000)
  }
  const [left, setLeft] = useState(startSec)
  useEffect(() => {
    const t = setInterval(() => setLeft((v) => Math.max(0, v - 1)), 1000)
    return () => clearInterval(t)
  }, [])
  const p = (v) => String(v).padStart(2, '0')
  return `${p(Math.floor(left / 3600))}:${p(Math.floor((left % 3600) / 60))}:${p(left % 60)}`
}

// 상단 바: ‹ 지름마켓 [장바구니] — 하위 화면 헤더 (흰 바탕)
export function MarketTop({ title = '지름마켓', onBack, onBag }) {
  return (
    <AppHeader
      bg="white"
      onBack={onBack}
      title={title}
      right={onBag !== false ? <IconButton icon={ShoppingBag} label="장바구니" onClick={onBag} /> : null}
    />
  )
}

// ⑤ 상품 상세
// product: { store, title, art, price, original?, timer?, rating, reviews, delivery }
export function ProductDetail({ product: p, onBack, onCart, onBuy, children }) {
  const timer = useCountdown(p.timer)
  return (
    <div className="bs-screen white kit-frame">
      <div className="kit-scroll" style={{ paddingBottom: 120 }}>
        <MarketTop onBack={onBack} onBag={onCart} />
        <div className="bs-thumb" style={{ height: 250, borderRadius: 0, alignItems: 'center' }}>
          <img src={p.art} alt="" style={{ height: 200 }} />
        </div>
        <div style={{ padding: '16px var(--side) 0' }}>
          <div className="t-caption">{p.store}</div>
          <div className="t-title3" style={{ fontWeight: 600 }}>{p.title}</div>
          {p.original ? (
            <>
              <div className="t-caption" style={{ textDecoration: 'line-through', marginTop: 10 }}>{krw(p.original)}</div>
              <div className="t-title1"><span className="c-orange">{pct(p.price, p.original)}%</span> {krw(p.price)}</div>
            </>
          ) : (
            <div className="t-title1" style={{ marginTop: 10 }}>{krw(p.price)}</div>
          )}
          <div style={{ marginTop: 10, display: 'flex', gap: 6, flexWrap: 'wrap' }}>
            {p.original && <Badge tone="orange">오늘만 · {timer} 남음</Badge>}
            <Badge>★ {p.rating} ({p.reviews.toLocaleString('ko-KR')})</Badge>
          </div>
          <div className="bs-divider" style={{ margin: '16px 0 12px' }} />
          <div className="t-body2 c-sub">{p.delivery}</div>
        </div>
      </div>
      {children /* 빼꼼 같은 블롭 층 */}
      <div className="bs-cta">
        <div className="bs-btn-row">
          <button className="bs-btn secondary" onClick={onCart}>장바구니</button>
          <button className="bs-btn primary" onClick={onBuy}>구매하기</button>
        </div>
      </div>
    </div>
  )
}

// ⑥ 장바구니 한 줄
export function CartRow({ art, name, option, price, end }) {
  return (
    <div className="bs-row">
      <div className="bs-thumb" style={{ width: 64, height: 64 }}><img src={art} alt="" style={{ width: 56 }} /></div>
      <div className="main">
        <div className="t-body1">{name}</div>
        {option && <div className="t-caption">{option}</div>}
      </div>
      <div className="end t-body1 bold">{end ?? krw(price)}</div>
    </div>
  )
}

// ⑥ 무료배송 안내 (연한 주황 카드 + 진행률)
// label: 다른 문구가 필요할 때 (배고파 '최소주문까지 …')
export function FreeShipCard({ left, ratio, label }) {
  return (
    <div className="bs-card tint" style={{ marginTop: 8 }}>
      <div className="t-body2 bold" style={{ color: 'var(--orange-700)' }}>
        {label ?? (left > 0 ? `무료배송까지 ${krw(left)} 남았어요` : '무료배송이에요')}
      </div>
      <div className="bs-progress" style={{ marginTop: 10, background: 'var(--orange-100)' }}>
        <i style={{ width: `${Math.round(Math.min(1, ratio) * 100)}%` }} />
      </div>
    </div>
  )
}

// 함께 사면 좋은 상품 (담기 버튼)
export function UpsellRow({ art, name, price, added, onAdd }) {
  return (
    <CartRow
      art={art}
      name={name}
      option={krw(price)}
      end={added ? <span className="t-caption">담았어요</span> : <WeakButton onClick={onAdd}>담기</WeakButton>}
    />
  )
}

// ⑥ 장바구니 화면 (주문하기 + 선택적 마켓 목록 복귀 버튼)
export function CartScreen({
  onBack,
  onOrder,
  onContinue,
  orderLabel = '주문하기',
  continueLabel = '마켓으로 돌아가기',
  children,
  overlay,
}) {
  return (
    <div className="bs-screen white kit-frame">
      <div className="kit-scroll" style={{ paddingBottom: onContinue ? 188 : 120 }}>
        <MarketTop title="장바구니" onBack={onBack} onBag={false} />
        <div style={{ padding: '0 var(--side)' }}>{children}</div>
      </div>
      <div className={`bs-cta${onContinue ? ' cart-cta' : ''}`}>
        <PrimaryButton style={{ width: '100%' }} onClick={onOrder}>{orderLabel}</PrimaryButton>
        {onContinue && (
          <button className="bs-btn text" onClick={onContinue}>{continueLabel}</button>
        )}
      </div>
      {overlay}
    </div>
  )
}

// ⑥ 비상페이 결제 시트
// lines: [['상품 금액', 38700], ['배송비', 3000]]
export function PaySheet({ lines, total, onPay, onDismiss, cardNo = '0303' }) {
  return (
    <BottomSheet title="비상페이로 결제" onDismiss={onDismiss}>
      <div style={{ margin: '0 0 16px', height: 92, borderRadius: 14, background: 'var(--grey-900)', color: '#fff', padding: '16px 18px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
        <span className="t-brand" style={{ fontSize: 15, color: 'var(--orange-400)' }}>비상카드</span>
        <span className="t-caption" style={{ color: 'var(--grey-400)' }}>**** {cardNo}</span>
      </div>
      {lines.map(([label, v], i) => (
        <div key={label} className="t-body2 c-sub" style={{ display: 'flex', justifyContent: 'space-between', marginTop: i ? 6 : 0 }}>
          <span>{label}</span><span>{v === 0 ? '무료' : krw(v)}</span>
        </div>
      ))}
      <div className="bs-divider" style={{ margin: '12px 0' }} />
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
        <span className="t-body1 bold">총 결제</span><span className="t-title2">{krw(total)}</span>
      </div>
      <PrimaryButton style={{ marginTop: 16, width: '100%' }} onClick={onPay}>{krw(total)} 결제하기</PrimaryButton>
    </BottomSheet>
  )
}

// 결제 완료 (그래도 살래 뒤)
export function OrderDone({ title = '결제 완료', sub, children }) {
  return (
    <div className="bs-screen white kit-frame kit-fade" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center', padding: '0 var(--side)' }}>
      <div className="t-title2">{title}</div>
      {sub && <div className="t-caption" style={{ marginTop: 4 }}>{sub}</div>}
      <div style={{ width: '100%', marginTop: 24, display: 'flex', flexDirection: 'column', gap: 8 }}>{children}</div>
    </div>
  )
}
