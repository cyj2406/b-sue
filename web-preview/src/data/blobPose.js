// ════════════════════════════════════════════════════════════════
//  블롭 그림 ↔ 앱 상태 (v4 그림, 2026-10-06)
//  그림 교체: public/blob-v4/ 에 같은 이름 파일 덮어쓰기
//  어느 화면에 어느 그림: 아래 SCENES 표만 고치기
// ════════════════════════════════════════════════════════════════

const v4 = (name) => `/blob-v4/${name}`

// 감정 포즈 — v4에는 '잠깐!'과 '호기심' 두 장뿐이라, 환호·괜찮아는 움직임으로 표현 (그림 오면 교체)
export const POSES = {
  curious: v4('pose_curious.webp'), // 오른쪽으로 기운 호기심 탐정 — 평소·빼꼼
  stop: v4('pose_stop.webp'),       // 손을 든 탐정 — 출동
  idle: v4('pose_curious.webp'),
  peek: v4('pose_curious.webp'),
  cheer: v4('pose_curious.webp'),   // TODO 환호 포즈 받으면 교체
  pat: v4('pose_curious.webp'),     // TODO 괜찮아 포즈 받으면 교체
}

// 움직임 (Blob.jsx 가 그려요)
export const MOTIONS = {
  none: '정지',
  breathe: '숨쉬기 + 가끔 통통',
  sneak: '살금살금 (두리번)',
  stopEnter: '등장 squash & stretch',
  countPress: '숫자마다 눌렸다 펴짐',
  cheerJump: '점프 2번 + 반짝',
  patSway: '좌우로 천천히 기울기',
  doze: '꾸벅꾸벅 졸기',
}

// ★ 상태 → 그림·움직임 표
export const SCENES = {
  corner:         { label: '구석 비서 (평소)',             image: POSES.curious, motion: 'breathe' },
  cornerScroll:   { label: '구석 비서 (스크롤 두리번)',      image: POSES.curious, motion: 'sneak' },
  cornerDoze:     { label: '구석 비서 (45초 무조작 졸기)',    image: POSES.curious, motion: 'doze' },
  cornerCheer:    { label: '구석 비서 (참았다 뒤)',         image: POSES.cheer,   motion: 'cheerJump' },
  cornerPat:      { label: '구석 비서 (그래도 살래 · 배웅)',  image: POSES.pat,     motion: 'patSway' },
  peek:           { label: '빼꼼 (쇼핑 진입)',             image: POSES.peek,    motion: 'sneak' },
  intervene:      { label: '가운데 개입 (다시 보기·심야)',    image: POSES.curious, motion: 'stopEnter' },
  intervenePause: { label: '가운데 개입 (장바구니 3만 원)',   image: POSES.stop,    motion: 'stopEnter' },
  dispatch:       { label: '출동 등장 + 3초 카운트',        image: POSES.stop,    motion: 'countPress' },
  held:           { label: '[참았다!]',                   image: POSES.cheer,   motion: 'cheerJump' },
  bought:         { label: '[그래도 살래]',                image: POSES.pat,     motion: 'patSway' },
  unlock:         { label: '수호신 해금',                  image: POSES.cheer,   motion: 'cheerJump' },
  empty:          { label: '빈 화면',                     image: POSES.curious, motion: 'breathe' },
}

// 수호신 → 미션 포즈 (카드·썸네일)
export const MISSION_ART = {
  zero_day: v4('mission_zero_day.svg'),
  two_plus_one: v4('mission_two_plus_one.svg'),
  sale: v4('mission_sale.svg'),
  cart_cleanup: v4('mission_cart_cleanup.svg'),
  delivery_seal: v4('pose_stop.webp'), // TODO 배달앱 봉인 미션 포즈가 SVG 폴더에 없어서 임시
  coffee_law: v4('mission_coffee_law.svg'),
  ghost_sub: v4('mission_ghost_sub.svg'),
  auto_pay: v4('mission_auto_pay.svg'),
  free_shipping: v4('mission_free_shipping.svg'),
  closet: v4('mission_closet.svg'),
  fridge: v4('mission_fridge.svg'),
  taxi: v4('mission_taxi.svg'),
}
export const missionPeekArt = (id) => MISSION_ART[id]

// 테스트 화면용 전체 그림
export const ALL_IMAGES = [
  'pose_curious.webp', 'pose_stop.webp',
  'mission_zero_day.svg', 'mission_two_plus_one.svg', 'mission_sale.svg', 'mission_cart_cleanup.svg',
  'mission_coffee_law.svg', 'mission_ghost_sub.svg', 'mission_auto_pay.svg', 'mission_free_shipping.svg',
  'mission_closet.svg', 'mission_closet_alt.svg', 'mission_fridge.svg', 'mission_taxi.svg',
].map((name) => ({ name: name.replace(/\.(svg|webp)$/, ''), src: v4(name) }))

export const ASPECT = 1 // 그림마다 비율이 달라서 정사각 칸 + 아래 가운데 맞춤
