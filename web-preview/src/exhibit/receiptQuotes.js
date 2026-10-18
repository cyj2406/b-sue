import { isSaved } from './result'

// ════════════════════════════════════════════════════════════════
//  영수증 문구 · 캐릭터 — 전시폰(ReceiptPaper) · /r 페이지 · 저장 이미지(receiptImage) 셋이 모두 receiptView() 하나만 읽어요
//  (한쪽만 고쳐서 서로 달라지는 일이 없게 · 2026-10-18 정한 그대로)
//  결과(saved 참음 / paused 못 참음)와 상황(a·b·c)에 따라 제목 · 내용 · 대사 · 푸터가 한 이야기로 맞게
//  '쓴 돈'이라는 말 · 고물가 같은 시장 문구는 쓰지 않아요.
// ════════════════════════════════════════════════════════════════
export const RECEIPT_TITLE = '비상수단'

// 결과마다 공통
const BY_RESULT = {
  saved: {
    subtitle: '안 지른 것들의 계산서',
    sign: '+',
    tail: '결제되지 않은 영수증 · 사기 전에, 딱 3초',
  },
  paused: {
    subtitle: '못 참은 것들의 기록',
    sign: '-',
    note: '3초 멈췄으면, 반은 이긴 거야',
    tail: '결제 완료 · 사기 전에, 딱 3초',
  },
}

// 상황마다: 상품 아래 한 줄 · 캐릭터 한마디 (saved / paused)
const BY_SCENARIO = {
  a: { // 새벽 러닝화
    item: { saved: '↳ 3초 멈추고 안 샀어', paused: '↳ 그래도 3초는 고민했어' },
    quote: { saved: '새벽 쇼핑 이겼다! 이제 자러 가.', paused: '잘 신으면 돼. 다음 판 있잖아.' },
  },
  b: { // 치킨 배달
    item: { saved: '↳ 3초 멈추고 안 샀어', paused: '↳ 그래도 3초는 고민했어' },
    quote: { saved: '야식 이겼다! 냉장고 열어보자.', paused: '못 참아도 괜찮아. 다음 판 있잖아.' },
  },
  c: { // 구독
    item: { saved: '↳ 3초 멈추고 해지했어', paused: '↳ 그래도 3초는 고민했어' },
    quote: { saved: '매달 새던 구멍 막았다!', paused: '이번 달엔 꼭 챙겨보자.' },
  },
}

// 영수증 캐릭터: 상황마다 고정 포즈 (src/assets/attract/ 그림 이름)
export const RECEIPT_POSE = { a: 'pose_sale', b: 'pose_delivery_lock', c: 'pose_autopay' }
// 알람시계 포즈 그림이 생기면 여기에 넣어요 → 못 참음(paused) 영수증에만 바뀌어요. 예: { a: 'pose_alarm', b: 'pose_alarm', c: 'pose_alarm' }
export const RECEIPT_POSE_PAUSED = {}

// 영수증 한 장에 보일 글자 · 그림 (receipt: { scenario, result, amount, cups, ... })
export function receiptView(rc) {
  const key = isSaved(rc.result) ? 'saved' : 'paused'
  const r = BY_RESULT[key]
  const sc = BY_SCENARIO[rc.scenario] ?? BY_SCENARIO.a
  const won = rc.amount.toLocaleString('ko-KR')
  return {
    title: RECEIPT_TITLE,
    subtitle: r.subtitle,
    itemAmount: `${r.sign}${won}`,
    itemNote: sc.item[key],
    label: key === 'saved' ? '지킨 돈 :' : '멈춘 시간 :',
    value: key === 'saved' ? `${won}원` : '3초',
    note: key === 'saved' ? `= 커피 ${rc.cups}잔` : r.note, // 커피 잔 수는 금액 ÷ 4,500 (scenarios.js coffee)
    quote: sc.quote[key],
    pose: (key === 'paused' && RECEIPT_POSE_PAUSED[rc.scenario]) || RECEIPT_POSE[rc.scenario] || RECEIPT_POSE.a,
    tail: r.tail,
  }
}

// 미리 불러올 영수증 그림 (preload.js)
export const RECEIPT_POSES = [...new Set([...Object.values(RECEIPT_POSE), ...Object.values(RECEIPT_POSE_PAUSED)])]
