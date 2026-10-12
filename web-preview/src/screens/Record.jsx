import { ChevronLeft, ChevronRight, Download } from 'lucide-react'
import { useState } from 'react'
import { AppHeader, IconButton } from '../components/AppChrome'
import Blob from '../components/Blob'
import { Avatar, BottomSheet, Card, InlineIcon, PrimaryButton } from '../components/Kit'
import { MISSION_ART } from '../data/blobPose'
import { byId, cups } from '../data/guardians'
import { saveReceiptImage } from './receiptImage'

// 기록 — reference ③: 이번 달 지킨 돈(가장 큰 숫자) → 출동 내역 카드 → 영수증 이미지 저장
// 출동 내역: 참았어(주황) + 샀어(회색, 담담하게) · 영수증과 지킨 돈은 참았어만
// seenAt 이후 새 줄은 하나씩 올라오며 나타나요
const krw = (n) => n.toLocaleString('ko-KR') + '원'
const p2 = (n) => String(n).padStart(2, '0')

// '어제 01:12' · '10/04 19:02'
function when(t) {
  const d = new Date(t)
  const hm = `${p2(d.getHours())}:${p2(d.getMinutes())}`
  const today = new Date()
  const y = new Date(today.getFullYear(), today.getMonth(), today.getDate() - 1)
  if (d.toDateString() === today.toDateString()) return `오늘 ${hm}`
  if (d.toDateString() === y.toDateString()) return `어제 ${hm}`
  return `${p2(d.getMonth() + 1)}/${p2(d.getDate())} ${hm}`
}
// '지름마켓 · 런웨이슈즈' → '지름마켓' (reference: 앱 이름 · 금액)
const appName = (app = '') => app.split(' · ')[0] || '알 수 없는 앱'

export default function Record({ receipts, boughts = [], seenAt, onToast }) {
  const now = new Date()
  const [ym, setYm] = useState([now.getFullYear(), now.getMonth()])
  const [showReceipt, setShowReceipt] = useState(false)
  const [y, m] = ym
  const isThisMonth = y === now.getFullYear() && m === now.getMonth()
  const move = (d) => {
    const next = new Date(y, m + d, 1)
    if (next > new Date(now.getFullYear(), now.getMonth(), 1)) return // 미래 달은 없음
    setYm([next.getFullYear(), next.getMonth()])
  }

  const inMonth = (r) => { const d = new Date(r.at); return d.getFullYear() === y && d.getMonth() === m }
  const items = receipts.filter(inMonth).sort((a, b) => b.at - a.at)        // 참았어 (영수증·지킨 돈)
  const rows = [...items.map((r) => ({ ...r, held: true })), ...boughts.filter(inMonth).map((r) => ({ ...r, held: false }))]
    .sort((a, b) => b.at - a.at)                                             // 출동 내역 전체
  const total = items.reduce((s, r) => s + (r.amount ?? 0), 0)
  const fresh = rows.filter((r) => r.at > seenAt).sort((a, b) => a.at - b.at)

  const save = async () => {
    try {
      await saveReceiptImage({ year: y, month: m + 1, items, total })
      onToast('영수증 이미지를 저장했어')
    } catch {
      onToast('저장이 안 됐어. 한 번 더 눌러줄래?')
    }
  }

  return (
    <>
      <AppHeader
        title="기록"
        right={<>
          <IconButton icon={ChevronLeft} label="이전 달" onClick={() => move(-1)} />
          <span className="t-body1" style={{ color: 'var(--grey-700)' }}>{m + 1}월</span>
          <IconButton icon={ChevronRight} label="다음 달" onClick={() => move(1)} color={isThisMonth ? 'var(--grey-300)' : 'var(--grey-700)'} />
        </>}
      />
      <div className="bs-body" style={{ paddingBottom: 24 }}>
        {/* 이번 달 지킨 돈 — 화면에서 가장 큰 숫자 */}
        <div style={{ padding: '2px 4px 16px' }}>
          <div className="t-caption">{isThisMonth ? '이번 달' : `${m + 1}월에`} 지킨 돈</div>
          <div className="t-display">{krw(total)}</div>
          <div className="t-caption">
            {rows.length ? <>출동 {rows.length}번 중 <span className="c-orange semi">{items.length}번 참았어</span></> : '아직 출동 기록이 없어'}
          </div>
        </div>

        {/* 출동 내역 */}
        <Card title="출동 내역" more={items.length ? '영수증' : undefined} onMore={() => setShowReceipt(true)}>
          {rows.length === 0 && (
            <div className="bs-row">
              <Blob scene="empty" width={52} />
              <div className="main">
                <div className="t-body1">아직 출동 전이야</div>
                <div className="t-caption">쇼핑앱을 열면 내가 나타날게</div>
              </div>
            </div>
          )}
          {rows.map((r) => {
            const g = byId(r.guardianId)
            const order = fresh.indexOf(r)
            return (
              <div key={`${r.held}${r.at}`} className={`bs-row${order >= 0 ? ' kit-rise' : ''}`} style={order >= 0 ? { animationDelay: `${0.3 + order * 0.25}s` } : undefined}>
                <Avatar src={MISSION_ART[r.guardianId]} />
                <div className="main">
                  <div className="t-body1">{appName(r.app)} · {r.amount != null ? krw(r.amount) : '금액 모름'}</div>
                  <div className="t-caption">{when(r.at)} · {g?.name}</div>
                </div>
                {r.held
                  ? <div className="end t-body2 bold c-orange">참았어</div>
                  : <div className="end t-body2 c-weak">샀어</div>}
              </div>
            )
          })}
        </Card>

        {items.length > 0 && (
          <button type="button" className="bs-btn secondary on-grey" style={{ width: '100%', gap: 6 }} onClick={save}>
            <InlineIcon icon={Download} />{isThisMonth ? '이번 달' : `${m + 1}월`} 영수증 이미지로 저장
          </button>
        )}
      </div>

      {/* 영수증 미리보기 */}
      {showReceipt && (
        <BottomSheet onDismiss={() => setShowReceipt(false)}>
          <div style={{ textAlign: 'center' }}>
            <div className="t-brand" style={{ fontSize: 22 }}>안 지른 영수증</div>
            <div className="t-caption">{y}년 {m + 1}월 · 탐정 블롭</div>
          </div>
          <div className="bs-divider" style={{ margin: '16px 0 8px' }} />
          <div style={{ maxHeight: 220, overflowY: 'auto' }}>
            {items.map((r) => (
              <div key={r.at} className="t-body2" style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0' }}>
                <span><span className="c-sub">{when(r.at)}</span> {byId(r.guardianId)?.name}</span>
                <span className="semi">{r.amount != null ? krw(r.amount) : '-'}</span>
              </div>
            ))}
          </div>
          <div className="bs-divider" style={{ margin: '8px 0 12px' }} />
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <span className="t-body1 bold">지킨 돈 {items.length}건</span>
            <span className="t-brand" style={{ fontSize: 26 }}>{krw(total)}</span>
          </div>
          <div className="t-caption c-orange semi" style={{ textAlign: 'right' }}>커피 {cups(total)}잔</div>
          <div className="t-caption" style={{ textAlign: 'center', marginTop: 12 }}>참아줘서 고마워! 다음에도 딱 3초만 같이 멈칫하자</div>
          <PrimaryButton style={{ width: '100%', marginTop: 16, gap: 6 }} onClick={save}>
            <InlineIcon icon={Download} />이미지로 저장
          </PrimaryButton>
        </BottomSheet>
      )}
    </>
  )
}
