import { useEffect, useState } from 'react'
import { Star } from 'lucide-react'
import { STROKE } from '../../components/AppChrome'
import { WeakButton } from '../../components/Kit'
import { byId } from '../../data/guardians'
import Dispatch from '../../screens/Dispatch'
import { PRESS_LOADING } from '../timing'
import { NotificationBanner } from '../parts/PhoneParts'
import { fill } from '../scenarios'
import {
  Band, Block, BottomBar, CartHeader, CartItem, FxHeader, PayFlowSheet, PriceRows, ProgressBox, Sheet, art, krw,
} from './FakeKit'

// ════════════════════════════════════════════════════════════════
//  전시 체험 B — 가짜 배달앱 '배고파' (2026-10-11 실제 앱 흐름으로 다시 짬)
//  ① 가게: 이름 · 별점 · "배달 25~35분 · 최소주문 25,000원 · 배달팁 0~3,000원" · 쿠폰 배너 · 메뉴 목록
//  ② 메뉴 → 옵션 시트: 뼈/순살(필수 · '뼈' 미리 골라 둠) · 음료 추가(선택) → [N원 담기]
//  ③ 담으면 냉장고 알림(블롭 · 지금 자리 그대로) + 아래 떠 있는 바 "1 · 장바구니 보기 · N원"
//  ④ 장바구니: 가게 · 메뉴(수량) · 최소주문 진행 바 · 함께 주문하면 좋아요 · 요청사항(수저 안 받기) · 결제금액 → [배달 주문하기]
//  ⑤ 비상페이 시트 → 출동
//  쿠폰은 배너를 누르든 안 누르든 장바구니에서 자동 적용 · 문구 · 숫자는 scenarios.js b
// ════════════════════════════════════════════════════════════════
export default function Baegopa({ s, onPeek, onResult, onBack }) {
  const st = s.store
  const [step, setStep] = useState('store') // store | cart | dispatch
  const [optFor, setOptFor] = useState(null) // 옵션 시트를 연 메뉴
  const [bone, setBone] = useState('bone')
  const [drinks, setDrinks] = useState({})
  const [cart, setCart] = useState([])       // [{ key, menu, bone, drinks, unit, qty }]
  const [sides, setSides] = useState({})
  const [couponTaken, setCouponTaken] = useState(false)
  const [fridgeNotice, setFridgeNotice] = useState(false)
  const [sawFridge, setSawFridge] = useState(false)
  const [paying, setPaying] = useState(false)
  const [upsellPeeked, setUpsellPeeked] = useState(false)

  // 가게 화면이 열리고 1.5초 뒤 빼꼼
  useEffect(() => {
    if (step !== 'store') return
    const t = setTimeout(() => onPeek(s.peeks.detail), 1500)
    return () => clearTimeout(t)
  }, [step])

  const optPrice = (m) => m.price + (s.options.bone.find((b) => b.id === bone)?.price ?? 0)
    + s.options.drinks.filter((d) => drinks[d.id]).reduce((a, d) => a + d.price, 0)

  const openMenu = (m) => { setOptFor(m); setBone('bone'); setDrinks({}) }
  const addMenu = () => {
    const m = optFor
    const unit = optPrice(m)
    setCart((c) => [...c, { key: `${m.id}-${c.length}`, menu: m, bone, drinks: s.options.drinks.filter((d) => drinks[d.id]).map((d) => d.name), unit, qty: 1 }])
    setOptFor(null)
    if (!sawFridge) { setFridgeNotice(true); setSawFridge(true) }
  }
  const addSide = (id) => {
    setSides((x) => ({ ...x, [id]: true }))
    if (!upsellPeeked) { setUpsellPeeked(true); onPeek(s.peeks.upsell) }
  }

  // ── 금액 (쿠폰 자동 적용) ──
  const sideList = s.sides.filter((x) => sides[x.id])
  const menuSum = cart.reduce((a, l) => a + l.unit * l.qty, 0)
  const items = menuSum + sideList.reduce((a, x) => a + x.price, 0)
  const tip = items > 0 ? st.tip : 0
  const total = items + tip // 2026-10-11 장바구니 금액 요약에 쿠폰 줄 없음 (메뉴 금액 · 배달팁만)
  const left = Math.max(0, st.minOrder - items)
  const count = cart.reduce((a, l) => a + l.qty, 0) + sideList.length
  const guardian = byId(s.guardian.default)

  // ① 가게 (+ ② 옵션 시트 · ③ 떠 있는 바)
  if (step === 'store') {
    return (
      <div key="store" className="fx-screen" data-fx="b-store" data-added={cart.length ? '1' : undefined} data-sheet={optFor ? '1' : undefined}>
        <div className="fx-scroll">
          <FxHeader title={s.opening.app} onBack={onBack} cartCount={count} onCart={() => cart.length && setStep('cart')} />
          <Block>
            <div className="fx-store-name">{st.name}</div>
            <div className="fx-meta" style={{ marginTop: 4 }}><Star size={16} strokeWidth={STROKE} fill="var(--grey-900)" /> <b>{st.rating}</b> · 리뷰 {st.reviews.toLocaleString('ko-KR')}개</div>
            <div className="fx-store-info">{st.eta} · 최소주문 {krw(st.minOrder)} · {st.tipRange}</div>
            <button type="button" className="fx-coupon" onClick={() => setCouponTaken(true)}>
              <span><span className="fx-sale">{krw(st.coupon)}</span> 쿠폰 {couponTaken ? '받았어요' : '받기'}</span>
              <span className="fx-muted fx-small">{couponTaken ? '주문할 때 자동 적용' : '받기 ↓'}</span>
            </button>
          </Block>
          <Block>
            <div className="fx-block-title">인기 메뉴</div>
            {s.menus.map((m, i) => (
              <button key={m.id} type="button" className="fx-menu" data-hint-target={i === 0 ? 'menu-first' : undefined} onClick={() => openMenu(m)}>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div className="fx-menu-name">{m.name}</div>
                  <div className="fx-menu-desc">{m.desc}</div>
                  <div className="fx-menu-price">{krw(m.price)}</div>
                </div>
                <div className="fx-thumb"><img src={art(m.art)} alt="" /></div>
              </button>
            ))}
          </Block>
          <div style={{ height: 160 }} />
        </div>

        {/* ③ 냉장고 알림 (블롭) — 지금 자리 그대로 */}
        {fridgeNotice && (
          <NotificationBanner floating {...s.fridgeNotice} autoDismissMs={4000} vibration={80} onTap={() => setFridgeNotice(false)} />
        )}

        {/* ③ 떠 있는 장바구니 바 */}
        {cart.length > 0 && (
          <BottomBar hintLane>
            <button type="button" className="bs-btn primary fx-floatbar" data-hint-target="cart-bar" onClick={() => setStep('cart')}>
              <span className="fx-floatbar-n">{count}</span> 장바구니 보기 · {krw(items)}
            </button>
          </BottomBar>
        )}

        {/* ② 옵션 시트 */}
        {optFor && (
          <Sheet onDismiss={() => setOptFor(null)}>
            <div data-fx="b-option">
              <div className="t-title2">{optFor.name}</div>
              <div className="fx-small fx-muted" style={{ marginTop: 4 }}>{optFor.desc}</div>
              <div className="fx-opt-group">
                <div className="fx-opt-head">뼈 / 순살 <span className="fx-tag">필수</span></div>
                {s.options.bone.map((b) => (
                  <div key={b.id} className="fx-opt-row" role="radio" aria-checked={bone === b.id} onClick={() => setBone(b.id)}>
                    <span className={`fx-radio${bone === b.id ? ' on' : ''}`} />{b.name}
                    <span className="fx-opt-price">{b.price ? `+${krw(b.price)}` : '추가 없음'}</span>
                  </div>
                ))}
              </div>
              <div className="fx-opt-group">
                <div className="fx-opt-head">음료 추가 <span className="fx-tag">선택</span></div>
                {s.options.drinks.map((d) => (
                  <div key={d.id} className="fx-opt-row" role="checkbox" aria-checked={!!drinks[d.id]} onClick={() => setDrinks((x) => ({ ...x, [d.id]: !x[d.id] }))}>
                    <span className={`fx-check${drinks[d.id] ? ' on' : ''}`} style={{ pointerEvents: 'none' }}>✓</span>{d.name}
                    <span className="fx-opt-price">+{krw(d.price)}</span>
                  </div>
                ))}
              </div>
              <button type="button" className="bs-btn primary" style={{ width: '100%', marginTop: 20 }} data-hint-target="option-add" onClick={addMenu}>{krw(optPrice(optFor))} 담기</button>
            </div>
          </Sheet>
        )}
      </div>
    )
  }

  // ④ 장바구니 — 배달앱처럼 '보이게만' (2026-10-11 목업 오른쪽)
  //  가게 이름 · 별점 · 배달 시간 → 메뉴 행(사진 · 이름 · 가격 · 수량 표시) → 최소주문 상자(콜라 · 치즈볼 [담기] 선택)
  //  → 금액 요약(메뉴 금액 · 배달팁) → 꼭 누를 것은 [N원 주문하기] 하나 → 바로 비상페이 결제창
  const sideRecos = s.sides.filter((x) => !sides[x.id])
  return (
    <>
      <div key="cart" className="fx-screen white" data-fx="b-cart">
        <div className="fx-scroll">
          <CartHeader count={count} onBack={() => { setPaying(false); setStep('store') }} />
          <div className="fx-store-head">
            <div className="fx-store-name">{st.name}</div>
            <div className="fx-meta" style={{ marginTop: 4 }}><Star size={16} strokeWidth={STROKE} fill="var(--grey-900)" /> <b>{st.rating}</b> · {st.eta}</div>
          </div>
          <Band />
          {cart.map((l) => (
            <CartItem key={l.key} view noCheck artKey={l.menu.art} name={l.menu.name} price={l.unit} qty={l.qty} />
          ))}
          {sideList.map((x) => (
            <CartItem key={x.id} view noCheck artKey={x.art} name={x.name} price={x.price} qty={1} />
          ))}
          <ProgressBox title={left > 0 ? fill(s.upsell.banner, { left }) : '최소주문 금액을 채웠어요'} ratio={items / st.minOrder} done={left === 0}>
            {left > 0 && sideRecos.map((x) => (
              <div key={x.id} className="fx-reco-row">
                <div className="fx-thumb" style={{ width: 48, height: 48 }}><img src={art(x.art)} alt="" style={{ width: 40 }} /></div>
                <span className="fx-reco-row-text">{x.name}<br /><b>{krw(x.price)}</b></span>
                <WeakButton onClick={() => addSide(x.id)}>담기</WeakButton>
              </div>
            ))}
          </ProgressBox>
          <Band />
          <div className="fx-summary">
            <PriceRows rows={[['메뉴 금액', items], ['배달팁', tip]]} total={total} totalLabel="결제 예정 금액" />
          </div>
          <div style={{ height: 160 }} />
        </div>
        <BottomBar hintLane>
          <button type="button" className="bs-btn primary" style={{ width: '100%' }} data-hint-target="b-order" onClick={() => setPaying(true)}>{krw(total)} 주문하기</button>
        </BottomBar>
        {paying && step === 'cart' && (
          <PayFlowSheet
            merchant={`${s.opening.app} · ${st.name}`}
            rows={[['메뉴', items], ['배달팁', tip]]}
            total={total}
            onDismiss={() => setPaying(false)}
            onPay={() => { setPaying(false); onPeek(null); setStep('dispatch') }} // 출동 화면 위에 빼꼼이 남지 않게
          />
        )}
      </div>

      {step === 'dispatch' && (
        <Dispatch
          guardian={guardian}
          amount={total}
          app={s.opening.app}
          resultSheets={false}
          pressDelay={PRESS_LOADING}
          onFinished={(result) => onResult({ result, amount: total, guardian: guardian.id, bonus: sawFridge ? s.guardian.bonus : null })}
        />
      )}
    </>
  )
}
