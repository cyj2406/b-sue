import { soundOn } from './exhibitStore'

// 알림 '띵동' — 소리 파일 없이 브라우저가 직접 만들어요 (운영자 메뉴에서 끌 수 있음)
// 관람객이 화면을 한 번 누른 뒤라야 소리가 나요 (브라우저 규칙). 안 나도 조용히 넘어가요.
let ctx = null
export function playDing() {
  if (!soundOn()) return
  try {
    ctx = ctx ?? new (window.AudioContext || window.webkitAudioContext)()
    if (ctx.state === 'suspended') ctx.resume()
    const t = ctx.currentTime
    ;[[880, 0], [1320, 0.12]].forEach(([freq, at]) => {
      const osc = ctx.createOscillator()
      const gain = ctx.createGain()
      osc.type = 'sine'
      osc.frequency.value = freq
      gain.gain.setValueAtTime(0, t + at)
      gain.gain.linearRampToValueAtTime(0.18, t + at + 0.01)
      gain.gain.exponentialRampToValueAtTime(0.001, t + at + 0.35)
      osc.connect(gain).connect(ctx.destination)
      osc.start(t + at)
      osc.stop(t + at + 0.4)
    })
  } catch {
    // 소리가 안 나도 체험은 그대로
  }
}
