package com.bsue.blob.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role as SemanticsRole
import androidx.compose.ui.unit.dp
import com.bsue.blob.ui.theme.BsueSpace
import com.bsue.blob.ui.theme.BsueType
import com.bsue.blob.ui.theme.Role

// ════════════════════════════════════════════════
//  탭바 — 3탭 (홈 · 수사대 · 기록) · 높이 56 + 시스템 바
//  아이콘 24 선 스타일 · 라벨 small · 선택: 진한 글씨 + 아이콘 아래 4 점(포인트색)
// ════════════════════════════════════════════════

enum class BsueTab(val label: String) {
    HOME("홈"),
    SQUAD("수사대"),
    RECORD("기록"),
}

@Composable
fun BsueTabBar(selected: BsueTab, onSelect: (BsueTab) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().background(Role.Surface)) {
        // 위쪽 얇은 구분선
        Box(Modifier.fillMaxWidth().height(1.dp).background(Role.Divider))
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(56.dp)
                .selectableGroup(),
        ) {
            BsueTab.entries.forEach { tab ->
                val isSelected = tab == selected
                val color = if (isSelected) Role.TextPrimary else Role.TextDisabled
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(selected = isSelected, onClick = { onSelect(tab) }, role = SemanticsRole.Tab),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                ) {
                    Canvas(Modifier.size(24.dp)) { drawTabIcon(tab, color) }
                    Spacer(Modifier.height(2.dp))
                    Text(tab.label, style = BsueType.Small, color = color)
                    Spacer(Modifier.height(2.dp))
                    // 선택 점
                    Box(
                        Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Role.Point else Color.Transparent),
                    )
                }
            }
        }
    }
}

// 선 아이콘 (아이콘 라이브러리 없이 직접 그림, 24×24 기준)
private fun DrawScope.drawTabIcon(tab: BsueTab, color: Color) {
    val u = size.width / 24f
    val stroke = Stroke(width = 1.8f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
    when (tab) {
        // 집
        BsueTab.HOME -> {
            val p = Path().apply {
                moveTo(4 * u, 11 * u); lineTo(12 * u, 4 * u); lineTo(20 * u, 11 * u)
                moveTo(6 * u, 9.5f * u); lineTo(6 * u, 20 * u); lineTo(18 * u, 20 * u); lineTo(18 * u, 9.5f * u)
                moveTo(10 * u, 20 * u); lineTo(10 * u, 14 * u); lineTo(14 * u, 14 * u); lineTo(14 * u, 20 * u)
            }
            drawPath(p, color, style = stroke)
        }
        // 돋보기 (수사대)
        BsueTab.SQUAD -> {
            drawCircle(color, radius = 6.5f * u, center = Offset(10.5f * u, 10.5f * u), style = stroke)
            drawLine(color, Offset(15.2f * u, 15.2f * u), Offset(20 * u, 20 * u), strokeWidth = 1.8f * u, cap = StrokeCap.Round)
        }
        // 영수증 (기록)
        BsueTab.RECORD -> {
            val p = Path().apply {
                moveTo(6 * u, 3.5f * u); lineTo(18 * u, 3.5f * u); lineTo(18 * u, 20.5f * u)
                lineTo(16 * u, 19 * u); lineTo(14 * u, 20.5f * u); lineTo(12 * u, 19 * u)
                lineTo(10 * u, 20.5f * u); lineTo(8 * u, 19 * u); lineTo(6 * u, 20.5f * u); close()
            }
            drawPath(p, color, style = stroke)
            drawLine(color, Offset(9 * u, 8.5f * u), Offset(15 * u, 8.5f * u), strokeWidth = 1.8f * u, cap = StrokeCap.Round)
            drawLine(color, Offset(9 * u, 12.5f * u), Offset(13 * u, 12.5f * u), strokeWidth = 1.8f * u, cap = StrokeCap.Round)
        }
    }
}

// 홈 오른쪽 위 설정 아이콘 (톱니 대신 단순한 선 3개 + 점)
@Composable
fun SettingsIcon(color: Color = Role.TextPrimary, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        val u = size.width / 24f
        val w = 1.8f * u
        listOf(7f, 12f, 17f).forEachIndexed { i, y ->
            drawLine(color, Offset(4 * u, y * u), Offset(20 * u, y * u), strokeWidth = w, cap = StrokeCap.Round)
            val knobX = listOf(15f, 9f, 13f)[i]
            drawCircle(Color.White, radius = 2.2f * u, center = Offset(knobX * u, y * u))
            drawCircle(color, radius = 2.2f * u, center = Offset(knobX * u, y * u), style = Stroke(w))
        }
    }
}

// QR 아이콘 (수사대 오른쪽 위)
@Composable
fun QrIcon(color: Color = Role.TextPrimary, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        val u = size.width / 24f
        val stroke = Stroke(width = 1.8f * u)
        listOf(Offset(4f, 4f), Offset(14f, 4f), Offset(4f, 14f)).forEach { o ->
            drawRoundRect(color, Offset(o.x * u, o.y * u), Size(6 * u, 6 * u), CornerRadius(1.5f * u), style = stroke)
        }
        drawRect(color, Offset(14 * u, 14 * u), Size(2.5f * u, 2.5f * u))
        drawRect(color, Offset(17.5f * u, 17.5f * u), Size(2.5f * u, 2.5f * u))
        drawRect(color, Offset(14 * u, 17.5f * u), Size(2.5f * u, 2.5f * u))
    }
}
