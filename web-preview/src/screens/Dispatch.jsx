import { useEffect, useState } from 'react'
import Blob from '../components/Blob'
import Burst from '../components/Burst'
import { Badge, BottomSheet, Bubble, ButtonRow, PrimaryButton, WeakButton } from '../components/Kit'
import { cups } from '../data/guardians'
import { usePressOnce } from '../components/PressOnce'

// 출동 화면 — reference ⑦ 출동 → ⑧ 선택 → 결과 바텀시트
// ⑦ 0~3초: 버튼 안이 3초 동안 차오름 (누를 수 없음)
// ⑧ 3초 뒤: 보조 왼쪽 1 [그래도 살래] : 주 버튼 오른쪽 2 [참았다!]
// initialStep: 미리보기 주소 ?screen=dispatch&step=held 처럼 특정 단계부터 보기
// resultSheets: false 면 결과 시트 없이 바로 onFinished (전시 체험은 다음 장면으로 이어져요)
const plain = (n) => n.toLocaleString('ko-KR')
// 하단 버튼 아래: 홈 바 + 16, 최소 34 (DESIGN.md 11장)
const BOTTOM = 'max(34px, calc(env(safe-area-inset-bottom, 0px) + 16px))'

export default function Dispatch({ guardian, amount, app, savedThisMonth = 0, onOpenReceipts, onFinished, initialStep = 'pause', resultSheets = true, pressDelay = 0.3 }) {
  const [step, setStep] = useState(initialStep) // pause | choose | held | bought
  const [count, setCount] = useState(3)
  const [burst, setBurst] = useState(false) // [참았다!] 직후 0.6초 반짝 (앱)
  const choice = usePressOnce(pressDelay) // 전시: 버튼 안 점 3개

  useEffect(() => {
    const timers = [
      setTimeout(() => setCount(2), 1000),
      setTimeout(() => setCount(1), 2000),
      setTimeout(() => setStep((s) => (s === 'pause' ? 'choose' : s)), 3000),
    ]
    return () => timers.forEach(clearTimeout)
  }, [])

  // [참았다!]: 카드 블롭 주변이 0.6초 반짝 → 결과로
  const hold = () => {
    if (burst) return
    setBurst(true)
    setTimeout(() => (resultSheets ? setStep('held') : onFinished('held')), 600)
  }

  const pausing = step === 'pause'
  const total = savedThisMonth + (amount ?? 0)

  return (
    <div className="kit-fade" style={{ position: 'absolute', inset: 0, background: 'var(--grey-900)', fontFamily: 'var(--font)', zIndex: 30 }}>
      {/* 위: 작은 회색 문구 + 흰 제목 */}
      <div style={{ textAlign: 'center', paddingTop: 32 }}>
        <div className="t-body2" style={{ color: 'var(--grey-400)' }}>{pausing ? '잠깐, 딱 3초' : '3초 지났어'}</div>
        <div className="t-title1 c-white" style={{ marginTop: 4 }}>{pausing ? '정말 긁을 거야?' : '이제 골라줘'}</div>
      </div>

      {/* 가운데: 흰 카드 한 장 + 위에 걸친 말풍선 */}
      <div className="kit-rise" style={{ width: 280, margin: '48px auto 0', position: 'relative' }}>
        <div style={{ position: 'absolute', left: 0, right: 0, top: -22, textAlign: 'center', zIndex: 2 }}>
          <Bubble key={pausing ? 'q' : 'ok'}>{pausing ? guardian.question : '못 참아도 괜찮아'}</Bubble>
        </div>
        <div style={{ background: '#fff', borderRadius: 28, padding: '34px 20px 22px', textAlign: 'center' }}>
          <div style={{ position: 'relative' }}>
            <Blob scene="dispatch" width={140} motionKey={count} style={{ margin: '0 auto' }} />
            {burst && <Burst size={140} tone="light" />}
          </div>
          <div style={{ marginTop: 12 }}><Badge tone="orange" style={{ fontSize: 13 }}>{guardian.name}</Badge></div>
          {amount != null ? (
            <>
              <div className="t-title1" style={{ marginTop: 10 }}>{plain(amount)}원</div>
              <div className="t-caption">{app} · 커피 {cups(amount)}잔</div>
            </>
          ) : (
            <>
              <div className="t-title3" style={{ marginTop: 10 }}>얼마인지 한 번 더 볼래?</div>
              <div className="t-caption">{app}</div>
            </>
          )}
        </div>
      </div>

      {/* 아래: ⑦ 차오르는 버튼 → ⑧ 버튼 두 개 */}
      {pausing ? (
        <div style={{ position: 'absolute', left: 'var(--side)', right: 'var(--side)', bottom: BOTTOM, height: 56, borderRadius: 16, background: 'var(--grey-800)', overflow: 'hidden' }}>
          <div className="kit-fill-3s" style={{ position: 'absolute', inset: 0, background: 'var(--orange-500)' }} />
          <div style={{ position: 'relative', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', font: '600 16px var(--font)' }}>
            {count}초만 더 생각해보자
          </div>
        </div>
      ) : (
        <div style={{ position: 'absolute', left: 'var(--side)', right: 'var(--side)', bottom: BOTTOM }}>
          {resultSheets ? (
            <ButtonRow secondaryOn="dark" secondary="그래도 살래" onSecondary={() => setStep('bought')} primary="참았다!" onPrimary={hold} />
          ) : (
            // 전시: 누른 버튼 안 점 3개(0.3초) → 기록 중 화면 (DESIGN.md '처리 중 표시') · 두 번 눌러도 한 번만
            <div className="bs-btn-row">
              <button className={`bs-btn secondary on-dark${choice.cls('bought')}`} onClick={() => choice.press('bought', () => onFinished('bought'))}>{choice.label('bought', '그래도 살래')}</button>
              <button className={`bs-btn primary${choice.cls('held')}`} onClick={() => choice.press('held', () => onFinished('held'))}>{choice.label('held', '참았다!')}</button>
            </div>
          )}
        </div>
      )}

      {/* 결과 바텀시트 */}
      {step === 'held' && (
        <BottomSheet title={amount != null ? `${plain(amount)}원 지켰어!` : '참았다! 멋져'} onDismiss={() => onFinished('held')}>
          <div className="bs-row">
            <Blob scene="held" width={64} />
            <div className="main">
              <div className="t-caption">이번 달 지킨 돈</div>
              <div className="t-title3">{plain(total)}원</div>
              <div className="t-caption c-orange semi">커피 {cups(total)}잔 아꼈어</div>
            </div>
            {onOpenReceipts && (
              <div className="end"><WeakButton onClick={() => { onFinished('held'); onOpenReceipts() }}>영수증 보기</WeakButton></div>
            )}
          </div>
          <div style={{ marginTop: 20 }}>
            <ButtonRow secondary="닫기" onSecondary={() => onFinished('held')} primary="쇼핑 그만하기" onPrimary={() => onFinished('held')} />
          </div>
        </BottomSheet>
      )}
      {step === 'bought' && (
        <BottomSheet title="알겠어, 대신 잘 쓰기!" onDismiss={() => onFinished('bought')}>
          <div className="bs-row">
            <Blob scene="bought" width={64} />
            <div className="main">
              <div className="t-body1">다음에 또 3초만 같이 멈칫하자</div>
              <div className="t-caption">필요한 거였을 수도 있지, 괜찮아</div>
            </div>
          </div>
          <PrimaryButton style={{ marginTop: 20, width: '100%' }} onClick={() => onFinished('bought')}>확인</PrimaryButton>
        </BottomSheet>
      )}
    </div>
  )
}
