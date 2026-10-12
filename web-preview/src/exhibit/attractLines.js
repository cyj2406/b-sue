// ════════════════════════════════════════════════════════════════
//  대기 화면 말풍선 + 포즈 표 — 이 순서대로 8초마다 바뀌고, 끝나면 처음부터 (간격은 timing.js ATTRACT_ROTATE)
//  pose: src/assets/attract/ 안의 그림 이름 (.png 빼고) · moment: 대기 화면 '블롭이 나타나는 순간' 카드의 상황 한마디 (캐릭터 이름 아님) · text: 말풍선 문구
//  순서를 바꾸려면 줄을 옮기고, 문구를 바꾸려면 text 만 고치면 돼요
// ════════════════════════════════════════════════════════════════
export const ATTRACT_LINES = [
  { pose: 'pose_sale', moment: '세일할 때', text: '세일이라서 산 적 있는 사람?' },
  { pose: 'pose_cart', moment: '장바구니 앞', text: '장바구니에 며칠째 담아둔 사람?' },
  { pose: 'pose_delivery_lock', moment: '배달 시킬 때', text: '배달앱 쿠폰 오면 못 참는 사람?' },
  { pose: 'pose_two_plus_one', moment: '1+1 볼 때', text: '1+1이면 일단 집는 사람?' },
  { pose: 'pose_free_shipping', moment: '무료배송 앞', text: '무료배송 맞추려고 더 담는 사람?' },
  { pose: 'pose_closet', moment: '옷 살 때', text: '비슷한 옷 또 산 사람?' },
  { pose: 'pose_fridge', moment: '장 볼 때', text: '냉장고에 뭐 있는지 모르는 사람?' },
  { pose: 'pose_autopay', moment: '구독 결제일', text: '안 보는 구독 있는 사람?' },
  { pose: 'pose_walk', moment: '택시 부를 때', text: '택시 부르기 전, 5분 걸어볼 사람?' },
]
