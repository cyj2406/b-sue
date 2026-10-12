import { useEffect, useRef, useState } from 'react'
import { CenterIntervention, CornerBlob } from './companion/Companion'
import { CENTER } from './companion/rules'
import useCompanion from './companion/useCompanion'
import { Settings } from 'lucide-react'
import { AppHeader, IconButton, TabBar } from './components/AppChrome'
import { Toast, useToast } from './components/Kit'
import { byId, guardians, todaysGuardian } from './data/guardians'
import { products, total } from './data/market'
import Dispatch from './screens/Dispatch'
import Home from './screens/Home'
import Market from './screens/Market'
import { FakeNotification, Unlock } from './screens/Overlays'
import ExhibitApp from './exhibit/ExhibitApp'
import { resetAll as resetExhibitCounter } from './exhibit/exhibitStore'
import ComponentGallery from './screens/ComponentGallery'
import PoseLab from './screens/PoseLab'
import Record from './screens/Record'
import SettingsScreen, { ConfirmSheet } from './screens/Settings'
import Squad from './screens/Squad'


// 브라우저에 기록 저장 (미리보기용)
function useStored(key, initial) {
  const [value, setValue] = useState(() => {
    try { return JSON.parse(localStorage.getItem(key)) ?? initial } catch { return initial }
  })
  useEffect(() => { try { localStorage.setItem(key, JSON.stringify(value)) } catch { /* 저장 못 해도 화면은 그대로 */ } }, [key, value])
  return [value, setValue]
}

// 주소 뒤에 ?screen=dex 처럼 붙이면 그 화면부터 열려요
// (home · dex · receipt · market · lab · dispatch · unlock)
const START = new URLSearchParams(location.search).get('screen') ?? 'home'

export default function App() {
  // 하단 탭 3개: home · squad(수사대) · record(기록) / 설정은 홈 오른쪽 위 아이콘
  const [tab, setTab] = useState({ dex: 'squad', squad: 'squad', receipt: 'record', record: 'record' }[START] ?? 'home')
  const [showSettings, setShowSettings] = useState(START === 'settings')
  const { toast, show: showToast } = useToast()
  const [showMarket, setShowMarket] = useState(START === 'market')
  const [showLab, setShowLab] = useState(START === 'lab')
  const [showGallery, setShowGallery] = useState(START === 'components') // 0단계 공통 컴포넌트 모음
  // 전시 모드: ?screen=exhibit 로 열리거나 (홈 화면 아이콘이 이 주소로 열려요) 도구 버튼으로
  const [exhibit, setExhibit] = useState(START === 'exhibit')
  const [dispatch, setDispatch] = useState(
    START === 'dispatch' ? { guardian: byId('free_shipping'), amount: 12900, app: '쿠팡' } : null,
  ) // { guardian, amount, app }
  const [marketResult, setMarketResult] = useState(null)
  const [celebrate, setCelebrate] = useState(START === 'unlock' ? byId('sale') : null) // 해금 연출할 수호신
  const [confirm, setConfirm] = useState(null)          // 설정: 한 번 더 묻는 시트 { title, body, label, action }
  const [notice, setNotice] = useState(null)            // 가짜 카드 승인 알림 { amount, merchant }
  const [receipts, setReceipts] = useStored('blob.receipts', [])      // [참았다!] 기록 (영수증·지킨 돈·해금 — 예전 그대로)
  const [boughts, setBoughts] = useStored('blob.bought', [])          // [그래도 살래] 기록 (기록 화면에 회색 '샀어'로만)
  const [unlocked, setUnlocked] = useStored('blob.unlocked', ['sale', 'delivery_seal', 'ghost_sub'])
  const [seenAt, setSeenAt] = useStored('blob.seenAt', 0)
  const [gaze, setGaze] = useState(0)                   // 구석 블롭이 바라보는 방향 (-1 왼쪽 ~ 1 오른쪽)
  const screenRef = useRef(null)
  const tabScrollRef = useRef(null)                     // 탭 화면 스크롤 (같은 탭 다시 누르면 맨 위로)

  const holds = Object.fromEntries(guardians.map((g) => [g.id, receipts.filter((r) => r.guardianId === g.id).length]))
  const now = new Date()
  const savedThisMonth = receipts
    .filter((r) => new Date(r.at).getMonth() === now.getMonth())
    .reduce((s, r) => s + (r.amount ?? 0), 0)

  // ── 블롭 비서 (BLOB-COMPANION.md) ──
  const companion = useCompanion({ savedThisMonth })
  const { emit } = companion

  // ?screen=intervene → 가운데 개입(L2) 바로 보기 / ?screen=bubble → 구석 말풍선 바로 보기
  const started = useRef(false) // 개발 모드에서 두 번 실행되는 것 막기
  useEffect(() => {
    if (started.current) return
    started.current = true
    if (START === 'intervene') emit('forceCenter', { content: CENTER.L2(products[1]) })
    if (START === 'bubble') emit('tapBlob')
  }, [])

  // 영수증 탭을 열면 새 항목 출력이 끝난 뒤 '봤음' 처리 + 블롭 한 마디
  useEffect(() => {
    if (tab !== 'record') return
    const fresh = receipts.filter((r) => r.at > seenAt).length
    const t = setTimeout(() => setSeenAt(Date.now()), 500 + fresh * 800 + 600)
    return () => clearTimeout(t)
  }, [tab])

  // 가짜 결제 알림 → 2.5초 뒤 구석 블롭이 배웅
  useEffect(() => {
    if (!notice) return
    const t = setTimeout(() => {
      emit('farewell', notice)
      setNotice(null)
    }, 2500)
    return () => clearTimeout(t)
  }, [notice])

  // 화면 어디를 누르든: 블롭이 그쪽을 바라봄 (콤냥이 시선 따라가기)
  const onScreenPointer = (e) => {
    const rect = screenRef.current.getBoundingClientRect()
    const blobX = rect.right - 52
    setGaze((e.clientX - blobX) / 200)
    companion.touched()
  }
  // 스크롤하면 두리번
  const lastScroll = useRef(0)
  const onScreenScroll = () => {
    const t = Date.now()
    if (t - lastScroll.current > 800) { lastScroll.current = t; emit('scroll') }
  }

  const startDispatch = (guardian, amount, app, product) => {
    setMarketResult(null)
    setDispatch({ guardian, amount, app, product })
  }

  const finishDispatch = (result) => {
    const d = dispatch
    setDispatch(null)
    setMarketResult(result)
    emit(result === 'held' ? 'held' : 'bought')
    const row = { at: Date.now(), amount: d.amount, guardianId: d.guardian.id, app: d.app, ...(d.product ? { product: d.product } : {}) }
    if (result !== 'held') {
      setBoughts([...boughts, row]) // 샀어: 기록 화면에만 (영수증·지킨 돈엔 안 들어감)
      return
    }
    const next = [...receipts, row]
    setReceipts(next)
    // 같은 수호신으로 3번 참으면 해금
    if (!unlocked.includes(d.guardian.id) && next.filter((r) => r.guardianId === d.guardian.id).length >= 3) {
      setUnlocked([...unlocked, d.guardian.id])
      setCelebrate(d.guardian)
    }
  }

  const openMarket = (forceNight = false) => {
    setShowLab(false)
    setShowMarket(true)
    emit('marketEnter', { forceNight })
  }

  const sendFakeNotice = (product) => {
    const samples = [[23000, '치킨나라'], [38700, '런웨이슈즈'], [4800, '달빛커피']]
    const [amount, merchant] = product ? [total(product), product.store] : samples[Math.floor(Math.random() * samples.length)]
    setNotice({ amount, merchant })
  }

  const randomDispatch = () => {
    const samples = [['delivery_seal', 23800, '배달의민족'], ['sale', 59000, '무신사'], ['free_shipping', 12900, '쿠팡'], ['taxi', 14200, '카카오 T']]
    const [id, amount, app] = samples[Math.floor(Math.random() * samples.length)]
    startDispatch(byId(id), amount, app)
  }

  const demoReceipts = () => {
    const h = 3600000
    const t = Date.now()
    setReceipts([
      ...receipts,
      { at: t - 50 * h, amount: 23000, guardianId: 'delivery_seal', app: '배달의민족 · 치킨나라' },
      { at: t - 26 * h, amount: 38700, guardianId: 'sale', app: '무신사 · 런웨이슈즈' },
      { at: t - 5 * h, amount: 12900, guardianId: 'free_shipping', app: '쿠팡 · 꽉찬마트' },
      { at: t - 1 * h, amount: 4800, guardianId: 'coffee_law', app: '달빛커피' },
    ])
    setBoughts([...boughts, { at: t - 30 * h, amount: 21000, guardianId: 'delivery_seal', app: '배고파 · 치킨나라' }])
    setSeenAt(t - 100 * h) // 영수증 탭을 열면 드르륵 출력되게
  }

  const reset = () => {
    setReceipts([])
    setBoughts([])
    setUnlocked([])
    setSeenAt(0)
  }

  // 구석 블롭(플로팅)은 '앱 밖'에서만 → 미리보기에선 지름마켓(= 쇼핑앱 역할) 안에서만 보여요
  const cornerBottom = 96
  const showCorner = !exhibit && showMarket && !dispatch && !companion.center && !celebrate

  return (
    <div className={`page ${exhibit ? 'exhibit' : ''}`}>
      <div className="phone">
        <div className="phone-screen" ref={screenRef} onPointerDownCapture={onScreenPointer} onScrollCapture={onScreenScroll}>
          {exhibit ? (
            // 전시 모드: 가짜 폰 + 블롭 층 (내 데이터와 따로 저장)
            <ExhibitApp onExit={() => { setExhibit(false); history.replaceState(null, '', location.pathname) }} />
          ) : showGallery ? (
            <ComponentGallery onClose={() => setShowGallery(false)} />
          ) : showLab ? (
            <PoseLab onClose={() => setShowLab(false)} />
          ) : showMarket ? (
            <Market
              onExit={() => setShowMarket(false)}
              onPay={(p) => startDispatch(byId(p.guardianId), total(p), `지름마켓 · ${p.store}`, p.name)}
              afterResult={marketResult}
              onFakeNotice={sendFakeNotice}
              onEvent={emit}
            />
          ) : showSettings ? (
            // 설정: 홈 오른쪽 위 아이콘으로 들어오는 하위 화면
            <SettingsScreen
              onBack={() => setShowSettings(false)}
              onStartExhibit={() => { setShowSettings(false); setExhibit(true) }}
              onResetExhibit={resetExhibitCounter}
              onConfirm={setConfirm}
              onToast={showToast}
              tools={[
                ['출동 테스트', () => { setShowSettings(false); randomDispatch() }],
                ['지름마켓 열기', () => { setShowSettings(false); openMarket() }],
                ['카드 승인 알림 → 배웅', () => { setShowSettings(false); openMarket(); sendFakeNotice(null) }],
                ['해금 연출', () => setCelebrate(guardians[Math.floor(Math.random() * 12)])],
                ['캐릭터 테스트', () => setShowLab(true)],
                ['공통 컴포넌트 모음', () => setShowGallery(true)],
                ['데모 기록 넣기', () => { demoReceipts(); showToast('참았어 4 · 샀어 1 넣었어') }],
                ['기록 초기화', () => { reset(); showToast('기록을 처음으로 돌렸어') }, '영수증 · 지킨 돈 · 수사대 해금이 처음으로 돌아가.'],
              ]}
            />
          ) : (
            <div className="bs-screen kit-frame">
              <div className="kit-scroll ui-with-tabbar" ref={tabScrollRef}>
                {tab === 'home' && (
                  <>
                    {/* 홈 헤더: 로고 + 설정 — reference ① */}
                    <AppHeader logo right={<IconButton icon={Settings} label="설정" onClick={() => setShowSettings(true)} />} />
                    <Home
                      savedThisMonth={savedThisMonth}
                      onOpenRecord={() => setTab('record')}
                      onOpenSquad={() => setTab('squad')}
                      onOpenSubscriptions={() => setShowSettings(true)}
                    />
                  </>
                )}
                {/* 수사대 — reference ② */}
                {tab === 'squad' && (
                  <Squad
                    unlocked={unlocked}
                    holds={holds}
                    onLocked={() => showToast('아직 못 만난 요원이야. 그 순간이 오면 나타날 거야')}
                    onShowUnlock={(g) => { if (!unlocked.includes(g.id)) setUnlocked([...unlocked, g.id]); setCelebrate(g) }}
                  />
                )}
                {/* 기록 — reference ③ */}
                {tab === 'record' && <Record receipts={receipts} boughts={boughts} seenAt={seenAt} onToast={showToast} />}
              </div>
              {/* 탭바: 탭 첫 화면에서만 (출동 · 해금 중엔 숨김) */}
              {!dispatch && !celebrate && (
                <TabBar
                  value={tab}
                  onChange={setTab}
                  onReselect={() => tabScrollRef.current?.scrollTo({ top: 0, behavior: 'smooth' })}
                />
              )}
            </div>
          )}

          {/* 블롭 비서: 평소엔 구석, 규칙에 걸리면 가운데로 */}
          {showCorner && <CornerBlob companion={companion} bottom={cornerBottom} gaze={gaze} />}
          {companion.center && (
            <CenterIntervention
              content={companion.center}
              onPrimary={companion.closeCenter}
              onSecondary={companion.closeCenter}
            />
          )}

          {dispatch && (
            <Dispatch
              key={dispatch.guardian.id + dispatch.amount}
              {...dispatch}
              savedThisMonth={savedThisMonth}
              initialStep={START === 'dispatch' ? (new URLSearchParams(location.search).get('step') ?? 'pause') : 'pause'}
              onOpenReceipts={() => { setShowMarket(false); setShowSettings(false); setTab('record') }}
              onFinished={finishDispatch}
            />
          )}
          {celebrate && <Unlock guardian={celebrate} onDone={() => setCelebrate(null)} />}
          {confirm && <ConfirmSheet confirm={confirm} onClose={() => setConfirm(null)} />}
          {notice && <FakeNotification {...notice} />}
          <Toast toast={toast} bottom={showMarket || showSettings ? 20 : 72} />
        </div>
      </div>

      {/* 미리보기 도구 (디자인 확인용 — 앱에는 없음). 전시 모드에선 숨김 */}
      {!exhibit && <aside className="tools">
        <b>미리보기 도구</b>
        <button onClick={() => setExhibit(true)}>전시 모드</button>
        <button onClick={() => { resetExhibitCounter(); showToast('전시 카운터를 초기화했어') }}>전시 카운터 초기화</button>
        <button onClick={() => window.open('?screen=counter', '_blank')}>전시 카운터 모니터 (E5, 새 탭)</button>
        <button onClick={() => openMarket()}>지름마켓 열기</button>
        <button onClick={() => setShowGallery(true)}>공통 컴포넌트 모음 (UI 키트)</button>
        <button onClick={randomDispatch}>출동 테스트 ①→②→③ (L1)</button>
        <button onClick={() => emit('forceCenter', { content: CENTER.L2(products[1]) })}>L2 같은 상품 3번째</button>
        <button onClick={() => openMarket(true)}>L3 심야 쇼핑 (밤 11시로 가정)</button>
        <button onClick={() => emit('forceCenter', { content: CENTER.L4(32600, '7.2') })}>L4 장바구니 3만 원</button>
        <button onClick={() => emit('reminder', { guardian: todaysGuardian() })}>L5 아침 미션 리마인더</button>
        <button onClick={() => sendFakeNotice(null)}>카드 승인 알림 → 배웅</button>
        <button onClick={() => setCelebrate(guardians[Math.floor(Math.random() * 12)])}>해금 연출</button>
        <button onClick={() => { setShowMarket(false); setShowLab(true) }}>캐릭터 테스트</button>
        <button onClick={demoReceipts}>데모 기록 (참았어 4 · 샀어 1)</button>
        <button onClick={reset}>기록 초기화</button>
        <b style={{ marginTop: 8 }}>블롭 개입 기록</b>
        <ol className="tools-log">
          {companion.log.length === 0 && <li>블롭을 톡 하거나 지름마켓에 들어가 봐</li>}
          {companion.log.map((l, i) => <li key={i}><span>{l.t}</span> {l.text}</li>)}
        </ol>
        <p>규칙표: <code>src/companion/rules.js</code><br />기획서: <code>BLOB-COMPANION.md</code><br />캐릭터 표: <code>src/data/blobPose.js</code><br />전시 대본: <code>src/exhibit/scenarios.js</code></p>
      </aside>}
    </div>
  )
}
