// 대기 화면 · 영수증이 같이 쓰는 그림 (src/assets/attract/) — 이름으로 주소 찾기
const IMAGES = import.meta.glob('../assets/attract/*.png', { eager: true, query: '?url', import: 'default' })
export const attractImage = (name) => IMAGES[`../assets/attract/${name}.png`]
