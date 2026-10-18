import { useEffect, useRef, useState } from 'react'
import { AppHeader } from '../../components/AppChrome'
import Blob from '../../components/Blob'
import { ButtonRow, Divider, PrimaryButton, TextButton } from '../../components/Kit'
import { Block, FxHeader, MenuRow } from './FakeKit'
import { usePressOnce } from '../../components/PressOnce'
import { PRESS_LOADING } from '../timing'
import { MISSION_ART } from '../../data/blobPose'
import { coffee } from '../scenarios'

// ════════════════════════════════════════════════════════════════
//  전시 체험 — 시나리오 C '결제일 전날' (2026-10-11 실제 앱 흐름으로 다시 짬)
//  ② 구독 상세 (비상수단 앱) → ③ 3초 뒤 [계속 쓸래] / [해지하러 가기]  ← 출동 대신 (선택 장면)
//  ④ 가짜 '스트림플러스' 멤버십 관리: 이용권 · 다음 결제일 · 금액 · 결제수단 · 마지막 시청 → 맨 아래 작은 회색 링크 "멤버십 해지"
//  ⑤ "정말 떠나시겠어요?" + 혜택 목록 + [3개월 50% 할인 받고 계속 보기](주) / [해지 계속하기](텍스트) — 선택 장면
//  ⑥ 해지 완료 · 다음 결제일부터 청구되지 않아요 → 엔딩
//  해지 완료 = 'saved'(지킨 돈에 들어감) · 계속 쓸래 / 할인 받고 계속 보기 = 'paused' (exhibit/result.js)
//  가짜 앱 안 문구는 존댓말 · 문구는 scenarios.js c
// ════════════════════════════════════════════════════════════════
const krw = (n) => n.toLocaleString('ko-KR') + '원'

export default function StreamPlus({ s, onPeek, onEnd, onBack }) {
  const [step, setStep] = useState('detail') // detail | membership | leave | done
  const [kept, setKept] = useState(false)
  const timer = useRef(null)
  useEffect(() => () => clearTimeout(timer.current), []) // 중간에 처음으로 가면 예약 취소
  const sub = s.subscription
  const m = s.membership
  const guardian = s.guardian.default
  const end = (result) => onEnd({ result, amount: s.amount.default, guardian })

  // 계속 쓰기 (③ 계속 쓸래 · ⑤ 할인 받고 계속 보기): 버튼 점 3개 뒤 바로 기록 중 화면 (DESIGN.md '처리 중 표시')
  const keep = () => {
    if (kept) return
    setKept(true)
    end('paused')
  }
  const leavePress = usePressOnce(PRESS_LOADING)
  const donePress = usePressOnce(PRESS_LOADING)

  if (step === 'detail') return <SubscriptionDetail s={s} onBack={onBack} onKeep={keep} onCancel={() => !kept && setStep('membership')} />

  // ⑥ 해지 완료
  if (step === 'done') {
    return (
      <div className="fx-screen kit-fade" data-fx="c-done" style={{ background: 'var(--white)', alignItems: 'center', justifyContent: 'center', textAlign: 'center', padding: '0 var(--side)' }}>
        <div className="t-title2">{m.done}</div>
        <div className="fx-small fx-muted" style={{ marginTop: 4 }}>{sub.name} · {m.doneSub}</div>
        <button type="button" className={`bs-btn primary${donePress.cls('next')}`} style={{ width: '100%', marginTop: 24 }} onClick={() => donePress.press('next', () => end('saved'))}>{donePress.label('next', '다음')}</button>
      </div>
    )
  }

  // ⑤ 정말 떠나시겠어요? (선택 장면 — 힌트 · 테두리 없음)
  if (step === 'leave') {
    return (
      <div className="fx-screen" data-fx="c-leave" style={{ background: 'var(--white)' }}>
        <div className="fx-scroll">
          <FxHeader title={sub.name} onBack={() => setStep('membership')} />
          <div style={{ padding: '8px var(--side) 0' }}>
            <div className="fx-leave-title">{m.leaveTitle}</div>
            <div className="fx-small fx-muted" style={{ marginTop: 8 }}>{m.leaveSub}</div>
            <div style={{ marginTop: 16 }}>
              {m.perks.map((perk) => <div key={perk} className="fx-perk"><i />{perk}</div>)}
            </div>
          </div>
          <div style={{ height: 180 }} />
        </div>
        <div className="bs-cta ex-btn-stack fx-bottom">
          <button type="button" className={`bs-btn primary${leavePress.cls('stay')}`} style={{ width: '100%' }} onClick={() => leavePress.press('stay', keep)}>{leavePress.label('stay', m.stayOffer)}</button>
          <button type="button" className={`bs-btn text${leavePress.cls('go')}`} style={{ width: '100%' }} onClick={() => leavePress.press('go', () => { setStep('done'); onPeek(s.held.cheer) })}>{leavePress.label('go', m.leaveGo)}</button>
        </div>
      </div>
    )
  }

  // ④ 멤버십 관리 — 해지는 맨 아래 작은 회색 링크 (10초 막히면 힌트: hints.js)
  return (
    <div className="fx-screen" data-fx="c-membership">
      <div className="fx-scroll">
        <FxHeader title={sub.name} onBack={() => setStep('detail')} />
        <Block>
          <div className="fx-small fx-muted">현재 이용권</div>
          <div className="fx-plan" style={{ marginTop: 4 }}>{sub.plan} · 월 {krw(sub.price)}</div>
        </Block>
        <Block style={{ paddingTop: 8, paddingBottom: 8 }}>
          <MenuRow label="다음 결제일" value={sub.nextPay} />
          <MenuRow label="결제 금액" value={krw(sub.price)} />
          <MenuRow label="결제수단" value={m.card} />
          <MenuRow label="시청 기록" value={`마지막 시청 ${sub.lastUsedDays}일 전`} />
        </Block>
        <Block style={{ paddingTop: 8, paddingBottom: 8 }}>
          <MenuRow label="프로필 관리" onClick={() => {}} />
          <MenuRow label="이용권 변경" onClick={() => {}} />
          <MenuRow label="고객센터" onClick={() => {}} />
        </Block>
        <button type="button" className="fx-link" data-hint-target="leave-link" onClick={() => setStep('leave')}>멤버십 해지</button>
        <div style={{ height: 'calc(var(--ex-cta-bottom) + 24px)' }} />
      </div>
    </div>
  )
}

// ② 구독 상세 (비상수단 앱 화면) + ③ 3초 뒤 선택
function SubscriptionDetail({ s, onBack, onKeep, onCancel }) {
  const sub = s.subscription
  const [count, setCount] = useState(3)
  const choice = usePressOnce(PRESS_LOADING) // 누른 버튼 안 점 3개 · 나머지는 흐리게
  useEffect(() => {
    const t = [1, 2, 3].map((i) => setTimeout(() => setCount(3 - i), i * 1000))
    return () => t.forEach(clearTimeout)
  }, [])
  const year = sub.price * 12

  return (
    <div className="bs-screen kit-frame">
      <div className="kit-scroll" style={{ paddingBottom: 120 }}>
        <AppHeader onBack={onBack} title="곧 빠져나갈 돈" />
        <div className="bs-body" style={{ paddingBottom: 0 }}>
          <div className="bs-card" style={{ textAlign: 'center' }}>
            <Blob look={{ image: MISSION_ART[s.guardian.default], motion: 'breathe' }} width={150} style={{ margin: '0 auto' }} />
            <div className="t-caption" style={{ marginTop: 12 }}>내일 {sub.name}</div>
            <div className="t-display" style={{ marginTop: 2 }}>{krw(sub.price)}</div>
            <div className="t-body2 semi" style={{ marginTop: 6 }}>마지막으로 본 건 {sub.lastUsedDays}일 전이야</div>
          </div>
          <div className="bs-card tint">
            <div className="t-body2 bold" style={{ color: 'var(--orange-700)' }}>1년이면 {krw(year)}</div>
            <div className="t-caption" style={{ marginTop: 2 }}>커피 {Math.round(coffee(year))}잔이야</div>
            <Divider style={{ margin: '12px 0', background: 'var(--orange-100)' }} />
            <div className="t-caption">해지는 그 앱에서 해야 해. 내가 길만 알려줄게</div>
          </div>
        </div>
      </div>

      {/* ③ 출동 대신 3초: 버튼 안이 차오른 뒤 두 개 — 선택 장면이라 힌트 · 테두리 없음 */}
      <div className="ex-cta">
        {count > 0 ? (
          <div style={{ position: 'relative', height: 56, borderRadius: 16, background: 'var(--grey-800)', overflow: 'hidden' }}>
            <div className="kit-fill-3s" style={{ position: 'absolute', inset: 0, background: 'var(--orange-500)' }} />
            <div style={{ position: 'relative', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', font: '600 16px var(--font)' }}>
              {count}초만 생각해보자
            </div>
          </div>
        ) : (
          <div className="kit-rise">
            <div className="bs-btn-row">
              <button type="button" className={`bs-btn secondary on-grey${choice.cls('keep')}`} onClick={() => choice.press('keep', onKeep)}>{choice.label('keep', '계속 쓸래')}</button>
              <button type="button" className={`bs-btn primary${choice.cls('cancel')}`} onClick={() => choice.press('cancel', onCancel)}>{choice.label('cancel', '해지하러 가기')}</button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
