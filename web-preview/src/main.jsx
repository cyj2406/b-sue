import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import CounterScreen from './exhibit/CounterScreen'
import ReceiptPage from './receipt/ReceiptPage'
import './styles/bsue-tokens.css' // UI 키트 — 앱 디자인의 기준 (먼저 불러옴)
import './styles/fonts.css'       // 같은 글꼴을 내 컴퓨터 파일로도 (인터넷 끊겨도 OK)
import './styles/motion.css'      // 움직임만 (키트 전환 · 블롭 움직임)
import './styles/companion.css'   // 블롭 비서 (구석 · 말풍선 · 가운데 개입)
import './styles/ui.css'          // 공통 틀: 헤더 · 탭바 · 아이콘 버튼 · 터치 영역
import './styles/preview.css'     // 미리보기 틀 (폰 모양 · 도구) — 앱 화면 아님

const IS_RECEIPT_PAGE = /^\/r\/?$/.test(location.pathname)

createRoot(document.getElementById('root')).render(
  <StrictMode>
    {/* /r → 관람객 폰의 영수증 페이지 (전시폰 QR) · ?screen=counter → 모니터용 전시 카운터 */}
    {IS_RECEIPT_PAGE ? <ReceiptPage />
      : new URLSearchParams(location.search).get('screen') === 'counter' ? <CounterScreen /> : <App />}
  </StrictMode>,
)

// 오프라인 저장 (빌드한 앱에서만 · 미리보기 개발 중엔 안 켜요) — sw-template.js
// 관람객 폰의 영수증 페이지(/r)에선 안 켜요 (영수증 하나 보려고 앱 파일 전부를 받지 않게)
if (import.meta.env.PROD && !IS_RECEIPT_PAGE && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => {
      // 저장이 안 돼도 앱은 그대로
    })
  })
}
