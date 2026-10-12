package com.bsue.blob.ui.receipt

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

// 감열지 종이 색 (앱 크림보다 살짝 밝게 — 검정 배경 위에서 종이처럼 보이게)
val PaperColor = Color(0xFFFBF5E9)
val InkColor = Color(0xFF2A2724)       // 감열지 글자 (완전 검정보다 살짝 바랜 느낌)
val InkFaded = Color(0xFF8C857A)

// 위는 반듯, 아래는 지그재그로 찢긴 종이 모양
class TornBottomShape(private val tooth: Dp = 9.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val t = with(density) { tooth.toPx() }
        val teeth = (size.width / (t * 2)).toInt().coerceAtLeast(1)
        val step = size.width / teeth
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - t)
            for (i in 0 until teeth) {
                lineTo(size.width - step * i - step / 2, size.height)
                lineTo(size.width - step * (i + 1), size.height - t)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

// 점선 (두 줄이면 합계 구분선)
@Composable
fun DashedLine(modifier: Modifier = Modifier, double: Boolean = false, color: Color = InkColor) {
    Canvas(modifier.fillMaxWidth().height(if (double) 7.dp else 2.dp)) {
        val effect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f))
        val stroke = 1.5.dp.toPx()
        drawLine(color, Offset(0f, stroke / 2), Offset(size.width, stroke / 2), stroke, pathEffect = effect)
        if (double) {
            drawLine(color, Offset(0f, size.height - stroke / 2), Offset(size.width, size.height - stroke / 2), stroke, pathEffect = effect)
        }
    }
}

// 장식용 바코드 (같은 seed면 같은 모양)
@Composable
fun FakeBarcode(seed: Int, modifier: Modifier = Modifier) {
    val bars = remember(seed) {
        val random = Random(seed)
        List(46) { random.nextInt(1, 4) }
    }
    Canvas(modifier.fillMaxWidth().height(46.dp)) {
        val unit = size.width / bars.sumOf { it * 2 }
        var x = 0f
        bars.forEachIndexed { i, w ->
            if (i % 2 == 0) drawRect(InkColor, Offset(x, 0f), Size(unit * w, size.height))
            x += unit * w * 2
        }
    }
}

// 영수증 프린터처럼 '드르륵' 출력되는 줄
// animate = true 면 delayMillis 뒤에 위에서부터 한 칸씩 끊어서 나타나요 (진동 틱틱틱)
@Composable
fun PrintReveal(
    animate: Boolean,
    delayMillis: Long,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        if (progress.value < 1f) {
            delay(delayMillis)
            val steps = 10
            for (i in 1..steps) {
                progress.snapTo(i / steps.toFloat())
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(PRINT_STEP_MS)
            }
        }
    }
    Box(
        modifier
            .clipToBounds()
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val visibleHeight = (placeable.height * progress.value).roundToInt()
                layout(placeable.width, visibleHeight) { placeable.place(0, 0) }
            },
    ) { content() }
}

const val PRINT_STEP_MS = 55L
const val PRINT_ROW_MS = 10 * PRINT_STEP_MS + 250 // 한 줄 출력 + 잠깐 쉼
