import { useEffect, useState } from 'react'

// ════════════════════════════════════════════════════════════════
//  반짝 터짐 — [참았다!] 누른 직후 · 해금(엔딩) 카드가 나타날 때만
//  0.6초 동안 한 번 터지고 사라져요 (반복 없음)
//  6개 · 작게 · 블롭 바깥 위쪽 반원으로만 퍼져요 (얼굴 안 가림)
//  색: 어두운 바탕 → --orange-400 + 흰색 / 밝은 바탕 → --orange-500 + --grey-300
//  사용: 블롭을 감싼 position: relative 칸 안에 <Burst size={블롭 너비} />
// ════════════════════════════════════════════════════════════════
const ANGLES = [-180, -144, -108, -72, -36, 0] // 왼쪽 → 위 → 오른쪽

export default function Burst({ size = 140, tone = 'light', delay = 0 }) {
  const [live, setLive] = useState(true)
  useEffect(() => {
    const t = setTimeout(() => setLive(false), delay + 650)
    return () => clearTimeout(t)
  }, [delay])
  if (!live) return null

  const colors = tone === 'dark' ? ['var(--orange-400)', 'var(--white)'] : ['var(--orange-500)', 'var(--grey-300)']
  const from = size * 0.5   // 블롭 가장자리에서 시작
  const to = size * 0.72    // 바깥으로 퍼짐
  return (
    <div className="kit-burst" aria-hidden="true" style={{ top: '55%' }}>
      {ANGLES.map((deg, i) => {
        const r = (deg * Math.PI) / 180
        const pos = (d) => `${Math.round(Math.cos(r) * d)}px, ${Math.round(Math.sin(r) * d)}px`
        return (
          <i
            key={deg}
            style={{
              '--from': pos(from),
              '--to': pos(to),
              background: colors[i % 2],
              animationDelay: `${delay}ms`,
              width: i % 2 ? 6 : 8,
              height: i % 2 ? 6 : 8,
            }}
          />
        )
      })}
    </div>
  )
}
