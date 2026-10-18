// ════════════════════════════════════════════════════════════════
//  전시 기록 저장 — 내 데이터(영수증·지킨 돈·도감)와 완전히 따로
//  브라우저 저장소의 'exhibit.' 이름표가 붙은 칸에만 써요.
// ════════════════════════════════════════════════════════════════

import { isSaved } from './result'

const KEY_RECORDS = 'exhibit.records'   // 관람객 체험 기록 전체
const KEY_ARCHIVE = 'exhibit.archive'   // 날짜별로 저장해 둔 카운터 (운영자 메뉴, E4)

function read(key, fallback) {
  try {
    return JSON.parse(localStorage.getItem(key)) ?? fallback
  } catch {
    return fallback
  }
}
function write(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch {
    // 저장이 안 돼도 관람객에게는 아무것도 보이지 않게
  }
}

const today = () => new Date().toLocaleDateString('sv-SE') // 2026-11-14

// 기록 한 건: { no, at, date, scenario, result: 'saved'|'paused', amount, guardian, seconds }
//  (2026-10-18 이전 기록은 'held'|'bought' — isSaved 가 둘 다 읽어요)
export function getRecords() {
  return read(KEY_RECORDS, [])
}

// 체험이 끝날 때 한 건 저장 (E3에서 사용). 관람객 번호를 돌려줘요.
export function addRecord({ scenario, result, amount, guardian, seconds }) {
  const records = getRecords()
  const todays = records.filter((r) => r.date === today())
  const record = {
    no: todays.length + 1,
    at: Date.now(),
    date: today(),
    scenario,
    result,
    amount,
    guardian,
    seconds,
  }
  write(KEY_RECORDS, [...records, record])
  return record
}

// 시작 화면 맨 위 카운터: 오늘 관람객이 지킨 돈 · 체험 수 · 참은 비율
export function todayStats() {
  const todays = getRecords().filter((r) => r.date === today())
  const held = todays.filter((r) => isSaved(r.result)) // 지킨 돈은 saved 만 · 인원은 모두
  return {
    saved: held.reduce((sum, r) => sum + (r.amount ?? 0), 0),
    count: todays.length,
    heldCount: held.length,
    heldRate: todays.length ? Math.round((held.length / todays.length) * 100) : null,
  }
}

// 운영자 메뉴 (E4): 오늘 카운터를 날짜별로 저장하고 초기화
export function archiveAndResetToday() {
  const stats = todayStats()
  write(KEY_ARCHIVE, [...read(KEY_ARCHIVE, []), { date: today(), ...stats, savedAt: Date.now() }])
  write(KEY_RECORDS, getRecords().filter((r) => r.date !== today()))
}

// 미리보기 도구 '전시 카운터 초기화'
export function resetAll() {
  write(KEY_RECORDS, [])
  write(KEY_ARCHIVE, [])
}

// 운영자 메뉴: 날짜별로 저장해 둔 카운터 (최근 날짜가 위로)
export function getArchive() {
  return read(KEY_ARCHIVE, []).slice().reverse()
}

// 알림 소리 켜기/끄기 (진동은 항상) — 운영자 메뉴
const KEY_SOUND = 'exhibit.sound'
export function soundOn() {
  return read(KEY_SOUND, true)
}
export function setSoundOn(on) {
  write(KEY_SOUND, on)
}

// 전시 카운터 화면(E5): 오늘 가장 많이 막은 요원 1위 { guardian, count } · 없으면 null
export function topGuardian() {
  const counts = {}
  getRecords()
    .filter((r) => r.date === today() && isSaved(r.result))
    .forEach((r) => { counts[r.guardian] = (counts[r.guardian] ?? 0) + 1 })
  const [guardian, count] = Object.entries(counts).sort((a, b) => b[1] - a[1])[0] ?? []
  return guardian ? { guardian, count } : null
}

// 오늘 마지막 기록 한 건 (카운터 화면 아래 '방금' 줄)
export function latestRecord() {
  const todays = getRecords().filter((r) => r.date === today())
  return todays[todays.length - 1] ?? null
}

// 다른 탭·창에서 기록이 바뀌면 알려줘요 (같은 브라우저 안에서만)
export const RECORDS_KEY = KEY_RECORDS

// 대기 화면 '오늘 지킨 돈' 카드: 최근 hours 시간 시간대별 참여 인원 (지금 시간대가 마지막)
// [{ hour: 14, label: '14시', count: 3, current: false }, …]
export function hourlyCounts(hours = 4) {
  const now = new Date()
  const todays = getRecords().filter((r) => r.date === today())
  return Array.from({ length: hours }, (_, i) => {
    const h = (now.getHours() - (hours - 1 - i) + 24) % 24
    const count = todays.filter((r) => new Date(r.at).getHours() === h).length
    return { hour: h, label: `${h}시`, count, current: i === hours - 1 }
  })
}
