package com.bsue.blob.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.TapeYellow
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// 환호 — 색종이 팡 (참았다! · 해금 · 블롭 소환에서 같이 씀)
// 칸 크기는 그대로 두고, 색종이는 칸 밖까지 퍼지게 그려요
@Composable
fun Confetti(modifier: Modifier, accent: Color, count: Int = 28) {
    val colors = listOf(BlobOrange, TapeYellow, BlobCream, accent)
    val pieces = remember {
        List(count) {
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            Triple(cos(angle).toFloat(), sin(angle).toFloat(), Random.nextFloat() * 0.5f + 0.5f)
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(1200)) }

    Canvas(modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val reach = 140.dp.toPx()
        val p = progress.value
        pieces.forEachIndexed { i, (dx, dy, speed) ->
            val dist = reach * speed * p
            val fall = 120f * p * p // 살짝 떨어지는 느낌
            drawCircle(
                color = colors[i % colors.size].copy(alpha = 1f - p),
                radius = 7f,
                center = Offset(center.x + dx * dist, center.y + dy * dist + fall),
            )
        }
    }
}
