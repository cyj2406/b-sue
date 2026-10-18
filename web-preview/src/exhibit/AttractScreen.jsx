import { PrimaryButton } from '../components/Kit'
import { ATTRACT_LINES } from './attractLines'
import { attractImage as img } from './attractImages'

// ════════════════════════════════════════════════════════════════
//  대기 화면 (전시 모드 첫 화면) — 2026-10-18 온보딩 스타일: 카드 없이 바탕 위에 떠 있어요 · 화면 아무 데나 눌러도 시작
//   헤더 56 (상태바 아래 · 좌우 20): 로고(title.png) 높이 28
//   위: 헤드라인 2줄 26 Bold "반가워! 지금부터 / 지출 방어 시작해볼까?" → 8 → 설명 15 회색 (가운데)
//   가운데 아래: 세일 포즈 블롭 -8도 기울여 둥실 + 흰 말풍선 (그림자)
//   흩어진 장식: 동전 · % 태그 · 돋보기 · 반짝이 — 그림 파일 없이 아래 SVG (주황 · 검정 · 크림만) · 일부는 화면 밖으로 살짝 잘려요
//   하단 고정 [시작하기]
// ════════════════════════════════════════════════════════════════
const TITLE = img('title')
const HERO = ATTRACT_LINES.find((l) => l.pose === 'pose_sale') ?? ATTRACT_LINES[0]

// 장식 자리 (390×844 기준 %) · size px · rot 기울기 · dur/delay 떠다니는 박자
const DECOS = [
  { kind: 'coin',   left: '-4%', top: '30%', size: 56, rot: -14, dur: 4.2, delay: 0 },
  { kind: 'tag',    left: '84%', top: '24%', size: 64, rot: 18,  dur: 5.0, delay: 0.6 },
  { kind: 'spark',  left: '14%', top: '44%', size: 24, rot: 0,   dur: 3.4, delay: 1.1 },
  { kind: 'glass',  left: '72%', top: '62%', size: 76, rot: -24, dur: 4.6, delay: 0.3 },
  { kind: 'coin',   left: '86%', top: '78%', size: 40, rot: 12,  dur: 3.8, delay: 1.4 },
  { kind: 'tag',    left: '2%',  top: '70%', size: 48, rot: -20, dur: 4.4, delay: 0.9 },
  { kind: 'spark',  left: '78%', top: '43%', size: 18, rot: 0,   dur: 3.0, delay: 0.2 },
]

export default function AttractScreen({ onStart }) {
  return (
    <div className="ah-screen kit-fade" role="button" aria-label="시작하기" onClick={onStart}>
      {/* 흩어진 장식 (눌러도 그냥 시작) */}
      <div className="ah-decos" aria-hidden="true">
        {DECOS.map((d, i) => (
          <span key={i} className="ah-deco" style={{ left: d.left, top: d.top, width: d.size, height: d.size, '--rot': `${d.rot}deg`, animationDuration: `${d.dur}s`, animationDelay: `${d.delay}s` }}>
            <Deco kind={d.kind} />
          </span>
        ))}
      </div>

      <header className="ah-header">
        <img src={TITLE} alt="비상수단" className="ah-title" draggable={false} />
      </header>

      <div className="ah-intro">
        <div className="ah-headline">반가워! 지금부터<br />지출 방어 시작해볼까?</div>
        <div className="ah-desc">나랑 같이, 혼나지 않고 멈칫해보자</div>
      </div>

      <div className="ah-stage">
        <div className="ah-mascot ex-float">
          <div className="ah-bubble">{HERO.text}</div>
          <img className="ah-pose" src={img(HERO.pose)} alt="" draggable={false} />
        </div>
      </div>

      <div className="ah-cta">
        <PrimaryButton style={{ width: '100%' }}>시작하기</PrimaryButton>
      </div>
    </div>
  )
}

// 작은 장식 그림 — 주황 #D64518 · 연주황 · 검정 #191F28 · 크림 #FFF1EB
const O = '#D64518', O2 = '#F2793F', K = '#191F28', C = '#FFF1EB'
function Deco({ kind }) {
  if (kind === 'coin') {
    return (
      <svg viewBox="0 0 48 48" width="100%" height="100%">
        <circle cx="24" cy="26" r="20" fill={O} />
        <circle cx="24" cy="23" r="20" fill={O2} stroke={K} strokeWidth="2.5" />
        <circle cx="24" cy="23" r="14" fill="none" stroke={C} strokeWidth="2" opacity=".7" />
        <text x="24" y="29.5" textAnchor="middle" fontFamily="Pretendard, sans-serif" fontWeight="800" fontSize="17" fill={K}>₩</text>
      </svg>
    )
  }
  if (kind === 'tag') {
    return (
      <svg viewBox="0 0 48 48" width="100%" height="100%">
        <path d="M8 6h20l14 14-20 20L8 26z" fill={O} stroke={K} strokeWidth="2.5" strokeLinejoin="round" />
        <circle cx="16" cy="14" r="3" fill={C} stroke={K} strokeWidth="2" />
        <text x="26" y="31" textAnchor="middle" fontFamily="Pretendard, sans-serif" fontWeight="800" fontSize="15" fill={C} transform="rotate(-45 26 26)">%</text>
      </svg>
    )
  }
  if (kind === 'glass') {
    return (
      <svg viewBox="0 0 48 48" width="100%" height="100%">
        <path d="M29 29l12 12" stroke="#8A5A2B" strokeWidth="7" strokeLinecap="round" />
        <path d="M29 29l12 12" stroke={K} strokeWidth="2" strokeLinecap="round" opacity=".35" />
        <circle cx="20" cy="20" r="14" fill={C} stroke={K} strokeWidth="4" />
        <path d="M13 17a8 8 0 0 1 6-6" stroke="#fff" strokeWidth="3" strokeLinecap="round" fill="none" />
      </svg>
    )
  }
  // spark: 네 갈래 반짝이
  return (
    <svg viewBox="0 0 24 24" width="100%" height="100%">
      <path d="M12 0c1 6 4 9 12 12-8 3-11 6-12 12-1-6-4-9-12-12 8-3 11-6 12-12z" fill={O} />
    </svg>
  )
}
