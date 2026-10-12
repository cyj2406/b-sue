import { useState } from 'react'
import { ButtonRow, Divider, ListRow, WeakButton } from '../components/Kit'
import { archiveAndResetToday, getArchive, setSoundOn, soundOn, todayStats } from './exhibitStore'
import { READY, SCENARIOS, SCENARIO_ORDER } from './scenarios'

// ════════════════════════════════════════════════════════════════
//  운영자 메뉴 (시작 화면 로고 5번 탭) — 기획서 9번
//  ① 오늘 카운터 → 날짜별 저장 후 초기화 (하루 끝)  ② 시나리오 바로 실행
//  ③ 알림 소리 켜기/끄기 (진동은 항상)  ④ 저장한 날짜들  ⑤ 전시 모드 끄기
//  (영수증은 프린터 없이 화면으로 — 2026-10-06 결정)
//  관람객에게는 안 보이는 메뉴
// ════════════════════════════════════════════════════════════════
const krw = (n) => n.toLocaleString('ko-KR') + '원'
const statLine = (s) => (s.count ? `${s.count}명 체험 · 참은 비율 ${s.heldRate}%` : '아직 체험 없음')

export default function OperatorMenu({ onRun, onToast, onCounterChanged, onExitExhibit, onClose }) {
  const [stats, setStats] = useState(todayStats)
  const [archive, setArchive] = useState(getArchive)
  const [sound, setSound] = useState(soundOn)
  const [confirmReset, setConfirmReset] = useState(false) // 한 번 더 눌러야 초기화

  const archiveToday = () => {
    if (!confirmReset) { setConfirmReset(true); return }
    archiveAndResetToday()
    setStats(todayStats())
    setArchive(getArchive())
    setConfirmReset(false)
    onCounterChanged()
    onToast('오늘 카운터를 저장하고 0으로 돌렸어요')
  }

  return (
    <>
      <div className="bs-dim kit-fade" onClick={onClose} />
      <div className="bs-sheet kit-sheet-up" style={{ maxHeight: '88%', overflowY: 'auto' }}>
        <div className="handle" />
        <div className="t-title3">운영자 메뉴</div>
        <div className="t-caption" style={{ marginBottom: 8 }}>관람객에게는 보이지 않는 메뉴예요.</div>

        {/* ① 오늘 카운터 */}
        <ListRow
          title={`오늘 ${krw(stats.saved)}`}
          titleBold
          subtitle={statLine(stats)}
          end={<WeakButton onClick={archiveToday}>{confirmReset ? '정말 초기화' : '저장 후 초기화'}</WeakButton>}
        />
        <Divider />

        {/* ② 시나리오 바로 실행 */}
        <div className="t-body2 semi" style={{ margin: '14px 0 8px' }}>시나리오 바로 실행</div>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 14 }}>
          {SCENARIO_ORDER.filter((id) => READY[id]).map((id) => (
            <WeakButton key={id} onClick={() => onRun(id)}>{SCENARIOS[id].card.tag} · {SCENARIOS[id].card.time}</WeakButton>
          ))}
        </div>
        <Divider />

        {/* ③ 소리 */}
        <ListRow
          title="알림 소리"
          subtitle="꺼도 진동은 그대로예요"
          end={<WeakButton onClick={() => { setSoundOn(!sound); setSound(!sound) }}>{sound ? '켜짐' : '꺼짐'}</WeakButton>}
        />

        {/* ④ 저장한 날짜들 (포트폴리오 '결과' 페이지용) */}
        {archive.length > 0 && (
          <>
            <Divider />
            <div className="t-body2 semi" style={{ margin: '14px 0 2px' }}>저장한 날짜</div>
            {archive.map((a) => (
              <ListRow key={a.savedAt} title={a.date} subtitle={statLine(a)} end={krw(a.saved)} />
            ))}
          </>
        )}

        <div style={{ marginTop: 16 }}>
          <ButtonRow secondary="닫기" onSecondary={onClose} primary="전시 모드 끄기" onPrimary={onExitExhibit} />
        </div>
      </div>
    </>
  )
}
