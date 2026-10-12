import { useEffect, useRef, useState } from 'react'
import { BLOB_ON, DEFAULT_AFTER, HINTS, TOAST_ON } from './hints'
import { HINT_BLINK } from './timing'
import { EX } from './exhibitTokens'

// ════════════════════════════════════════════════════════════════
//  체험 중 힌트 층 — 가짜 앱 '위'에 얹는 전시용 층 (터치는 전부 아래로 통과)
//  · 하단 버튼 바로 위 12px 회색 한 줄 (숫자 없이 문구만 · 버튼이 없으면 홈 바 위)
//  · 바텀시트가 열려 있으면 시트 바깥 위쪽 어두운 곳에 흰 글자 (시트 위끝에서 12px 위 · 가운데)
//  · 토스트가 떠 있는 동안은 글자를 숨겨요 (같은 자리)
//  · 몇 초 동안 아무것도 안 누르면 눌러야 할 버튼 바깥에 주황 테두리가 천천히 깜빡 (버튼은 그대로)
//  · 블롭이 나와 있으면 숨김
//  단계 · 문구 · 버튼 찾는 기준은 전부 hints.js 표에서
// ════════════════════════════════════════════════════════════════
const POLL = 300

function matches(root, phase, when) {
  if (when.phase && when.phase !== phase) return false
  if (when.has && !root.querySelector(when.has)) return false
  if (when.not && root.querySelector(when.not)) return false
  return true
}

// 하단 버튼 영역의 맨 위 (힌트는 그 위 12px) — 화면 아래 40% 안에 있는 버튼들 중 가장 위
const CTA_BUTTONS = '.bs-btn.primary, .bs-btn.secondary, .bs-btn.text'
function ctaTop(root) {
  const a = root.getBoundingClientRect()
  const tops = [...root.querySelectorAll(CTA_BUTTONS)]
    .filter((el) => !el.closest('.ex-hint-layer') && el.getClientRects().length && el.offsetWidth > 0)
    .map((el) => el.getBoundingClientRect())
    .filter((r) => r.top > a.top + a.height * 0.6)
    .map((r) => r.top)
  return tops.length ? Math.min(...tops) - a.top : null
}

// 화면에 실제로 보이는 것만 (숨은 버튼 · 크기 0 은 빼고)
function visible(root, selector) {
  return [...root.querySelectorAll(selector)].find((el) => el.getClientRects().length && el.offsetWidth > 0)
}

export default function HintLayer({ rootRef, scenarioId, phase, paused }) {
  const [view, setView] = useState(null) // { key, text, bottom, onDim, ring: {x,y,w,h,r} | null, showText }
  const stepKey = useRef(null)
  const since = useRef(Date.now()) // 단계가 바뀌었거나 마지막으로 누른 시각

  // 누르면: 테두리 사라지고 시계 다시
  useEffect(() => {
    const root = rootRef.current
    if (!root) return
    const touched = () => { since.current = Date.now() }
    root.addEventListener('pointerdown', touched, true)
    return () => root.removeEventListener('pointerdown', touched, true)
  }, [rootRef])

  useEffect(() => {
    const steps = HINTS[scenarioId]
    if (!steps || paused) { setView(null); stepKey.current = null; return }

    const tick = () => {
      const root = rootRef.current
      if (!root) return
      // 블롭이 나와 있으면 숨김 (그동안은 '안 누른 시간'도 다시 세요)
      if (root.querySelector(BLOB_ON)) { since.current = Date.now(); setView(null); return }
      // 아래쪽 단계부터 맞춰보기
      let step = null
      for (let i = steps.length - 1; i >= 0; i--) {
        if (matches(root, phase, steps[i].when)) { step = steps[i]; break }
      }
      if (!step) { setView(null); stepKey.current = null; return }

      const key = `${scenarioId}:${steps.indexOf(step)}`
      if (stepKey.current !== key) { stepKey.current = key; since.current = Date.now() }
      const waited = (Date.now() - since.current) / 1000
      const after = step.after ?? DEFAULT_AFTER
      const ready = waited >= after

      // 테두리: 대상 버튼 위치를 재서 바깥에 따로 그려요
      let ring = null
      const el = ready && visible(root, step.target)
      if (el) {
        const a = root.getBoundingClientRect()
        const b = el.getBoundingClientRect()
        const r = parseFloat(getComputedStyle(el).borderTopLeftRadius) || 12
        const o = EX.ringOffset
        ring = { x: b.left - a.left - o, y: b.top - a.top - o, w: b.width + o * 2, h: b.height + o * 2, r: r + EX.ringRadiusPlus }
      }
      // 위치: 열린 바텀시트가 있으면 시트 위끝 12px 위(어두운 곳) · 아니면 하단 버튼 12px 위
      const h = root.getBoundingClientRect().height
      const sheet = visible(root, '.bs-sheet')
      let bottom = null
      let onDim = false
      if (sheet) {
        bottom = h - (sheet.getBoundingClientRect().top - root.getBoundingClientRect().top) + EX.hintGap
        onDim = true
      } else {
        const t = ctaTop(root)
        bottom = t == null ? null : h - t + EX.hintGap
      }
      setView({
        key,
        text: step.text,
        bottom,
        onDim,
        showText: (step.quiet ? ready : true) && !root.querySelector(TOAST_ON),
        ring,
      })
    }
    tick()
    const t = setInterval(tick, POLL)
    return () => clearInterval(t)
  }, [rootRef, scenarioId, phase, paused])

  if (!view) return null
  const onDark = phase === 'lock-opening'
  return (
    <div className="ex-hint-layer" aria-hidden={!view.showText}>
      {view.ring && (
        <div
          className="ex-hint-ring"
          style={{ left: view.ring.x, top: view.ring.y, width: view.ring.w, height: view.ring.h, borderRadius: view.ring.r, animationDuration: `${HINT_BLINK * 2}s` }}
        />
      )}
      {view.showText && (
        <div key={view.key} className={`ex-hint-line kit-fade${onDark ? ' on-dark' : ''}${view.onDim ? ' on-dim' : ''}`} aria-live="polite" style={view.bottom != null ? { bottom: view.bottom } : undefined}>
          {view.text}
        </div>
      )}
    </div>
  )
}
