import { useEffect, useState } from 'react'
import { Bell } from 'lucide-react'
import { InlineIcon, SecondaryButton, WeakButton } from '../components/Kit'
import {
  CartRow, CartScreen, FreeShipCard, MarketTop, OrderDone, PaySheet, ProductDetail,
} from '../components/MarketKit'
import { MISSION_ART } from '../data/blobPose'
import { discount, products, total } from '../data/market'

const NOTICE = '체험용 가상 쇼핑몰이에요. 실제로 결제되지 않아요.'
const krw = (n) => n.toLocaleString('ko-KR') + '원'

// 지름마켓 (앱 미리보기): 목록 → 상세(⑤) → 장바구니 + 비상페이 시트(⑥) → [결제하기] → 출동
// 상품 그림은 수호신 미션 그림 (reference ⑤처럼 블롭이 상품을 들고 있는 그림)
// afterResult: 출동 화면에서 고른 결과 ('held' | 'bought' | null)
export default function Market({ onExit, onPay, afterResult, onFakeNotice, onEvent = () => {} }) {
  const [step, setStep] = useState('list') // list | detail | cart | done
  const [paying, setPaying] = useState(false)
  const [product, setProduct] = useState(null)

  // 블롭 비서에게 알리기: 상품 보기(같은 상품 3번째면 가운데 개입) · 5초 넘게 보기
  useEffect(() => {
    if (step !== 'detail' || !product) return
    const t = setTimeout(() => onEvent('productDwell', { product }), 5000)
    return () => clearTimeout(t)
  }, [step, product])
  const openProduct = (p) => {
    setProduct(p)
    setStep('detail')
    onEvent('productView', { product: p })
  }

  useEffect(() => {
    if (afterResult === 'held') { setPaying(false); setStep('detail') } // 참았다 → 뒤로가기처럼 상세로
    if (afterResult === 'bought') { setPaying(false); setStep('done') } // 그래도 살래 → 결제 완료
  }, [afterResult])

  if (step === 'list' || !product) {
    return (
      <div className="bs-screen white kit-frame">
        <div className="kit-scroll">
          <MarketTop onBack={onExit} onBag={() => product && setStep('cart')} />
          <div style={{ padding: '0 var(--side) 24px' }}>
            <div className="t-caption">{NOTICE}</div>
            <div style={{ margin: '12px 0 4px' }}><WeakButton onClick={() => onFakeNotice(null)} style={{ gap: 4 }}><InlineIcon icon={Bell} />결제 알림 체험</WeakButton></div>
            {products.map((p) => (
              <div key={p.id} className="ui-row-btn" style={{ margin: '0 calc(var(--side) * -1)', padding: '0 var(--side)' }} onClick={() => openProduct(p)} role="button">
                <CartRow
                  art={MISSION_ART[p.guardianId]}
                  name={p.name}
                  option={`${p.store} · ★ ${p.rating}`}
                  end={
                    <div>
                      {discount(p) && <div className="t-caption c-orange semi">{discount(p)}%</div>}
                      <div className="t-body1 bold">{krw(p.price)}</div>
                    </div>
                  }
                />
              </div>
            ))}
          </div>
        </div>
      </div>
    )
  }

  const detail = {
    store: product.store,
    title: product.name,
    art: MISSION_ART[product.guardianId],
    price: product.price,
    original: product.original,
    rating: product.rating,
    reviews: product.reviews,
    delivery: product.shippingText,
  }

  if (step === 'detail') {
    return (
      <ProductDetail
        product={detail}
        onBack={() => setStep('list')}
        onCart={() => { onEvent('cartAdd', { amount: total(product) }); setStep('cart') }}
        onBuy={() => { setStep('cart'); setPaying(true) }}
      />
    )
  }

  if (step === 'cart') {
    const items = product.cartItems ?? [[product.name, product.price]]
    return (
      <CartScreen
        onBack={() => setStep('detail')}
        onContinue={() => { setPaying(false); setStep('list') }}
        onOrder={() => setPaying(true)}
        overlay={paying && (
          <PaySheet
            lines={[['상품 금액', product.price], ['배송비', product.shippingFee]]}
            total={total(product)}
            onDismiss={() => setPaying(false)}
            onPay={() => onPay(product)}
          />
        )}
      >
        {items.map(([n, v]) => (
          <CartRow key={n} art={MISSION_ART[product.guardianId]} name={n} option={product.store} price={v} />
        ))}
        {product.freeShippingLeft && (
          <FreeShipCard left={product.freeShippingLeft} ratio={product.price / (product.price + product.freeShippingLeft)} />
        )}
        <div className="t-caption" style={{ marginTop: 16 }}>{NOTICE}</div>
      </CartScreen>
    )
  }

  // 결제 완료 (그래도 살래)
  return (
    <OrderDone sub={`${product.store} · ${krw(total(product))} · 체험이라 실제로 결제되지 않았어요`}>
      <button className="bs-btn primary" onClick={() => onFakeNotice(product)}>카드 승인 알림 받아보기</button>
      <SecondaryButton onClick={() => setStep('list')}>쇼핑 계속하기</SecondaryButton>
    </OrderDone>
  )
}
