import { useState } from 'react'
import { Avatar, Badge, Card, WeakButton } from '../components/Kit'
import { MISSION_ART, POSES } from '../data/blobPose'
import { cups, todaysGuardian } from '../data/guardians'
import { SUBSCRIPTIONS } from '../data/subscriptions'

// 홈 — reference ①: 숫자 하나가 주인공, 카드 3장
// (상단 바 · 탭바는 App.jsx 에서 그려요)

// 오늘의 미션 두 번째 줄 — 짧게 줄인 미션
const SHORT = {
  zero_day: '하루 0원 버티기',
  two_plus_one: '쓸 개수만 담기',
  sale: "'세일' 가리고 다시 보기",
  cart_cleanup: '1개 비우기',
  delivery_seal: '한 끼 배달 쉬기',
  coffee_law: '커피 잔 수로 말해보기',
  ghost_sub: '안 쓰는 구독 찾기',
  auto_pay: '매달 나가는 것 표시',
  free_shipping: '맞춤 담기 빼보기',
  closet: '안 입은 옷 꺼내기',
  fridge: '냉장고 사진 한 장',
  taxi: '대중교통 시간 비교',
}

const krw = (n) => n.toLocaleString('ko-KR') + '원'

export default function Home({ savedThisMonth, onOpenRecord, onOpenSquad, onOpenSubscriptions }) {
  const today = todaysGuardian()
  const [done, setDone] = useState(false)

  return (
    <div className="bs-body" style={{ paddingBottom: 24 }}>
      {/* ① 지킨 돈 — 화면에서 가장 큰 글자 */}
      <div className="bs-card">
        <div className="t-caption">이번 달 지킨 돈</div>
        <div className="t-display" style={{ marginTop: 2 }}>{krw(savedThisMonth)}</div>
        {savedThisMonth > 0
          ? <div className="t-caption c-orange semi">커피 {cups(savedThisMonth)}잔 아꼈어</div>
          : <div className="t-caption" style={{ paddingRight: 96 }}>아직 출동 전이야. 쇼핑앱을 열면 내가 나타날게</div>}
        <img src={POSES.curious} alt="" style={{ position: 'absolute', right: 18, top: 16, height: 80 }} />
        <div style={{ marginTop: 14 }}><WeakButton onClick={onOpenRecord}>영수증 보기</WeakButton></div>
      </div>

      {/* ② 오늘의 미션 */}
      <Card title="오늘의 미션" more="수사대" onMore={onOpenSquad}>
        <div className="bs-row">
          <Avatar src={MISSION_ART[today.id]} large />
          <div className="main">
            <div className="t-body1 bold">{today.question}</div>
            <div className="t-caption">{today.name} · {SHORT[today.id]}</div>
          </div>
        </div>
        <div style={{ marginTop: 6 }}>
          {done
            ? <div className="t-body2 semi" style={{ height: 36, display: 'flex', alignItems: 'center' }}>해냈어! 오늘 정말 멋졌어</div>
            : <WeakButton onClick={() => setDone(true)}>오늘 해냈어</WeakButton>}
        </div>
      </Card>

      {/* ③ 곧 빠져나갈 돈 */}
      <Card title="곧 빠져나갈 돈" more="전체" onMore={onOpenSubscriptions}>
        {SUBSCRIPTIONS.map((s) => (
          <div className="bs-row" key={s.id}>
            <Avatar src={s.art} />
            <div className="main">
              <div className="t-body1">
                {s.name}{' '}
                {s.dday <= 1 && <Badge tone="solid" style={{ height: 20, fontSize: 11 }}>D-{s.dday}</Badge>}
              </div>
              <div className="t-caption">{s.note}</div>
            </div>
            <div className="end t-body1 bold">{krw(s.amount)}</div>
          </div>
        ))}
      </Card>
    </div>
  )
}
