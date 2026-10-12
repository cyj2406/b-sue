import { useEffect, useRef, useState } from 'react'
import { ASPECT, SCENES } from '../data/blobPose'

// 탐정 블롭 — 기준점 '아래 가운데'
//   scene: blobPose.js 의 SCENES 이름 (예: "home", "dispatch")
//   look:  { image, motion } 을 직접 줄 때 (테스트 화면 등)
//   mirrored: 좌우 반전 (빼꼼이 오른쪽에서 나올 때)
//   motionKey: 바뀌면 한 번짜리 움직임 다시 재생 (카운트 3→2→1)
export default function Blob({ scene, look, width = 120, mirrored = false, motionKey = 0, style }) {
  const current = look ?? SCENES[scene]
  const motion = current.motion

  // ── 크로스페이드: 그림이 바뀌면 새 그림을 위에 올리고 0.15초 동안 교차 ──
  const [layers, setLayers] = useState([{ src: current.image, id: 0 }])
  useEffect(() => {
    setLayers((prev) => {
      if (prev[prev.length - 1].src === current.image) return prev
      return [prev[prev.length - 1], { src: current.image, id: prev[prev.length - 1].id + 1 }]
    })
    const t = setTimeout(() => setLayers((prev) => prev.slice(-1)), 170)
    return () => clearTimeout(t)
  }, [current.image])

  // ── 그림이 바뀔 때 살짝 튀기 ──
  const popRef = useRef(null)
  const firstImage = useRef(true)
  useEffect(() => {
    if (firstImage.current) { firstImage.current = false; return }
    restart(popRef.current, 'blob-pop')
  }, [current.image])

  // ── 한 번짜리 움직임 다시 재생 (카운트 숫자마다 등) ──
  const motionRef = useRef(null)
  const entered = useRef(false)
  const [oneShot, setOneShot] = useState(motion === 'countPress' ? 'stopEnter' : motion)
  useEffect(() => {
    // 카운트: 처음엔 등장 squash & stretch, 그다음부터 숫자마다 꾹
    const next = motion === 'countPress' ? (entered.current ? 'countPress' : 'stopEnter') : motion
    entered.current = true
    setOneShot(next)
    restart(motionRef.current, null)
  }, [motion, motionKey])

  return (
    <div className="blob" style={{ width, height: width / ASPECT, ...style }}>
      <div ref={motionRef} className={`blob-motion m-${oneShot}`}>
        <div className={`blob-hop ${motion === 'breathe' ? 'h-on' : ''}`}>
          <div ref={popRef} className="blob-popwrap">
            <div style={{ position: 'absolute', inset: 0, transform: mirrored ? 'scaleX(-1)' : 'none' }}>
              {layers.map((layer, i) => (
                <img
                  key={layer.id}
                  src={layer.src}
                  alt="탐정 블롭"
                  draggable={false}
                  className={`blob-img ${layers.length > 1 ? (i === layers.length - 1 ? 'fade-in' : 'fade-out') : ''}`}
                />
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

// CSS 애니메이션을 처음부터 다시 틀기
function restart(el, className) {
  if (!el) return
  if (className) el.classList.remove(className)
  el.style.animation = 'none'
  void el.offsetWidth // 브라우저에 '지금 멈췄어' 알려주기
  el.style.animation = ''
  if (className) el.classList.add(className)
}
