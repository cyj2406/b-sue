package com.bsue.blob.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.bsue.blob.ui.blob.BlobLook
import com.bsue.blob.ui.blob.BlobLooks
import com.bsue.blob.ui.blob.BlobMotion
import com.bsue.blob.ui.blob.BlobScene
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.TapeYellow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val CROSSFADE_MS = 150

// 탐정 블롭 (그림: res/drawable-nodpi, 어느 그림을 쓸지는 ui/blob/BlobPose.kt)
// 기준점은 '아래 가운데' — 그림이 바뀌거나 움직여도 발 위치가 그대로예요.
@Composable
fun BlobCharacter(
    scene: BlobScene,
    modifier: Modifier = Modifier,
    fromRight: Boolean = false,
    motionKey: Any? = null,
) = BlobCharacter(BlobLooks.of(scene, fromRight), modifier, motionKey)

// look 을 직접 줄 때 (테스트 화면, 잠긴 카드 실루엣 등)
// motionKey 가 바뀌면 한 번짜리 움직임을 다시 재생 (예: 카운트 3 → 2 → 1)
// silhouette 색을 주면 한 가지 색 실루엣으로, 움직임 없이 그려요
@Composable
fun BlobCharacter(
    look: BlobLook,
    modifier: Modifier = Modifier,
    motionKey: Any? = null,
    silhouette: Color? = null,
) {
    val motion = if (silhouette != null) BlobMotion.NONE else look.motion

    // ── 그림이 바뀔 때 살짝 튀기 (크로스페이드와 같이) ──
    val pop = remember { Animatable(1f) }
    var shownImage by remember { mutableIntStateOf(look.image) }
    LaunchedEffect(look.image) {
        if (look.image != shownImage) {
            shownImage = look.image
            pop.snapTo(0.9f)
            pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium))
        }
    }

    // ── 계속 반복되는 움직임 ──
    val loop = rememberInfiniteTransition(label = "blobLoop")
    val breathe by loop.animateFloat(
        0f, 1f, infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breathe",
    )
    val sneak by loop.animateFloat(
        -1f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sneak",
    )
    val sway by loop.animateFloat(
        -1f, 1f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sway",
    )

    // 숨쉬기 중 가끔 '통통'
    val hop = remember { Animatable(0f) }
    LaunchedEffect(motion) {
        hop.snapTo(0f)
        if (motion == BlobMotion.BREATHE) {
            while (true) {
                delay(Random.nextLong(2500, 4500))
                hop.animateTo(1f, tween(140, easing = FastOutSlowInEasing))
                hop.animateTo(0f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMediumLow))
            }
        }
    }

    // ── 한 번짜리 움직임: 눌림·늘어남(squash) + 점프 ──
    val squash = remember { Animatable(1f) } // 1 = 보통, <1 납작, >1 길쭉
    val jump = remember { Animatable(0f) }   // 0 = 바닥, 1 = 가장 높이
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(motion, motionKey) {
        when (motion) {
            BlobMotion.STOP_ENTER -> squashEnter(squash)
            // 처음엔 등장 squash & stretch, 그다음부터는 숫자마다 꾹
            BlobMotion.COUNT_PRESS -> if (!entered) squashEnter(squash) else squashPress(squash)
            BlobMotion.CHEER_JUMP -> repeat(2) { jumpOnce(squash, jump) }
            else -> {
                squash.snapTo(1f)
                jump.snapTo(0f)
            }
        }
        entered = true
    }

    Box(modifier) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 1f) // 아래 가운데
                    var sx = pop.value
                    var sy = pop.value
                    var tx = 0f
                    var ty = 0f
                    var rot = 0f
                    when (motion) {
                        BlobMotion.BREATHE -> {
                            sy *= 1f + 0.03f * breathe
                            sx *= 1f - 0.015f * breathe
                            ty = -hop.value * size.height * 0.07f
                        }
                        BlobMotion.SNEAK -> {
                            tx = sneak * size.width * 0.03f
                            rot = sneak * 3f
                        }
                        BlobMotion.PAT_SWAY -> rot = sway * 6f
                        else -> Unit
                    }
                    sy *= squash.value
                    sx *= 2f - squash.value // 납작해지면 옆으로 퍼짐
                    ty -= jump.value * size.height * 0.18f
                    scaleX = if (look.mirrored) -sx else sx
                    scaleY = sy
                    translationX = tx
                    translationY = ty
                    rotationZ = rot
                },
        ) {
            Crossfade(targetState = look.image, animationSpec = tween(CROSSFADE_MS), label = "blobImage") { image ->
                Image(
                    painter = painterResource(image),
                    contentDescription = "탐정 블롭",
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomCenter,
                    colorFilter = silhouette?.let { ColorFilter.tint(it) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        if (motion == BlobMotion.CHEER_JUMP) Sparkles(Modifier.fillMaxSize())
    }
}

private suspend fun squashEnter(squash: Animatable<Float, AnimationVector1D>) {
    squash.snapTo(0.55f)
    squash.animateTo(
        1f,
        keyframes {
            durationMillis = 520
            1.18f at 180
            0.92f at 340
            1.03f at 440
        },
    )
}

private suspend fun squashPress(squash: Animatable<Float, AnimationVector1D>) {
    squash.snapTo(1f)
    squash.animateTo(
        1f,
        keyframes {
            durationMillis = 260
            0.86f at 80
            1.05f at 180
        },
    )
}

private suspend fun jumpOnce(squash: Animatable<Float, AnimationVector1D>, jump: Animatable<Float, AnimationVector1D>) {
    squash.animateTo(0.82f, tween(90))                                  // 웅크리고
    coroutineScope {
        launch { squash.animateTo(1.12f, tween(120)) }                 // 쭉 늘어나며
        jump.animateTo(1f, tween(220, easing = FastOutSlowInEasing))   // 위로
    }
    squash.animateTo(1f, tween(80))
    jump.animateTo(0f, tween(200, easing = FastOutLinearInEasing))     // 착지
    squash.animateTo(0.88f, tween(70))
    squash.animateTo(1f, spring(dampingRatio = 0.4f))
}

// 환호 반짝이 ✦ (블롭 둘레에서 깜빡깜빡)
@Composable
private fun Sparkles(modifier: Modifier) {
    val twinkle = rememberInfiniteTransition(label = "sparkle")
    val phase by twinkle.animateFloat(0f, 1f, infiniteRepeatable(tween(900)), label = "phase")
    // (x, y, 크기) — 칸 크기에 대한 비율
    val stars = remember {
        listOf(
            Triple(0.08f, 0.22f, 0.07f), Triple(0.9f, 0.18f, 0.06f), Triple(0.18f, 0.55f, 0.045f),
            Triple(0.86f, 0.5f, 0.05f), Triple(0.5f, 0.02f, 0.05f), Triple(0.72f, 0.06f, 0.035f),
        )
    }
    Canvas(modifier) {
        stars.forEachIndexed { i, (fx, fy, fs) ->
            val t = (phase + i / stars.size.toFloat()) % 1f
            val alpha = if (t < 0.5f) t * 2 else (1 - t) * 2
            val r = size.minDimension * fs * (0.7f + 0.3f * alpha)
            val c = Offset(size.width * fx, size.height * fy)
            val star = Path().apply {
                moveTo(c.x, c.y - r)
                quadraticTo(c.x, c.y, c.x + r, c.y)
                quadraticTo(c.x, c.y, c.x, c.y + r)
                quadraticTo(c.x, c.y, c.x - r, c.y)
                quadraticTo(c.x, c.y, c.x, c.y - r)
                close()
            }
            drawPath(star, (if (i % 2 == 0) TapeYellow else BlobCream).copy(alpha = alpha))
        }
    }
}
