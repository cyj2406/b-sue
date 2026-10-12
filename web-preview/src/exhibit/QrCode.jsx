import { useMemo } from 'react'
import qrcode from 'qrcode-generator'

// QR 그림 — 흰 바탕 + 가장자리 흰 여백 quiet (카메라가 잘 읽게) · 인터넷 없이 폰 안에서 그려요
export default function QrCode({ text, size = 120, quiet = 8 }) {
  const svg = useMemo(() => {
    try {
      const qr = qrcode(0, 'M')
      qr.addData(text)
      qr.make()
      return qr.createSvgTag({ cellSize: 4, margin: 0, scalable: true }).replace('<svg ', '<svg width="100%" height="100%" ')
    } catch {
      return ''
    }
  }, [text])
  if (!svg) return null
  return (
    <div style={{ width: size + quiet * 2, height: size + quiet * 2, padding: quiet, background: 'var(--white)', borderRadius: 'var(--r-thumb)', flex: 'none', boxSizing: 'border-box' }}>
      <div style={{ width: size, height: size }} dangerouslySetInnerHTML={{ __html: svg }} />
    </div>
  )
}
