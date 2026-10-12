import { SecondaryButton } from '../components/Kit'
import { EX } from './exhibitTokens'
import QrCode from './QrCode'

// ════════════════════════════════════════════════════════════════
//  QR 시트 — 영수증 화면 [내 폰에 저장하기] → 아래에서 0.25초 올라옴
//  뒤는 검정 55% 덮개(눌러도 닫힘) · 흰 배경 · 위 둥글기 28 · 안쪽 24 (exhibitTokens.js)
//  QR 190 (흰 바탕 · 검정 · 바깥 흰 여백) → 관람객 폰에서 /r 영수증 페이지 (저장 · 공유는 거기서)
// ════════════════════════════════════════════════════════════════
export default function ReceiptQrSheet({ url, onClose }) {
  return (
    <>
      <div className="ex-sheet-dim" onClick={onClose} />
      <div className="ex-sheet" role="dialog" aria-label="폰 카메라로 찍어줘">
        <div className="t-title2">폰 카메라로 찍어줘</div>
        <div className="t-body2" style={{ marginTop: 8, color: 'var(--grey-600)' }}>
          영수증이 네 폰에 열려.<br />저장이랑 공유는 거기서!
        </div>
        <div style={{ display: 'flex', justifyContent: 'center', margin: '24px 0' }}>
          <QrCode text={url} size={EX.qrSheetSize} quiet={16} />
        </div>
        <SecondaryButton style={{ width: '100%' }} onClick={onClose}>닫기</SecondaryButton>
      </div>
    </>
  )
}
