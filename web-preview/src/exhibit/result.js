import { SCENARIOS, coffee, fill } from './scenarios'

// ════════════════════════════════════════════════════════════════
//  체험 결과 — 한 곳에서만 정해요 (2026-10-18 '결과 처리 단일화')
//   'saved'  : 참았다! (A·B 출동) · 해지 완료 (C)          → 지킨 돈 · 카운터에 금액 더함
//   'paused' : 그래도 살래 (A·B 출동) · 계속 쓸래 · 할인 받고 계속 보기 (C) → 참여 인원만 +1
//  선택 버튼이 넘긴 값은 decideResult() 한 번만 거쳐요.
//  엔딩 · 처리 중 문구 · 영수증 · QR(r=) · /r · 카운터는 모두 이 값(또는 isSaved)만 읽어요.
// ════════════════════════════════════════════════════════════════
export const SAVED = 'saved'
export const PAUSED = 'paused'

// 선택 버튼 값 → 결과. 출동 화면(Dispatch)은 앱과 같이 쓰는 부품이라 'held' | 'bought' 를 넘겨요.
const CHOICES = { saved: SAVED, held: SAVED, paused: PAUSED, bought: PAUSED }
export function decideResult(choice) {
  const r = CHOICES[choice]
  if (!r) throw new Error(`[전시] 모르는 선택 값: ${choice}`)
  return r
}

// 저장된 기록 · 예전 QR 값까지 읽기 ('held' 는 2026-10-18 이전 기록)
export const isSaved = (result) => result === SAVED || result === 'held'

// QR 주소의 r= 한 글자 (s 지킴 · p 멈춤) · 예전 QR 의 h / b 도 읽어요
export const resultToParam = (result) => (isSaved(result) ? 's' : 'p')
export const resultFromParam = (v) => ({ s: SAVED, h: SAVED, p: PAUSED, b: PAUSED })[v] ?? null

// 엔딩 화면 위 두 줄 (문구는 scenarios.js ending)
export function endingText(scenarioId, result, amount) {
  const e = SCENARIOS[scenarioId].ending
  const vars = { amount, cups: coffee(amount) }
  return isSaved(result)
    ? { sub: '새 요원이 합류했어', title: fill(e.savedTitle, vars) }
    : { sub: '그래도 3초 멈췄잖아', title: fill(e.pausedTitle, vars) }
}

// 기록 중 화면 한 줄
export const processingText = (result) => (isSaved(result) ? '지킨 돈 계산하는 중' : '멈춘 3초 기록하는 중')
