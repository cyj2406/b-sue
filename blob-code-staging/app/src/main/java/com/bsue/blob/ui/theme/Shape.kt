package com.bsue.blob.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// ════════════════════════════════════════════════
//  간격 · 모서리 · 그림자 — DESIGN.md 4번
// ════════════════════════════════════════════════

// ── 모서리 ──
object BsueShape {
    val Card = RoundedCornerShape(20.dp)
    val Button = RoundedCornerShape(16.dp)
    val Thumb = RoundedCornerShape(12.dp)
    val Toast = RoundedCornerShape(12.dp)
    val SmallCard = RoundedCornerShape(16.dp) // 출동 화면 작은 수호신 카드
    val HeroCard = RoundedCornerShape(28.dp)  // 출동 화면 가운데 카드 (토스 레벨업 카드)
    val SheetTop = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val Pill = RoundedCornerShape(percent = 50) // 칩·배지 (999)

    // 말풍선: 18, 블롭 쪽 꼬리 모서리만 4
    val BubbleTailLeft = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp)
    val BubbleTailRight = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 4.dp, bottomStart = 18.dp)
}

// ── 간격 (4pt 그리드: 4, 8, 12, 16, 20, 24, 32, 40, 56만 써요) ──
object BsueSpace {
    val X4 = 4.dp
    val X8 = 8.dp
    val X12 = 12.dp
    val X16 = 16.dp
    val X20 = 20.dp
    val X24 = 24.dp
    val X32 = 32.dp
    val X40 = 40.dp
    val X56 = 56.dp

    val ScreenSide = 24.dp       // 화면 좌우 여백
    val CardPaddingH = 24.dp     // 카드 안쪽 좌우
    val CardPaddingV = 20.dp     // 카드 안쪽 위아래
    val CardGap = 12.dp          // 카드와 카드 사이
    val SectionGap = 12.dp       // 섹션 제목과 내용 사이
    val ListRow = 64.dp          // 리스트 행 높이 (한 줄)
    val ListRowTwoLine = 72.dp   // 리스트 행 높이 (두 줄)
    val IconTextGap = 16.dp      // 아이콘과 글자 사이
    val ButtonHeight = 56.dp     // Primary · Secondary
    val SmallButtonHeight = 40.dp // Weak · Text
}

// ── 그림자: 기본 없음. 떠 있는 것(말풍선, 플로팅 블롭, 바텀시트)만 ──
// DESIGN.md: 0 4 16 / 12% — 색은 앱 기본 글씨색(BlobBlack)의 투명도만 조절
private val FloatingShadowColor = BlobBlack.copy(alpha = 0.12f)

fun Modifier.floatingShadow(shape: Shape): Modifier =
    shadow(elevation = 8.dp, shape = shape, ambientColor = FloatingShadowColor, spotColor = FloatingShadowColor)

// Material 기본 컴포넌트용 모서리
val BsueShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
