import { useRef, useState } from 'react'

// ════════════════════════════════════════════════════════════════
//  처리 중 표시 1단계 — 버튼을 누르면 (DESIGN.md '처리 중 표시')
//   · 누른 버튼: 살짝 눌림(0.98) → 글자 대신 점 3개
//   · 나머지 버튼: 흐려지고(0.4) 안 눌려요 · 두 번 눌러도 한 번만
//   · delay 초 뒤 run()
//  쓰는 법: const { press, cls, label } = usePressOnce(0.3)
//          <button className={'bs-btn primary' + cls('ok')} onClick={() => press('ok', go)}>{label('ok', '참았다!')}</button>
// ════════════════════════════════════════════════════════════════
export function Dots() {
  return <span className="btn-dots" aria-hidden="true"><i /><i /><i /></span>
}

export function usePressOnce(delay = 0.3) {
  const [pressed, setPressed] = useState(null)
  const done = useRef(false)
  const press = (id, run) => {
    if (done.current) return
    done.current = true
    setPressed(id)
    setTimeout(run, delay * 1000)
  }
  const cls = (id) => (pressed == null ? '' : pressed === id ? ' is-loading' : ' is-dimmed')
  const label = (id, text) => (pressed === id ? <><Dots /><span className="sr-only" style={{ position: 'absolute', width: 1, height: 1, overflow: 'hidden', clip: 'rect(0 0 0 0)' }}>{text}</span></> : text)
  return { press, cls, label, pressed }
}
