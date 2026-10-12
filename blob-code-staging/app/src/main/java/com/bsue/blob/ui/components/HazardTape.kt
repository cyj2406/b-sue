package com.bsue.blob.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.TapeYellow

// 노랑·검정 사선 경고테이프. 높이는 modifier로 정해요 (예: Modifier.fillMaxWidth().height(10.dp))
// phase(0~1)를 바꾸면 줄무늬가 옆으로 밀려요 → MovingHazardTape
@Composable
fun HazardTape(
    modifier: Modifier = Modifier,
    stripeWidth: Dp = 12.dp,
    phase: Float = 0f,
) {
    Canvas(modifier.clipToBounds()) {
        drawRect(TapeYellow)
        val w = stripeWidth.toPx()
        val h = size.height
        var x = -h - w * 2 + phase * w * 2
        while (x < size.width + h) {
            val stripe = Path().apply {
                moveTo(x, h)
                lineTo(x + w, h)
                lineTo(x + w + h, 0f)
                lineTo(x + h, 0f)
                close()
            }
            drawPath(stripe, BlobBlack)
            x += w * 2
        }
    }
}

// 흐르는 경고테이프 (출동 화면 위아래). reverse = 반대 방향으로 흐름
@Composable
fun MovingHazardTape(
    modifier: Modifier = Modifier,
    stripeWidth: Dp = 16.dp,
    reverse: Boolean = false,
    periodMillis: Int = 600,
) {
    val transition = rememberInfiniteTransition(label = "tape")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing)),
        label = "tapePhase",
    )
    HazardTape(modifier, stripeWidth, if (reverse) 1f - phase else phase)
}

// "지출주의" 같은 검정 바탕 스텐실 딱지
@Composable
fun WarningBadge(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontFamily = BlackHanSans,
        fontSize = 14.sp,
        color = BlobOrange,
        modifier = modifier
            .background(BlobBlack)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
