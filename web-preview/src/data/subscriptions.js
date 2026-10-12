// 곧 빠져나갈 돈 — 웹 미리보기용 데모 구독 (안드로이드에선 결제 알림에서 모아요)
// dday: 결제일까지 남은 날 · note: 두 번째 줄 회색 글씨
import { MISSION_ART } from './blobPose'

export const SUBSCRIPTIONS = [
  { id: 'stream', name: '스트림플러스', amount: 17000, dday: 1, note: '마지막 시청 41일 전', art: MISSION_ART.ghost_sub },
  { id: 'music', name: '음악 구독', amount: 10900, dday: 5, note: '10월 11일', art: MISSION_ART.auto_pay },
]
