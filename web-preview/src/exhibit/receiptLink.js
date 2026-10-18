import { byId } from '../data/guardians'
import { ATTRACT_LINES } from './attractLines'
import { receiptBaseUrl } from './config'
import { resultFromParam, resultToParam } from './result'
import { SCENARIOS, coffee } from './scenarios'

// ════════════════════════════════════════════════════════════════
//  영수증 ↔ QR 주소 — 서버 없이 주소 안에 영수증 내용을 담아요 (개인정보 없음)
//   s 시나리오(a·b·c) · d 날짜(YYMMDD) · t 시간(HHMM) · a 금액 · g 요원 key
//   r 결과 s(saved 지킴)/p(paused 멈춤) — 예전 QR 의 h/b 도 읽어요 · n 오늘 몇 번째 관람객 · w 오늘 관람객이 지킨 돈
//   p 영수증 캐릭터 포즈 번호 (attractLines.js 순서 0~8 · 없으면 0)
//  상품명은 글자 대신 시나리오 코드로 찾아요 (주소가 짧고, 아무 글자나 넣은 가짜 영수증도 막아요)
//  예: /r?s=a&d=261011&t=0112&a=41700&g=sale&r=s&n=57&w=1284300&p=0
// ════════════════════════════════════════════════════════════════

// 영수증 내용(receipt) → 주소
export function receiptUrl(rc) {
  const q = new URLSearchParams({
    s: rc.scenario,
    d: rc.date.replace(/\D/g, '').slice(-6),   // 2026.10.11 → 261011
    t: rc.time.replace(/\D/g, '').slice(0, 4), // 01:12 → 0112
    a: String(rc.amount),
    g: rc.guardian.id,
    r: resultToParam(rc.result),
    n: String(rc.no),
    w: String(rc.todaySaved),
    p: String(Math.max(0, ATTRACT_LINES.findIndex((l) => l.pose === rc.pose))),
  })
  return `${receiptBaseUrl()}?${q}`
}

const int = (v, max) => (/^\d{1,10}$/.test(v ?? '') && Number(v) <= max ? Number(v) : null)

// 주소(?s=…) → 영수증 내용 · 하나라도 이상하면 null (페이지는 오류 대신 안내만)
export function receiptFromSearch(search) {
  try {
    const q = new URLSearchParams(search)
    const sc = SCENARIOS[q.get('s')]
    const d = q.get('d'), t = q.get('t')
    const guardian = byId(q.get('g'))
    const amount = int(q.get('a'), 10_000_000)
    const no = int(q.get('n'), 99_999)
    const todaySaved = int(q.get('w'), 1_000_000_000)
    const result = resultFromParam(q.get('r'))
    const pi = q.has('p') ? int(q.get('p'), ATTRACT_LINES.length - 1) : 0
    if (!sc || !guardian || amount == null || !no || todaySaved == null || pi == null) return null
    if (!/^\d{6}$/.test(d ?? '') || !/^\d{4}$/.test(t ?? '') || !result) return null
    const [mm, dd, hh, mi] = [d.slice(2, 4), d.slice(4, 6), t.slice(0, 2), t.slice(2, 4)].map(Number)
    if (mm < 1 || mm > 12 || dd < 1 || dd > 31 || hh > 23 || mi > 59) return null
    return {
      scenario: sc.id,
      date: `20${d.slice(0, 2)}.${d.slice(2, 4)}.${d.slice(4, 6)}`,
      time: `${t.slice(0, 2)}:${t.slice(2, 4)}`,
      itemName: sc.ending.itemName,
      amount,
      result,
      guardian,
      cups: coffee(amount),
      todaySaved,
      no,
      pose: ATTRACT_LINES[pi].pose,
    }
  } catch {
    return null
  }
}

// ── 영수증에 보이는 모양 (전시폰 · /r · 저장 이미지 공통) ──
const DAYS = ['일', '월', '화', '수', '목', '금', '토']
// 2026.10.11 → 2026.10.11 (일)
export function dateLabel(date) {
  const [y, m, d] = date.split('.').map(Number)
  const day = new Date(y, m - 1, d).getDay()
  return Number.isNaN(day) ? date : `${date} (${DAYS[day]})`
}
// 01:12 → 01:12 AM · 19:02 → 07:02 PM
export function timeLabel(time) {
  const [h, mi] = time.split(':').map(Number)
  if (Number.isNaN(h)) return time
  const h12 = h % 12 === 0 ? 12 : h % 12
  return `${String(h12).padStart(2, '0')}:${String(mi).padStart(2, '0')} ${h < 12 ? 'AM' : 'PM'}`
}
// 방어 번호 #002
export const defenseNo = (no) => `#${String(no).padStart(3, '0')}`
