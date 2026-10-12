import { useState } from 'react'
import { AppHeader } from '../components/AppChrome'
import { Avatar, BottomSheet, Chip, PrimaryButton, Progress } from '../components/Kit'
import { MISSION_ART } from '../data/blobPose'
import { guardians } from '../data/guardians'

// 지출 수사대 — reference ②: 진행률 + 칩 + 2열 카드
// 만난 요원: 톡 → 미션 시트 / 못 만난 요원: 톡 → '아직 못 만난 요원이야. 그 순간이 오면 나타날 거야'
// 미리보기: 카드 우클릭 → 해금 연출
const no = (g) => `No.${String(g.number).padStart(2, '0')}`

export default function Squad({ unlocked, holds, onLocked, onShowUnlock }) {
  const [filter, setFilter] = useState('전체')
  const [open, setOpen] = useState(null)
  const met = unlocked.length
  const pct = Math.round((met / guardians.length) * 100)
  const list = filter === '전체' ? guardians : guardians.filter((g) => g.type === filter)
  const count = (type) => guardians.filter((g) => g.type === type).length

  return (
    <>
      <AppHeader title="지출 수사대" />
      <div className="bs-body" style={{ paddingBottom: 24 }}>
        {/* 진행률 */}
        <div className="bs-card" style={{ padding: '18px 20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 10 }}>
            <span className="t-body1 bold">{guardians.length}명 중 {met}명 합류</span>
            <span className="t-caption c-orange semi">{pct}%</span>
          </div>
          <Progress value={met / guardians.length} />
        </div>

        {/* 칩 */}
        <div style={{ margin: '2px 0 12px' }}>
          {['전체', '충동', '누수'].map((c) => (
            <Chip key={c} on={filter === c} onClick={() => setFilter(c)}>{c === '전체' ? c : `${c} ${count(c)}`}</Chip>
          ))}
        </div>

        {/* 2열 카드 */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          {list.map((g) => {
            const on = unlocked.includes(g.id)
            const held = holds[g.id] ?? 0
            return (
              <button
                key={g.id}
                type="button"
                className="bs-card ui-card-btn"
                style={{ margin: 0, padding: '14px 14px 16px' }}
                onClick={() => (on ? setOpen(g) : onLocked(g))}
                onContextMenu={(e) => { e.preventDefault(); onShowUnlock(g) }}
                aria-label={on ? `${no(g)} ${g.name}` : `${no(g)} 아직 못 만난 요원`}
              >
                <div style={{ height: 104, display: 'flex', alignItems: 'flex-end', justifyContent: 'center' }}>
                  <img src={MISSION_ART[g.id]} alt="" style={{ maxHeight: 100, maxWidth: 124, filter: on ? undefined : 'brightness(0) opacity(.08)' }} />
                </div>
                <div className="t-caption" style={{ marginTop: 10 }}>{no(g)}{!on && held > 0 ? ` · 참기 ${held}/3` : ''}</div>
                <div className={`t-body2 bold${on ? '' : ' c-weak'}`}>{on ? g.name : '아직 못 만났어'}</div>
              </button>
            )
          })}
        </div>
      </div>

      {/* 만난 요원 → 미션 */}
      {open && (
        <BottomSheet onDismiss={() => setOpen(null)}>
          <div className="bs-row" style={{ paddingTop: 0 }}>
            <Avatar src={MISSION_ART[open.id]} large />
            <div className="main">
              <div className="t-caption">{no(open)} · {open.type}</div>
              <div className="t-title3">{open.name}</div>
            </div>
          </div>
          <div className="bs-card tint" style={{ margin: '8px 0 0', padding: 16 }}>
            <div className="t-body1 bold">{open.question}</div>
            <div className="t-body2" style={{ marginTop: 4 }}>{open.mission}</div>
          </div>
          <div className="t-caption" style={{ marginTop: 12 }}>출동 신호 · {open.trigger}</div>
          <PrimaryButton style={{ width: '100%', marginTop: 20 }} onClick={() => setOpen(null)}>확인</PrimaryButton>
        </BottomSheet>
      )}
    </>
  )
}
