import { EX } from './exhibitTokens'
import { attractImage } from './attractImages'
import { RECEIPT_SUBTITLE, RECEIPT_TAIL, RECEIPT_TITLE, receiptQuote, receiptResultLines } from './receiptQuotes'
import { dateLabel, defenseNo, timeLabel } from './receiptLink'

// ════════════════════════════════════════════════════════════════
//  영수증 종이 한 장 — 2026-10-11 목업 · Galmuri 픽셀 스타일 · 종이는 키트 크림색
//  전시폰 화면(ScreenReceipt) · 관람객 페이지(/r)가 같이 써요. 저장 이미지(receipt/receiptImage.js)도 같은 배치
//  글자 위계: ① 제목 Galmuri Bold (종이 폭 약 55%) ② 지킨 돈 / 멈춘 시간 Galmuri Bold
//            ③ 방어 번호 Galmuri Bold ④ 본문 Nanum Gothic Coding
//            회색 작게: 부제 · 날짜·시간 · 머리줄 · 요원 줄 · 꼬리 글
//  점선 3개: 머리줄 아래 · 상품 묶음 아래 · 지킨 돈 묶음 아래 / 위아래 톱니
//  맨 아래: 랜덤 포즈(receipt.pose)와 그 포즈의 한마디 → 꼬리 글 — 문구는 모두 receiptQuotes.js
//  결과 줄은 receipt.result ('saved' | 'paused') 만 읽어요 (exhibit/result.js)
//  종이색 #EEEDE8 (exhibitTokens.js EX.receiptPaper — 저장 이미지도 같은 값)
// ════════════════════════════════════════════════════════════════
const won = (n) => n.toLocaleString('ko-KR')

export default function ReceiptPaper({ receipt, className = '', style }) {
  const { date, time, itemName, amount, guardian, no, pose } = receipt
  const res = receiptResultLines(receipt)
  return (
    <div className={`rc-paper ${className}`} style={{ background: EX.receiptPaper, ...style }}>
      <div className="rc-title">{RECEIPT_TITLE}</div>
      <div className="rc-sub rc-center">{RECEIPT_SUBTITLE}</div>

      <div className="rc-row rc-gap">
        <span className="rc-mid">방어 번호</span><span className="rc-mid">{defenseNo(no)}</span>
      </div>
      <div className="rc-row rc-sub"><span>{dateLabel(date)}</span><span>{timeLabel(time)}</span></div>

      <div className="rc-row rc-sub rc-gap"><span><span className="rc-qty">수량</span>항목</span><span>금액</span></div>
      <div className="rc-dash" />

      <div className="rc-row rc-body"><span><span className="rc-qty">1 x</span>{itemName}</span><span>{won(amount)}</span></div>
      <div className="rc-sub rc-indent">└ {guardian.name}</div>
      <div className="rc-sub rc-indent2">"{guardian.question}"</div>
      <div className="rc-dash" />

      <div className="rc-row rc-big"><span>{res.label}</span><span>{res.value}</span></div>
      <div className="rc-body rc-right">{res.note}</div>
      <div className="rc-dash" />

      <div className="rc-center" style={{ marginTop: 16 }}>
        <img src={attractImage(pose)} alt="" style={{ height: 72, width: 'auto', display: 'inline-block' }} />
      </div>
      <div className="rc-body rc-center" style={{ marginTop: 8, fontSize: 13 }}>"{receiptQuote(pose)}"</div>
      <div className="rc-sub rc-center rc-tail" style={{ marginTop: 8 }}>{RECEIPT_TAIL}</div>
    </div>
  )
}
