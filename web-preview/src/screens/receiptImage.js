import { byId, cups } from '../data/guardians'

// ════════════════════════════════════════════════════════════════
//  '안 지른 영수증' 이미지 저장 (PNG) — 기록 화면 [이미지로 저장]
//  색·글꼴은 UI 키트 변수에서 읽어요 (새 색 없음)
//  Black Han Sans 는 영수증 제목·합계에만 (키트 README 5)
// ════════════════════════════════════════════════════════════════
const krw = (n) => n.toLocaleString('ko-KR') + '원'
const p2 = (n) => String(n).padStart(2, '0')

export async function saveReceiptImage({ year, month, items, total }) {
  const css = getComputedStyle(document.documentElement)
  const v = (name) => css.getPropertyValue(name).trim()
  const C = { bg: v('--white'), ink: v('--grey-900'), sub: v('--grey-600'), line: v('--grey-200'), orange: v('--orange-500') }
  const F = { body: v('--font'), brand: v('--font-brand') }
  await document.fonts?.ready

  const W = 720
  const rowH = 64
  const H = 270 + items.length * rowH + 240
  const scale = 2
  const cv = document.createElement('canvas')
  cv.width = W * scale
  cv.height = H * scale
  const g = cv.getContext('2d')
  g.scale(scale, scale)
  g.fillStyle = C.bg
  g.fillRect(0, 0, W, H)

  const text = (s, x, y, { font, color = C.ink, align = 'left' }) => {
    g.font = font
    g.fillStyle = color
    g.textAlign = align
    g.fillText(s, x, y)
  }
  const line = (y) => {
    g.fillStyle = C.line
    g.fillRect(48, y, W - 96, 2)
  }

  text('비상수단', W / 2, 96, { font: `56px ${F.brand}`, align: 'center' })
  text('안 지른 것들의 계산서', W / 2, 140, { font: `700 26px ${F.body}`, align: 'center' })
  text(`${year}년 ${month}월 · 담당 탐정 블롭`, W / 2, 178, { font: `400 22px ${F.body}`, color: C.sub, align: 'center' })
  line(220)

  let y = 270
  for (const r of [...items].sort((a, b) => a.at - b.at)) {
    const d = new Date(r.at)
    text(`${p2(d.getMonth() + 1)}/${p2(d.getDate())} ${p2(d.getHours())}:${p2(d.getMinutes())}`, 48, y, { font: `400 22px ${F.body}`, color: C.sub })
    text(byId(r.guardianId)?.name ?? '', 220, y, { font: `500 24px ${F.body}` })
    text(r.amount != null ? krw(r.amount) : '-', W - 48, y, { font: `600 24px ${F.body}`, align: 'right' })
    y += rowH
  }
  line(y - 24)

  text(`지킨 돈 ${items.length}건`, 48, y + 40, { font: `700 26px ${F.body}` })
  text(krw(total), W - 48, y + 48, { font: `52px ${F.brand}`, align: 'right' })
  text(`커피 ${cups(total)}잔`, W - 48, y + 92, { font: `600 24px ${F.body}`, color: C.orange, align: 'right' })
  text('참아줘서 고마워! 다음에도 딱 3초만 같이 멈칫하자', W / 2, y + 170, { font: `500 22px ${F.body}`, color: C.sub, align: 'center' })
  text('* 이 영수증은 결제 영수증이 아니에요 *', W / 2, y + 206, { font: `400 18px ${F.body}`, color: C.sub, align: 'center' })

  const blob = await new Promise((res, rej) => cv.toBlob((b) => (b ? res(b) : rej(new Error('toBlob'))), 'image/png'))
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `안지른영수증-${year}-${p2(month)}.png`
  document.body.appendChild(a)
  a.click()
  a.remove()
  setTimeout(() => URL.revokeObjectURL(a.href), 1000)
}
