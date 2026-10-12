// ════════════════════════════════════════════════════════════════
//  블롭 비서 규칙표 (BLOB-COMPANION.md 를 그대로 옮긴 것)
//  숫자·대사를 바꾸고 싶으면 여기만 고치면 돼요
// ════════════════════════════════════════════════════════════════

export const TIMING = {
  bubbleMs: 4000,            // 말풍선이 떠 있는 시간
  bubbleCooldownMs: 20000,   // 저절로 뜨는 말풍선은 20초에 한 번까지 (블롭을 직접 누른 건 예외)
  centerCooldownMs: 180000,  // 가운데 개입은 3분에 한 번까지 (결제 출동 L1 은 예외)
  dozeAfterMs: 45000,        // 45초 동안 아무것도 안 하면 꾸벅꾸벅
  dwellMs: 5000,             // 상품을 5초 넘게 보면 말 걸기
  longPressMs: 500,          // 블롭 길게 누르기
  cartLimit: 30000,          // L4: 오늘 담은 금액이 이걸 넘으면 개입
  revisitCount: 3,           // L2: 같은 상품을 이 횟수째 열면 개입
  lateNight: [23, 2],        // L3: 밤 11시 ~ 새벽 2시
}

// ── 구석 말풍선 대사 (첫 줄 질문, 둘째 줄 작은 글씨) ──
export const LINES = {
  tapBlob: (saved, cups) => [`이번 달 ${saved.toLocaleString()}원 지켰어`, `커피 ${cups}잔이야`],
  restOn: ['알겠어, 오늘은 쉴게', '결제할 때만 나올게'],
  restOff: ['다시 옆에 있을게', '필요할 때 부를게'],
  marketEnter: ['구경만 하는 거지?', '결제 전에 나 부를게'],
  dwell: ['5초째 보는 중…', '진짜 필요한 거야?'],
  dwellSale: ['세일이라서 끌리는 거야?', '원래 값이어도 살까?'],
  cartAdd: (total) => ['담기만 하는 거 맞지?', `오늘 담은 거 ${total.toLocaleString()}원`],
  held: ['오늘 참은 거 적어뒀어', '영수증 탭에 있어'],
  bought: ['잘 쓰기로 약속!', '못 참아도 괜찮아'],
  farewell: (amount, merchant) => [`잘 가, ${amount.toLocaleString()}원…`, `${merchant}에서 잘 쓰고 와`],
  receiptOpen: ['우리가 지킨 거야', '한 줄씩 쌓이는 중'],
  lockedTap: ['아직 못 만난 요원이야', '그 순간이 오면 나타날 거야'],
}

// ── 가운데 개입 (L1~L5) ──
// L1 결제 출동은 Dispatch 화면이 따로 담당
export const CENTER = {
  L2: (p) => ({
    id: `L2:${p.id}`, scene: 'intervene', title: '벌써 세 번째\n보는 중이야', body: '그만큼 갖고 싶은 거지?',
    caption: `${p.name} · ${p.price.toLocaleString()}원`, secondary: '계속 볼래', primary: '오늘은 그만 볼래',
  }),
  L3: () => ({
    id: 'L3', scene: 'intervene', title: '이 시간 장바구니는\n내일 보면 달라', body: '밤에는 다 갖고 싶어지거든',
    caption: '밤 11시 ~ 새벽 2시엔 한 번 물어봐', secondary: '그래도 볼래', primary: '내일 볼래',
  }),
  L4: (total, cups) => ({
    id: 'L4', scene: 'intervenePause', title: '오늘 담은 게\n3만 원 넘었어', body: `${total.toLocaleString()}원 · 커피 ${cups}잔이야`,
    caption: '하나만 빼볼까?', secondary: '괜찮아', primary: '하나 빼볼래',
  }),
  L5: (g) => ({
    id: 'L5', scene: 'intervene', art: g.id, title: '오늘의 미션\n도착했어', body: g.question,
    caption: `${g.name} · ${g.mission}`, primary: '확인',
  }),
}

// 지금이 심야 시간인지
export function isLateNight(hour = new Date().getHours()) {
  const [from, to] = TIMING.lateNight
  return hour >= from || hour < to
}
