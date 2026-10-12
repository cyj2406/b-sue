// ════════════════════════════════════════════════════════════════
//  전시 모드 시간 값 — 여기 숫자만 바꾸면 돼요 (단위: 초)
// ════════════════════════════════════════════════════════════════

// 무조작: 이만큼 안 만지면 "아직 있어?" → 그 뒤 이만큼 더 기다리면 대기 화면으로
export const IDLE_FIRST = 30
export const IDLE_SECOND = 15

// 체험 중 힌트: 이만큼 안 누르면 눌러야 할 버튼 바깥에 주황 테두리 (깜빡임 간격)
export const HINT_NUDGE = 5
export const HINT_BLINK = 1.2

// C 멤버십 화면: 이만큼 안 누르면 "해지 버튼은 작게 숨겨두는 법이야" + [구독 해지] 테두리
export const MEMBERSHIP_QUIET = 10

// 대기 화면 말풍선이 바뀌는 간격
export const ATTRACT_ROTATE = 8

// 엔딩 → 영수증 자동 (결과를 읽을 짧은 시간) · 영수증 → 대기 화면 자동
export const ENDING_WAIT = 4
export const RECEIPT_WAIT = 15
// 영수증 화면 자동 복귀 (숫자 없이 · 만지면 다시 · QR 시트가 열려 있으면 멈추고 닫으면 다시 20초부터)
export const RECEIPT_WAIT_QR = 20
// QR 시트가 열린 채 이만큼 안 만지면 대기 화면으로
export const RECEIPT_SHEET_IDLE = 30

// 처리 중 표시 (DESIGN.md '처리 중 표시') — 버튼 점 3개 → 기록 중 화면 → 체크 완료 → 엔딩
export const PRESS_LOADING = 0.3   // 누른 버튼 안 점 3개
export const PROCESS_MIN = 1.2     // 기록 중 화면 최소 (준비가 빨리 끝나도 이만큼은 보여줘요)
export const PROCESS_MAX = 3       // 기록 중 화면 최대 (준비가 덜 끝나도 다음으로)
export const PROCESS_DONE = 0.5    // 체크 완료
export const ENDING_FADE = 0.3     // 완료 → 엔딩 카드 페이드
