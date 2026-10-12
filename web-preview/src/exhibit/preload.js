import { ATTRACT_LINES } from './attractLines'
import { attractImage } from './attractImages'

// ════════════════════════════════════════════════════════════════
//  영수증 화면 미리 불러오기 — 엔딩 화면이 뜰 때 한 번 (ExhibitApp)
//  영수증 글꼴(Galmuri11 Bold · Nanum Gothic Coding · Pretendard 600)과 캐릭터 포즈 9장을 미리 받아 둬서
//  [영수증 받기]를 누르는 순간 바로 그려지게. 실패해도 조용히 넘어가요.
// ════════════════════════════════════════════════════════════════
const FONTS = ['700 40px Galmuri11', '400 13px "Nanum Gothic Coding"', '600 17px Pretendard']
const SAMPLE = '비상수단 방어 번호 지킨 돈 멈춘 시간 0123456789#₩,:원초'

export function preloadReceipt() {
  try {
    FONTS.forEach((f) => document.fonts?.load(f, SAMPLE).catch(() => {}))
    ATTRACT_LINES.forEach(({ pose }) => {
      const img = new Image()
      img.src = attractImage(pose)
      img.decode?.().catch(() => {})
    })
  } catch {
    // 미리 못 불러와도 영수증은 그대로 떠요
  }
}
