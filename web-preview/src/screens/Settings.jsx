import { useEffect, useState } from 'react'
import { ChevronDown, ChevronUp, Minus, Plus } from 'lucide-react'
import { AppHeader, IconButton } from '../components/AppChrome'
import { ButtonRow, Card, Chevron, Divider, ListRow, WeakButton } from '../components/Kit'
import { SUBSCRIPTIONS } from '../data/subscriptions'
import { cups } from '../data/guardians'

// 설정 — 홈 오른쪽 위 아이콘으로 들어오는 하위 화면
// 카드 순서: ① 출동 권한 ② 출동 규칙 ③ 매달 나가는 돈 ④ 지켜보는 앱 ⑤ 전시 모드 ⑥ 미리보기 도구(접힘)
// 웹에선 권한·규칙을 흉내만 내고 브라우저에 저장해요 (진짜 동작은 안드로이드 SettingsScreen.kt)

const krw = (n) => n.toLocaleString('ko-KR') + '원'

// 브라우저 저장 (App.jsx 의 useStored 와 같은 방식)
function useStored(key, initial) {
  const [value, setValue] = useState(() => {
    try { return { ...initial, ...JSON.parse(localStorage.getItem(key)) } } catch { return initial }
  })
  useEffect(() => { try { localStorage.setItem(key, JSON.stringify(value)) } catch { /* 저장 못 해도 화면은 그대로 */ } }, [key, value])
  return [value, setValue]
}

const PERMISSIONS = [
  ['overlay', '다른 앱 위에 표시', '쇼핑·배달앱 위에 내가 떠 있으려면 필요해'],
  ['access', '앱·결제 화면 알아보기', '결제 직전에 출동하려면 필요해 (접근성)'],
  ['notice', '결제 알림 읽기', '카드 승인 알림이 오면 배웅해. 금액·가게·날짜만 폰에 남겨'],
]

const WATCHED = ['쿠팡', '배달의민족', '무신사', '카카오 T', '29CM', '요기요']

const today = () => new Date().toLocaleDateString('sv-SE')

// confirm: 바텀시트로 한 번 더 묻기 → App.jsx 가 { title, body, action, label } 을 받아서 띄워요
export default function Settings({ onBack, onStartExhibit, onResetExhibit, onConfirm, onToast, tools }) {
  const [perms, setPerms] = useStored('blob.permissions', { overlay: true, access: true, notice: false })
  const [rules, setRules] = useStored('blob.rules', { perDay: 5, restDate: '' })
  const [toolsOpen, setToolsOpen] = useState(false)
  const resting = rules.restDate === today()
  const monthly = SUBSCRIPTIONS.reduce((s, x) => s + x.amount, 0)

  return (
    <div className="bs-screen kit-frame">
      <div className="kit-scroll">
        <AppHeader onBack={onBack} title="설정" />
        <div className="bs-body" style={{ paddingBottom: 40 }}>

          {/* ① 출동 권한 — 켜진 건 회색 '켜짐', 꺼진 건 주황 '켜러 가기' */}
          <Card title="출동 권한">
            {PERMISSIONS.map(([id, title, desc]) => (
              <ListRow
                key={id}
                title={title}
                subtitle={desc}
                end={perms[id]
                  ? <span className="t-body2 c-weak">켜짐</span>
                  : <WeakButton onClick={() => { setPerms({ ...perms, [id]: true }); onToast('켜졌어! (미리보기에선 흉내만 내)') }}>켜러 가기</WeakButton>}
              />
            ))}
          </Card>

          {/* ② 출동 규칙 */}
          <Card title="출동 규칙">
            <ListRow
              title="하루 최대 출동"
              subtitle="하루에 내가 나오는 최대 횟수야"
              end={
                <div style={{ display: 'flex', alignItems: 'center' }}>
                  <IconButton icon={Minus} size={20} label="한 번 줄이기" onClick={() => setRules({ ...rules, perDay: Math.max(1, rules.perDay - 1) })} />
                  <span className="t-body1 bold" style={{ minWidth: 32, textAlign: 'center' }}>{rules.perDay}번</span>
                  <IconButton icon={Plus} size={20} label="한 번 늘리기" edge="right" onClick={() => setRules({ ...rules, perDay: Math.min(10, rules.perDay + 1) })} />
                </div>
              }
            />
            <ListRow
              title="오늘은 쉴래"
              subtitle={resting ? '오늘은 안 나올게. 내일 다시 올게' : '켜면 오늘 하루 내가 안 나와. 내일 자동으로 꺼져'}
              end={resting
                ? <WeakButton onClick={() => setRules({ ...rules, restDate: '' })}>다시 나와줘</WeakButton>
                : <WeakButton onClick={() => { setRules({ ...rules, restDate: today() }); onToast('알겠어, 오늘은 푹 쉴게') }}>쉬기</WeakButton>}
            />
          </Card>

          {/* ③ 매달 나가는 돈 — 홈 '곧 빠져나갈 돈 › 전체' 가 여기로 와요 */}
          <Card title="매달 나가는 돈">
            <div className="t-caption">한 달에</div>
            <div className="t-title1" style={{ marginTop: 2 }}>{krw(monthly)}</div>
            <div className="t-caption" style={{ marginBottom: 8 }}>커피 {cups(monthly)}잔만큼이야</div>
            <Divider />
            {SUBSCRIPTIONS.map((s) => (
              <ListRow
                key={s.id}
                avatar={s.art}
                title={s.name}
                subtitle={`${s.note} · D-${s.dday}`}
                end={krw(s.amount)}
                onClick={() => onToast('해지는 그 앱에서 해야 해. 내가 알려만 줄게')}
              />
            ))}
          </Card>

          {/* ④ 지켜보는 앱 */}
          <Card title="지켜보는 앱">
            <p className="t-body2" style={{ margin: '0 0 6px', color: 'var(--grey-700)' }}>{WATCHED.join(' · ')}</p>
            <p className="t-caption" style={{ margin: 0 }}>이 앱에서 결제하려고 할 때만 나와. 다른 앱은 안 봐</p>
          </Card>

          {/* ⑤ 전시 모드 */}
          <Card title="전시 모드">
            <ListRow
              title="전시 모드 시작"
              subtitle="관람객용 시작 화면으로 바뀌어. 내 기록과 따로 저장돼"
              end={<Chevron />}
              onClick={onStartExhibit}
            />
            <ListRow
              title="전시 카운터 모니터 화면"
              subtitle="체험대 위 가로 모니터용 · 새 창으로 열려"
              end={<Chevron />}
              onClick={() => window.open('?screen=counter', '_blank')}
            />
            <ListRow
              title="오늘 전시 카운터 초기화"
              subtitle="관람객이 지킨 돈 · 체험 수 · 참은 비율"
              end={<Chevron />}
              onClick={() => onConfirm({
                title: '전시 카운터를 0으로 돌릴까?',
                body: '관람객 기록만 지워져. 내 영수증·지킨 돈은 그대로야.',
                label: '초기화',
                action: () => { onResetExhibit(); onToast('전시 카운터를 0으로 돌렸어') },
              })}
            />
          </Card>

          {/* ⑥ 미리보기 도구 — 평소엔 접혀 있음 (디자인 확인용, 앱엔 없음) */}
          {tools && (
            <Card>
              <div
                className="bs-row ui-row-btn"
                role="button"
                onClick={() => setToolsOpen(!toolsOpen)}
                style={{ minHeight: 44 }}
              >
                <div className="main">
                  <div className="t-title3">미리보기 도구</div>
                  <div className="t-caption">디자인 확인용 · 실제 앱엔 없어</div>
                </div>
                <div className="end" style={{ color: 'var(--grey-500)', display: 'flex' }}>
                  {toolsOpen ? <ChevronUp size={20} strokeWidth={1.75} /> : <ChevronDown size={20} strokeWidth={1.75} />}
                </div>
              </div>
              {toolsOpen && tools.map(([label, run, danger]) => (
                <ListRow
                  key={label}
                  title={label}
                  end={<Chevron />}
                  onClick={danger
                    ? () => onConfirm({ title: `${label}할까?`, body: danger, label: '초기화', action: run })
                    : run}
                />
              ))}
            </Card>
          )}

          <p className="t-small c-weak" style={{ textAlign: 'center', margin: '20px 0 0' }}>비상수단 · 홍익대 디자인 졸업전시</p>
        </div>
      </div>
    </div>
  )
}

// 한 번 더 묻는 바텀시트 (App.jsx 에서 그려요)
export function ConfirmSheet({ confirm, onClose }) {
  return (
    <>
      <div className="bs-dim kit-fade" onClick={onClose} />
      <div className="bs-sheet kit-sheet-up">
        <div className="handle" />
        <div className="t-title2">{confirm.title}</div>
        <p className="t-body2" style={{ margin: '8px 0 24px', color: 'var(--grey-600)' }}>{confirm.body}</p>
        <ButtonRow
          secondary="취소"
          onSecondary={onClose}
          primary={confirm.label}
          onPrimary={() => { confirm.action(); onClose() }}
        />
      </div>
    </>
  )
}
