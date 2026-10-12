import { useState } from 'react'
import { RotateCcw } from 'lucide-react'
import Blob from '../components/Blob'
import { AppHeader } from '../components/AppChrome'
import { Card, Chip, ListRow, WeakButton } from '../components/Kit'
import { ALL_IMAGES, MOTIONS, SCENES } from '../data/blobPose'

// 캐릭터 테스트 (미리보기 전용): 그림 18장 × 움직임 7개 + 앱에서 쓰는 곳
// 화면 = 회색 바탕(.bs-screen) + 흰 카드 + 칩 — UI 키트 값만
export default function PoseLab({ onClose }) {
  const [image, setImage] = useState(ALL_IMAGES[0].src)
  const [motion, setMotion] = useState('breathe')
  const [mirrored, setMirrored] = useState(false)
  const [dark, setDark] = useState(false)
  const [key, setKey] = useState(0)
  const replay = () => setKey((k) => k + 1)
  const name = ALL_IMAGES.find((i) => i.src === image)?.name

  return (
    <div className="bs-screen kit-frame">
      <div className="kit-scroll">
        <AppHeader onBack={onClose} title="캐릭터 테스트" />
        <div className="bs-body">
          {/* 무대: 블롭 + 바닥선 */}
          <div style={{
            position: 'relative', height: 300, borderRadius: 'var(--r-card)', marginBottom: 12,
            display: 'flex', alignItems: 'flex-end', justifyContent: 'center', paddingBottom: 24,
            background: dark ? 'var(--grey-900)' : 'var(--white)', transition: 'background .25s var(--ease)',
          }}>
            <Blob look={{ image, motion }} width={240} mirrored={mirrored} motionKey={key} />
            <div style={{ position: 'absolute', left: 0, right: 0, bottom: 24, height: 1, background: dark ? 'var(--grey-700)' : 'var(--grey-200)' }} />
          </div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 8 }}>
            <span className="t-body2 semi">{name} · {MOTIONS[motion]}{mirrored ? ' · 좌우 반전' : ''}</span>
            <WeakButton onClick={replay}><RotateCcw size={16} strokeWidth={1.75} style={{ marginRight: 4 }} />다시 재생</WeakButton>
          </div>

          <h2 className="t-title3" style={{ margin: '28px 0 10px' }}>움직임</h2>
          <div style={{ display: 'flex', flexWrap: 'wrap', rowGap: 12 }}>
            {Object.entries(MOTIONS).map(([k, label]) => (
              <Chip key={k} on={k === motion} onClick={() => { setMotion(k); replay() }}>{label}</Chip>
            ))}
          </div>
          <div style={{ display: 'flex', marginTop: 12 }}>
            <Chip on={mirrored} onClick={() => setMirrored(!mirrored)}>좌우 반전</Chip>
            <Chip on={dark} onClick={() => setDark(!dark)}>어두운 바탕</Chip>
          </div>

          <h2 className="t-title3" style={{ margin: '28px 0 10px' }}>그림 {ALL_IMAGES.length}장</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 8 }}>
            {ALL_IMAGES.map((i) => {
              const on = i.src === image
              return (
                <button
                  key={i.name}
                  type="button"
                  className="ui-card-btn"
                  onClick={() => { setImage(i.src); replay() }}
                  style={{ background: on ? 'var(--orange-50)' : 'var(--white)', borderRadius: 'var(--r-thumb)', padding: 8, textAlign: 'center' }}
                >
                  <img src={i.src} alt={i.name} style={{ width: '100%', aspectRatio: '1100 / 1020', objectFit: 'contain', objectPosition: 'bottom' }} />
                  <span className={`t-small${on ? ' c-orange bold' : ' c-sub'}`} style={{ display: 'block', marginTop: 4, wordBreak: 'break-all' }}>
                    {i.name.replace('blob_', '')}
                  </span>
                </button>
              )
            })}
          </div>

          <h2 className="t-title3" style={{ margin: '28px 0 10px' }}>앱에서 쓰는 곳</h2>
          <Card>
            <p className="t-caption" style={{ margin: '0 0 4px' }}>표: src/data/blobPose.js</p>
            {Object.entries(SCENES).map(([k, s]) => (
              <ListRow
                key={k}
                avatar={s.image}
                title={s.label}
                subtitle={`${s.image.split('/').pop().replace(/\.(webp|svg|png)$/, '')} · ${MOTIONS[s.motion]}${k === 'peek' ? ' · 오른쪽 벽이면 반전' : ''}`}
                onClick={() => { setImage(s.image); setMotion(s.motion); setMirrored(k === 'peek'); replay() }}
              />
            ))}
          </Card>
        </div>
      </div>
    </div>
  )
}
