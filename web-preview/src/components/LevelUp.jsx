import { MISSION_ART } from '../data/blobPose'
import Blob from './Blob'
import Burst from './Burst'
import { Badge, Bubble } from './Kit'

// ════════════════════════════════════════════════════════════════
//  엔딩 · 해금 — reference ⑨ (토스 '레벨업' 구조)
//  짙은 딤 위에: 작은 회색 문구 + 흰 제목 → 요원 카드(말풍선 걸침) → 안내 → 버튼
//  배치 (2026-10-18): 세로 flex — 헤드라인(상단) · 카드 + 안내(남는 공간 세로 가운데) · 버튼(하단 고정)
//  요원 카드 그림 = 그 수호신의 미션 그림 · 카드가 나타날 때 0.6초 한 번 반짝 (Burst)
//  앱 해금 연출(screens/Overlays.jsx)과 전시 엔딩(exhibit/ExhibitApp.jsx)이 같이 써요
// ════════════════════════════════════════════════════════════════
// solid: 어두운 막 대신 바탕 자체를 #191F28 로 (전시 엔딩 — DESIGN.md '처리 중 표시')
export default function LevelUp({ sub, title, guardian, bubble, caption, primary, onPrimary, text, onText, autoNextSeconds, solid = false }) {
  const no = String(guardian.number).padStart(2, '0')
  return (
    <div style={{ position: 'absolute', inset: 0, zIndex: 40, fontFamily: 'var(--font)' }}>
      {solid ? <div style={{ position: 'absolute', inset: 0, background: 'var(--grey-900)' }} /> : <div className="bs-dim strong kit-fade" />}
      <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
        <div className="kit-fade" style={{ flex: 'none', textAlign: 'center', paddingTop: 40 }}>
          <div className="t-body2" style={{ color: 'var(--grey-400)' }}>{sub}</div>
          <div className="t-title2 c-white" style={{ marginTop: 4 }}>{title}</div>
        </div>

        <div style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '46px 0 24px' }}>
        <div className="kit-rise" style={{ width: 260, position: 'relative', animationDelay: '.15s' }}>
          {bubble && (
            <div style={{ position: 'absolute', left: 0, right: 0, top: -22, textAlign: 'center', zIndex: 2 }}>
              <Bubble tail="down">{bubble}</Bubble>
            </div>
          )}
          <div style={{ background: 'linear-gradient(180deg, var(--orange-50), #fff 60%)', borderRadius: 28, padding: '34px 20px 24px', textAlign: 'center' }}>
            <div style={{ position: 'relative' }}>
              <Blob look={{ image: MISSION_ART[guardian.id], motion: 'none' }} width={150} style={{ margin: '0 auto' }} />
              {/* 카드가 올라온 뒤 한 번만 반짝 (밝은 카드 위) */}
              <Burst size={150} tone="light" delay={400} />
            </div>
            <div style={{ marginTop: 12 }}><Badge>No.{no} · {guardian.type}</Badge></div>
            <div className="t-title3" style={{ marginTop: 8 }}>{guardian.name}</div>
          </div>
        </div>

        {caption && <div className="t-caption" style={{ textAlign: 'center', color: 'var(--grey-400)', marginTop: 16 }}>{caption}</div>}
        </div>

        <div className="lvl-cta">
          {autoNextSeconds && (
            <div className="lvl-auto-next" aria-live="polite">
              <span>{autoNextSeconds}초 뒤 영수증이 자동으로 열려요</span>
              <span className="lvl-auto-track"><i style={{ animationDuration: `${autoNextSeconds}s` }} /></span>
            </div>
          )}
          <button className="bs-btn primary" style={{ width: '100%' }} onClick={onPrimary}>{primary}</button>
          {text && <button className="bs-btn text" style={{ width: '100%', color: 'var(--grey-400)' }} onClick={onText}>{text}</button>}
        </div>
      </div>
    </div>
  )
}
