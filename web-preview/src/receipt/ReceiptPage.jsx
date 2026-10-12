import { useEffect, useMemo, useState } from 'react'
import { PrimaryButton, SecondaryButton } from '../components/Kit'
import { POSES } from '../data/blobPose'
import ReceiptPaper from '../exhibit/ReceiptPaper'
import { receiptFromSearch } from '../exhibit/receiptLink'
import { drawReceipt } from './receiptImage'

// ════════════════════════════════════════════════════════════════
//  관람객 영수증 페이지 — 주소 /r (전시폰 영수증의 QR을 찍으면 관람객 폰에서 열려요)
//  서버 없이 주소의 값만 읽어서 전시폰과 같은 영수증(ReceiptPaper)을 그려요
//  [이미지로 저장] 1080×1920 PNG · [공유하기] 폰 공유창 (안 되는 브라우저면 숨김)
//  주소가 이상하면 오류 대신 "비상수단 부스에서 영수증을 받아봐"
// ════════════════════════════════════════════════════════════════
const FILE_NAME = '비상수단-영수증.png'

function canShareFiles() {
  try {
    const f = new File([new Blob(['x'], { type: 'image/png' })], 'x.png', { type: 'image/png' })
    return !!navigator.canShare?.({ files: [f] })
  } catch {
    return false
  }
}

export default function ReceiptPage() {
  const receipt = useMemo(() => receiptFromSearch(location.search), [])
  const [busy, setBusy] = useState(null) // 'save' | 'share' | null
  const [note, setNote] = useState('')
  const shareOk = useMemo(canShareFiles, [])

  useEffect(() => { document.title = '비상수단 영수증' }, [])

  const say = (t) => { setNote(t); setTimeout(() => setNote(''), 2500) }

  const save = async () => {
    setBusy('save')
    try {
      const blob = await drawReceipt(receipt)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = FILE_NAME
      document.body.appendChild(a)
      a.click()
      a.remove()
      setTimeout(() => URL.revokeObjectURL(url), 10000)
      say('저장했어! 사진첩이나 다운로드 폴더를 봐')
    } catch {
      say('저장이 안 됐어. 화면을 캡처해도 돼')
    }
    setBusy(null)
  }

  const share = async () => {
    setBusy('share')
    try {
      const blob = await drawReceipt(receipt)
      const file = new File([blob], FILE_NAME, { type: 'image/png' })
      await navigator.share({ files: [file], title: '비상수단 영수증' })
    } catch {
      // 공유창을 닫은 경우도 여기로 와요 — 조용히
    }
    setBusy(null)
  }

  return (
    <div style={{ minHeight: '100dvh', background: 'var(--grey-900)', fontFamily: 'var(--font)', display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '28px var(--side) 24px' }}>
      {receipt ? (
        <>
          <div className="t-title3 c-white">내 영수증</div>
          <ReceiptPaper receipt={receipt} style={{ marginTop: 14 }} />
          <div style={{ width: 300, marginTop: 20, display: 'flex', flexDirection: 'column', gap: 8 }}>
            <PrimaryButton onClick={save} disabled={!!busy}>{busy === 'save' ? '만드는 중…' : '이미지로 저장'}</PrimaryButton>
            {shareOk && <SecondaryButton on="dark" onClick={share}>{busy === 'share' ? '만드는 중…' : '공유하기'}</SecondaryButton>}
            <div className="t-caption" style={{ minHeight: 18, textAlign: 'center', color: 'var(--grey-300)' }}>{note}</div>
          </div>
        </>
      ) : (
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center' }}>
          <img src={POSES.curious} alt="" style={{ height: 140 }} />
          <div className="t-title3 c-white" style={{ marginTop: 16 }}>비상수단 부스에서 영수증을 받아봐</div>
        </div>
      )}
      <div style={{ flex: receipt ? 1 : 0, minHeight: 16 }} />
      <div className="t-caption" style={{ color: 'var(--grey-500)' }}>비상수단 · 사기 전에, 딱 3초</div>
    </div>
  )
}
