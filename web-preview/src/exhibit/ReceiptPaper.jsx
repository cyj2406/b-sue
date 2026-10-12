import { EX } from './exhibitTokens'
import { RECEIPT_CHARACTER, RECEIPT_SIGNOFF } from './receiptQuotes'
import { dateLabel, defenseNo, timeLabel } from './receiptLink'

// ════════════════════════════════════════════════════════════════
//  영수증 종이 한 장 — 2026-10-11 목업 · Galmuri 픽셀 스타일 · 종이는 키트 크림색
//  전시폰 화면(ScreenReceipt) · 관람객 페이지(/r)가 같이 써요. 저장 이미지(receipt/receiptImage.js)도 같은 배치
//  글자 위계: ① 제목 Galmuri Bold (종이 폭 약 55%) ② 지킨 돈 / 쓴 돈 Galmuri Bold
//            ③ 방어 번호 Galmuri Bold ④ 본문 Nanum Gothic Coding
//            회색 작게: 부제 · 날짜·시간 · 머리줄 · 요원 줄 · 꼬리 글
//  점선 3개: 머리줄 아래 · 상품 묶음 아래 · 지킨 돈 묶음 아래 / 위아래 톱니
//  맨 아래: 공통 블롭 캐릭터와 비상수단의 한마디 (receiptQuotes.js) → 꼬리 글
//  종이색 #EEEDE8 (exhibitTokens.js EX.receiptPaper — 저장 이미지도 같은 값)
// ════════════════════════════════════════════════════════════════
const won = (n) => n.toLocaleString('ko-KR')

export default function ReceiptPaper({ receipt, className = '', style }) {
  const { date, time, itemName, amount, held, guardian, cups, no } = receipt
  return (
    <div className={`rc-paper ${className}`} style={{ background: EX.receiptPaper, ...style }}>
      <div className="rc-title">비상수단</div>
      <div className="rc-sub rc-center">오늘의 소비 기록</div>

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

      {held ? (
        <>
          <div className="rc-row rc-big"><span>지킨 돈 :</span><span>{won(amount)}원</span></div>
          <div className="rc-body rc-right">= 커피 {cups}잔</div>
        </>
      ) : (
        <>
          <div className="rc-row rc-big"><span>쓴 돈 :</span><span>{won(amount)}원</span></div>
          <div className="rc-sub rc-right">다음엔 결제 전에 다시 살펴보자</div>
        </>
      )}
      <div className="rc-dash" />

      <div className="rc-center" style={{ marginTop: 16 }}>
        <img src={RECEIPT_CHARACTER} alt="" style={{ height: 72, width: 'auto', display: 'inline-block' }} />
      </div>
      <div className="rc-body rc-center" style={{ marginTop: 8, fontSize: 13 }}>"{RECEIPT_SIGNOFF}"</div>
      <div className="rc-sub rc-center rc-tail" style={{ marginTop: 8 }}>결제되지 않은 영수증 · 사기 전에, 딱 3초</div>
    </div>
  )
}
