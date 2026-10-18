import { Component, useCallback, useEffect, useRef, useState } from 'react'
import AttractScreen from './AttractScreen'
import { Toast, useToast } from '../components/Kit'
import { OrderDone } from '../components/MarketKit'
import ExhibitStart from './ExhibitStart'
import { addRecord, todayStats } from './exhibitStore'
import LevelUp from '../components/LevelUp'
import { byId } from '../data/guardians'
import HintLayer from './HintLayer'
import IdleCheck from './IdleCheck'
import OperatorMenu from './OperatorMenu'
import ProcessingScreen from './ProcessingScreen'
import { receiptUrl } from './receiptLink'
import { decideResult, endingText } from './result'
import { attractImage } from './attractImages'
import { ATTRACT_LINES } from './attractLines'
import { preloadReceipt } from './preload'
import ScreenReceipt from './ScreenReceipt'
import { LockScreen, NotificationBanner, PeekLayer, TimeSkip } from './parts/PhoneParts'
import { READY, SCENARIOS, coffee } from './scenarios'
import ShopFlow from './ShopFlow'
import Baegopa from './apps/Baegopa'
import StreamPlus from './apps/StreamPlus'
import { ENDING_FADE, ENDING_WAIT, IDLE_FIRST } from './timing'
import { EX_CSS_VARS } from './exhibitTokens'
import '../styles/exhibit.css'

// ════════════════════════════════════════════════════════════════
//  전시 모드 — 앱 안의 '가짜 폰' + 그 위에 뜨는 블롭 층
//  흐름: 대기 화면(아무 데나 누르기) → 시작 화면 → 잠금화면(④) → 1초 뒤 알림 → 알림 탭 → 지름마켓(⑤⑥) → 출동(⑦⑧)
//   참았다 → 시간 건너뛰기 → 아침 잠금화면 알림 → 엔딩
//   그래도 살래 → 결제 완료 → 카드 승인 알림 + 배웅 빼꼼 → 엔딩
//  엔딩(⑨): 새 요원 카드 + [영수증 받기] · 이때 체험 기록 한 건 저장 (exhibitStore)
//   [영수증 받기] 또는 4초 무조작 → 화면 영수증 (사진 찍어가기 · 프린터 없음) → 15초 뒤 대기 화면
//  갤럭시 크롬 '홈 화면에 추가' 전체화면 + '앱 고정' 기준:
//   · 화면 꺼짐 방지(Wake Lock) · 뒤로가기는 시작 화면까지만 · 길게 눌러 메뉴·확대·당겨서 새로고침 막기
//  무인 운영: 어느 화면이든 30초 안 만지면 "아직 있어?" → 15초 뒤 대기 화면 (시간 값은 timing.js)
//  체험 중 힌트: HintLayer (단계 · 문구 · 버튼은 hints.js)
//   · 어떤 오류가 나도 관람객에겐 오류 화면 대신 시작 화면 (맨 아래 SafeExhibit)
// ════════════════════════════════════════════════════════════════
function ExhibitApp({ onExit, recovered = false }) {
  const [screen, setScreen] = useState('attract') // attract | start | lock | shop | done | ending | receipt
  const [lockView, setLockView] = useState(null)  // 잠금화면에 보일 시계·알림 (처음 / 다음 날 아침)
  const [floating, setFloating] = useState(null)  // 앱 위로 내려오는 알림
  const [result, setResult] = useState(null)      // { result: 'saved'|'paused', amount, guardian, bonus } — result.js
  const [scenarioId, setScenarioId] = useState(null)
  const [showNoti, setShowNoti] = useState(false)
  const [peek, setPeek] = useState(null)
  const [skip, setSkip] = useState(null)        // { from, to, then }
  const [operator, setOperator] = useState(false)
  const [receipt, setReceipt] = useState(null)    // 화면 영수증 내용
  const [activity, setActivity] = useState(0)       // 화면을 만질 때마다 +1 (무조작 시계 다시 세기)
  const [idleAsk, setIdleAsk] = useState(false)     // "아직 있어?"
  const [stats, setStats] = useState(todayStats)
  const { toast, show } = useToast()
  const peekTimer = useRef(null)
  const startedAt = useRef(0)   // 체험 시작 시각 (기록의 '걸린 시간')
  const saved = useRef(false)   // 엔딩에서 기록은 한 번만
  const record = useRef(null)   // 방금 저장한 기록 (영수증의 '제 N번')
  const receiptReady = useRef(null) // 기록 중 화면에서 미리 만든 이번 관람객 영수증
  const later = useRef([])      // 예약된 알림·빼꼼 (장면이 바뀌면 취소)
  const after = (ms, fn) => later.current.push(setTimeout(fn, ms))
  const cancelLater = () => { later.current.forEach(clearTimeout); later.current = [] }
  const s = scenarioId ? SCENARIOS[scenarioId] : null
  const rootRef = useRef(null)

  // 처음으로 (뒤로가기 · 운영자 · 체험 끝)
  const goStart = useCallback(() => {
    cancelLater()
    setScreen('start')
    setScenarioId(null)
    setShowNoti(false)
    setFloating(null)
    setResult(null)
    setPeek(null)
    setSkip(null)
    setReceipt(null)
    receiptReady.current = null
    setIdleAsk(false)
    setStats(todayStats())
  }, [])

  // 대기 화면으로 (무조작 · 영수증 끝) — 처음으로와 같이 비우고 화면만 대기
  const goAttract = useCallback(() => {
    goStart()
    setScreen('attract')
  }, [goStart])

  // 시나리오 시작: 잠금화면 → 1초 뒤 알림
  const start = (id) => {
    const sc = SCENARIOS[id]
    lastPicked.current = id // [랜덤으로] 버튼이 다음엔 다른 걸 고르게
    startedAt.current = Date.now()
    saved.current = false
    setResult(null)
    setFloating(null)
    setScenarioId(id)
    setLockView({ kind: 'opening', clock: sc.clock, noti: sc.opening, next: () => setScreen('shop') })
    setScreen('lock')
    setShowNoti(false)
    after(1000, () => setShowNoti(true))
  }

  // 빼꼼: 4초 뒤 사라짐, 아무것도 막지 않음
  const showPeek = (text) => {
    clearTimeout(peekTimer.current)
    setPeek({ text, id: Date.now() })
    peekTimer.current = setTimeout(() => setPeek(null), 4000)
  }

  // 선택 직후(참았다 · 그래도 살래 · 계속 쓸래 · 해지 완료) → 기록 중 화면 → 체크 완료 → 엔딩 (DESIGN.md '처리 중 표시')
  // 결과는 여기서 한 번만 정해요 (result.js decideResult) — 아래는 모두 result.result 만 읽어요
  const onResult = (choice) => {
    const r = { ...choice, result: decideResult(choice.result) }
    receiptReady.current = null // 앞 관람객 영수증이 남지 않게
    cancelLater()
    setFloating(null)
    setPeek(null)
    setSkip(null)
    setResult(r)
    setScreen('processing')
  }


  // 기록 중 화면 동안 준비: 기록 한 건 저장 · 영수증 내용 · 랜덤 포즈 · QR 주소 · 그림/글꼴 미리 불러오기
  const prepareEnding = useCallback(async () => {
    if (!s || !result) return
    if (!saved.current) {
      saved.current = true
      record.current = addRecord({
        scenario: scenarioId,
        result: result.result,
        amount: result.amount,
        guardian: result.guardian,
        seconds: Math.round((Date.now() - startedAt.current) / 1000),
      })
    }
    const data = buildReceipt()
    data.url = receiptUrl(data)
    receiptReady.current = data
    preloadReceipt()
    await Promise.all([
      document.fonts?.ready,
      ...[attractImage(data.pose)].map((src) => { const im = new Image(); im.src = src; return im.decode?.().catch(() => {}) }),
    ])
  }, [s, result])

  // 혹시 기록 중 화면을 거치지 않았으면 엔딩에서 저장 (예전 흐름 안전장치)
  useEffect(() => {
    if (screen !== 'ending' || !result || saved.current) return
    saved.current = true
    cancelLater()
    setFloating(null)
    setPeek(null)
    record.current = addRecord({
      scenario: scenarioId,
      result: result.result,
      amount: result.amount,
      guardian: result.guardian,
      seconds: Math.round((Date.now() - startedAt.current) / 1000),
    })
  }, [screen, result])

  // 영수증 내용 (랜덤 포즈 포함) — 기록 중 화면에서 미리 만들어 둬요
  const buildReceipt = () => {
    const g = byId(result.guardian)
    return {
      scenario: s.id, // QR 주소용 (상품명은 시나리오로 찾아요)
      date: new Date().toLocaleDateString('sv-SE').replaceAll('-', '.'),
      time: s.clock.time,
      itemName: s.ending.itemName,
      amount: result.amount,
      result: result.result, // 'saved' | 'paused'
      guardian: g,
      cups: coffee(result.amount),
      todaySaved: todayStats().saved,
      no: record.current?.no ?? 1,
      pose: ATTRACT_LINES[Math.floor(Math.random() * ATTRACT_LINES.length)].pose, // 영수증 캐릭터 (랜덤 · QR에도 담겨요)
    }
  }

  // [영수증 받기]: 미리 만든 영수증을 바로 띄워요 (없거나 이번 결과와 다르면 그 자리에서 다시 만들기)
  const openReceipt = useCallback(() => {
    if (!s || !result) return
    const ready = receiptReady.current
    const fresh = ready && ready.scenario === s.id && ready.result === result.result && ready.amount === result.amount
    const data = fresh ? ready : buildReceipt()
    if (!fresh) data.url = receiptUrl(data)
    setReceipt(data)
    setScreen('receipt')
  }, [s, result])

  // 엔딩이 뜨면 영수증 글꼴·그림을 미리 불러놔요 (누르는 순간 바로 그려지게)
  useEffect(() => {
    if (screen === 'ending') preloadReceipt()
  }, [screen])

  // 엔딩에서 4초 동안 아무도 안 만지면 → 영수증 자동으로
  useEffect(() => {
    if (screen !== 'ending') return
    const t = setTimeout(openReceipt, ENDING_WAIT * 1000)
    return () => clearTimeout(t)
  }, [screen, openReceipt])

  // 시작 화면 · 체험 중(잠금화면 · 쇼핑 · 결제 완료) 30초 동안 아무도 안 만지면 → "아직 있어?" → 대기 화면
  // 엔딩·영수증은 각자 자동 전환 시계가 있고, 대기 화면은 그대로 기다려요
  useEffect(() => {
    if (!['start', 'lock', 'shop', 'done'].includes(screen) || idleAsk || skip || operator) return
    const t = setTimeout(() => setIdleAsk(true), IDLE_FIRST * 1000)
    return () => clearTimeout(t)
  }, [screen, activity, idleAsk, skip, operator])

  // [고르기 어려우면 랜덤으로]: 준비된 시나리오 중 하나 (직전에 한 것은 빼고)
  const lastPicked = useRef(null)
  const startRandom = () => {
    const ids = Object.keys(SCENARIOS).filter((id) => READY[id])
    const pool = ids.length > 1 ? ids.filter((id) => id !== lastPicked.current) : ids
    start(pool[Math.floor(Math.random() * pool.length)])
  }

  // 주소에 ?scenario=a 가 있으면 바로 시작 (미리보기 · 운영자 확인용)
  useEffect(() => {
    const id = new URLSearchParams(location.search).get('scenario')
    if (id && SCENARIOS[id] && !recovered) start(id) // 오류 뒤 다시 그릴 땐 시작 화면에서
  }, [])

  // ── 전체화면 운영용 장치 ──
  useEffect(() => {
    // 화면 꺼짐 방지 (크롬 Wake Lock — HTTPS 에서만 동작)
    let lock = null
    const request = async () => {
      try {
        lock = await navigator.wakeLock?.request('screen')
      } catch {
        // 지원 안 하면 조용히
      }
    }
    request()
    const onVisible = () => document.visibilityState === 'visible' && request()
    document.addEventListener('visibilitychange', onVisible)

    // 뒤로가기는 시작 화면까지만 (앱 밖으로 안 나가게)
    history.pushState({ exhibit: true }, '')
    const onPop = () => {
      history.pushState({ exhibit: true }, '')
      goStart()
    }
    window.addEventListener('popstate', onPop)

    // 당겨서 새로고침 · 길게 눌러 메뉴 막기
    document.body.classList.add('exhibit-lock')

    // 뒤에서 난 오류는 조용히 (관람객 화면엔 아무것도 안 뜨게)
    const quiet = (e) => e.preventDefault()
    window.addEventListener('unhandledrejection', quiet)

    return () => {
      lock?.release?.()
      document.removeEventListener('visibilitychange', onVisible)
      window.removeEventListener('popstate', onPop)
      document.body.classList.remove('exhibit-lock')
      window.removeEventListener('unhandledrejection', quiet)
    }
  }, [goStart])

  return (
    <div ref={rootRef} className="ex-root" style={EX_CSS_VARS} onContextMenu={(e) => e.preventDefault()} onPointerDownCapture={() => setActivity((n) => n + 1)}>
      {/* 대기 화면: 아무 데나 누르면 시작 화면 */}
      {screen === 'attract' && <AttractScreen onStart={() => setScreen('start')} />}

      {screen === 'start' && (
        <ExhibitStart
          stats={stats}
          onPick={start}
          onRandom={startRandom}
          onOperator={() => setOperator(true)}
        />
      )}

      {screen === 'lock' && s && lockView && (
        // 첫 잠금화면의 안내는 힌트 층이 "1/4 알림을 눌러봐"로 · 다음 날 아침은 기존 안내 그대로
        <LockScreen clock={lockView.clock} hint={showNoti && lockView.kind !== 'opening' ? lockView.noti.hint : null}>
          {showNoti && (
            <NotificationBanner
              app={lockView.noti.app}
              title={lockView.noti.title}
              body={lockView.noti.body}
              onTap={lockView.next}
            />
          )}
        </LockScreen>
      )}

      {/* 가짜 앱: A 지름마켓 · B 배고파 (→ 비상페이 → 출동 → onResult)
          C 구독 상세 → 3초 선택 → 스트림플러스 (출동 없이 바로 엔딩 → onEnd) */}
      {screen === 'shop' && s && (
        <div className="ex-scene kit-fade">
          {s.id === 'a' && <ShopFlow s={s} onPeek={showPeek} onResult={onResult} onBack={goStart} />}
          {s.id === 'b' && <Baegopa s={s} onPeek={showPeek} onResult={onResult} onBack={goStart} />}
          {s.id === 'c' && (
            <StreamPlus s={s} onPeek={showPeek} onBack={goStart} onEnd={onResult} />
          )}
        </div>
      )}

      {/* 그래도 살래 → 결제 완료 */}
      {screen === 'done' && s && (
        <div className="ex-scene">
          <OrderDone title={s.bought.done} sub={`${s.opening.app} · 체험이라 실제로 결제되지 않았어요`}>
            <button className="bs-btn primary" onClick={() => { setFloating(null); setScreen('ending') }}>다음</button>
          </OrderDone>
        </div>
      )}

      {/* 엔딩 · 해금 (⑨) */}
      {/* 기록 중 → 체크 완료 (화면 자체가 #191F28) */}
      {screen === 'processing' && s && result && (
        <ProcessingScreen
          guardian={result.guardian}
          result={result.result}
          prepare={prepareEnding}
          onDone={() => setScreen('ending')}
        />
      )}

      {/* 엔딩 카드: 어두운 막 없이 바탕 자체가 #191F28 · 0.3초 페이드 */}
      {screen === 'ending' && s && result && (
        <div className="ex-scene ex-ending-in" style={{ background: 'var(--grey-900)', '--ex-ending-fade': `${ENDING_FADE}s` }}>
          <LevelUp
            solid
            {...endingText(s.id, result.result, result.amount)}
            guardian={byId(result.guardian)}
            bubble={byId(result.guardian).question}
            caption={result.bonus && s.ending.bonusCaption ? `${s.ending.bonusCaption} · ${s.ending.boardCaption}` : s.ending.boardCaption}
            primary="영수증 받기"
            onPrimary={openReceipt}
            text="처음으로"
            onText={goStart}
          />
        </div>
      )}

      {/* 화면 영수증 → 15초 뒤 대기 화면 */}
      {screen === 'receipt' && receipt && (
        <div className="ex-scene">
          <ScreenReceipt receipt={receipt} onDone={goAttract} />
        </div>
      )}

      {floating && (
        <NotificationBanner floating app={floating.app} title={floating.title} body={floating.body} onTap={() => setFloating(null)} />
      )}

      {/* 블롭 층 (가짜 앱 위) */}
      <PeekLayer peek={peek} />

      {skip && <TimeSkip from={skip.from} to={skip.to} onDone={() => { const then = skip.then; setSkip(null); then?.() }} />}

      {/* 체험 중 힌트 (블롭이 나와 있거나 다른 창이 떠 있으면 숨김) */}
      {s && (screen === 'lock' || screen === 'shop') && (
        <HintLayer
          rootRef={rootRef}
          scenarioId={s.id}
          phase={screen === 'lock' ? `lock-${lockView?.kind}` : 'shop'}
          paused={!!(idleAsk || operator || skip || floating)}
        />
      )}

      {/* 30초 무조작 → "아직 있어?" → 15초 → 대기 화면 */}
      {idleAsk && <IdleCheck onStay={() => setIdleAsk(false)} onLeave={goAttract} />}

      {operator && (
        <OperatorMenu
          onRun={(id) => { setOperator(false); start(id) }}
          onToast={show}
          onCounterChanged={() => setStats(todayStats())}
          onClose={() => setOperator(false)}
          onExitExhibit={() => {
            setOperator(false)
            onExit()
          }}
        />
      )}
      <Toast toast={toast} bottom={24} />
    </div>
  )
}

// ── 오류 막이: 어디서 오류가 나도 관람객에겐 시작 화면만 (다시 그려서 처음부터) ──
class SafeExhibit extends Component {
  state = { round: 0, broken: false }
  static getDerivedStateFromError() {
    return { broken: true }
  }
  componentDidCatch(error) {
    console.warn('[전시] 오류가 나서 처음으로 돌아갔어요', error) // 운영자만 보는 기록
    this.setState((s) => ({ round: s.round + 1, broken: false }))
  }
  render() {
    if (this.state.broken) return <div className="ex-root" />
    return <ExhibitApp key={this.state.round} recovered={this.state.round > 0} {...this.props} />
  }
}

export default SafeExhibit
