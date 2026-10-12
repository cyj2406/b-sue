import { useState } from 'react'
import { Settings, X } from 'lucide-react'
import { AppHeader, IconButton, TabBar } from '../components/AppChrome'
import {
  Avatar, Badge, BottomCta, BottomSheet, Bubble, ButtonRow, Card, Chip, Divider, ListRow, Progress,
  TextButton, Toast, WeakButton, useToast,
} from '../components/Kit'
import { MISSION_ART } from '../data/blobPose'

// 공통 컴포넌트 모음 (UI 키트 확인용) — ?screen=components
// 화면 = 회색 바탕(.bs-screen) + 흰 카드(.bs-card) + 리스트 행(.bs-row)
export default function ComponentGallery({ onClose }) {
  const [chip, setChip] = useState('전체')
  const [sheet, setSheet] = useState(false)
  const [tab, setTab] = useState('home')
  const { toast, show } = useToast()

  return (
    <div className="bs-screen kit-frame">
      <div className="kit-scroll ui-with-tabbar">
        <AppHeader
          title="컴포넌트 모음"
          right={<>
            <IconButton icon={Settings} label="설정" onClick={() => show('설정')} />
            <IconButton icon={X} label="닫기" onClick={onClose} />
          </>}
        />
        <div className="bs-body" style={{ paddingBottom: 200 }}>
          <Card title="버튼">
            <ButtonRow secondary="그래도 살래" onSecondary={() => show('그래도 살래')} primary="참았다!" onPrimary={() => show('참았다!')} />
            <div style={{ display: 'flex', gap: 8, marginTop: 12, alignItems: 'center' }}>
              <WeakButton>오늘 해냈어</WeakButton>
              <TextButton>나중에 할래</TextButton>
            </div>
          </Card>

          <Card title="리스트 행" more="전체보기">
            <ListRow avatar={MISSION_ART.free_shipping} title="쿠팡 · 12,900원" subtitle="어제 23:41 · 무료배송의 배신"
              end="참았어" endClass="t-body1 bold" />
            <ListRow avatar={MISSION_ART.delivery_seal} title="배달의민족 · 23,000원" subtitle="10/04 21:10 · 배달앱 봉인"
              end="샀어" endClass="t-body1" />
            <Divider />
            <ListRow avatar={MISSION_ART.ghost_sub} title="스트림플러스 · 17,000원" subtitle="마지막 시청 41일 전"
              end={<Badge tone="solid">D-1</Badge>} endClass="" onClick={() => show('행 전체가 눌려')} />
          </Card>

          {/* 칩은 회색 화면 위에 (키트: 흰 칩) */}
          <div style={{ marginBottom: 12 }}>
            {['전체', '충동', '누수'].map((c) => <Chip key={c} on={chip === c} onClick={() => setChip(c)}>{c}</Chip>)}
          </div>
          <Card title="진행률 · 배지">
            <div className="t-caption" style={{ marginBottom: 8 }}>12명 중 5명 합류</div>
            <Progress value={5 / 12} />
            <div style={{ display: 'flex', gap: 8, marginTop: 16 }}>
              <Badge>D-7</Badge><Badge tone="orange">NEW</Badge><Badge tone="solid">D-1</Badge>
            </div>
          </Card>

          <Card title="연한 카드 · 말풍선" tint>
            <Bubble>잠깐, 딱 3초만!</Bubble>
          </Card>

          <Card title="시트 · 토스트">
            <ButtonRow secondary="토스트 보기" onSecondary={() => show('갤러리에 저장했어')} primary="바텀시트 열기" onPrimary={() => setSheet(true)} />
            <div style={{ marginTop: 12 }}><Avatar src={MISSION_ART.free_shipping} large /></div>
          </Card>
        </div>
      </div>

      <div style={{ position: 'absolute', left: 0, right: 0, bottom: 56 }}>
        <BottomCta onGrey primary="결제하기" onPrimary={() => show('결제하기를 눌렀어')} secondary="닫기" onSecondary={onClose} />
      </div>
      <TabBar value={tab} onChange={setTab} />

      {sheet && (
        <BottomSheet title="12,900원 지켰어!" onDismiss={() => setSheet(false)}>
          <div className="t-body2" style={{ marginBottom: 20 }}>바텀시트 안 내용이에요. 뒤를 누르면 닫혀요.</div>
          <ButtonRow secondary="닫기" onSecondary={() => setSheet(false)} primary="확인" onPrimary={() => setSheet(false)} />
        </BottomSheet>
      )}
      <Toast toast={toast} bottom={72} />
    </div>
  )
}
