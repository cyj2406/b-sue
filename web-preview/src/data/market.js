// 지름마켓 상품 (전부 가상 브랜드) — 안드로이드 MarketProducts.kt 와 같은 내용
export const products = [
  { id: 'drink', guardianId: 'two_plus_one', store: '다모아편의점', name: '톡쏘는 탄산음료 500ml 2+1 (3병)',
    emoji: '🥤', tile: '#DDF3FF', price: 3600, original: 5400, badge: '2+1', rating: 4.7, reviews: 2841,
    shippingFee: 0, shippingText: '편의점 픽업 · 지금 바로', note: '하나 더 담으면 하나 더 공짜! (진짜 필요해?)' },
  { id: 'sneakers', guardianId: 'sale', store: '런웨이슈즈', name: '에어러너 데일리 러닝화',
    emoji: '👟', tile: '#FFE4DC', price: 38700, original: 129000, badge: '70% 세일', rating: 4.8, reviews: 12093,
    shippingFee: 0, shippingText: '무료배송 · 내일 도착', note: '⏰ 세일 종료까지 2시간!' },
  { id: 'chicken', guardianId: 'delivery_seal', store: '치킨나라', name: '야식 반반치킨 (후라이드+양념)',
    emoji: '🍗', tile: '#FFF0CC', price: 20000, badge: '야식 1위', rating: 4.9, reviews: 30412,
    shippingFee: 3000, shippingText: '배달 35~45분 · 배달팁 3,000원', note: '🌙 지금 주문 폭주 중' },
  { id: 'headphones', guardianId: 'cart_cleanup', store: '소리공방', name: '노이즈캔슬링 무선 헤드폰 X3',
    emoji: '🎧', tile: '#E6E1FF', price: 189000, original: 219000, badge: '장바구니 14일째', rating: 4.6, reviews: 5220,
    shippingFee: 0, shippingText: '무료배송 · 모레 도착', note: '🛒 장바구니에 담은 지 14일째예요' },
  { id: 'cart', guardianId: 'free_shipping', store: '꽉찬마트', name: '장바구니 상품 3개',
    emoji: '📦', tile: '#DFF5E8', price: 12000, badge: '무료배송까지 3,000원', rating: 4.5, reviews: 987,
    shippingFee: 3000, shippingText: '배송비 3,000원 (15,000원 이상 무료배송)',
    cartItems: [['키친타월 6롤', 4900], ['세제 리필 1L', 4600], ['그래놀라 300g', 2500]], freeShippingLeft: 3000 },
  { id: 'coffee_goods', guardianId: 'coffee_law', store: '달빛커피', name: '가을 시즌 한정 텀블러 473ml',
    emoji: '☕', tile: '#F3E6D8', price: 32000, badge: '시즌 한정', rating: 4.8, reviews: 7706,
    shippingFee: 0, shippingText: '무료배송 · 오늘까지 한정 판매', note: '✨ 이번 시즌 지나면 다시 안 나와요' },
]

export const total = (p) => p.price + p.shippingFee
export const discount = (p) => (p.original ? Math.floor(((p.original - p.price) * 100) / p.original) : null)
