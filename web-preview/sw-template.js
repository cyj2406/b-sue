// ════════════════════════════════════════════════════════════════
//  오프라인 저장 (서비스 워커) — vite.config.js 가 빌드할 때 이 파일로 dist/sw.js 를 만들어요
//  · 처음 열 때: 앱 파일 전부(화면 · 그림 · 글꼴)를 폰에 저장
//  · 그다음부터: 저장된 것부터 보여줘서 인터넷이 끊겨도 체험이 돌아가요
//  · 인터넷 글꼴(키트의 Pretendard · Black Han Sans)도 한 번 받으면 저장해요
// ════════════════════════════════════════════════════════════════
const VERSION = '__VERSION__'
const CACHE = `bsue-${VERSION}`
const FILES = __FILES__

self.addEventListener('install', (event) => {
  event.waitUntil(caches.open(CACHE).then((c) => c.addAll(FILES)).then(() => self.skipWaiting()))
})

// 옛 버전 저장분 지우기
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k.startsWith('bsue-') && k !== CACHE).map((k) => caches.delete(k))))
      .then(() => self.clients.claim()),
  )
})

self.addEventListener('fetch', (event) => {
  const req = event.request
  if (req.method !== 'GET') return
  const url = new URL(req.url)

  // 화면 열기(주소창 · 홈 화면 아이콘): ?screen=exhibit 같은 주소도 같은 앱 화면으로
  if (req.mode === 'navigate') {
    event.respondWith(fetch(req).catch(() => caches.match('/', { ignoreSearch: true })))
    return
  }

  // 같은 사이트 파일 · 인터넷 글꼴: 저장된 것 먼저, 없으면 받아서 저장
  const font = /fonts\.(googleapis|gstatic)\.com|cdn\.jsdelivr\.net/.test(url.host)
  if (url.origin !== location.origin && !font) return
  event.respondWith(
    caches.match(req, { ignoreSearch: url.origin === location.origin }).then((hit) => hit ?? fetch(req).then((res) => {
      if (res.ok || res.type === 'opaque') {
        const copy = res.clone()
        caches.open(CACHE).then((c) => c.put(req, copy))
      }
      return res
    })),
  )
})
