import { useEffect, useRef, useState } from 'react'
import { isSaved } from './result'
import Blob from '../components/Blob'
import Burst from '../components/Burst'
import { MISSION_ART, SCENES } from '../data/blobPose'
import { byId } from '../data/guardians'
import { RECORDS_KEY, latestRecord, todayStats, topGuardian } from './exhibitStore'
import { coffee } from './scenarios'

// ════════════════════════════════════════════════════════════════
//  전시 카운터 — 체험대 위 가로 모니터용 (기획서 8번 · E5)  주소: ?screen=counter
//  큰 숫자 "오늘 관람객이 지킨 돈" · 체험 수 · 참은 비율 · 요원 1위
//  새 기록이 오면 숫자가 굴러 올라가고 블롭이 폴짝 + 아래 '방금' 줄
//  기록은 이 브라우저의 전시 기록(exhibit.records)을 3초마다 + 다른 탭이 바꾸면 바로 읽어요
//  → 전시폰과 같은 브라우저(폰 화면 미러링, 또는 같은 기기의 다른 탭)에서만 숫자가 맞아요
//  색·글꼴은 UI 키트 변수만 (어두운 바탕: 글씨 흰색 · 강조 --orange-400)
// ════════════════════════════════════════════════════════════════
const won = (n) => n.toLocaleString('ko-KR')

function read() {
  return { stats: todayStats(), top: topGuardian(), last: latestRecord() }
}

// 숫자 롤링: 이전 값 → 새 값으로 1.2초 동안 (처음 켤 때는 바로)
function useRolling(target) {
  const [shown, setShown] = useState(target)
  const from = useRef(target)
  useEffect(() => {
    const start = from.current
    if (start === target) return
    const t0 = performance.now()
    let raf
    const tick = (now) => {
      const p = Math.min(1, (now - t0) / 1200)
      const eased = 1 - Math.pow(1 - p, 3)
      setShown(Math.round(start + (target - start) * eased))
      if (p < 1) raf = requestAnimationFrame(tick)
      else from.current = target
    }
    raf = requestAnimationFrame(tick)
    return () => { cancelAnimationFrame(raf); from.current = target }
  }, [target])
  return shown
}

function useClock() {
  const [now, setNow] = useState(() => new Date())
  useEffect(() => {
    const t = setInterval(() => setNow(new Date()), 10000)
    return () => clearInterval(t)
  }, [])
  return now
}

export default function CounterScreen() {
  const [data, setData] = useState(read)
  const [jump, setJump] = useState(0)        // 새 기록마다 +1 → 블롭 폴짝 · 반짝
  const [cheering, setCheering] = useState(false)
  const lastAt = useRef(data.last?.at ?? 0)
  const now = useClock()

  // 3초마다 + 다른 탭에서 기록이 바뀌면 바로 다시 읽기
  useEffect(() => {
    const refresh = () => {
      const next = read()
      setData(next)
      if (next.last && next.last.at !== lastAt.current) {
        lastAt.current = next.last.at
        setJump((n) => n + 1)
      } else if (!next.last) {
        lastAt.current = 0
      }
    }
    const t = setInterval(refresh, 3000)
    const onStorage = (e) => e.key === RECORDS_KEY && refresh()
    window.addEventListener('storage', onStorage)
    return () => { clearInterval(t); window.removeEventListener('storage', onStorage) }
  }, [])

  // 폴짝은 1.6초 동안만, 그다음 다시 숨쉬기
  useEffect(() => {
    if (!jump) return
    setCheering(true)
    const t = setTimeout(() => setCheering(false), 1600)
    return () => clearTimeout(t)
  }, [jump])

  const { stats, top, last } = data
  const saved = useRolling(stats.saved)
  const held = Math.round(((stats.heldRate ?? 0) / 100) * stats.count)
  const topG = top && byId(top.guardian)
  const lastG = last && byId(last.guardian)

  return (
    <div style={{
      position: 'fixed', inset: 0, background: 'var(--grey-900)', color: 'var(--white)', fontFamily: 'var(--font)',
      display: 'grid', gridTemplateColumns: '1fr 34vw', gridTemplateRows: 'auto 1fr auto',
      padding: '4vh 5vw', gap: '0 4vw', overflow: 'hidden', cursor: 'none',
    }}>
      {/* 위: 로고 · 날짜 시간 */}
      <div style={{ gridColumn: '1 / 3', display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
        <span className="t-brand" style={{ fontSize: 'clamp(24px, 2.6vw, 48px)' }}>비상수단</span>
        <span style={{ fontSize: 'clamp(16px, 1.5vw, 28px)', color: 'var(--grey-400)', fontWeight: 500 }}>
          {now.toLocaleDateString('ko-KR', { month: 'long', day: 'numeric', weekday: 'short' })} · {now.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: false })}
        </span>
      </div>

      {/* 왼쪽: 큰 숫자 + 작은 숫자 3칸 */}
      <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', minWidth: 0 }}>
        <div style={{ fontSize: 'clamp(18px, 2vw, 36px)', color: 'var(--grey-400)', fontWeight: 600 }}>오늘 관람객이 지킨 돈</div>
        <div style={{ fontSize: 'clamp(56px, 10vw, 220px)', fontWeight: 700, lineHeight: 1.05, letterSpacing: '-0.03em', fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap' }}>
          ₩{won(saved)}
        </div>
        <div style={{ fontSize: 'clamp(18px, 2vw, 36px)', color: 'var(--orange-400)', fontWeight: 600, marginTop: '0.6vh' }}>
          {stats.saved > 0 ? `커피 ${won(Math.round(coffee(stats.saved)))}잔만큼 참았어` : '첫 관람객을 기다리는 중'}
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1.4fr', gap: '1.2vw', marginTop: '5vh' }}>
          <Tile label="체험한 사람" value={`${won(stats.count)}명`} />
          <Tile label="참은 비율" value={stats.heldRate == null ? '–' : `${stats.heldRate}%`} sub={stats.count ? `${held}명이 참았어` : null} />
          <Tile
            label="요원 1위"
            value={topG ? topG.name : '–'}
            sub={top ? `${top.count}번 막았어` : null}
            art={topG ? MISSION_ART[topG.id] : null}
            small
          />
        </div>
      </div>

      {/* 오른쪽: 블롭 */}
      <div style={{ position: 'relative', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        <div className="bs-bubble down" style={{ fontSize: 'clamp(16px, 1.6vw, 30px)', padding: '0.8em 1.1em', marginBottom: '2vh', color: 'var(--grey-900)' }}>
          사기 전에, 딱 3초
        </div>
        <div style={{ position: 'relative' }}>
          <Blob
            look={cheering ? SCENES.cornerCheer : SCENES.corner}
            motionKey={jump}
            width={Math.round(Math.min(window.innerWidth * 0.26, window.innerHeight * 0.5))}
          />
          {cheering && <Burst key={jump} size={Math.round(Math.min(window.innerWidth * 0.26, window.innerHeight * 0.5))} tone="dark" />}
        </div>
      </div>

      {/* 아래: 방금 들어온 기록 */}
      <div style={{ gridColumn: '1 / 3', minHeight: '4vh', fontSize: 'clamp(16px, 1.6vw, 30px)', color: 'var(--grey-300)' }}>
        {last && (
          <div key={last.at} className="kit-rise">
            방금 <b style={{ color: 'var(--white)' }}>제 {last.no}번 관람객</b>
            {isSaved(last.result)
              ? <>이 <b style={{ color: 'var(--orange-400)' }}>{won(last.amount)}원</b>을 지켰어</>
              : <>이 딱 3초 멈췄어</>}
            {lastG && <span style={{ color: 'var(--grey-500)' }}> · 요원 '{lastG.name}'</span>}
          </div>
        )}
      </div>
    </div>
  )
}

// 작은 숫자 칸 (어두운 바탕 위 --grey-800 카드)
function Tile({ label, value, sub, art, small }) {
  return (
    <div style={{ background: 'var(--grey-800)', borderRadius: 'var(--r-card)', padding: '2.4vh 1.6vw', display: 'flex', alignItems: 'center', gap: '1vw', minWidth: 0 }}>
      {art && <img src={art} alt="" style={{ width: '4.6vw', height: '4.6vw', objectFit: 'contain', flex: 'none' }} />}
      <div style={{ minWidth: 0 }}>
        <div style={{ fontSize: 'clamp(13px, 1.2vw, 22px)', color: 'var(--grey-400)', fontWeight: 500 }}>{label}</div>
        <div style={{ fontSize: small ? 'clamp(18px, 2vw, 38px)' : 'clamp(26px, 3.4vw, 64px)', fontWeight: 700, marginTop: '0.4vh', lineHeight: 1.2, ...(small ? { wordBreak: 'keep-all' } : { whiteSpace: 'nowrap' }) }}>{value}</div>
        {sub && <div style={{ fontSize: 'clamp(13px, 1.2vw, 22px)', color: 'var(--grey-400)', marginTop: '0.3vh' }}>{sub}</div>}
      </div>
    </div>
  )
}
