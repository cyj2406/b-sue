import LevelUp from '../components/LevelUp'
import { NotificationBanner } from '../exhibit/parts/PhoneParts'

// 해금 연출 — reference ⑨ 구조 (같은 수법을 3번 참으면)
export function Unlock({ guardian, onDone }) {
  return (
    <LevelUp
      sub="새 요원이 합류했어"
      title="같은 수법을 3번이나 참았어"
      guardian={guardian}
      bubble={guardian.question}
      caption="수사대 탭에서 언제든 다시 볼 수 있어"
      primary="수사대에 넣기"
      onPrimary={onDone}
    />
  )
}

// 가짜 카드 승인 알림 (화면 위에서 내려오는 알림처럼) — 전시와 같은 알림 배너
export function FakeNotification({ amount, merchant }) {
  return (
    <NotificationBanner
      floating
      app="비상카드"
      title={`[비상카드] 승인 ${amount.toLocaleString('ko-KR')}원`}
      body={`${merchant} · 일시불`}
    />
  )
}
