// ════════════════════════════════════════════════════════════════
//  전시 체험 대본 — 문구·금액·시간은 여기서만 고치면 돼요 (코드 안 건드려도 됨)
//  기획서: '비상수단 전시 체험 기획서 — 새벽 1시의 나'
//
//  문구 안의 {amount}, {cups} 는 실제 금액·커피 잔 수로 자동으로 바뀌어요.
//  guardian 값은 data/guardians.js 의 id (예: 'sale', 'free_shipping')
// ════════════════════════════════════════════════════════════════

export const SCENARIOS = {
  // ── A · 새벽 1시 쇼핑 (약 90초) ──
  a: {
    id: 'a',
    // 시작 화면 목록 한 칸: headline(제목) / action(주황 행동) — app 은 화면에 안 보여요 · tag·time 은 운영자 메뉴용
    card: { tag: 'A', time: '새벽 1시', headline: '새벽 1시 쇼핑', app: '쇼핑앱', action: '70% 러닝화 사기', line: '잠이 안 와서\n쇼핑앱을 열었다' },
    wallpaper: 'night',
    clock: { time: '01:12', date: '11월 14일 금요일' },
    // 1) 잠금화면에 1초 뒤 내려오는 알림
    opening: {
      app: '지름마켓',
      title: '잠 못 드는 밤, 러닝화 70% 할인',
      body: '오늘 특가 · 할인 종료까지 3시간',
      hint: '알림을 눌러봐',
    },
    // 2~4) 지름마켓: 상품 상세 → [장바구니](토스트) → 장바구니 → 비상페이 시트 · 또는 [바로 구매] → 비상페이 시트  (화면: exhibit/ShopFlow.jsx)
    product: {
      store: '런웨이슈즈', name: '데일리 러닝화', title: '데일리 러닝화 · 화이트', color: '화이트',
      price: 38700, original: 129000, timer: '02:59:59', timerTotal: 3 * 3600, rating: 4.8, reviews: 12093, viewers: 23,
      arrival: '내일(토) 도착', shippingFee: 3000, freeShipAt: 41700, // 무료배송 기준 41,700원 (2026-10-11 결정)
      size: 260, // 미리 골라진 사이즈 (상세의 옵션 줄 '화이트 / 260' · 2026-10-11 사이즈 칩 삭제)
    },
    upsell: { name: '러닝 양말 3켤레', price: 3900 },
    // 장바구니 무료배송 상자 안 '함께 사면 좋은 상품' (담아도 되고 안 담아도 돼요)
    upsells: [
      { id: 'socks', name: '러닝 양말 3켤레', price: 3900, art: 'free_shipping' }, // 2026-10-11: 양말 하나만 (무료배송 상자 안)
    ],
    address: { label: '집', line: '서울시 마포구 와우산로 94' }, // 가짜 배송지 (개인정보 아님)
    // 빼꼼: 4초 뒤 사라지고 아무것도 막지 않아요 (text: 굵은 줄, sub: 작은 줄)
    peeks: {
      detail: { text: '잠 안 와서 둘러보다 발견했구나', sub: '원래 사려던 신발이야?' },
      upsell: { text: '배송비 아끼려고 더 담는 중?', sub: '양말, 원래 사려던 거야?' },
    },
    // 4~6) 결제 → 출동
    pay: { app: '비상페이', card: '비상카드' },
    guardian: { default: 'sale', withUpsell: 'free_shipping' },
    amount: { default: 41700, withUpsell: 42600 }, // 러닝화+배송비 / 러닝화+양말(무료배송)
    // 7) 결과
    held: {
      skipTo: { time: '07:30', date: '11월 14일 금요일' },
      notification: { app: '비상수단', title: '어젯밤 {amount}원 지켰어', body: '커피 {cups}잔이야' },
    },
    bought: {
      done: '결제 완료',
      notification: { app: '비상카드', title: '[비상카드] 승인 {amount}원', body: '지름마켓 · 일시불' },
      farewell: '그래, 대신 잘 신기!',
    },
    // 8) 엔딩
    ending: {
      savedTitle: '오늘 밤, {amount}원을 지켰어',
      pausedTitle: '오늘 밤, 딱 3초 멈췄어',
      itemName: '한정판 러닝화',
      boardCaption: '이 요원은 보드판 쇼핑앱 칸에 있어', // 엔딩 카드 아래 작은 글씨 → 보드판으로
    },
  },

  // ── B · 저녁 7시 배달 (약 70초) — 화면: exhibit/apps/Baegopa.jsx ──
  b: {
    id: 'b',
    card: { tag: 'B', time: '저녁 7시', headline: '퇴근길 배달', app: '배달앱', action: '치킨 시키기', line: '퇴근길,\n배달앱 쿠폰이 왔다' },
    wallpaper: 'evening',
    clock: { time: '19:02', date: '11월 14일 금요일' },
    opening: {
      app: '배고파',
      title: '퇴근길 배고프죠? 치킨 3,000원 쿠폰 도착',
      body: '오늘 밤 12시까지',
      hint: '알림을 눌러봐',
    },
    // 2~5) 배고파: 가게 → 옵션 시트 → (떠 있는 장바구니 바) → 장바구니 → 비상페이 시트  (화면: exhibit/apps/Baegopa.jsx)
    store: { name: '치킨나라', rating: 4.9, reviews: 8412, eta: '배달 25~35분', minOrder: 25000, tipRange: '배달팁 0~3,000원', tip: 3000, coupon: 3000 },
    // 쿠폰은 배너를 누르든 안 누르든 장바구니에서 자동 적용 (2026-10-11 결정)
    menus: [
      { id: 'fried', name: '후라이드 치킨', price: 21000, desc: '바삭한 한 마리', art: 'delivery_seal' },
      { id: 'yangnyeom', name: '양념 치킨', price: 22000, desc: '달콤 매콤 양념', art: 'fridge' },
      { id: 'half', name: '반반 치킨', price: 22000, desc: '후라이드 반 + 양념 반', art: 'two_plus_one' },
    ],
    options: {
      bone: [{ id: 'bone', name: '뼈', price: 0 }, { id: 'boneless', name: '순살', price: 2000 }], // 필수 — '뼈'를 미리 골라 둬요
      drinks: [{ id: 'cola', name: '콜라 1.25L', price: 2000 }, { id: 'cider', name: '사이다 1.25L', price: 2000 }], // 선택
    },
    sides: [
      { id: 'cola', name: '콜라 1.25L', price: 2000, art: 'free_shipping' },
      { id: 'cheeseball', name: '치즈볼 5개', price: 4000, art: 'two_plus_one' },
    ],
    upsell: { name: '치즈볼 5개', price: 4000, banner: '최소주문까지 {left}원' },
    product: { name: '후라이드 치킨', price: 21000, minOrder: 25000 },
    // 3) 메뉴를 처음 담으면 앱 위로 내려오는 비상수단 알림
    fridgeNotice: {
      app: '비상수단',
      title: '냉장고 먼저 열어봤어?',
      body: '이번 주 배달 3번째야. 집에 있는 걸로 한 끼 어때?',
    },
    peeks: {
      detail: '오늘도 시켜먹을래?',
      upsell: '최소금액 맞추려고 더 사는 중?',
    },
    pay: { app: '비상페이', card: '비상카드' },
    guardian: { default: 'delivery_seal', withUpsell: 'delivery_seal', bonus: 'fridge' },
    amount: { default: 21000, withUpsell: 25000 },
    held: {
      skipTo: { time: '21:30', date: '11월 14일 금요일' },
      notification: { app: '비상수단', title: '오늘 저녁은 냉장고 파티', body: '{amount}원 지켰어' },
    },
    bought: {
      done: '주문 완료 · 25분 후 도착',
      notification: { app: '비상카드', title: '[비상카드] 승인 {amount}원', body: '배고파 · 일시불' },
      farewell: '맛있게 먹어! 냉장고 두부는 내일 꼭',
    },
    ending: {
      savedTitle: '오늘 저녁, {amount}원을 지켰어',
      pausedTitle: '오늘 저녁, 딱 3초 멈췄어',
      itemName: '치킨 배달',
      boardCaption: '보드판 배달앱 칸에 있어',
      bonusCaption: '냉장고 소탕 요원도 함께 합류했어', // 냉장고 알림이 떴으면
    },
  },

  // ── C · 구독 결제 전날 (약 60초) — 화면: exhibit/apps/StreamPlus.jsx. 출동 대신 '해지할지 3초' ──
  c: {
    id: 'c',
    card: { tag: 'C', time: '결제일 전날', headline: '결제일 전날', app: '알림', action: '빠질 돈 확인하기', line: '내일 돈이\n빠져나간다는 알림' },
    wallpaper: 'dusk',
    clock: { time: '22:40', date: '11월 14일 금요일' },
    opening: {
      app: '비상수단',
      title: '내일 스트림플러스 17,000원이 나가',
      body: '마지막으로 본 건 41일 전이야',
      hint: '알림을 눌러봐',
    },
    // 2) 구독 상세 (비상수단 앱) → 3) 3초 뒤 [계속 쓸래] / [해지하러 가기]
    subscription: { name: '스트림플러스', price: 17000, lastUsedDays: 41, plan: '프리미엄', nextPay: '11월 15일' },
    // 4a) 가짜 스트림플러스 멤버십 화면 — 만류 문구 (실제 서비스 흉내 아님)
    // 멤버십 관리 → (작은 링크) 멤버십 해지 → 만류 화면 → 해지 완료  (화면: exhibit/apps/StreamPlus.jsx)
    membership: {
      card: '비상카드 **** 0303',
      perks: ['4K 화질로 보기', '동시 시청 4명', '오프라인 저장', '광고 없이 보기'],
      leaveTitle: '정말 떠나시겠어요?',
      leaveSub: '해지하면 다음 결제일부터 아래 혜택을 쓸 수 없어요',
      stayOffer: '3개월 50% 할인 받고 계속 보기', // 누르면 '계속 쓸래'와 같게 기록 (2026-10-11 결정)
      leaveGo: '해지 계속하기',
      done: '해지 완료',
      doneSub: '다음 결제일부터 청구되지 않아요',
    },
    guardian: { default: 'ghost_sub' },
    amount: { default: 17000 },
    held: { cheer: '매달 17,000원 새는 구멍 막았다!' },  // 해지하러 가기
    bought: { farewell: '좋아, 그럼 이번 달에는 꼭 보기!' }, // 계속 쓸래
    ending: {
      savedTitle: '오늘 밤, 매달 {amount}원 구멍을 막았어',
      pausedTitle: '오늘 밤, 딱 3초 멈췄어',
      itemName: '스트림플러스 구독',
      boardCaption: '보드판 침대·밤 칸에 있어',
    },
  },
}

export const SCENARIO_ORDER = ['a', 'b', 'c']

// 지금 만들어진 시나리오 (E2 A · E6 B·C) — false 로 바꾸면 시작 화면에 '준비 중'
export const READY = { a: true, b: true, c: true }

// '{amount}원' 같은 자리에 값 채우기
export function fill(text, vars = {}) {
  return text.replace(/\{(\w+)\}/g, (_, key) => {
    const v = vars[key]
    if (v == null) return `{${key}}`
    return typeof v === 'number' ? v.toLocaleString('ko-KR') : String(v)
  })
}

// 커피 잔 수 (4,500원 기준)
export const coffee = (amount) => Math.round((amount / 4500) * 10) / 10
