import { useEffect, useMemo, useRef, useState } from 'react'
import { PrimaryButton, TextButton } from '../components/Kit'
import ReceiptPaper from './ReceiptPaper'
import ReceiptQrSheet from './ReceiptQrSheet'
import { receiptUrl } from './receiptLink'
import { RECEIPT_SHEET_IDLE, RECEIPT_WAIT_QR } from './timing'

// ════════════════════════════════════════════════════════════════
//  전시폰 화면 영수증 (2026-10-11 개편 · DESIGN.md 11장 '영수증 화면')
//  · 종이: 상태바 아래 ~ 하단 버튼 사이 공간의 가운데 (넘치면 위 24 · 버튼과 최소 24)
//  · 등장: 최종 위치에서 위 → 아래로 프린터처럼 0.8초 (2초 뒤 settled — 무조건 보이게)
//  · 하단: [내 폰에 저장하기](주) + 8 + [처음으로](텍스트) — 버튼 안에 숫자 없음
//  · [내 폰에 저장하기] → QR 시트 (ReceiptQrSheet) — 관람객 폰 카메라로 /r 페이지 열기
//  · 자동 복귀: 숫자 없이 20초 무조작 → 대기 화면 (만지면 다시) · 시트가 열리면 멈추고 닫으면 다시 20초부터
//    시트가 열린 채 30초 무조작 → 대기 화면 (timing.js)
// ════════════════════════════════════════════════════════════════
export default function ScreenReceipt({ receipt, onDone }) {
  const [settled, setSettled] = useState(false)
  const [sheet, setSheet] = useState(false)
  const [touch, setTouch] = useState(0) // 만질 때마다 +1 → 시계 다시
  const url = useMemo(() => receiptUrl(receipt), [receipt])
  const done = useRef(onDone)
  done.current = onDone

  useEffect(() => {
    const t = setTimeout(() => setSettled(true), 2000)
    return () => clearTimeout(t)
  }, [])

  // 숨은 자동 복귀: 시트 닫힘 → 20초 / 시트 열림 → 30초 무조작
  useEffect(() => {
    const t = setTimeout(() => done.current(), (sheet ? RECEIPT_SHEET_IDLE : RECEIPT_WAIT_QR) * 1000)
    return () => clearTimeout(t)
  }, [sheet, touch])

  return (
    <div
      className={`ex-receipt${settled ? ' settled' : ''}`}
      onPointerDown={() => setTouch((n) => n + 1)}
      style={{ position: 'absolute', inset: 0, background: 'var(--grey-900)', display: 'flex', flexDirection: 'column' }}
    >
      {/* 종이: 남는 공간 가운데 (위치는 시트가 열리고 닫혀도 그대로) */}
      <div className="ex-receipt-space">
        <ReceiptPaper receipt={receipt} className="ex-receipt-print" />
      </div>

      {!settled && (
        <div className="ex-receipt-loading" role="status" aria-live="polite">
          <span className="ex-receipt-spinner" aria-hidden="true" />
          영수증을 준비하고 있어요
        </div>
      )}

      {/* 하단: 주 버튼 위, 텍스트 버튼 아래 (사이 8) */}
      <div className="ex-btn-stack" style={{ flex: 'none', padding: '0 var(--ex-side) var(--ex-cta-bottom)' }}>
        <PrimaryButton style={{ width: '100%' }} onClick={() => setSheet(true)}>내 폰에 저장하기</PrimaryButton>
        <TextButton style={{ color: 'var(--grey-400)' }} onClick={onDone}>처음으로</TextButton>
      </div>

      {sheet && <ReceiptQrSheet url={url} onClose={() => setSheet(false)} />}
    </div>
  )
}
