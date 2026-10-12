// ════════════════════════════════════════════════════════════════
//  전시 설정 — 영수증 QR에 넣을 주소 (여기 한 곳만 바꾸면 돼요)
//
//  RECEIPT_SITE: Netlify 에 올린 사이트 주소 (끝에 / 없이)
//   · Netlify → Site configuration → Change site name 에서 이름을 'bsue-exhibit' 으로 바꾸면 아래 그대로 쓰면 돼요
//   · 다른 이름이 되면 여기 주소만 그 이름으로 고쳐서 다시 빌드 → 다시 올리기
//   · 빈 글자('')로 두면 지금 열린 주소를 그대로 써요 (컴퓨터 미리보기 확인용)
// ════════════════════════════════════════════════════════════════
export const RECEIPT_SITE = 'https://bsue-exhibit.netlify.app'

// 관람객 영수증 페이지 경로 (Netlify 의 public/_redirects 덕분에 바로 열려요)
export const RECEIPT_PATH = '/r'

export function receiptBaseUrl() {
  const site = (RECEIPT_SITE || location.origin).replace(/\/+$/, '')
  return site + RECEIPT_PATH
}
