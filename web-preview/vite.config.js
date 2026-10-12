import { createHash } from 'node:crypto'
import { readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs'
import { join, relative } from 'node:path'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// ── 오프라인 준비 (전시장 와이파이가 끊겨도 체험이 돌아가게) ──
// npm run build 할 때 dist 안의 모든 파일 목록을 sw.js 에 적어 넣어요.
// 폰이 처음 한 번 열 때 전부 저장해 두고, 그다음부터는 인터넷 없이도 열려요.
// 파일이 바뀌면 버전(해시)도 바뀌어서 다음에 열 때 새 것으로 갈아끼워요.
function offlineServiceWorker() {
  let outDir = 'dist'
  return {
    name: 'offline-service-worker',
    apply: 'build',
    configResolved(config) { outDir = config.build.outDir },
    closeBundle() {
      const files = []
      const walk = (dir) => {
        for (const name of readdirSync(dir)) {
          const full = join(dir, name)
          if (statSync(full).isDirectory()) walk(full)
          // 안 쓰는 글꼴(Gothic A1 · 나눔고딕코딩 Bold)은 폰에 안 받아요 (나눔고딕코딩 보통은 영수증 본문)
          // _redirects · _headers 는 Netlify 설정 파일이라 주소로 받을 수 없어요 → 목록에서 빼요
          else if (name !== 'sw.js' && name !== '_redirects' && name !== '_headers' && !/gothic_a1|nanum_gothic_coding_bold/.test(name)) files.push(full)
        }
      }
      walk(outDir)
      const hash = createHash('sha1')
      files.forEach((f) => hash.update(f).update(readFileSync(f)))
      const urls = ['/', ...files.map((f) => '/' + relative(outDir, f).split('\\').join('/'))]
      const template = readFileSync('sw-template.js', 'utf8')
      writeFileSync(join(outDir, 'sw.js'), template
        .replace('__VERSION__', hash.digest('hex').slice(0, 10))
        .replace('__FILES__', JSON.stringify(urls, null, 2)))
    },
  }
}

// --host: 같은 와이파이의 폰에서도 열 수 있게 (터미널에 Network 주소가 떠요)
// 포트 5180 고정: 다른 프로젝트(card 등)가 5173을 쓰고 있으면 그림 요청이 그쪽으로 새서
//   이미지가 깨졌어요 (2026-10-06). strictPort — 5180이 이미 쓰이면 다른 번호로 몰래 바꾸지 않고 알려줘요
export default defineConfig({
  plugins: [react(), offlineServiceWorker()],
  server: { port: 5180, strictPort: true, open: true },
  preview: { port: 5181, strictPort: true },
})
