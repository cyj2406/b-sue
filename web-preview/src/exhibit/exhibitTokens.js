// ════════════════════════════════════════════════════════════════
//  전시 모드 화면 규칙 — 토큰 (DESIGN.md 11장)
//  값은 여기 한 곳에서만 바꿔요. 전시 화면 CSS(styles/exhibit.css)는 아래 EX_CSS_VARS(--ex-*)를 쓰고,
//  JS(힌트 층 등)는 EX 를 직접 읽어요. 색은 UI 키트(bsue-tokens.css) 변수만.
// ════════════════════════════════════════════════════════════════
export const EX = {
  // 간격 (4의 배수만)
  space: [4, 8, 12, 16, 20, 24, 32],
  side: 20,          // 화면 좌우 여백 (헤더 포함)
  cardPad: 24,       // 카드 안쪽
  cardGap: 12,       // 카드 사이
  cardRadius: 20,

  // 하단 버튼 영역
  ctaTop: 12,
  ctaSide: 20,
  ctaBottom: 16,     // + 홈 바 영역(safe-area-inset-bottom)
  ctaBottomMin: 34,  // 최소
  btnHeight: 56,
  btnRadius: 16,
  btnGap: 8,         // 두 개일 때 사이 (보조 1 : 주 2)
  secondaryOnGrey: { background: 'var(--grey-200)', color: 'var(--grey-700)' }, // #E5E8EB — 흰 버튼 금지
  // 버튼 종류 3가지 (주 · 보조 · 텍스트) — 모두 높이 56 · 폭 꽉 · 둥글기 16
  textButton: { size: 16, weight: 600, color: 'var(--grey-600)' }, // 채움 없음, 가운데, 누르는 영역 56 전체
  btnStackGap: 8,    // 위아래로 쌓을 때 (주 버튼이 위)

  // 읽기 · 누르기
  touchMin: 48,
  fontMin: 13,
  textOnWhite: 'var(--grey-600)',  // 흰 바탕 회색 글자 (대비 4.6)
  textOnGrey: 'var(--grey-700)',   // 회색 바탕 회색 글자 (대비 6.6)
  orangeText: 'var(--orange-600)', // 흰 바탕 주황 글자 (대비 5.3)

  // 힌트 · 테두리
  hintGap: 12,       // 하단 버튼 바로 위
  ringOffset: 4,     // 버튼에서 밖으로
  ringRadiusPlus: 4, // 둥글기 = 버튼 + 4

  // 하단 시트
  sheetDim: 'rgba(0, 0, 0, 0.55)', // 검정 55% (키트 덮개와 다른 전시 전용 값 — 2026-10-11 지정)
  sheetRadius: 28,
  sheetPad: 24,
  sheetUp: 0.25,     // 올라오는 시간(초)

  // 영수증
  receiptPaper: '#EEEDE8', // 종이색 (전시폰 · /r · 저장 이미지)
  receiptGap: 24,          // 공간보다 길 때 위 여백 · 하단 버튼과 최소 간격
  receiptPrint: 0.8,       // 프린터처럼 출력되는 시간(초)
  qrSheetSize: 190,        // QR 시트 안 QR

  // 가짜 앱 (지름마켓 · 배고파 · 스트림플러스 · 비상페이) — 새 강조색 없이, 할인 표시만 빨강
  saleRed: '#F04452',
}

// 하단 여백: 홈 바 + 16, 최소 34
export const CTA_BOTTOM = `max(${EX.ctaBottomMin}px, calc(env(safe-area-inset-bottom, 0px) + ${EX.ctaBottom}px))`

// 전시 화면 맨 바깥(.ex-root)에 붙이는 CSS 변수
export const EX_CSS_VARS = {
  '--ex-side': `${EX.side}px`,
  '--ex-card-pad': `${EX.cardPad}px`,
  '--ex-card-gap': `${EX.cardGap}px`,
  '--ex-card-radius': `${EX.cardRadius}px`,
  '--ex-cta-top': `${EX.ctaTop}px`,
  '--ex-cta-side': `${EX.ctaSide}px`,
  '--ex-cta-bottom': CTA_BOTTOM,
  '--ex-btn-h': `${EX.btnHeight}px`,
  '--ex-btn-radius': `${EX.btnRadius}px`,
  '--ex-btn-gap': `${EX.btnGap}px`,
  '--ex-touch': `${EX.touchMin}px`,
  '--ex-text-white': EX.textOnWhite,
  '--ex-text-grey': EX.textOnGrey,
  '--ex-orange-text': EX.orangeText,
  '--ex-secondary-bg': EX.secondaryOnGrey.background,
  '--ex-secondary-text': EX.secondaryOnGrey.color,
  '--ex-text-btn-size': `${EX.textButton.size}px`,
  '--ex-text-btn-weight': EX.textButton.weight,
  '--ex-text-btn-color': EX.textButton.color,
  '--ex-stack-gap': `${EX.btnStackGap}px`,
  '--ex-sheet-dim': EX.sheetDim,
  '--ex-sheet-radius': `${EX.sheetRadius}px`,
  '--ex-sheet-pad': `${EX.sheetPad}px`,
  '--ex-receipt-gap': `${EX.receiptGap}px`,
  '--ex-receipt-print': `${EX.receiptPrint}s`,
  '--ex-sale-red': EX.saleRed,
}
