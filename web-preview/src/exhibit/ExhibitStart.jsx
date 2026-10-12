import { ChevronRight } from 'lucide-react'
import { AppHeader, STROKE } from '../components/AppChrome'
import { PrimaryButton } from '../components/Kit'
import { MISSION_ART, POSES } from '../data/blobPose'
import { READY, SCENARIOS, SCENARIO_ORDER } from './scenarios'

// ════════════════════════════════════════════════════════════════
//  전시 시작 화면 — 2026-10-11 토스 스타일 목업 (390×844 한 화면, 스크롤 없이)
//  ① 소개 (카드 없이 배경 위): 작은 회색 글 → 제목 2줄 → 설명 한 줄 + 오른쪽 pose_idle
//  ② 흰 카드 하나: "하나 골라줘" + 행 3개 (원형 캐릭터 · 제목 / 주황 행동 · 화살표) — 문구는 scenarios.js card
//  ③ 카운터 한 줄  ④ 하단 고정 [랜덤으로 하기] (직전과 다른 것)
//  로고 3초 길게 누르기 → 운영자 메뉴 (관람객 눈엔 안 보이는 입구)
//  모양은 styles/exhibit.css 의 .xs-*
// ════════════════════════════════════════════════════════════════
const won = (n) => n.toLocaleString('ko-KR')

export default function ExhibitStart({ stats, onPick, onRandom, onOperator }) {
  const list = SCENARIO_ORDER.filter((id) => READY[id])

  return (
    <div className="xs-screen">
      <div className="xs-scroll">
        <AppHeader logo onLogoHold={onOperator} />

        {/* ① 소개 */}
        <div className="xs-intro">
          <div className="xs-intro-text">
            <div className="xs-eyebrow">1분 시뮬레이션</div>
            <div className="xs-title">지름신 오는 순간,<br />직접 겪어봐</div>
            <div className="xs-desc">평소처럼 사봐. 블롭이 끼어들 거야.</div>
          </div>
          <img className="xs-pose" src={POSES.idle} alt="" draggable={false} />
        </div>

        {/* ② 상황 고르기 */}
        <div className="xs-card">
          <div className="xs-card-title">하나 골라줘</div>
          {list.map((id) => {
            const c = SCENARIOS[id].card
            return (
              <button key={id} type="button" className="xs-row" onClick={() => onPick(id)} aria-label={`${c.headline}, ${c.action}`}>
                <span className="xs-avatar"><img src={MISSION_ART[SCENARIOS[id].guardian.default]} alt="" /></span>
                <span className="xs-row-text">
                  <span className="xs-row-title">{c.headline}</span>
                  <span className="xs-row-action">{c.action}</span>
                </span>
                <span className="xs-chevron"><ChevronRight size={20} strokeWidth={STROKE} aria-hidden="true" /></span>
              </button>
            )
          })}
        </div>

        {/* ③ 카운터 */}
        <div className="xs-counter" aria-live="polite">
          {stats.count === 0 ? '오늘 첫 번째로 해볼 사람?' : `오늘 ${won(stats.count)}명 참여 · ${won(stats.saved)}원 지킴`}
        </div>
        <div style={{ height: 24 }} />
      </div>

      {/* ④ 하단 고정 */}
      <div className="xs-cta">
        <PrimaryButton style={{ width: '100%' }} onClick={onRandom}>랜덤으로 하기</PrimaryButton>
      </div>
    </div>
  )
}
