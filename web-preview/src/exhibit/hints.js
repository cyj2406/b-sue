import { HINT_NUDGE, MEMBERSHIP_QUIET } from './timing'

// ════════════════════════════════════════════════════════════════
//  체험 중 힌트 표 — 문구 · 단계 알아보는 기준 · 깜빡일 버튼을 여기 한곳에
//  (가짜 앱 코드는 안 건드려요. HintLayer 가 0.3초마다 화면을 살펴서 지금 단계를 찾아요)
//
//  한 줄 = 한 단계
//   text   : 하단 버튼 바로 위 12px 회색 한 줄 — 단계 숫자 없이 문구만 (2026-10-11)
//            바텀시트(결제 · 옵션)가 열려 있으면 시트 바깥 위쪽 어두운 곳, 시트 위끝에서 12px 위에 흰 글자
//            '아래 · 위' 같은 방향 말 금지 — 누를 버튼 이름으로 (DESIGN.md 11장)
//   선택 장면(그래도 살래/참았다 · 계속 쓸래/해지하러 가기)은 표에 넣지 않아요 — 힌트 · 테두리 없음
//   when   : 이 단계인지 알아보는 기준 — 모두 맞아야 해요
//            phase : 'lock-opening'(첫 잠금화면) | 'shop'(가짜 앱 화면)
//            has   : 화면에 이게 있어야 함 (CSS 선택자)
//            not   : 화면에 이게 없어야 함
//   target : 깜빡일 버튼 (CSS 선택자 — 버튼 글자가 바뀌어도 안 깨지게 모양으로 찾아요)
//   after  : 몇 초 안 누르면 테두리 (기본 HINT_NUDGE)
//   quiet  : true 면 after 초가 지나야 글자도 테두리도 나와요
//  아래쪽 단계부터 먼저 맞춰봐요 (결제 시트가 주문서 위에 떠 있을 때 시트 쪽이 이기게)
// ════════════════════════════════════════════════════════════════

// 화면 · 버튼 찾는 기준 — 가짜 앱에 붙인 이름표(data-fx · data-hint-target)로 찾아요
//  버튼 글자나 화면 제목이 바뀌어도 안 깨져요 (이름표는 ShopFlow.jsx · apps/*.jsx)
const T = (name) => `[data-hint-target="${name}"]`
const notification = '.ex-noti'                                     // 잠금화면 알림 배너

export const HINTS = {
  // A 지름마켓: 알림 → 장바구니 → 장바구니로 → 구매하기 → 결제하기(시트)  (2026-10-11 주문서 · 사이즈 칩 없음)
  //  [바로 구매]를 누르면 결제창이 바로 떠요 → 맨 아래 '결제하기' 단계가 이겨서 가운데 단계는 건너뛰어요
  a: [
    { text: '알림을 눌러봐', when: { phase: 'lock-opening', has: notification }, target: notification },
    { text: '장바구니에 담아봐', when: { phase: 'shop', has: '[data-fx="a-detail"]:not([data-added])' }, target: T('add-cart') },
    { text: '장바구니로 가봐', when: { phase: 'shop', has: '[data-fx="a-detail"][data-added]', not: '.fx-toast' }, target: T('cart-icon') },
    { text: '구매하기를 눌러봐', when: { phase: 'shop', has: '[data-fx="a-cart"] [data-hint-target="buy"]', not: '.bs-sheet' }, target: T('buy') },
    { text: '결제하기를 눌러봐', when: { phase: 'shop', has: '[data-fx="pay-sheet"]' }, target: T('pay-sheet') },
  ],
  // B 배고파: 알림 → 메뉴 → 담기(옵션 시트) → 장바구니 보기 → 주문하기 → 결제하기(시트)
  b: [
    { text: '알림을 눌러봐', when: { phase: 'lock-opening', has: notification }, target: notification },
    { text: '메뉴를 골라봐', when: { phase: 'shop', has: '[data-fx="b-store"]:not([data-added]):not([data-sheet])' }, target: T('menu-first') },
    { text: '담기를 눌러봐', when: { phase: 'shop', has: '[data-fx="b-option"]' }, target: T('option-add') },
    { text: '장바구니 보기를 눌러봐', when: { phase: 'shop', has: '[data-fx="b-store"][data-added]:not([data-sheet])' }, target: T('cart-bar') },
    { text: '주문하기를 눌러봐', when: { phase: 'shop', has: '[data-fx="b-cart"] [data-hint-target="b-order"]', not: '.bs-sheet' }, target: T('b-order') }, // 빈 장바구니면 없음
    { text: '결제하기를 눌러봐', when: { phase: 'shop', has: '[data-fx="pay-sheet"]' }, target: T('pay-sheet') },
  ],
  // C: 알림만 — '곧 빠져나갈 돈' · '정말 떠나시겠어요?'는 선택 장면이라 힌트 없음
  c: [
    { text: '알림을 눌러봐', when: { phase: 'lock-opening', has: notification }, target: notification },
    // 멤버십 관리: 평소엔 힌트 없음 → 10초 안 누르면 한 줄 + "멤버십 해지" 링크 테두리
    { text: '해지 버튼은 작게 숨겨두는 법이야', when: { phase: 'shop', has: '[data-fx="c-membership"]' }, target: T('leave-link'), quiet: true, after: MEMBERSHIP_QUIET },
  ],
}

// 토스트("장바구니에 담았어요 [보기]")가 떠 있는 동안은 힌트 글자를 숨겨요 (같은 자리라서)
export const TOAST_ON = '.fx-toast'

// 블롭이 나와 있는 동안엔 힌트를 숨겨요 (빼꼼 · 출동/선택의 말풍선 · 배고파 냉장고 알림)
export const BLOB_ON = '.ex-peek, .ex-scene .bs-bubble, .ex-scene .ex-noti.floating'

export const DEFAULT_AFTER = HINT_NUDGE
