// 12 수호신 — 안드로이드 Guardian.kt 와 같은 내용
// 카드 색은 카드 이미지(public/cards)의 바탕색에서 뽑았어요.

export const guardians = [
  // ── 충동 ──
  { id: 'zero_day', number: 1, type: '충동', name: '하루 0원 챌린지', question: '오늘 꼭 써야 돼?',
    trigger: '아침 데일리 미션', mission: '오늘 하루 0원으로 버텨보기. 못 해도 괜찮아, 내일 또 하면 돼!', color: '#365DB2' },
  { id: 'two_plus_one', number: 2, type: '충동', name: '2+1의 함정', question: '하나 더 진짜 필요해?',
    trigger: '수량 2개 이상 · 편의점 결제', mission: '묶음 할인이 보이면 딱 3초, 진짜 쓸 개수만 담아보기', color: '#F09522' },
  { id: 'sale', number: 3, type: '충동', name: '세일이라서 샀다', question: '세일 아니어도 살 거야?',
    trigger: '결제화면에 할인 · 세일 · %', mission: "'세일' 글자를 손으로 가리고, 원래 값이어도 살지 생각해보기", color: '#3F949E' },
  { id: 'cart_cleanup', number: 4, type: '충동', name: '장바구니 대청소', question: '담아만 둔 지 며칠째야?',
    trigger: '장바구니 화면', mission: '장바구니에서 일주일 넘게 담아둔 것 하나만 빼보기', color: '#EB6641' },
  { id: 'delivery_seal', number: 5, type: '충동', name: '배달앱 봉인', question: '오늘도 시켜먹을래?',
    trigger: '배민 · 쿠팡이츠 · 요기요 실행', mission: '오늘 한 끼는 배달 말고 다른 방법으로 해결해보기', color: '#CC3042' },
  { id: 'coffee_law', number: 6, type: '충동', name: '커피 몇 잔의 법칙', question: '이거 커피 몇 잔이야?',
    trigger: '모든 결제 직전 금액 환산', mission: '결제 전에 금액을 커피 잔 수로 바꿔서 말해보기', color: '#664533' },
  // ── 누수 ──
  { id: 'ghost_sub', number: 7, type: '누수', name: '유령 구독 색출', question: '마지막에 쓴 게 언제야?',
    trigger: '구독 결제일 전날', mission: '구독 목록을 열고 한 달 넘게 안 쓴 것 찾아보기', color: '#4B3A96' },
  { id: 'auto_pay', number: 8, type: '누수', name: '자동결제 잠복수사', question: '나도 모르게 빠져나가는 중',
    trigger: '같은 결제처 매달 반복', mission: '이번 달 카드 내역에서 매달 나가는 것에 표시해보기', color: '#654DA9' },
  { id: 'free_shipping', number: 9, type: '누수', name: '무료배송의 배신', question: '배송비 아끼려 더 샀지?',
    trigger: "'무료배송까지' 문구", mission: '무료배송 맞추려고 담은 것, 빼도 괜찮은지 다시 보기', color: '#4DAB49' },
  { id: 'closet', number: 10, type: '누수', name: '옷장 대방출', question: '작년에 산 거 입긴 했어?',
    trigger: '무신사 · 지그재그 · 29CM 실행', mission: '옷장에서 1년 동안 안 입은 옷 하나 꺼내보기', color: '#7967B6' },
  { id: 'fridge', number: 11, type: '누수', name: '냉장고 소탕', question: '냉장고에 또 사뒀지?',
    trigger: '컬리 등 장보기앱 · 저녁 배달시간', mission: '장보기 전에 냉장고 안을 사진 한 장 찍어보기', color: '#CF2E64' },
  { id: 'taxi', number: 12, type: '누수', name: '만원짜리 5분', question: '택시비 5분 아끼는 값이야?',
    trigger: '카카오T 등 택시앱 실행', mission: '택시 부르기 전에 대중교통 도착 시간 한 번 비교해보기', color: '#52BB9F' },
]

export const byId = (id) => guardians.find((g) => g.id === id)


// 오늘의 미션카드 (날짜마다 정해진 한 장)
export function todaysGuardian(date = new Date()) {
  const day = Math.floor(date.getTime() / 86400000)
  return guardians[day % guardians.length]
}

export const COFFEE_PRICE = 4500
export const won = (n) => '₩' + n.toLocaleString('ko-KR')
export function cups(n) {
  const c = n / COFFEE_PRICE
  if (c >= 10) return String(Math.floor(c))
  const r = Math.round(c * 10) / 10
  return Number.isInteger(r) ? String(r) : r.toFixed(1)
}
