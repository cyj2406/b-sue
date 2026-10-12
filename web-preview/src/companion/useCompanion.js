import { useCallback, useEffect, useRef, useState } from 'react'
import { cups } from '../data/guardians'
import { CENTER, LINES, TIMING, isLateNight } from './rules'

// 블롭 비서의 머리: 화면들이 보내는 '사건'을 받아서
//   → 구석에서 반응(포즈·말풍선)할지, 가운데로 커져서 개입할지 정해요.
export default function useCompanion({ savedThisMonth }) {
  const [scene, setScene] = useState('corner')     // 구석 블롭 포즈
  const [bubble, setBubble] = useState(null)       // { lines: [첫줄, 둘째줄], id }
  const [center, setCenter] = useState(null)       // 가운데 개입 내용 (CENTER.Lx)
  const [resting, setResting] = useState(false)    // 오늘은 쉬어
  const [log, setLog] = useState([])               // 미리보기 도구에 보여줄 개입 기록

  const mem = useRef({
    lastBubbleAt: 0, lastLine: '', lastCenterAt: 0, lastTouchAt: Date.now(),
    fired: new Set(), views: {}, cartTotal: 0, sceneTimer: null, bubbleTimer: null,
  })

  const note = (text) => setLog((l) => [{ t: new Date().toLocaleTimeString('ko-KR'), text }, ...l].slice(0, 12))

  // 잠깐 다른 포즈였다가 평소로
  const pose = useCallback((next, ms = 2600) => {
    clearTimeout(mem.current.sceneTimer)
    setScene(next)
    if (ms) mem.current.sceneTimer = setTimeout(() => setScene('corner'), ms)
  }, [])

  // 구석 말풍선 (force = 사용자가 직접 블롭을 눌렀을 때 → 쿨다운 무시)
  const say = useCallback((lines, { force = false, why = '' } = {}) => {
    const m = mem.current
    const now = Date.now()
    if (!force && now - m.lastBubbleAt < TIMING.bubbleCooldownMs) return note(`(말풍선 쿨다운) ${why}`)
    if (!force && m.lastLine === lines[0]) return
    m.lastBubbleAt = now
    m.lastLine = lines[0]
    setBubble({ lines, id: now })
    clearTimeout(m.bubbleTimer)
    m.bubbleTimer = setTimeout(() => setBubble(null), TIMING.bubbleMs)
    note(`말풍선: ${lines[0]} ${why && `← ${why}`}`)
  }, [])

  // 가운데 개입 (force = 미리보기 도구에서 강제로)
  const intervene = useCallback((content, { force = false, why = '' } = {}) => {
    const m = mem.current
    const now = Date.now()
    if (!force) {
      if (resting) return note(`(쉬는 중이라 개입 안 함) ${why}`)
      if (m.fired.has(content.id)) return note(`(오늘 이미 한 번) ${why}`)
      if (now - m.lastCenterAt < TIMING.centerCooldownMs) return note(`(가운데 개입 3분 쿨다운) ${why}`)
    }
    m.fired.add(content.id)
    m.lastCenterAt = now
    setBubble(null)
    setCenter(content)
    note(`가운데 개입 ${content.id.split(':')[0]}: ${content.title.replace('\n', ' ')} ← ${why}`)
  }, [resting])

  // ── 화면들이 보내는 사건 ──
  const emit = useCallback((type, p = {}) => {
    const m = mem.current
    m.lastTouchAt = Date.now()
    if (scene === 'cornerDoze') setScene('corner')

    switch (type) {
      case 'scroll':
        if (scene === 'corner') pose('cornerScroll', 1200)
        break
      case 'tapBlob':
        pose('cornerCheer', 1300)
        say(LINES.tapBlob(savedThisMonth, cups(savedThisMonth)), { force: true, why: '블롭을 톡' })
        break
      case 'longPressBlob': {
        const next = !resting
        setResting(next)
        pose(next ? 'cornerPat' : 'cornerCheer', 1800)
        say(next ? LINES.restOn : LINES.restOff, { force: true, why: '블롭 길게 누르기' })
        break
      }
      case 'marketEnter':
        pose('peek', 3000)
        if (isLateNight(p.hour) || p.forceNight) {
          intervene(CENTER.L3(), { force: p.forceNight, why: '심야 쇼핑 진입' })
        } else if (!resting) {
          say(LINES.marketEnter, { why: '지름마켓 진입' })
        }
        break
      case 'productView': {
        const count = (m.views[p.product.id] ?? 0) + 1
        m.views[p.product.id] = count
        if (count >= TIMING.revisitCount) intervene(CENTER.L2(p.product), { why: `같은 상품 ${count}번째` })
        break
      }
      case 'productDwell':
        if (resting) break
        pose('cornerScroll', 2000)
        say(p.product.original ? LINES.dwellSale : LINES.dwell, { why: '상품을 5초 넘게 봄' })
        break
      case 'cartAdd': {
        const before = m.cartTotal
        m.cartTotal += p.amount
        if (before < TIMING.cartLimit && m.cartTotal >= TIMING.cartLimit) {
          intervene(CENTER.L4(m.cartTotal, cups(m.cartTotal)), { why: `오늘 담은 금액 ${m.cartTotal.toLocaleString()}원` })
        } else {
          say(LINES.cartAdd(m.cartTotal), { force: true, why: '장바구니 담기' })
        }
        break
      }
      case 'reminder':
        intervene(CENTER.L5(p.guardian), { force: true, why: '아침 9시 미션 리마인더' })
        break
      case 'held':
        pose('cornerCheer', 3000)
        say(LINES.held, { force: true, why: '[참았다!]' })
        break
      case 'bought':
        pose('cornerPat', 3000)
        say(LINES.bought, { force: true, why: '[그래도 살래]' })
        break
      case 'farewell':
        pose('cornerPat', 4000)
        say(LINES.farewell(p.amount, p.merchant), { force: true, why: '카드 승인 알림' })
        break
      case 'receiptOpen':
        say(LINES.receiptOpen, { why: '기록 탭' })
        break
      case 'lockedTap':
        say(LINES.lockedTap, { force: true, why: '잠긴 카드' })
        break
      case 'forceCenter':
        intervene(p.content, { force: true, why: '미리보기 도구' })
        break
      default:
        break
    }
  }, [scene, resting, savedThisMonth, pose, say, intervene])

  // 45초 동안 아무것도 안 하면 꾸벅꾸벅
  useEffect(() => {
    const t = setInterval(() => {
      if (Date.now() - mem.current.lastTouchAt > TIMING.dozeAfterMs && scene === 'corner') {
        setScene('cornerDoze')
        note('졸기 (45초 무조작)')
      }
    }, 3000)
    return () => clearInterval(t)
  }, [scene])

  const touched = useCallback(() => {
    mem.current.lastTouchAt = Date.now()
    setScene((s) => (s === 'cornerDoze' ? 'corner' : s))
  }, [])

  return {
    scene, bubble, center, resting, log,
    emit, touched,
    closeBubble: () => setBubble(null),
    closeCenter: () => setCenter(null),
  }
}
