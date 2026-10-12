import { RECEIPT_CHARACTER, RECEIPT_SIGNOFF } from '../exhibit/receiptQuotes'
import { dateLabel, defenseNo, timeLabel } from '../exhibit/receiptLink'
import { EX } from '../exhibit/exhibitTokens'

// ════════════════════════════════════════════════════════════════
//  관람객 영수증 → 1080×1920 이미지 (배경 포함) — /r 페이지의 [이미지로 저장] · [공유하기]
//  화면의 ReceiptPaper(Galmuri 픽셀 스타일) 배치를 2.8배로 그대로 옮겨 그려요
//  (줄 높이 · 여백은 styles/exhibit.css 의 .rc-* 값과 같아요 — 한쪽을 바꾸면 다른 쪽도)
// ════════════════════════════════════════════════════════════════
const W = 1080, H = 1920
const K = 2.8                       // 화면 종이 300px → 840px
const PAPER_W = 300 * K, PAD_X = 20 * K, PAD_Y = 32 * K, TOOTH_W = 12 * K, TOOTH_H = 6 * K
const C = {
  bg: '#191F28',      // --grey-900
  paper: EX.receiptPaper, // #EEEDE8 (화면 종이와 같은 값)
  ink: '#1D1D1D',
  sub: '#666666',
  dash: '#9A9A9A',
  foot: '#8B95A1',    // --grey-500
}
const PIXEL = '"Galmuri11", "Pretendard", sans-serif'
const MONO = '"Nanum Gothic Coding", monospace'
const f = (weight, px, family) => `${weight} ${Math.round(px * K)}px ${family}`
const won = (n) => n.toLocaleString('ko-KR')

async function loadImage(src) {
  const img = new Image()
  img.src = src
  await img.decode()
  return img
}

export async function drawReceipt(rc) {
  try {
    await Promise.all([f(700, 41, PIXEL), f(700, 22, PIXEL), f(400, 14, MONO), f(400, 13, MONO)]
      .map((font) => document.fonts.load(font, '비상수단 방어 번호 지킨 돈 0123456789')))
  } catch { /* 글꼴이 늦어도 그려요 */ }
  const pose = await loadImage(RECEIPT_CHARACTER).catch(() => null)

  // 줄 목록 (화면 px 기준 높이 · 위 여백)
  const T = { title: [700, 41, PIXEL, C.ink], sub: [400, 13, MONO, C.sub], body: [400, 14, MONO, C.ink], closing: [400, 13, MONO, C.ink], mid: [700, 15, PIXEL, C.ink], big: [700, 22, PIXEL, C.ink] }
  const rows = [
    { h: 45, style: 'title', center: '비상수단' },
    { h: 20, style: 'sub', center: '오늘의 소비 기록' },
    { h: 22, top: 16, style: 'mid', left: '방어 번호', right: defenseNo(rc.no) },
    { h: 20, style: 'sub', left: dateLabel(rc.date), right: timeLabel(rc.time) },
    { h: 20, top: 16, style: 'sub', left: '수량', left2: '항목', right: '금액' },
    { dash: true },
    { h: 20, style: 'body', left: '1 x', left2: rc.itemName, right: won(rc.amount) },
    { h: 20, top: 2, style: 'sub', indent: 36, left: `└ ${rc.guardian.name}` },
    { h: 20, style: 'sub', indent: 44, left: `"${rc.guardian.question}"` },
    { dash: true },
    rc.held
      ? { h: 30, top: 2, style: 'big', left: '지킨 돈 :', right: `${won(rc.amount)}원` }
      : { h: 30, top: 2, style: 'big', left: '쓴 돈 :', right: `${won(rc.amount)}원` },
    { h: 20, style: rc.held ? 'body' : 'sub', right: rc.held ? `= 커피 ${rc.cups}잔` : '다음엔 결제 전에 다시 살펴보자' },
    { dash: true },
    { h: 72, top: 16, image: true },
    { h: 20, top: 8, style: 'closing', center: `"${RECEIPT_SIGNOFF}"` },
    { h: 20, top: 8, style: 'sub', center: '결제되지 않은 영수증 · 사기 전에, 딱 3초' },
  ]
  const heightOf = (r) => (r.dash ? 21 : (r.top ?? 0) + r.h) * K
  const paperH = PAD_Y * 2 + rows.reduce((s, r) => s + heightOf(r), 0)

  const canvas = document.createElement('canvas')
  canvas.width = W
  canvas.height = H
  const g = canvas.getContext('2d')
  g.fillStyle = C.bg
  g.fillRect(0, 0, W, H)

  // 종이 + 위아래 톱니 (12px 마다 높이 6px 삼각형)
  const x0 = (W - PAPER_W) / 2
  const y0 = Math.max(60, (H - 120 - paperH) / 2)
  g.fillStyle = C.paper
  g.beginPath()
  g.moveTo(x0, y0 + TOOTH_H)
  for (let x = x0; x < x0 + PAPER_W - 0.5; x += TOOTH_W) { g.lineTo(x + TOOTH_W / 2, y0); g.lineTo(x + TOOTH_W, y0 + TOOTH_H) }
  g.lineTo(x0 + PAPER_W, y0 + paperH - TOOTH_H)
  for (let x = x0 + PAPER_W; x > x0 + 0.5; x -= TOOTH_W) { g.lineTo(x - TOOTH_W / 2, y0 + paperH); g.lineTo(x - TOOTH_W, y0 + paperH - TOOTH_H) }
  g.closePath()
  g.fill()

  const L = x0 + PAD_X, R = x0 + PAPER_W - PAD_X, MID = W / 2, QTY = 36 * K
  let y = y0 + PAD_Y
  g.textBaseline = 'middle'
  for (const r of rows) {
    if (r.dash) {
      const dy = y + 10.5 * K
      g.strokeStyle = C.dash; g.lineWidth = K; g.setLineDash([3 * K, 3 * K])
      g.beginPath(); g.moveTo(L, dy); g.lineTo(R, dy); g.stroke(); g.setLineDash([])
      y += heightOf(r)
      continue
    }
    const cy = y + ((r.top ?? 0) + r.h / 2) * K
    if (r.image) {
      if (pose) {
        const ih = r.h * K, iw = ih * (pose.naturalWidth / pose.naturalHeight)
        g.drawImage(pose, MID - iw / 2, y + (r.top ?? 0) * K, iw, ih)
      }
    } else {
      const [weight, px, family, color] = T[r.style]
      g.font = f(weight, px, family)
      g.fillStyle = color
      if (r.center) { g.textAlign = 'center'; g.fillText(r.center, MID, cy) }
      if (r.left) { g.textAlign = 'left'; g.fillText(r.left, L + (r.indent ?? 0) * K, cy) }
      if (r.left2) { g.textAlign = 'left'; g.fillText(r.left2, L + QTY, cy) }
      if (r.right) { g.textAlign = 'right'; g.fillText(r.right, R, cy) }
    }
    y += heightOf(r)
  }

  // 맨 아래 작은 글씨
  g.fillStyle = C.foot; g.font = `400 34px "Pretendard", sans-serif`; g.textAlign = 'center'
  g.fillText('비상수단 · 사기 전에, 딱 3초', MID, H - 80)

  return new Promise((resolve) => canvas.toBlob(resolve, 'image/png'))
}
