import { isSaved } from './result'

// ════════════════════════════════════════════════════════════════
//  영수증 문구 — 전시폰(ReceiptPaper) · /r 페이지 · 저장 이미지(receiptImage) 셋이 모두 여기서 읽어요
//  (한쪽만 고쳐서 서로 달라지는 일이 없게 · 2026-10-18 정한 그대로)
//  '쓴 돈'이라는 말은 쓰지 않아요.
// ════════════════════════════════════════════════════════════════
export const RECEIPT_TITLE = '비상수단'
export const RECEIPT_SUBTITLE = '안 지른 것들의 계산서'
export const RECEIPT_TAIL = '결제되지 않은 영수증 · 사기 전에, 딱 3초'

// 결과 줄: saved → "지킨 돈 : N원" / "= 커피 N잔" · paused → "멈춘 시간 : 3초" / "그래도 딱 3초 멈췄어"
export function receiptResultLines(rc) {
  const won = rc.amount.toLocaleString('ko-KR')
  return isSaved(rc.result)
    ? { label: '지킨 돈 :', value: `${won}원`, note: `= 커피 ${rc.cups}잔` }
    : { label: '멈춘 시간 :', value: '3초', note: '그래도 딱 3초 멈췄어' }
}

// 캐릭터 한마디 — 포즈와 짝 (포즈 9개는 attractLines.js · 영수증마다 랜덤 하나)
export const RECEIPT_QUOTES = {
  pose_sale: '세일은 또 와. 진짜로.',
  pose_cart: '장바구니는 도망 안 가.',
  pose_delivery_lock: '배달 전에 냉장고부터!',
  pose_two_plus_one: '하나면 충분할 때도 있어.',
  pose_free_shipping: '배송비보다 더 쓴 건 아니지?',
  pose_closet: '옷장에 비슷한 거 있지?',
  pose_fridge: '오늘 저녁은 냉장고 파티.',
  pose_autopay: '안 보는 구독, 하나만 정리해볼까?',
  pose_walk: '5분 걸으면 택시비가 남아.',
}
export const receiptQuote = (pose) => RECEIPT_QUOTES[pose] ?? RECEIPT_QUOTES.pose_sale
