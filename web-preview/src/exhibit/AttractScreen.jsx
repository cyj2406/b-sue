import { useEffect, useState } from 'react'
import { PrimaryButton } from '../components/Kit'
import { ATTRACT_LINES } from './attractLines'
import { attractImage as img } from './attractImages'
import { ATTRACT_ROTATE } from './timing'

// ════════════════════════════════════════════════════════════════
//  대기 화면 — 단순하게 (2026-10-11): 캐릭터 카드가 주인공 · 화면 아무 데나 눌러도 시작
//   헤더 56 (상태바 아래 · 좌우 20): 로고(title.png) 높이 28
//   두 카드 묶음을 헤더와 하단 버튼 사이 세로 가운데 (사이 12)
//    ① 캐릭터 카드: 안쪽 위아래 32 · 말풍선 → 24 → 포즈 180(바닥 맞춤) → 20 → "사기 전에, 딱 3초" 22 Bold → 4 → 설명 15 회색
//    ② 한 줄 카드 56: "오늘 N명 참여 · N원 지킴" (숫자만 진하게) · 0명이면 "오늘 첫 번째로 해볼 사람?"
//   하단 고정 [시작하기] · 모든 글은 한 줄
// ════════════════════════════════════════════════════════════════
const TITLE = img('title')
const won = (n) => n.toLocaleString('ko-KR')

export default function AttractScreen({ stats, onStart }) {
  const [i, setI] = useState(0)
  const [leaving, setLeaving] = useState(false) // 0.2초 흐려지는 중

  // 9장 미리 불러오기 (바뀔 때 깜빡이지 않게)
  useEffect(() => {
    ATTRACT_LINES.forEach(({ pose }) => {
      const im = new Image()
      im.src = img(pose)
      im.decode?.().catch(() => {})
    })
  }, [])

  // 8초마다: 0.2초 흐려지고 → 다음 포즈가 아래에서 튀어 오르며
  useEffect(() => {
    let out
    const t = setInterval(() => {
      setLeaving(true)
      out = setTimeout(() => {
        setI((n) => (n + 1) % ATTRACT_LINES.length)
        setLeaving(false)
      }, 200)
    }, ATTRACT_ROTATE * 1000)
    return () => { clearInterval(t); clearTimeout(out) }
  }, [])

  const line = ATTRACT_LINES[i]

  return (
    <div className="ah-screen kit-fade" role="button" aria-label="시작하기" onClick={onStart}>
      <header className="ah-header">
        <img src={TITLE} alt="비상수단" className="ah-title" draggable={false} />
      </header>

      <div className="ah-middle">
        {/* ① 캐릭터 카드 (주인공) */}
        <div className="ah-card ah-hero">
          {/* 말풍선 + 포즈가 같이 흔들려서 사이 24 가 그대로 */}
          <div className="ex-float">
            <div key={i} className={leaving ? 'ex-attract-leave' : 'ex-attract-enter'} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <div className="ah-bubble">{line.text}</div>
              <div className="ex-attract-pop ah-pose-box">
                <img src={img(line.pose)} alt="" draggable={false} />
              </div>
            </div>
          </div>
          <div className="ah-hero-title">사기 전에, 딱 3초</div>
          <div className="ah-hero-sub">충동구매 직전을 직접 겪어봐</div>
        </div>

        {/* ② 한 줄 카드 */}
        <div className="ah-card ah-line">
          <span>{stats.count === 0
            ? '오늘 첫 번째로 해볼 사람?'
            : <>오늘 <b>{won(stats.count)}명</b> 참여 · <b>{won(stats.saved)}원</b> 지킴</>}</span>
        </div>
      </div>

      <div className="ah-cta">
        <PrimaryButton style={{ width: '100%' }}>시작하기</PrimaryButton>
      </div>
    </div>
  )
}
